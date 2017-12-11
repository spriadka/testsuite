package org.jboss.hal.testsuite.test.configuration.infinispan.cache.type;

import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * An abstraction used in conjunction with {@link InfinispanPage} responsible for following requirements
 * <ol>
 *     <li>Navigation to specific cache type using {@link InfinispanPage}</li>
 *     <li>(If necessary) Create resources in model</li>
 *     <li>(If necessary) Remove created resources in model</li>
 * </ol>
 */
public interface CacheContext {

    /**
     * Creates cache in model
     */
    void createCacheInModel() throws IOException, TimeoutException, InterruptedException;

    /**
     * Removes cache in model
     */
    void removeCacheInModel() throws IOException, OperationException, TimeoutException, InterruptedException;

    /**
     * Returns {@link Address} associated with cache
     * @return
     */
    Address getCacheAddress();

    /**
     * Navigates to cache using {@link InfinispanPage} page
     * @param page used for navigation
     */
    void navigateToCache(InfinispanPage page);
}
