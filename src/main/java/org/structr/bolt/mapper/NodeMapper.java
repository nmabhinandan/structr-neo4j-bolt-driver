/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.mapper;

import java.util.function.Function;
import org.structr.api.graph.Node;
import org.structr.bolt.BoltDatabaseService;
import org.structr.bolt.wrapper.NodeWrapper;

/**
 *
 * @author Christian Morgner
 */
public class NodeMapper extends EntityMapper implements Function<org.structr.bolt.db.Node , Node> {

	public NodeMapper(final BoltDatabaseService graphDb) {
		super(graphDb);
	}

	@Override
	public Node apply(org.structr.bolt.db.Node t) {
		return NodeWrapper.getWrapper(graphDb, t);
	}
}
