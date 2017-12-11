package org.jboss.hal.testsuite.test.configuration.infinispan;

/**
 * Possible locking modes for cache
 */
public enum LockingType {

    PESSIMISTIC("PESSIMISTIC"),
    OPTIMISTIC("OPTIMISTIC");

    private String lockingValue;

    LockingType(String lockingValue) {
        this.lockingValue = lockingValue;
    }

    public String getLockingValue() {
        return lockingValue;
    }
}
