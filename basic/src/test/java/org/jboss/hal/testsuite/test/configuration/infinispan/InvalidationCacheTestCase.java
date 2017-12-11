package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.infinispan.AddInvalidationCacheWizard;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CacheContainerContext;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.junit.ArquillianParametrized;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.util.Collection;

@RunWith(ArquillianParametrized.class)
@RunAsClient
public class InvalidationCacheTestCase extends InfinispanTestCaseAbstract {

    private static final String INVALIDATION_CACHE = "invalidation-cache";
    private static final String JNDI_NAME = "jndi-name";
    private static final String MODE = "mode";
    private static final String MODULE = "module";
    private static final String REMOTE_TIMEOUT = "remote-timeout";
    private static final String STATISTICS_ENABLED = "statistics-enabled";

    public final CacheContainerContext cacheContainerContext;

    public InvalidationCacheTestCase(CacheContainerContext cacheContainerContext) {
        this.cacheContainerContext = cacheContainerContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}")
    public static Collection parameters() {
        return new ParametersFactory(client).cacheContainerTable();
    }

    @Test
    public void addInvalidationCacheTest() throws Exception {
        final String invalidationCacheName = "invalidation_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address invalidationCacheAddress = cacheContainerContext.getCacheContainerAddress().and(INVALIDATION_CACHE,
                invalidationCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.invalidation()
                    .getResourceManager()
                    .addResource(AddInvalidationCacheWizard.class)
                    .name(invalidationCacheName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created invalidation cache should be present in the table",
                    page.getResourceManager().isResourcePresent(invalidationCacheName));
            new ResourceVerifier(invalidationCacheAddress, client).verifyExists();

        } finally {
            operations.removeIfExists(invalidationCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void removeInvalidationCacheTest() throws Exception {
        final String invalidationCacheName = "invalidation_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address invalidationCacheAddress = cacheContainerContext.getCacheContainerAddress().and(INVALIDATION_CACHE,
                invalidationCacheName);
        final ResourceVerifier verifier = new ResourceVerifier(invalidationCacheAddress, client);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(invalidationCacheAddress);
            administration.reloadIfRequired();
            verifier.verifyExists();
            cacheContainerContext.navigateToCacheContainer(page);
            page.invalidation()
                    .getResourceManager()
                    .removeResource(invalidationCacheName)
                    .confirmAndDismissReloadRequiredMessage();
            Assert.assertFalse("Newly removed invalidation cache should not be present in the table anymore",
                    page.getResourceManager().isResourcePresent(invalidationCacheName));
            verifier.verifyDoesNotExist();
        } finally {
            operations.removeIfExists(invalidationCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editAttributesTest() throws Exception {
        final String jndiName = "jndi://" + RandomStringUtils.randomAlphanumeric(7);
        final SyncMode mode = SyncMode.ASYNC;
        final String module = "module_" + RandomStringUtils.randomAlphanumeric(7);
        final long remoteTimeout = 10000;
        final boolean statisticsEnabled = true;
        final String invalidationCacheName = "invalidation_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address invalidationCacheAddress = cacheContainerContext.getCacheContainerAddress().and(INVALIDATION_CACHE,
                invalidationCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(invalidationCacheAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.invalidation()
                    .getResourceManager()
                    .selectByName(invalidationCacheName);
            new ConfigChecker.Builder(client, invalidationCacheAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, JNDI_NAME, jndiName)
                    .edit(ConfigChecker.InputType.SELECT, MODE, mode.getSyncModeValue())
                    .edit(ConfigChecker.InputType.TEXT, MODULE, module)
                    .edit(ConfigChecker.InputType.TEXT, REMOTE_TIMEOUT, String.valueOf(remoteTimeout))
                    .edit(ConfigChecker.InputType.CHECKBOX, STATISTICS_ENABLED, statisticsEnabled)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JNDI_NAME, jndiName)
                    .verifyAttribute(MODE, mode.getSyncModeValue())
                    .verifyAttribute(MODULE, module)
                    .verifyAttribute(REMOTE_TIMEOUT, remoteTimeout)
                    .verifyAttribute(STATISTICS_ENABLED, statisticsEnabled);
        } finally {
            operations.removeIfExists(invalidationCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

}
