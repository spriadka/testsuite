package org.jboss.hal.testsuite.test.configuration.infinispan.cache.container;

import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;

/**
 * An abstraction used in conjunction with {@link InfinispanPage} responsible for following requirements
 * <ol>
 *     <li>Navigation to specific cache container using {@link InfinispanPage}</li>
 *     <li>(If necessary) Create resources in model</li>
 *     <li>(If necessary) Remove created resources in model</li>
 * </ol>
 */
public interface CacheContainerContext {

    /**
     * Gets an {@link Address} of cache container
     * @return cache container address
     */
    Address getCacheContainerAddress();

    /**
     * Navigates to cache container section
     * @param page used for navigation
     */
    void navigateToCacheContainer(InfinispanPage page);

    /**
     * Creates cache container in model
     */
    void createCacheContainerInModel() throws IOException;

    /**
     * Removes cache container in model
     */
    void removeCacheContainerInModel() throws IOException, OperationException;
}
