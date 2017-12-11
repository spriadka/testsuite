package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.CacheContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CacheContainerContext;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.junit.ArquillianParametrized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.util.Collection;


@RunWith(ArquillianParametrized.class)
@RunAsClient
public class LockingTestCase extends InfinispanTestCaseAbstract {

    private static final String LOCKING_LABEL = "Locking";
    private static final String COMPONENT = "component";
    private static final String LOCKING = "locking";
    private static final String ACQUIRE_TIMEOUT = "acquire-timeout";
    private static final String CONCURRENCY_LEVEL = "concurrency-level";
    private static final String ISOLATION = "isolation";
    private static final String STRIPING = "striping";

    public CacheContext cacheContext;
    public CacheContainerContext cacheContainerContext;

    public LockingTestCase(CacheContainerContext cacheContainerContext, CacheContext navigator) {
        this.cacheContainerContext = cacheContainerContext;
        this.cacheContext = navigator;
    }

    @Parameterized.Parameters(name = "Cache container: {0}, Cache type: {1}")
    public static Collection parameters() {
        return new ParametersFactory(client).containerTypeMatrix();
    }

    @Test
    public void editLockingTest() throws Exception {
        final long acquireTimeout = 2000;
        final int concurrencyLevel = 5000;
        final boolean striping = true;
        final IsolationType isolation = IsolationType.READ_UNCOMMITTED;
        final Address lockingAddress = cacheContext.getCacheAddress().and(COMPONENT, LOCKING);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(LOCKING_LABEL);
            new ConfigChecker.Builder(client, lockingAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, ACQUIRE_TIMEOUT, String.valueOf(acquireTimeout))
                    .edit(ConfigChecker.InputType.TEXT, CONCURRENCY_LEVEL, String.valueOf(concurrencyLevel))
                    .edit(ConfigChecker.InputType.SELECT, ISOLATION, isolation.getValue())
                    .edit(ConfigChecker.InputType.CHECKBOX, STRIPING, striping)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(ACQUIRE_TIMEOUT, acquireTimeout)
                    .verifyAttribute(CONCURRENCY_LEVEL, concurrencyLevel)
                    .verifyAttribute(STRIPING, striping)
                    .verifyAttribute(ISOLATION, isolation.getValue());

        } finally {
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

}
