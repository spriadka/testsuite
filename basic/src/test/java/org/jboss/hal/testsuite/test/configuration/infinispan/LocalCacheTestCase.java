package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class LocalCacheTestCase extends AbstractCacheTestCase {

    @Override
    @Test
    public void createCache() throws Exception {
        String name = "cn_" + RandomStringUtils.randomAlphanumeric(5);
        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(name)
                .finish();

        Assert.assertTrue("Window should be closed", result);
        administration.reloadIfRequired();
        new ResourceVerifier(cacheAddress, client).verifyExists();

        page.content().getResourceManager().removeResourceAndConfirm(name);
        administration.reloadIfRequired();
        new ResourceVerifier(cacheAddress, client).verifyDoesNotExist();
    }

    @Override
    protected CacheType getCacheType() {
        return CacheType.LOCAL;
    }

    @Before
    public void before_() {
        page.local();
        page.selectCache(cacheName);
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

