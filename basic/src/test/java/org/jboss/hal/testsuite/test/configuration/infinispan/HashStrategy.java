package org.jboss.hal.testsuite.test.configuration.infinispan;

/**
 * Possible valid hash strategies for configuration of distributed cache in Infinispan subsystem
 */
public enum HashStrategy {

    INTER_CACHE("INTER_CACHE"),
    INTRA_CACHE("INTRA_CACHE");

    private String hashStrategyValue;

    HashStrategy(String hashStrategyValue) {
        this.hashStrategyValue = hashStrategyValue;
    }

    public String getHashStrategyValue() {
        return hashStrategyValue;
    }
}
