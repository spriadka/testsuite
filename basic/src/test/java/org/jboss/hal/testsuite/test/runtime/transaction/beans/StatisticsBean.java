package org.jboss.hal.testsuite.test.runtime.transaction.beans;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.ejb3.annotation.TransactionTimeout;
import org.jboss.logging.Logger;
import org.jboss.hal.testsuite.test.runtime.transaction.TxnRollbackException;

@Stateless
@Remote(StatisticsRemote.class)
public class StatisticsBean implements StatisticsRemote {
    private static Logger log = Logger.getLogger(StatisticsBean.class);

    private static volatile CountDownLatch latch = new CountDownLatch(1);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void testTxRollback() throws TxnRollbackException {
        log.info("testTxSleep called");
        throw new TxnRollbackException("Rollback called transaction");
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void testTx() {
        log.info("testTx called");
    }

    @Override
    @TransactionTimeout(value = 1, unit = TimeUnit.SECONDS)
    public void testTxTimeout() {
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
