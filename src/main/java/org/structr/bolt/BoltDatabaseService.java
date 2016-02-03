/**
 * Copyright (C) 2010-2016 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Structr is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Structr. If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.bolt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.ResultCursor;
import org.neo4j.driver.v1.Session;
import org.structr.api.DatabaseService;
import org.structr.api.NativeResult;
import org.structr.api.Transaction;
import org.structr.api.config.Structr;
import org.structr.api.graph.GraphProperties;
import org.structr.api.graph.Label;
import org.structr.api.graph.Node;
import org.structr.api.graph.Relationship;
import org.structr.api.graph.RelationshipType;
import org.structr.api.index.IndexManager;
import org.structr.api.util.FixedSizeCache;
import org.structr.bolt.wrapper.NodeWrapper;
import org.structr.bolt.wrapper.RelationshipWrapper;
import org.structr.bolt.wrapper.TransactionWrapper;

/**
 *
 * @author Christian Kramp <christian.kramp@structr.com>
 */
public class BoltDatabaseService implements DatabaseService, GraphProperties{

    private static final Logger logger = Logger.getLogger(BoltDatabaseService.class.getName());
    
    public static final String BOLT_CONNECTION_HOST = "bolt.connection.host";
    public static final String RELATIONSHIP_CACHE_SIZE  = "database.cache.relationship.size";
    public static final String NODE_CACHE_SIZE          = "database.cache.node.size";
    
    private static final Map<String, RelationshipType> relTypeCache          = new ConcurrentHashMap<>();
    private static final Map<String, Label> labelCache                       = new ConcurrentHashMap<>();
    private FixedSizeCache<Long, RelationshipWrapper> relationshipCache     = null;
    private FixedSizeCache<Long, NodeWrapper> nodeCache                     = null;
    private IndexManager relationshipIndexer                                = null;
    private IndexManager nodeIndexer                                        = null;
    private String databasePath                                             = null;  
    private String connectionHost                                           = null;
    
    private Driver dbDriver = null;
    private Session dbSession = null;
    
    // ----- interface DatabaseService -----
    @Override
    public void initialize(final Properties config) {
        
        databasePath = config.getProperty(Structr.DATABASE_PATH);
        connectionHost = config.getProperty(BOLT_CONNECTION_HOST, "bolt://localhost");      
        
        
        final int relationshipCacheSize = Integer.valueOf(config.getProperty(RELATIONSHIP_CACHE_SIZE, "10000"));
        if (relationshipCacheSize > 0) {

                logger.log(Level.INFO, "Relationship cache size set to {0}", relationshipCacheSize);
                relationshipCache = new FixedSizeCache<>(relationshipCacheSize);

        } else {

                logger.log(Level.INFO, "Relationship cache disabled.");
        }

        final int nodeCacheSize = Integer.valueOf(config.getProperty(NODE_CACHE_SIZE, "10000"));
        if (nodeCacheSize > 0) {

                logger.log(Level.INFO, "Node cache size set to {0}", nodeCacheSize);
                nodeCache = new FixedSizeCache<>(nodeCacheSize);

        } else {

                logger.log(Level.INFO, "Node cache disabled.");
        }
        
        logger.log(Level.INFO, "Initializing database driver ... Host: ", connectionHost);
        dbDriver = GraphDatabase.driver(connectionHost);
        logger.log(Level.INFO, "Initializing bolt session ...");
        dbSession = dbDriver.session();
    }

