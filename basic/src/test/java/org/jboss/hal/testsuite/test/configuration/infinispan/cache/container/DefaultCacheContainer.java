package org.jboss.hal.testsuite.test.configuration.infinispan.cache.container;

import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.operations.Address;

public enum DefaultCacheContainer implements CacheContainerContext {

    HIBERNATE("hibernate"),
    WEB("web"),
    EJB("ejb"),
    SERVER("server");

    private final String name;
    private static final String INFINISPAN = "infinispan";
    private static final String CACHE_CONTAINER = "cache-container";
    private static final Address INFINISPAN_SUBSYSTEM_ADDRESS = Address.subsystem(INFINISPAN);


    DefaultCacheContainer(String name) {
        this.name = name;
    }

    @Override
    public Address getCacheContainerAddress() {
        return INFINISPAN_SUBSYSTEM_ADDRESS.and(CACHE_CONTAINER, name);
    }

    @Override
    public void navigateToCacheContainer(InfinispanPage page) {
        page.navigateToCacheContainer(name);
    }

    @Override
    public void createCacheContainerInModel() {

    }

    @Override
    public void removeCacheContainerInModel() {

    }
}
