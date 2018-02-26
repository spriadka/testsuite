package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.infinispan.AddLocalCacheWizard;
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
public class LocalCacheTestCase extends InfinispanTestCaseAbstract {

    private static final String LOCAL_CACHE = "local-cache";
    private static final String MODULE = "module";
    private static final String STATISTICS_ENABLED = "statistics-enabled";

    public CacheContainerContext cacheContainerContext;

    public LocalCacheTestCase(CacheContainerContext cacheContainerContext) {
        this.cacheContainerContext = cacheContainerContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}")
    public static Collection values() {
        return new ParametersFactory(client).cacheContainerTable();
    }

    @Test
    public void addLocalCacheRequiredFieldsOnlyTest() throws Exception {
        final String localCacheName = "local_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address localCacheAddress = cacheContainerContext.getCacheContainerAddress().and(LOCAL_CACHE, localCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.localCaches()
                    .getResourceManager()
                    .addResource(AddLocalCacheWizard.class)
                    .name(localCacheName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created local cache should be present in the table",
                    page.getResourceManager().isResourcePresent(localCacheName));
            new ResourceVerifier(localCacheAddress, client).verifyExists();
        } finally {
            operations.removeIfExists(localCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void removeLocalCacheTest() throws Exception {
        final String localCacheName = "local_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address localCacheAddress = cacheContainerContext.getCacheContainerAddress().and(LOCAL_CACHE, localCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(localCacheAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.localCaches()
                    .getResourceManager()
                    .removeResource(localCacheName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed local cache should not be present in the table anymore",
                    page.getResourceManager().isResourcePresent(localCacheName));
            new ResourceVerifier(localCacheAddress, client).verifyDoesNotExist();
        } finally {
            operations.removeIfExists(localCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editAttributesTest() throws Exception {
        final String localCacheName = "local_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final String module = "module_" + RandomStringUtils.randomAlphanumeric(7);
        final Address localCacheAddress = cacheContainerContext.getCacheContainerAddress().and(LOCAL_CACHE, localCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(localCacheAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.localCaches()
                    .getResourceManager().selectByName(localCacheName);
            new ConfigChecker.Builder(client, localCacheAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, MODULE, module)
                    .edit(ConfigChecker.InputType.CHECKBOX, STATISTICS_ENABLED, true)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(MODULE, module)
                    .verifyAttribute(STATISTICS_ENABLED, true);
        } finally {
            operations.removeIfExists(localCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }
}

