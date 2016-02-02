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

import java.util.Map;
import java.util.Set;
import org.structr.bolt.BoltDatabaseService;

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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public org.structr.bolt.db.Node getStartNode(){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public org.structr.bolt.db.Node getEndNode(){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public org.structr.bolt.db.Node getOtherNode(final org.structr.bolt.db.Node node){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Map<String, Object> getProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getProperty(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperty(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getPropertyKeys() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasProperty(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeProperty(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
