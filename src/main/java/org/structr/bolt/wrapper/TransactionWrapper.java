/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.bolt.wrapper;

import java.util.HashSet;
import java.util.Set;
import org.neo4j.graphdb.NotInTransactionException;
import org.structr.api.Transaction;

/**
 *
 * @author Christian Morgner
 */
public class TransactionWrapper implements Transaction {

	private static final ThreadLocal<TransactionWrapper> transactions = new ThreadLocal<>();
	private final Set<EntityWrapper> modifiedEntites                  = new HashSet<>();
	private org.neo4j.driver.v1.Transaction tx                          = null;

	public TransactionWrapper(final org.neo4j.driver.v1.Transaction tx) {
		transactions.set(this);
		this.tx = tx;
	}

        public org.neo4j.driver.v1.ResultCursor run(String nativeQuery){
            return tx.run(nativeQuery);
        }
                
	@Override
	public void failure() {
		tx.failure();
	}

	@Override
	public void success() {
		tx.success();
	}

	@Override
	public void close() {

		tx.close();
		transactions.remove();

		for (final EntityWrapper entity : modifiedEntites) {
			entity.clearCaches();
		}
	}

	public void registerModified(final EntityWrapper entity) {
		modifiedEntites.add(entity);
	}

	// ----- public static methods -----
	public static TransactionWrapper getCurrentTransaction() {

		final TransactionWrapper tx = transactions.get();
		if (tx == null) {

			throw new NotInTransactionException();
		}

		return tx;
	}
}
