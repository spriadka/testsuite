package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
        CacheWizard wizard = page.content().addCache();

        try {
            boolean result = wizard.name(CACHE_TBA_NAME).finishAndDismissReloadRequiredWindow();

            Assert.assertTrue("Window should be closed", result);
            administration.reloadIfRequired();
            new ResourceVerifier(CACHE_TBA_ADDRESS, client).verifyExists();
        } finally {
            operations.removeIfExists(CACHE_TBA_ADDRESS);
        }
    }

    @BeforeClass
    public static void beforeClass_() {
        initializeAddresses(CacheType.LOCAL);
    }

    @Before
    public void before_() {
        page.local();
        page.selectCache(CACHE_NAME);
    }

    protected void addCache() throws IOException {
        operations.add(CACHE_ADDRESS, Values.of("mode", "SYNC"));
        operations.add(TRANSACTION_ADDRESS);
        addStoreToCache();
        operations.add(LOCKING_ADDRESS);
    }

    protected void deleteCache() throws IOException, OperationException {
        operations.removeIfExists(CACHE_ADDRESS);
    }
}

