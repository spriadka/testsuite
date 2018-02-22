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
public class ReplicatedCacheTestCase extends InfinispanTestCaseAbstract {

    private static final String REPLICATED_CACHE = "replicated-cache";
    private static final String MODULE = "module";
    private static final String REMOTE_TIMEOUT = "remote-timeout";
    private static final String STATISTICS_ENABLED = "statistics-enabled";

    public CacheContainerContext cacheContainerContext;

    public ReplicatedCacheTestCase(CacheContainerContext cacheContainerContext) {
        this.cacheContainerContext = cacheContainerContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}")
    public static Collection parameters() {
        return new ParametersFactory(client).cacheContainerTable();
    }

    @Test
    public void addReplicatedCacheCacheTest() throws Exception {
        final String replicatedCacheName = "replicated_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address replicatedCacheAddress = cacheContainerContext.getCacheContainerAddress().and(REPLICATED_CACHE,
                replicatedCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.replicatedCaches()
                    .getResourceManager()
                    .addResource(AddInvalidationCacheWizard.class)
                    .name(replicatedCacheName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created replicated cache should be present in the table",
                    page.getResourceManager().isResourcePresent(replicatedCacheName));
            new ResourceVerifier(replicatedCacheAddress, client).verifyExists();

        } finally {
            operations.removeIfExists(replicatedCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void removeReplicatedCacheTest() throws Exception {
        final String replicatedCacheName = "replicated_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address replicatedCacheAddress = cacheContainerContext.getCacheContainerAddress().and(REPLICATED_CACHE,
                replicatedCacheName);
        final ResourceVerifier verifier = new ResourceVerifier(replicatedCacheAddress, client);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(replicatedCacheAddress);
            administration.reloadIfRequired();
            verifier.verifyExists();
            cacheContainerContext.navigateToCacheContainer(page);
            page.replicatedCaches()
                    .getResourceManager()
                    .removeResource(replicatedCacheName)
                    .confirmAndDismissReloadRequiredMessage();
            Assert.assertFalse("Newly removed replicated cache should not be present in the table anymore",
                    page.getResourceManager().isResourcePresent(replicatedCacheName));
            verifier.verifyDoesNotExist();
        } finally {
            operations.removeIfExists(replicatedCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editAttributesTest() throws Exception {
        final String module = "module_" + RandomStringUtils.randomAlphanumeric(7);
        final long remoteTimeout = 10000;
        final boolean statisticsEnabled = true;
        final String replicatedCacheName = "replicated_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address replicatedCacheAddress = cacheContainerContext.getCacheContainerAddress().and(REPLICATED_CACHE,
                replicatedCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(replicatedCacheAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.replicatedCaches()
                    .getResourceManager()
                    .selectByName(replicatedCacheName);
            new ConfigChecker.Builder(client, replicatedCacheAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, MODULE, module)
                    .edit(ConfigChecker.InputType.TEXT, REMOTE_TIMEOUT, String.valueOf(remoteTimeout))
                    .edit(ConfigChecker.InputType.CHECKBOX, STATISTICS_ENABLED, statisticsEnabled)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(MODULE, module)
                    .verifyAttribute(REMOTE_TIMEOUT, remoteTimeout)
                    .verifyAttribute(STATISTICS_ENABLED, statisticsEnabled);
        } finally {
            operations.removeIfExists(replicatedCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }
}
