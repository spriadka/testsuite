package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CacheContainerContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.CacheContext;

import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.junit.ArquillianParametrized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.util.Collection;

@RunWith(ArquillianParametrized.class)
@RunAsClient
public class ExpirationTestCase extends InfinispanTestCaseAbstract {

    private static final String EXPIRATION = "expiration";
    private static final String EXPIRATION_LABEL = "Expiration";

    private static final String COMPONENT = "component";

    private static final String INTERVAL = "interval";
    private static final String LIFESPAN = "lifespan";
    private static final String MAX_IDLE = "max-idle";

    public CacheContext cacheContext;
    public CacheContainerContext cacheContainerContext;

    public ExpirationTestCase(CacheContainerContext cacheContainerContext, CacheContext cacheContext) {
        this.cacheContainerContext = cacheContainerContext;
        this.cacheContext = cacheContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}, Cache type: {1}")
    public static Collection values() {
        return new ParametersFactory(client).containerTypeMatrix();
    }

    @Test
    public void editExpirationTest() throws Exception {
        final long interval = 123456;
        final long lifespan = 654321;
        final long maxIdle = 234;
        final Address expirationAddress = cacheContext.getCacheAddress().and(COMPONENT, EXPIRATION);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(EXPIRATION_LABEL);
            new ConfigChecker.Builder(client, expirationAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, INTERVAL, String.valueOf(interval))
                    .edit(ConfigChecker.InputType.TEXT, LIFESPAN, String.valueOf(lifespan))
                    .edit(ConfigChecker.InputType.TEXT, MAX_IDLE, String.valueOf(maxIdle))
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(INTERVAL, interval)
                    .verifyAttribute(LIFESPAN, lifespan)
                    .verifyAttribute(MAX_IDLE, maxIdle);
        } finally {
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

}
