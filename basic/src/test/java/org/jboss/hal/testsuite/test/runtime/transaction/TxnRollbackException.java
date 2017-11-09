package org.jboss.hal.testsuite.test.runtime.transaction;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TxnRollbackException extends Exception {

    private static final long serialVersionUID = 1L;

    public TxnRollbackException(String message) {
        super(message);
    }
}
