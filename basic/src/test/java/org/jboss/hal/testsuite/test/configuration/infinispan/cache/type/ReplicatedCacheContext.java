package org.jboss.hal.testsuite.test.configuration.infinispan.cache.type;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

public class ReplicatedCacheContext extends AbstractCacheContext {

    private static final String REPLICATED_CACHE = "replicated-cache";

    public ReplicatedCacheContext(OnlineManagementClient client, Address cacheContainerAddress, String name) {
        super(client);
        this.name = name;
        this.cacheTypeAddress = cacheContainerAddress.and(REPLICATED_CACHE, name);
    }

    public ReplicatedCacheContext(OnlineManagementClient client, Address cacheContainerAddress) {
        this(client, cacheContainerAddress, "replicated_cache_" + RandomStringUtils.randomAlphanumeric(7));
    }


    @Override
    public void navigateToCache(InfinispanPage page) {
        page.replicatedCaches().selectCache(name);
    }

    @Override
    public String toString() {
        return "Replicated Cache";
    }
}