    @Override
    public void shutdown() {
        try {
            dbSession.close();
            dbDriver.close();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public <T> T forName(Class<T> type, String name) {

        if (Label.class.equals(type)) {

                return (T)getOrCreateLabel(name);
        }

        if (RelationshipType.class.equals(type)) {

                return (T)getOrCreateRelationshipType(name);
        }

        throw new RuntimeException("Cannot create object of type " + type);
    }

    @Override
    public Transaction beginTx() {
        return new TransactionWrapper(dbSession.beginTransaction());
    }

    @Override
    public Node createNode() {
        String cypherStatement = "CREATE (n) RETURN n;";
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(cypherStatement);
        try{
            res.next();
            return NodeWrapper.getWrapper(this, new org.structr.bolt.db.Node(res.record().value("n").asNode()) );
        } catch (org.neo4j.driver.v1.exceptions.NoRecordException nre){
            return null;
        }
    }

    @Override
    public Node getNodeById(long id) {
        String cypherStatement = "MATCH (n) WHERE id(n)="+id+" RETURN n;";
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(cypherStatement);
        try{
            res.next();
            return NodeWrapper.getWrapper(this, new org.structr.bolt.db.Node(res.record().value("n").asNode()) );
        } catch (org.neo4j.driver.v1.exceptions.NoRecordException nre){
            return null;
        }
    }

    @Override
    public Relationship getRelationshipById(long id) {
        String cypherStatement = "MATCH ()-[r]-() WHERE id(r)="+id+" RETURN r;";
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(cypherStatement);
        try{
            res.next();
            return RelationshipWrapper.getWrapper(this, new org.structr.bolt.db.Relationship(res.record().value("r").asRelationship()) );
        } catch (org.neo4j.driver.v1.exceptions.NoRecordException nre){
            return null;
        }
    }

    @Override
    public Iterable<Node> getAllNodes() {
        String cypherStatement = "MATCH (n) RETURN n;";
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(cypherStatement);

        List<Node> list = new LinkedList<>();
        while(res.next()){
            list.add(
                    NodeWrapper.getWrapper(this,new org.structr.bolt.db.Node(res.record().value("n").asNode()))
            );
        }
        return list;
    }

    @Override
    public Iterable<Relationship> getAllRelationships() {
        String cypherStatement = "MATCH ()-[r]-() RETURN DISTINCT r;";
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(cypherStatement);

        List<Relationship> list = new LinkedList<>();
        while(res.next()){
            list.add(
                    RelationshipWrapper.getWrapper(this,new org.structr.bolt.db.Relationship(res.record().value("r").asRelationship()))
            );
        }
        return list;
    }

    @Override
    public GraphProperties getGlobalProperties() {
        return this;
    }

    @Override
    public IndexManager<Node> nodeIndexer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IndexManager<Relationship> relationshipIndexer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NativeResult execute(String nativeQuery, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NativeResult execute(String nativeQuery) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void invalidateCache() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    // ----- interface GraphProperties -----
    @Override
    public void setProperty(String name, Object value) {
        
        final Properties properties = new Properties();
        final File propertiesFile   = new File(databasePath + "/graph.properties");

        try (final Reader reader = new FileReader(propertiesFile)) {
                properties.load(reader);
        } catch (IOException ioex) {}

        properties.setProperty(name, value.toString());

        try (final Writer writer = new FileWriter(propertiesFile)) {
                properties.store(writer, "Created by Structr at " + new Date());
        } catch (IOException ioex) {
                ioex.printStackTrace();
                logger.log(Level.WARNING, "Unable to write properties file");
        }
    }

    @Override
    public Object getProperty(String name) {

		final Properties properties = new Properties();
		final File propertiesFile   = new File(databasePath + "/graph.properties");

		try (final Reader reader = new FileReader(propertiesFile)) {
			properties.load(reader);
		} catch (IOException ioex) {}

		return properties.getProperty(name);
    }
    
    // ----- BoltDatabaseService -----    
    public Label getOrCreateLabel(final String name) {

            Label label = labelCache.get(name);
            if (label == null) {

                    label = new LabelImpl(name);
                    labelCache.put(name, label);
            }

            return label;
    }

    public RelationshipType getOrCreateRelationshipType(final String name) {

            RelationshipType relType = relTypeCache.get(name);
            if (relType == null) {

                    relType = new RelationshipTypeImpl(name);
                    relTypeCache.put(name, relType);
            }

            return relType;
    }
    
    public NodeWrapper getNodeFromCache(final long id) {

            if (nodeCache != null) {
                    return nodeCache.get(id);
            }

            return null;
    }

    public void storeNodeInCache(final NodeWrapper node) {

            if (nodeCache != null) {
                    nodeCache.put(node.getId(), node);
            }
    }

    public void removeNodeFromCache(final long id) {

            if (nodeCache != null) {
                    nodeCache.remove(id);
            }
    }

    public RelationshipWrapper getRelationshipFromCache(final long id) {

            if (relationshipCache != null) {
                    return relationshipCache.get(id);
            }

            return null;
    }

    public void storeRelationshipInCache(final RelationshipWrapper relationship) {

            if (relationshipCache != null) {
                    relationshipCache.put(relationship.getId(), relationship);
            }
    }

    public void removeRelationshipFromCache(final long id) {

            if (relationshipCache != null) {
                    relationshipCache.remove(id);
            }
    }
    
    // ----- nested classes -----
    private static class LabelImpl implements Label {

            private String name = null;

            private LabelImpl(final String name) {
                    this.name = name;
            }

            @Override
            public String name() {
                    return name;
            }

            @Override
            public int hashCode() {
                    return name.hashCode();
            }

            @Override
            public boolean equals(final Object other) {

                    if (other instanceof Label) {
                            return other.hashCode() == hashCode();
                    }

                    return false;
            }
    }

    private static class RelationshipTypeImpl implements RelationshipType {

            private String name = null;

            private RelationshipTypeImpl(final String name) {
                    this.name = name;
            }

            @Override
            public String name() {
                    return name;
            }

            @Override
            public int hashCode() {
                    return name.hashCode();
            }

            @Override
            public boolean equals(final Object other) {

                    if (other instanceof RelationshipType) {
                            return other.hashCode() == hashCode();
                    }

                    return false;
            }
    }
    
}
