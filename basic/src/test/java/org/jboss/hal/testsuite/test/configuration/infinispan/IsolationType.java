package org.jboss.hal.testsuite.test.configuration.infinispan;

/**
 * Possible locking isolation types
 */
public enum IsolationType {

    READ_COMMITTED("READ_COMMITTED"),
    READ_UNCOMMITTED("READ_UNCOMMITTED"),
    REPEATABLE_READ("REPEATABLE_READ"),
    SERIALIZABLE("SERIALIZABLE"),
    NONE("NONE");

    private String isolationValue;

    IsolationType(String isolationValue) {
        this.isolationValue = isolationValue;
    }

    public String getValue() {
        return isolationValue;
    }
}
