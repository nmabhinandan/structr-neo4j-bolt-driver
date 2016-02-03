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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.neo4j.driver.v1.ResultCursor;
import org.neo4j.graphdb.DynamicLabel;
import org.structr.bolt.wrapper.TransactionWrapper;

/**
 *
 * @author Christian Kramp <christian.kramp@structr.com>
 */
public class Node implements org.structr.bolt.db.Entity{
    private final org.neo4j.driver.v1.Node dbNode;
    
    public Node(org.neo4j.driver.v1.Node dbNode){
        this.dbNode = dbNode;
    }

    public void addLabel(org.neo4j.graphdb.Label label){
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("SET n :").append(label.name()).append(" ")
           .append("RETURN n;");
        TransactionWrapper.getCurrentTransaction().run(str.toString());
    }
    
    public void removeLabel(org.neo4j.graphdb.Label label){
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("REMOVE n :").append(label.name()).append(" ")
           .append("RETURN n;");
        TransactionWrapper.getCurrentTransaction().run(str.toString());
    }
    
    public Iterable<org.neo4j.graphdb.Label> getLabels(){
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("RETURN DISTINCT labels(n);");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        List<org.neo4j.graphdb.Label> list = new LinkedList<>();
        try{
            res.next();
            res.record().value("labels(n)").asList().forEach(
                    l -> list.add(DynamicLabel.label(l.asString()))
            );
        } catch (org.neo4j.driver.v1.exceptions.NoRecordException nre){
            nre.printStackTrace();
        }
        return list;
    }
    
    public Iterable<org.structr.bolt.db.Relationship> getRelationships(){
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n)-[r]-() WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("RETURN DISTINCT r;");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        List<org.structr.bolt.db.Relationship> list = new LinkedList<>();
        while(res.next()){
            list.add(
                    new org.structr.bolt.db.Relationship( 
                            res.record().value("r").asRelationship() 
                    )
            );
        }
        return list;
    }
    
    public Iterable<org.structr.bolt.db.Relationship> getRelationships(org.neo4j.graphdb.Direction dir){
        StringBuilder str = new StringBuilder();
        switch(dir){
            case INCOMING:
                str.append("MATCH (n)<-[r]-() WHERE ");
                break;
            case OUTGOING:
                str.append("MATCH (n)-[r]->() WHERE ");
                break;
            case BOTH:
                str.append("MATCH (n)-[r]-() WHERE ");
                break;
        }
        str.append("id(n)=").append(this.getId()).append(" ")
           .append("RETURN DISTINCT r;");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        List<org.structr.bolt.db.Relationship> list = new LinkedList<>();
        while(res.next()){
            list.add(
                    new org.structr.bolt.db.Relationship( 
                            res.record().value("r").asRelationship() 
                    )
            );
        }
        return list;
    }
    
    public Iterable<org.structr.bolt.db.Relationship> getRelationships(org.neo4j.graphdb.Direction dir, org.neo4j.graphdb.RelationshipType type){
        StringBuilder str = new StringBuilder();
        switch(dir){
            case INCOMING:
                str.append("MATCH (n)<-[r:")
                   .append(type.name())
                   .append("]-() WHERE ");
                break;
            case OUTGOING:
                str.append("MATCH (n)-[r:")
                   .append(type.name())
                   .append("]->() WHERE ");
                break;
            case BOTH:
                str.append("MATCH (n)-[r:")
                   .append(type.name())
                   .append("]-() WHERE ");
                break;
        }
        str.append("id(n)=").append(this.getId()).append(" ")
           .append("RETURN DISTINCT r;");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        List<org.structr.bolt.db.Relationship> list = new LinkedList<>();
        while(res.next()){
            list.add(
                    new org.structr.bolt.db.Relationship( 
                            res.record().value("r").asRelationship() 
                    )
            );
        }
        return list;
    }
    
    public long getId(){
         return this.dbNode.identity().asLong();
    }
    
    public org.structr.bolt.db.Relationship createRelationshipTo(org.structr.bolt.db.Node endNode, org.neo4j.graphdb.RelationshipType relType){
        long idStart, idEnd;
        idStart = this.getId();
        idEnd = endNode.getId();
        StringBuilder str = new StringBuilder();
        str.append("MATCH (a),(b) WHERE ")
          .append("id(a)=").append(idStart).append(" ")
          .append("AND ")
          .append("id(b)=").append(idEnd).append(" ")
          .append("CREATE (a)-[r:").append(relType.name())
          .append("]->(b) ")
          .append("RETURN r;");        
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        res.next();
        return new org.structr.bolt.db.Relationship(res.record().value("r").asRelationship());
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
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("RETURN n.")
           .append(key)
           .append(";");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        res.next();
        return res.record().value("n."+key).asString();
    }

    @Override
    public void setProperty(String key, Object value) {
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("SET n.")
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
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("RETURN keys(n);");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        Set<String> set = new HashSet<>();
        while(res.next()){
            set.add(res.record().value("keys(n)").asString());
        }
        return set;
    }

    @Override
    public boolean hasProperty(String key) {
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("RETURN n.")
           .append(key)
           .append(";");
        ResultCursor res = TransactionWrapper.getCurrentTransaction().run(str.toString());
        return res.next();
    }

    @Override
    public void removeProperty(String key) {
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("REMOVE n.")
           .append(key)
           .append(";");
        TransactionWrapper.getCurrentTransaction().run(str.toString());   
    }

    @Override
    public void delete() {
        this.getRelationships().forEach(
                (org.structr.bolt.db.Relationship rel) -> rel.delete()
        );
        
        StringBuilder str = new StringBuilder();
        str.append("MATCH (n) WHERE ")
           .append("id(n)=").append(this.getId()).append(" ")
           .append("DELETE n;");
        TransactionWrapper.getCurrentTransaction().run(str.toString());  
    }
    
}
