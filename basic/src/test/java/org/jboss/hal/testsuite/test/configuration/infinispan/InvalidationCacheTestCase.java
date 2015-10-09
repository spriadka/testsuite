package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.Operation;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class InvalidationCacheTestCase extends AbstractCacheTestCase {


    @Override
    protected AddressTemplate getCacheTemplate() {
        return ABSTRACT_CACHE_TEMPLATE.append("/invalidation-cache=*");
    }

    @Before
    public void setUp() {
        page.invalidation();
    }

    public void addCache() {
        dispatcher.execute(new Operation.Builder("add", cacheAddress.resolve(context, cacheName))
                .param("mode", "SYNC")
                .build());
        dispatcher.execute(new Operation.Builder("add", transactionTemplate.resolve(context, cacheName)).build());
        dispatcher.execute(new Operation.Builder("add", storeTemplate.resolve(context, cacheName))
                .param("class", "org.infinispan.configuration.cache.SingleFileStoreConfigurationBuilder")
                .build());
        dispatcher.execute(new Operation.Builder("add", lockingTemplate.resolve(context, cacheName)).build());
    }

    public void deleteCache() {
        dispatcher.execute(new Operation.Builder("remove", cacheAddress.resolve(context, cacheName)).build());
    }
}

