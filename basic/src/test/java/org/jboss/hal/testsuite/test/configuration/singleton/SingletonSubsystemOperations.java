package org.jboss.hal.testsuite.test.configuration.singleton;

import org.apache.commons.lang.RandomStringUtils;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * Class containing common methods for singleton subsystem tests
 */
public class SingletonSubsystemOperations {

    private Operations operations;

    /**
     * Create new instance
     * @param client used for executing operations
     * @return new instance
     */
    public SingletonSubsystemOperations(OnlineManagementClient client) {
        this.operations = new Operations(client);
    }

    /**
     * Prepares cache container for use with singleton policy
     * @param cacheContainerAddress address of cache container - not yet created
     * @throws IOException
     */
    public void prepareCacheContainer(Address cacheContainerAddress) throws IOException {
        final String defaultCacheName = "singleton-cache_" + RandomStringUtils.randomAlphanumeric(5);
        operations.add(cacheContainerAddress).assertSuccess();
        operations.add(cacheContainerAddress.and("local-cache", defaultCacheName)).assertSuccess();
        operations.writeAttribute(cacheContainerAddress, "default-cache", defaultCacheName).assertSuccess();
    }

    /**
     * Removes created cache container properly
     * @param cacheContainerAddress address of cache container
     * @throws IOException
     */
    public void removeCacheContainer(Address cacheContainerAddress) throws IOException {
        operations.undefineAttribute(cacheContainerAddress, "default-cache").assertSuccess();
        operations.remove(cacheContainerAddress).assertSuccess();
    }

}
