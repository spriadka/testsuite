package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CacheContainerContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CustomCacheContainerContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.DefaultCacheContainer;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.DistributedCacheContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.InvalidationCacheContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.LocalCacheContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.ReplicatedCacheContext;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.util.Arrays;
import java.util.Collection;

/**
 * Class providing arguments to parameterized test cases.
 */
public class ParametersFactory {

    private OnlineManagementClient client;

    public ParametersFactory(OnlineManagementClient client) {
        this.client = client;
    }

    /**
     * Represents all possible registration points for cache containers, as a list,
     * to be passed into corresponding test cases constructors
     * The list contains following cache containers:
     * <ul>
     *     <li>hibernate</li>
     *     <li>web</li>
     *     <li>ejb</li>
     *     <li>server</li>
     *     <li>custom</li>
     * </ul>
     * The return type must be assignable to Iterable &ltObject[]&gt as it is desired way to inject parameters
     * into JUnit's parameterized test case runner.
     */
    public Collection cacheContainerTable() {
        CacheContainerContext hibernate = DefaultCacheContainer.HIBERNATE;
        CacheContainerContext web = DefaultCacheContainer.WEB;
        CacheContainerContext ejb = DefaultCacheContainer.EJB;
        CacheContainerContext server = DefaultCacheContainer.SERVER;
        CacheContainerContext custom = new CustomCacheContainerContext(client, "custom_" + RandomStringUtils.randomAlphanumeric(7));
        return Arrays.asList(new Object[][]{
                {hibernate},
                {web},
                {ejb},
                {server},
                {custom}
        });
    }

    /**
     * Represents parameterized matrix of cache containers and cache types to be used in corresponding test cases
     * constructors as a table
     * The parameterized matrix looks like following:
     * <table>
     *  <tr>
     *      <th>Cache container</th>
     *      <th>Cache type</th>
     *  </tr>
     *  <tr><td>Hibernate</td><td>Local cache</td></tr>
     *  <tr><td>Hibernate</td><td>Invalidation cache</td></tr>
     *  <tr><td>Hibernate</td><td>Distribution cache</td></tr>
     *  <tr><td>Hibernate</td><td>Replicated cache</td></tr>
     *  <tr><td>Web</td><td>Local cache</td></tr>
     *  <tr><td>Web</td><td>Invalidation cache</td></tr>
     *  <tr><td>Web</td><td>Distribution cache</td></tr>
     *  <tr><td>Web</td><td>Replicated cache</td></tr>
     *  <tr><td>Server</td><td>Local cache</td></tr>
     *  <tr><td>Server</td><td>Invalidation cache</td></tr>
     *  <tr><td>Server</td><td>Distribution cache</td></tr>
     *  <tr><td>Server</td><td>Replicated cache</td></tr>
     *  <tr><td>EJB</td><td>Local cache</td></tr>
     *  <tr><td>EJB</td><td>Invalidation cache</td></tr>
     *  <tr><td>EJB</td><td>Distribution cache</td></tr>
     *  <tr><td>EJB</td><td>Replicated cache</td></tr>
     *  <tr><td>Custom</td><td>Local cache</td></tr>
     *  <tr><td>Custom</td><td>Invalidation cache</td></tr>
     *  <tr><td>Custom</td><td>Distribution cache</td></tr>
     *  <tr><td>Custom</td><td>Replicated cache</td></tr>
     * </table>
     * The return must be assignable to Iterable &ltObject[]&gt as it is desired way to inject parameters
     * into JUnit's parameterized test case runner.
     */
    public Collection containerTypeMatrix() {
        CacheContainerContext hibernate = DefaultCacheContainer.HIBERNATE;
        CacheContainerContext web = DefaultCacheContainer.WEB;
        CacheContainerContext ejb = DefaultCacheContainer.EJB;
        CacheContainerContext server = DefaultCacheContainer.SERVER;
        CacheContainerContext custom = new CustomCacheContainerContext(client, "custom_" + RandomStringUtils.randomAlphanumeric(7));
        return Arrays.asList(new Object[][]{
                {hibernate, new LocalCacheContext(client, hibernate.getCacheContainerAddress())},
                {hibernate, new InvalidationCacheContext(client, hibernate.getCacheContainerAddress())},
                {hibernate, new DistributedCacheContext(client, hibernate.getCacheContainerAddress())},
                {hibernate, new ReplicatedCacheContext(client, hibernate.getCacheContainerAddress())},
                {web, new LocalCacheContext(client, web.getCacheContainerAddress())},
                {web, new InvalidationCacheContext(client, web.getCacheContainerAddress())},
                {web, new DistributedCacheContext(client, web.getCacheContainerAddress())},
                {web, new ReplicatedCacheContext(client, web.getCacheContainerAddress())},
                {server, new LocalCacheContext(client, server.getCacheContainerAddress())},
                {server, new InvalidationCacheContext(client, server.getCacheContainerAddress())},
                {server, new DistributedCacheContext(client, server.getCacheContainerAddress())},
                {server, new ReplicatedCacheContext(client, server.getCacheContainerAddress())},
                {ejb, new LocalCacheContext(client, ejb.getCacheContainerAddress())},
                {ejb, new InvalidationCacheContext(client, ejb.getCacheContainerAddress())},
                {ejb, new DistributedCacheContext(client, ejb.getCacheContainerAddress())},
                {ejb, new ReplicatedCacheContext(client, ejb.getCacheContainerAddress())},
                {custom, new LocalCacheContext(client, custom.getCacheContainerAddress())},
                {custom, new InvalidationCacheContext(client, custom.getCacheContainerAddress())},
                {custom, new DistributedCacheContext(client, custom.getCacheContainerAddress())},
                {custom, new ReplicatedCacheContext(client, custom.getCacheContainerAddress())}
        });
    }
}
