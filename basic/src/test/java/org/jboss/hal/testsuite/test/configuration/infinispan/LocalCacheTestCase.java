package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class LocalCacheTestCase extends AbstractCacheTestCase {

    @Override
    @Test
    public void createCache() {
        String name = "cn_" + RandomStringUtils.randomAlphanumeric(5);
        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(name)
                .finish();

        Assert.assertTrue("Window should be closed", result);
        reloadIfRequiredAndWaitForRunning();
        validVerifier.verifyResource(cacheAddress.resolve(context, name));

        page.content().getResourceManager().removeResourceAndConfirm(name);
        reloadIfRequiredAndWaitForRunning();
        validVerifier.verifyResource(cacheAddress.resolve(context, name), false);
    }

    @Before
    public void setUp() {
        page.local();
    }

    @Override
    protected AddressTemplate getCacheTemplate() {
        return ABSTRACT_CACHE_TEMPLATE.append("/local-cache=*");
    }

    public void addCache() {
        dispatcher.execute(new Operation.Builder("add", cacheAddress.resolve(context, cacheName)).build());
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

