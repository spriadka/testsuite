package org.jboss.hal.testsuite.test.runtime.transaction.beans;

import org.jboss.hal.testsuite.test.runtime.transaction.TxnRollbackException;


public interface StatisticsRemote {
    void testTxRollback() throws TxnRollbackException;
    void testTx();
    void testTxTimeout();
}
