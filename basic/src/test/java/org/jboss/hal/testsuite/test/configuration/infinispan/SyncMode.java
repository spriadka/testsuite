package org.jboss.hal.testsuite.test.configuration.infinispan;

/**
 * Possible clustered cache mode for replicated cache configuration
 */
public enum SyncMode {

    SYNC("SYNC"),
    ASYNC("ASYNC");

    private String syncModeValue;

    SyncMode(String modeValue) {
        this.syncModeValue = modeValue;
    }

    public String getSyncModeValue() {
        return syncModeValue;
    }
}
