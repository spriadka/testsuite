package org.jboss.hal.testsuite.test.configuration.infinispan.cache.type;

import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractCacheContext implements CacheContext {

    protected Administration administration;
    protected Operations operations;
    protected Address cacheTypeAddress;
    protected String name;

    public AbstractCacheContext(OnlineManagementClient client) {
        this.administration = new Administration(client);
        this.operations = new Operations(client);
    }

    @Override
    public Address getCacheAddress() {
        return cacheTypeAddress;
    }

    @Override
    public void removeCacheInModel() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(cacheTypeAddress);
        administration.reloadIfRequired();
    }

    @Override
    public void createCacheInModel() throws IOException, TimeoutException, InterruptedException {
        operations.add(cacheTypeAddress).assertSuccess();
        administration.reloadIfRequired();
    }
}
