/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.wrapper;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.structr.api.graph.RelationshipType;
import org.structr.bolt.BoltDatabaseService;

/**
 *
 * @author Christian Morgner
 */
public class RelationshipTypeWrapper implements RelationshipType {

	private org.neo4j.graphdb.RelationshipType relType = null;

	public RelationshipTypeWrapper(final BoltDatabaseService graphDb, final org.neo4j.graphdb.RelationshipType relType) {
		this.relType = relType;
	}

	@Override
	public int hashCode() {
		return relType.hashCode();
	}

	@Override
	public boolean equals(final Object other) {

		if (other instanceof RelationshipType) {
			return other.hashCode() == hashCode();
		}

		return false;
	}

	@Override
	public String name() {
		return relType.name();
	}

	// ----- helper methods -----
	public org.neo4j.graphdb.RelationshipType unwrap() {
		return relType;
	}

	public org.neo4j.graphdb.RelationshipType unwrap(final RelationshipType relationshipType) {

		if (relationshipType instanceof RelationshipTypeWrapper) {

			return ((RelationshipTypeWrapper)relationshipType).unwrap();
		}

		return DynamicRelationshipType.withName(relationshipType.name());
	}
}
