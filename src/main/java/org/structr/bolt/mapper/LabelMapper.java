/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.mapper;

import java.util.function.Function;
import org.structr.api.graph.Label;
import org.structr.bolt.wrapper.LabelWrapper;

/**
 *
 * @author Christian Morgner
 */
public class LabelMapper implements Function<org.neo4j.graphdb.Label, Label> {

	@Override
	public Label apply(org.neo4j.graphdb.Label t) {
		return new LabelWrapper(t);
	}
}
