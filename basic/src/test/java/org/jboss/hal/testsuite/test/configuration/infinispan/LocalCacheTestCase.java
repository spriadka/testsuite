package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
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
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class LocalCacheTestCase extends AbstractCacheTestCase {

    @Override
    @Test
    public void testCreateCache() throws Exception {
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
        initializeTBAAddress(CacheType.LOCAL);
    }

    @Before
    public void before_() {
        page.local();
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
    }

    protected Address createCache() throws IOException, TimeoutException, InterruptedException {
        final Address address = ABSTRACT_CACHE_ADDRESS.and(CacheType.LOCAL.getAddressName(), RandomStringUtils.randomAlphabetic(7));
        operations.add(address, Values.of("mode", "SYNC")).assertSuccess();
        return address;
    }
}

