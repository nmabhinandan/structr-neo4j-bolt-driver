/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.wrapper;

import org.structr.api.graph.Node;
import org.structr.api.NotInTransactionException;
import org.structr.api.graph.Relationship;
import org.structr.api.graph.RelationshipType;
import org.structr.bolt.BoltDatabaseService;

/**
 *
 * @author Christian Morgner
 */
public class RelationshipWrapper extends EntityWrapper<org.structr.bolt.db.Relationship> implements Relationship {

	private RelationshipWrapper(final BoltDatabaseService graphDb, final org.structr.bolt.db.Relationship relationship) {
		super(graphDb, relationship);
	}

	@Override
	public String toString() {
		return "RelationshipWrapper(" + entity.getId() + ", " + entity.getType().name() + ")";
	}

	@Override
	public int hashCode() {
		return entity.hashCode();
	}

	@Override
	public boolean equals(final Object other) {

		if (other instanceof Relationship) {
			return other.hashCode() == hashCode();
		}

		return false;
	}

	@Override
	public Node getStartNode() {

		try {

			return NodeWrapper.getWrapper(graphDb, entity.getStartNode());

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public Node getEndNode() {

		try {

			return NodeWrapper.getWrapper(graphDb, entity.getEndNode());

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public Node getOtherNode(final Node node) {

		try {
			return NodeWrapper.getWrapper(graphDb, entity.getOtherNode(unwrap(node)));

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public RelationshipType getType() {

		try {

			return new RelationshipTypeWrapper(graphDb, entity.getType());

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public long getId() {
		return entity.getId();
	}

	@Override
	public void delete() throws NotInTransactionException {

		try {


			final TransactionWrapper tx = TransactionWrapper.getCurrentTransaction();
			final NodeWrapper startNode = NodeWrapper.getWrapper(graphDb, entity.getStartNode());
			final NodeWrapper endNode   = NodeWrapper.getWrapper(graphDb, entity.getEndNode());

			tx.registerModified(startNode);
			tx.registerModified(endNode);
			tx.registerModified(this);

			entity.delete();
			deleted = true;

			// remove deleted relationship from cache
			graphDb.removeRelationshipFromCache(getId());
			startNode.clearCaches();
			endNode.clearCaches();

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public boolean isSpatialEntity() {
		return false;
	}

	// ----- helper methods -----
	public org.structr.bolt.db.Relationship unwrap() {
		return entity;
	}

	// ----- public static methods -----
	public static RelationshipWrapper getWrapper(final BoltDatabaseService graphDb, final org.structr.bolt.db.Relationship relationship) {

		RelationshipWrapper wrapper = graphDb.getRelationshipFromCache(relationship.getId());
		if (wrapper == null) {

			wrapper = new RelationshipWrapper(graphDb, relationship);
			graphDb.storeRelationshipInCache(wrapper);
		}

		return wrapper;
	}
}
