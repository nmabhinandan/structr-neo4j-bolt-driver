/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.structr.api.graph.Direction;
import org.structr.api.util.Iterables;
import org.structr.api.graph.Label;
import org.structr.api.graph.Node;
import org.structr.api.NotInTransactionException;
import org.structr.api.graph.Relationship;
import org.structr.api.graph.RelationshipType;
import org.structr.bolt.BoltDatabaseService;
import org.structr.bolt.mapper.LabelMapper;
import org.structr.bolt.mapper.RelationshipMapper;

/**
 *
 * @author Christian Morgner
 */
public class NodeWrapper extends EntityWrapper<org.structr.bolt.db.Node> implements Node {

	private final Map<String, Set<Relationship>> relationshipCache = new HashMap<>();

	private NodeWrapper(final BoltDatabaseService graphDb, final org.structr.bolt.db.Node node) {
		super(graphDb, node);
	}

	@Override
	public int hashCode() {
		return entity.hashCode();
	}

	@Override
	public boolean equals(final Object other) {

		if (other instanceof Node) {
			return other.hashCode() == hashCode();
		}

		return false;
	}

	@Override
	public Relationship createRelationshipTo(final Node endNode, final RelationshipType relationshipType) {

		try {

			TransactionWrapper.getCurrentTransaction().registerModified(this);

			// clear caches of start and end node
			((NodeWrapper)endNode).relationshipCache.clear();
			relationshipCache.clear();

			return RelationshipWrapper.getWrapper(graphDb, entity.createRelationshipTo(unwrap(endNode), unwrap(relationshipType)));

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public void addLabel(final Label label) {

		try {
			TransactionWrapper.getCurrentTransaction().registerModified(this);
			entity.addLabel(unwrap(label));

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public void removeLabel(final Label label) {

		try {
			TransactionWrapper.getCurrentTransaction().registerModified(this);
			entity.removeLabel(unwrap(label));

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public Iterable<Label> getLabels() {

		try {
			return Iterables.map(new LabelMapper(), entity.getLabels());

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public Iterable<Relationship> getRelationships() {

		Set<Relationship> allRelationships = relationshipCache.get("*");
		if (allRelationships == null) {

			try {

				allRelationships = Iterables.toSet(Iterables.map(new RelationshipMapper(graphDb), entity.getRelationships()));
				relationshipCache.put("*", allRelationships);

			} catch (org.neo4j.graphdb.NotInTransactionException t) {

				throw new NotInTransactionException(t);
			}
		}

		return allRelationships;
	}

	@Override
	public Iterable<Relationship> getRelationships(Direction direction) {

		Set<Relationship> relationships = relationshipCache.get(direction.name());
		if (relationships == null) {

			try {

				relationships = Iterables.toSet(Iterables.map(new RelationshipMapper(graphDb), entity.getRelationships(unwrap(direction))));
				relationshipCache.put(direction.name(), relationships);

			} catch (org.neo4j.graphdb.NotInTransactionException t) {

				throw new NotInTransactionException(t);
			}
		}

		return relationships;
	}

	@Override
	public Iterable<Relationship> getRelationships(Direction direction, RelationshipType relationshipType) {

		Set<Relationship> relationships = relationshipCache.get(direction.name() + relationshipType.name());
		if (relationships == null) {

			try {

				relationships = Iterables.toSet(Iterables.map(new RelationshipMapper(graphDb), entity.getRelationships(unwrap(direction), unwrap(relationshipType))));
				relationshipCache.put(direction.name() + relationshipType.name(), relationships);

			} catch (org.neo4j.graphdb.NotInTransactionException t) {

				throw new NotInTransactionException(t);
			}
		}

		return relationships;
	}

	@Override
	public long getId() {
		return entity.getId();
	}

	@Override
	public void delete() throws NotInTransactionException {

		try {

			TransactionWrapper.getCurrentTransaction().registerModified(this);
			entity.delete();
			deleted = true;

			graphDb.removeNodeFromCache(getId());

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);
		}
	}

	@Override
	public boolean isSpatialEntity() {
		return false;
	}

	// ----- helper methods -----
	public org.structr.bolt.db.Node unwrap() {
		return entity;
	}

	@Override
	public void clearCaches() {
		super.clearCaches();
		relationshipCache.clear();
	}

	// ----- public static methods -----
	public static NodeWrapper getWrapper(final BoltDatabaseService graphDb, final org.structr.bolt.db.Node node) {

		NodeWrapper wrapper = graphDb.getNodeFromCache(node.getId());
		if (wrapper == null) {

			wrapper = new NodeWrapper(graphDb, node);
			graphDb.storeNodeInCache(wrapper);
		}

		return wrapper;
	}
}
