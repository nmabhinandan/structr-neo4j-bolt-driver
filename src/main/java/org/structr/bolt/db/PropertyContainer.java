/**
 * Copyright (C) 2010-2015 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.bolt.db;

import java.util.Map;
import java.util.Set;

/**
 * Container for {@link Object} properties
 * Stores properties as key-value-pairs
 * @author Christian Kramp <christian.kramp@structr.com>
 */


public interface PropertyContainer {
    /**
     * Returns all properties
     * @return key-value-pairs stored in {@link Map<String,Object>}
     */
    Map<String,Object> getProperties();
    
    /**
     * Returns property identified by the given key
     * @param key {@link String}
     * @return raw Object value
     */
    Object getProperty(final String key);
    
    /**
     * Adds a set to the properties
     * @param key {@link String} identifier
     * @param value raw {@link Object} 
     */
    void setProperty(final String key,final Object value);
    
    /**
     * Removes the property associated with given key
     * @param key  {@link String} identifier
     */
    void removeProperty(final String key);
    
    /**
     * Returns all property keys
     * @return List of {@link String} property keys
     */
    Set<String> getPropertyKeys();
    
    /**
     * Checks if property with given key exists
     * @param key {@link String}
     * @return Whether the property exists or not
     */
    boolean hasProperty(final String key);
    
}
