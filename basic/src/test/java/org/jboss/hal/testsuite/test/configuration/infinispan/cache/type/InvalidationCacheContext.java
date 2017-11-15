package org.jboss.hal.testsuite.test.configuration.infinispan.cache.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

public class InvalidationCacheContext extends AbstractCacheContext {

    private static final String INVALIDATION_CACHE = "invalidation-cache";

    public InvalidationCacheContext(OnlineManagementClient client, Address cacheContainerAddress) {
        this(client, cacheContainerAddress, "invalidation_cache_" + RandomStringUtils.randomAlphanumeric(7));
    }

    public InvalidationCacheContext(OnlineManagementClient client, Address cacheContainerAddress, String name) {
        super(client);
        this.name = name;
        cacheTypeAddress = cacheContainerAddress.and(INVALIDATION_CACHE, name);
    }

    @Override
    public void navigateToCache(InfinispanPage page) {
        page.invalidation()
                .getResourceManager()
                .selectByName(name);
    }

    @Override
    public String toString() {
        return "Invalidation Cache";
    }
}
