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
package org.structr.bolt.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.neo4j.driver.v1.ResultCursor;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.structr.bolt.BoltDatabaseService;
import org.structr.bolt.wrapper.TransactionWrapper;

/**
 *
 * @author Christian Kramp <christian.kramp@structr.com>
 */
public class Relationship implements org.structr.bolt.db.Entity{
    private final org.neo4j.driver.v1.Relationship dbRel;
    
    public Relationship(org.neo4j.driver.v1.Relationship dbRel){
        this.dbRel = dbRel;
    }
    
    public long getId(){
         return this.dbRel.identity().asLong();
    }
    
    public org.neo4j.graphdb.RelationshipType getType(){
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]-() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("RETURN type(r);");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        res.next();
        return DynamicRelationshipType.withName(res.record().value("type(r)").asString());
    }
    
    public org.structr.bolt.db.Node getStartNode(){
        StringBuilder str = new StringBuilder();
        str.append("MATCH (a)-[r]->() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("RETURN a;");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        res.next();
        return new org.structr.bolt.db.Node(res.record().value("a").asNode());
    }
    
    public org.structr.bolt.db.Node getEndNode(){
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]->(b) WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("RETURN b;");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        res.next();
        return new org.structr.bolt.db.Node(res.record().value("b").asNode());
    }
    
    public org.structr.bolt.db.Node getOtherNode(final org.structr.bolt.db.Node node){
        StringBuilder str = new StringBuilder();
        str.append("MATCH (a)-[r]-(b) WHERE ")
           .append("id(r)=").append(this.getId()).append(" AND ")
           .append("id(a)=").append(node.getId()).append(" ")
           .append("RETURN b;");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        res.next();
        return new org.structr.bolt.db.Node(res.record().value("b").asNode());
    }
    
    @Override
    public Map<String, Object> getProperties() {
        Map<String,Object> map = new HashMap();
        this.getPropertyKeys().forEach(
                (String s) -> map.put(s, this.getProperty(s))
        );
        return map;
    }

    @Override
    public Object getProperty(String key) {
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]-() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("RETURN r.")
           .append(key)
           .append(";");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        res.next();
        return res.record().value("r."+key).asString();
    }

    @Override
    public void setProperty(String key, Object value) {
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]-() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("SET r.")
           .append(key)
           .append("=")
           //TODO: Implement proper type casting
           .append(value.toString())
           .append(";");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());  
    }

    @Override
    public Set<String> getPropertyKeys() {
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]-() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("RETURN keys(r);");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        Set<String> set = new HashSet<>();
        while(res.next()){
            set.add(res.record().value("keys(r)").asString());
        }
        return set;
    }

    @Override
    public boolean hasProperty(String key) {
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]-() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("RETURN r.")
           .append(key)
           .append(";");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        return res.next();
    }

    @Override
    public void removeProperty(String key) {
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]-() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("REMOVE r.")
           .append(key)
           .append(";");
        TransactionWrapper.getCurrentTransaction().run(str.toString());  
    }

    @Override
    public void delete() {
        StringBuilder str = new StringBuilder();
        str.append("MATCH ()-[r]-() WHERE ")
           .append("id(r)=").append(this.getId()).append(" ")
           .append("DELETE r;");
        TransactionWrapper.getCurrentTransaction().run(str.toString());  
    }
    
}
