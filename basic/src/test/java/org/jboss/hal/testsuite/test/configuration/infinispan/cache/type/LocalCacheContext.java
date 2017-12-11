package org.jboss.hal.testsuite.test.configuration.infinispan.cache.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;


public class LocalCacheContext extends AbstractCacheContext {

    private static final String LOCAL_CACHE = "local-cache";

    public LocalCacheContext(OnlineManagementClient client, Address cacheContainerAddress) {
        this(client, cacheContainerAddress, "local_cache_" + RandomStringUtils.randomAlphanumeric(7));
    }

    public LocalCacheContext(OnlineManagementClient client, Address cacheContainerAddress, String name) {
        super(client);
        this.name = name;
        this.cacheTypeAddress = cacheContainerAddress.and(LOCAL_CACHE, name);
    }

    @Override
    public void navigateToCache(InfinispanPage page) {
        page.localCaches()
                .getResourceManager()
                .selectByName(name);
    }

    @Override
    public String toString() {
        return "Local Cache";
    }
}
