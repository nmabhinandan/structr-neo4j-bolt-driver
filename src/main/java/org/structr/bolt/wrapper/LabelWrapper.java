/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.wrapper;

import org.structr.api.graph.Label;

/**
 *
 * @author Christian Morgner
 */
public class LabelWrapper implements Label {

	private org.neo4j.graphdb.Label label = null;

	public LabelWrapper(org.neo4j.graphdb.Label label) {
		this.label = label;
	}

	@Override
	public String name() {
		return label.name();
	}

	@Override
	public int hashCode() {
		return label.name().hashCode();
	}

	@Override
	public boolean equals(final Object other) {

		if (other instanceof Label) {
			return other.hashCode() == hashCode();
		}

		return false;
	}

	// ----- helper methods -----
	public org.neo4j.graphdb.Label unwrap() {
		return label;
	}
}
