package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class DistributedCacheTestCase extends AbstractCacheTestCase {

    @BeforeClass
    public static void beforeClass_() {
        initializeTBAAddress(CacheType.DISTRIBUTED);
    }

    @Before
    public void before_() {
        page.distributed();
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
    }

    protected Address createCache() throws IOException, TimeoutException, InterruptedException {
        final Address address = ABSTRACT_CACHE_ADDRESS.and(CacheType.DISTRIBUTED.getAddressName(), RandomStringUtils.randomAlphabetic(7));
        operations.add(address, Values.of("mode", "SYNC")).assertSuccess();
        return address;
    }
}

