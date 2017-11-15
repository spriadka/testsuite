package org.jboss.hal.testsuite.test.configuration.infinispan;

/**
 * Possible cache eviction strategies
 */
public enum  StrategyType {
    NONE("NONE"),
    UNORDERED("UNORDERED"),
    FIFO("FIFO"),
    LRU("LRU"),
    LIRS("LIRS");
    private String strategyValue;

    StrategyType(String strategyValue) {
        this.strategyValue = strategyValue;
    }

    public String getStrategyValue() {
        return strategyValue;
    }
}
