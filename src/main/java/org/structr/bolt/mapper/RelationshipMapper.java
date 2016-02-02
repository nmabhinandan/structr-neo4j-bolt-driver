/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.mapper;

import java.util.function.Function;
import org.structr.api.graph.Relationship;
import org.structr.bolt.BoltDatabaseService;
import org.structr.bolt.wrapper.RelationshipWrapper;

/**
 *
 * @author Christian Morgner
 */
public class RelationshipMapper extends EntityMapper implements Function<org.structr.bolt.db.Relationship , Relationship> {

	public RelationshipMapper(final BoltDatabaseService graphDb) {
		super(graphDb);
	}

	@Override
	public Relationship apply(org.structr.bolt.db.Relationship t) {
		return RelationshipWrapper.getWrapper(graphDb, t);
	}
}
