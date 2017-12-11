package org.jboss.hal.testsuite.test.configuration.infinispan.cache.type;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

public class DistributedCacheContext extends AbstractCacheContext {

    private static final String DISTRIBUTED_CACHE = "distributed-cache";

    public DistributedCacheContext(OnlineManagementClient client, Address cacheContainerAddress, String name) {
        super(client);
        this.name = name;
        this.cacheTypeAddress = cacheContainerAddress.and(DISTRIBUTED_CACHE, name);
    }

    public DistributedCacheContext(OnlineManagementClient client, Address cacheContainerAddress) {
        this(client, cacheContainerAddress, "distributed_cache_" + RandomStringUtils.randomAlphanumeric(7));
    }

    @Override
    public void navigateToCache(InfinispanPage page) {
        page.distributed().getResourceManager().selectByName(name);
    }

    @Override
    public String toString() {
        return "Distributed Cache";
    }
}
