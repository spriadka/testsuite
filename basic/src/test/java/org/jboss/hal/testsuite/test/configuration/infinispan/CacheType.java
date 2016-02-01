package org.jboss.hal.testsuite.test.configuration.infinispan;


enum CacheType {

    DISTRIBUTED("distributed-cache"),
    INVALIDATION("invalidation-cache"),
    LOCAL("local-cache"),
    REPLICATED("replicated-cache");

    String address;

    CacheType(String address) {
        this.address = address;
    }

    String getAddressName() {
        return address;
    }
}
