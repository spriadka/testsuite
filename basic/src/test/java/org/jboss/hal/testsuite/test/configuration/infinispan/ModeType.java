package org.jboss.hal.testsuite.test.configuration.infinispan;

/**
 * Possible cache transaction modes
 */
public enum ModeType {

    NONE("NONE"),
    BATCH("BATCH"),
    NON_XA("NON_XA"),
    NON_DURABLE_XA("NON_DURABLE_XA"),
    FULL_XA("FULL_XA");

    private String modeValue;

    ModeType(String modeValue) {
        this.modeValue = modeValue;
    }

    public String getModeValue() {
        return modeValue;
    }
}
