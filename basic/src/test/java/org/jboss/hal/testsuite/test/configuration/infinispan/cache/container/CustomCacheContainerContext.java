package org.jboss.hal.testsuite.test.configuration.infinispan.cache.container;

import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;

public class CustomCacheContainerContext implements CacheContainerContext {

    private static final String INFINISPAN = "infinispan";
    private static final Address INFINISPAN_SUBSYSTEM = Address.subsystem(INFINISPAN);
    private static final String CACHE_CONTAINER = "cache-container";
    private static final String TRANSPORT = "transport";
    private static final String JGROUPS = "jgroups";

    private final Operations operations;
    private final Administration administration;

    private Address cacheContainerAddress;
    private String name;

    public CustomCacheContainerContext(OnlineManagementClient client, String name) {
        operations = new Operations(client);
        administration = new Administration(client);
        this.name = name;
        this.cacheContainerAddress = INFINISPAN_SUBSYSTEM.and(CACHE_CONTAINER, name);
    }

    @Override
    public Address getCacheContainerAddress() {
        return cacheContainerAddress;
    }

    @Override
    public void navigateToCacheContainer(InfinispanPage page) {
        page.navigateToCacheContainer(name);
    }

    @Override
    public void createCacheContainerInModel() throws IOException {
        operations.add(cacheContainerAddress).assertSuccess();
        addJGroupsTransport();
    }

    private void addJGroupsTransport() throws IOException {
        final Address jgroupsTransportAddress = cacheContainerAddress.and(TRANSPORT, JGROUPS);
        operations.add(jgroupsTransportAddress).assertSuccess();
    }

    @Override
    public void removeCacheContainerInModel() throws IOException, OperationException {
        operations.removeIfExists(cacheContainerAddress);
    }

    @Override
    public String toString() {
        return "CUSTOM";
    }
}
