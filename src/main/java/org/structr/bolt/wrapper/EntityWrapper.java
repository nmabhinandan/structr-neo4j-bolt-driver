/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.wrapper;

import java.util.HashMap;
import java.util.Map;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.structr.api.NotFoundException;
import org.structr.api.NotInTransactionException;
import org.structr.api.graph.Direction;
import org.structr.api.graph.Label;
import org.structr.api.graph.Node;
import org.structr.api.graph.PropertyContainer;
import org.structr.api.graph.Relationship;
import org.structr.api.graph.RelationshipType;
import org.structr.bolt.BoltDatabaseService;

/**
 *
 * @author Christian Morgner
 */
public abstract class EntityWrapper<T extends org.structr.bolt.db.Entity> implements PropertyContainer {

	protected Map<String, Boolean> hasCache = new HashMap<>();
	protected Map<String, Object> cache     = new HashMap<>();
	protected BoltDatabaseService graphDb  = null;
	protected boolean deleted               = false;
	protected T entity                      = null;

	public EntityWrapper(final BoltDatabaseService graphDb, final T entity) {
		this.graphDb = graphDb;
		this.entity  = entity;
	}

	@Override
	public boolean hasProperty(final String name) {

		assertNotDeleted();

		Boolean hasProperty = hasCache.get(name);
		if (hasProperty == null) {

			try {

				hasProperty = entity.hasProperty(name);
				hasCache.put(name, hasProperty);

			} catch (org.neo4j.graphdb.NotInTransactionException t) {

				throw new NotInTransactionException(t);

			} catch (Throwable t) {

				throw new NotFoundException(t);
			}
		}

		return hasProperty;
	}

	@Override
	public Object getProperty(final String name) {

		assertNotDeleted();

		Object value = cache.get(name);
		if (value == null) {

			try {

				value = entity.getProperty(name);
				if (value != null) {

					cache.put(name, value);
				}

			} catch (org.neo4j.graphdb.NotInTransactionException t) {

				throw new NotInTransactionException(t);

			} catch (Throwable t) {

				throw new NotFoundException(t);
			}
		}

		return value;
	}

	@Override
	public Object getProperty(final String name, final Object defaultValue) {

		final Object value = getProperty(name);
		if (value == null) {

			return defaultValue;
		}

		return value;
	}

	@Override
	public void setProperty(final String name, final Object value) {

		assertNotDeleted();

		try {

			TransactionWrapper.getCurrentTransaction().registerModified(this);
			entity.setProperty(name, value);
			cache.put(name, value);
			hasCache.put(name, Boolean.TRUE);

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);

		} catch (Throwable t) {

			throw new NotFoundException(t);
		}
	}

	@Override
	public void removeProperty(final String name) {

		assertNotDeleted();

		try {
			TransactionWrapper.getCurrentTransaction().registerModified(this);
			entity.removeProperty(name);
			cache.remove(name);
			hasCache.put(name, Boolean.FALSE);

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);

		} catch (Throwable t) {

			throw new NotFoundException(t);
		}
	}

	@Override
	public Iterable<String> getPropertyKeys() {

		assertNotDeleted();

		try {
			return entity.getPropertyKeys();

		} catch (org.neo4j.graphdb.NotInTransactionException t) {

			throw new NotInTransactionException(t);

		} catch (Throwable t) {

			throw new NotFoundException(t);
		}
	}

	// ----- helper methods -----
	public org.structr.bolt.db.Node unwrap(final Node node) {

		if (node instanceof NodeWrapper) {

			return ((NodeWrapper)node).unwrap();
		}

		throw new RuntimeException("Invalid node type " + node.getClass());
	}

	public org.structr.bolt.db.Relationship unwrap(final Relationship relationship) {

		if (relationship instanceof RelationshipWrapper) {

			return ((RelationshipWrapper)relationship).unwrap();
		}

		throw new RuntimeException("Invalid relationship type " + relationship.getClass());
	}

	public org.neo4j.graphdb.RelationshipType unwrap(final RelationshipType relationshipType) {

		if (relationshipType instanceof RelationshipTypeWrapper) {

			return ((RelationshipTypeWrapper)relationshipType).unwrap();
		}

		return DynamicRelationshipType.withName(relationshipType.name());
	}

	public org.neo4j.graphdb.Label unwrap(final Label label) {

		if (label instanceof LabelWrapper) {

			return ((LabelWrapper)label).unwrap();
		}

		return DynamicLabel.label(label.name());
	}

	public org.neo4j.graphdb.Direction unwrap(final Direction direction) {

		switch (direction) {

			case BOTH:
				return org.neo4j.graphdb.Direction.BOTH;

			case INCOMING:
				return org.neo4j.graphdb.Direction.INCOMING;

			case OUTGOING:
				return org.neo4j.graphdb.Direction.OUTGOING;
		}

		return null;
	}

	public void clearCaches() {
		cache.clear();
		hasCache.clear();
	}

	// ----- private methods -----
	private void assertNotDeleted() {

		if (deleted) {
			throw new NotFoundException("Entity with ID " + getId() + " has been deleted");
		}
	}
}
