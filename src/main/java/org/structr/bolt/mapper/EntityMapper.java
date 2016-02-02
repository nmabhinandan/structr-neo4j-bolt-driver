/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.mapper;

import org.structr.bolt.BoltDatabaseService;

/**
 *
 * @author Christian Morgner
 */
public class EntityMapper {

	protected BoltDatabaseService graphDb = null;

	public EntityMapper(final BoltDatabaseService graphDb) {
		this.graphDb = graphDb;
	}
}
