package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class InvalidationCacheTestCase extends AbstractCacheTestCase {

    @Before
    public void before_() {
        page.invalidation();
    }

    @Override
    protected CacheType getCacheType() {
        return CacheType.INVALIDATION;
    }

    public void addCache() throws IOException {
        operations.add(cacheAddress, Values.of("mode", "SYNC"));
        operations.add(transactionAddress);
        operations.add(storeAddress, Values.of("class", "org.infinispan.configuration.cache.SingleFileStoreConfigurationBuilder"));
        operations.add(lockingAddress);
    }

    public void deleteCache() throws IOException, OperationException {
        operations.removeIfExists(cacheAddress);
    }
}

