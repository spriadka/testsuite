package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.infinispan.AddDistributedCacheWizard;
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
public class DistributedCacheTestCase extends InfinispanTestCaseAbstract {

    private static final String DISTRIBUTED_CACHE = "distributed-cache";
    private static final String CAPACITY_FACTOR = "capacity-factor";
    private static final String CONSISTENT_HASH_STRATEGY = "consistent-hash-strategy";
    private static final String L1_LIFESPAN = "l1-lifespan";
    private static final String MODULE = "module";
    private static final String OWNERS = "owners";
    private static final String REMOTE_TIMEOUT = "remote-timeout";
    private static final String SEGMENTS = "segments";
    private static final String STATISTICS_ENABLED = "statistics-enabled";

    public CacheContainerContext cacheContainerContext;

    public DistributedCacheTestCase(CacheContainerContext cacheContainerContext) {
        this.cacheContainerContext = cacheContainerContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}")
    public static Collection parameters() {
        return new ParametersFactory(client).cacheContainerTable();
    }

    @Test
    public void addDistributedCacheTest() throws Exception {
        final String distributedCacheName = "distributed_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address distributedCacheAddress = cacheContainerContext.getCacheContainerAddress().and(DISTRIBUTED_CACHE,
                distributedCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.distributed()
                    .getResourceManager()
                    .addResource(AddDistributedCacheWizard.class)
                    .name(distributedCacheName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created distributed cache should be present in the table",
                    page.getResourceManager().isResourcePresent(distributedCacheName));
            new ResourceVerifier(distributedCacheAddress, client).verifyExists();
        } finally {
            operations.removeIfExists(distributedCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void removeDistributedCacheTest() throws Exception {
        final String distributedCacheName = "distributed_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address distributedCacheAddress = cacheContainerContext.getCacheContainerAddress().and(DISTRIBUTED_CACHE,
                distributedCacheName);
        final ResourceVerifier verifier = new ResourceVerifier(distributedCacheAddress, client);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(distributedCacheAddress);
            administration.reloadIfRequired();
            verifier.verifyExists();
            cacheContainerContext.navigateToCacheContainer(page);
            page.distributed()
                    .getResourceManager()
                    .removeResource(distributedCacheName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed distributed cache should not be present in the table",
                    page.getResourceManager().isResourcePresent(distributedCacheName));
            verifier.verifyDoesNotExist();
        } finally {
            operations.removeIfExists(distributedCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editAttributesTest() throws Exception {
        final double capacityFactor = 20;
        final HashStrategy hashStrategy = HashStrategy.INTRA_CACHE;
        final long l1Lifespan = 250;
        final String module = "module_" + RandomStringUtils.randomAlphanumeric(7);
        final int owners = 20;
        final long remoteTimeout = 250;
        final int segments = 789;
        final boolean statisticsEnabled = true;
        final String distributedCacheName = "distributed_cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address distributedCacheAddress = cacheContainerContext.getCacheContainerAddress().and(DISTRIBUTED_CACHE,
                distributedCacheName);
        try {
            cacheContainerContext.createCacheContainerInModel();
            operations.add(distributedCacheAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            page.distributed()
                    .getResourceManager()
                    .selectByName(distributedCacheName);
            new ConfigChecker.Builder(client, distributedCacheAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, CAPACITY_FACTOR, String.valueOf(capacityFactor))
                    .edit(ConfigChecker.InputType.SELECT, CONSISTENT_HASH_STRATEGY, hashStrategy.getHashStrategyValue())
                    .edit(ConfigChecker.InputType.TEXT, L1_LIFESPAN, String.valueOf(l1Lifespan))
                    .edit(ConfigChecker.InputType.TEXT, MODULE, module)
                    .edit(ConfigChecker.InputType.TEXT, OWNERS, String.valueOf(owners))
                    .edit(ConfigChecker.InputType.TEXT, REMOTE_TIMEOUT, String.valueOf(remoteTimeout))
                    .edit(ConfigChecker.InputType.TEXT, SEGMENTS, String.valueOf(segments))
                    .edit(ConfigChecker.InputType.CHECKBOX, STATISTICS_ENABLED, statisticsEnabled)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CAPACITY_FACTOR, new ModelNode(capacityFactor))
                    .verifyAttribute(CONSISTENT_HASH_STRATEGY, hashStrategy.getHashStrategyValue())
                    .verifyAttribute(L1_LIFESPAN, l1Lifespan)
                    .verifyAttribute(MODULE, module)
                    .verifyAttribute(OWNERS, owners)
                    .verifyAttribute(REMOTE_TIMEOUT, remoteTimeout)
                    .verifyAttribute(SEGMENTS, segments)
                    .verifyAttribute(STATISTICS_ENABLED, statisticsEnabled);
        } finally {
            operations.removeIfExists(distributedCacheAddress);
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }

    }
}
