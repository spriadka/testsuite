package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddRemoteDestinationOutboundSocketBinding;
import org.jboss.hal.testsuite.creaper.command.RemoveRemoteDestinationOutboundSocketBinding;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CacheContainerContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.CacheContext;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.junit.ArquillianParametrized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(ArquillianParametrized.class)
@RunAsClient
public class RemoteStoreTestCase extends InfinispanTestCaseAbstract {

    private static final String STORE = "store";
    private static final String REMOTE_STORE = "remote";

    private static final String REMOTE_STORE_LABEL = "Remote Store";
    private static final String REMOTE_SERVERS = "remote-servers";
    private static final String ALLOW_RESOURCE_SERVICE_RESTART = "allow-resource-service-restart";
    private static final String MAIL_SMTP = "mail-smtp";

    private static final String CACHE = "cache";
    private static final String FETCH_STATE = "fetch-state";
    private static final String PASSIVATION = "passivation";
    private static final String PRELOAD = "preload";
    private static final String PROPERTIES = "properties";
    private static final String PROPERTY = "property";
    private static final String VALUE = "value";
    private static final String PURGE = "purge";
    private static final String SHARED = "shared";
    private static final String SOCKET_TIMEOUT = "socket-timeout";
    private static final String TCP_NO_DELAY = "tcp-no-delay";

    public CacheContainerContext cacheContainerContext;
    public CacheContext cacheContext;

    public RemoteStoreTestCase(CacheContainerContext cacheContainerContext, CacheContext cacheContext) {
        this.cacheContainerContext = cacheContainerContext;
        this.cacheContext = cacheContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}, Cache type: {1}")
    public static Collection parametersMatrix() {
        return new ParametersFactory(client).containerTypeMatrix();
    }

    @Test
    public void editCacheTest() throws Exception {
        final String cache = "cache_" + RandomStringUtils.randomAlphanumeric(7);
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CACHE, cache)
                    .verifyFormSaved()
                    .verifyAttribute(CACHE, cache);
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void toggleFetchStateTest() throws Exception {
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            ModelNodeResult fetchState = operations.readAttribute(storeAddress, FETCH_STATE);
            fetchState.assertSuccess();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, FETCH_STATE, !fetchState.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(FETCH_STATE, !fetchState.booleanValue());
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void togglePassivationTest() throws Exception {
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            ModelNodeResult passivation = operations.readAttribute(storeAddress, PASSIVATION);
            passivation.assertSuccess();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, PASSIVATION, !passivation.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(PASSIVATION, !passivation.booleanValue());
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void togglePreloadTest() throws Exception {
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            ModelNodeResult preload = operations.readAttribute(storeAddress, PRELOAD);
            preload.assertSuccess();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, PRELOAD, !preload.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(PRELOAD, !preload.booleanValue());
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editPropertiesTest() throws Exception {
        final Property[] properties = {new Property("Hello", "World"), new Property("Good", "Bye") };
        final String propertiesValue = Arrays.stream(properties)
                .map(property -> String.format("%s=%s", property.key, property.value))
                .collect(Collectors.joining("\n"));
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            page.getConfigFragment().editTextAndSave(PROPERTIES, propertiesValue);
            for (Property property : properties) {
                new ResourceVerifier(storeAddress.and(PROPERTY, property.key), client)
                        .verifyAttribute(VALUE, property.value);
            }

        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    private class Property {
        String key;
        String value;

        Property(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    @Test
    public void togglePurge() throws Exception {
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            ModelNodeResult purge = operations.readAttribute(storeAddress, PURGE);
            purge.assertSuccess();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, PURGE, !purge.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(PURGE, !purge.booleanValue());
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void togglePurgeTest() throws Exception {
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            ModelNodeResult purge = operations.readAttribute(storeAddress, PURGE);
            purge.assertSuccess();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, PURGE, !purge.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(PURGE, !purge.booleanValue());
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editRemoteServersTest() throws Exception {
        final String remoteServerName = "remote_server_" + RandomStringUtils.randomAlphanumeric(7);
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            client.apply(new AddRemoteDestinationOutboundSocketBinding.Builder(remoteServerName)
                    .host(RandomStringUtils.randomAlphanumeric(7))
                    .port(AvailablePortFinder.getNextAvailableNonPrivilegedPort())
                    .build());
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, REMOTE_SERVERS, remoteServerName)
                    .verifyFormSaved()
                    .verifyAttribute(REMOTE_SERVERS, new ModelNodeGenerator.ModelNodeListBuilder().addAll(remoteServerName).build());
        } finally {
            client.apply(new RemoveRemoteDestinationOutboundSocketBinding(remoteServerName));
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void toggleSharedTest() throws Exception {
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            ModelNodeResult shared = operations.readAttribute(storeAddress, SHARED);
            shared.assertSuccess();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, SHARED, !shared.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(SHARED, !shared.booleanValue());
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSocketTimeoutTest() throws Exception {
        final long socketTimeout = 456789;
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOCKET_TIMEOUT, socketTimeout)
                    .verifyFormSaved()
                    .verifyAttribute(SOCKET_TIMEOUT, socketTimeout);
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void toggleTCPNoDelayTest() throws Exception {
        final Address storeAddress = cacheContext.getCacheAddress().and(STORE, REMOTE_STORE);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            addRemoteStoreInModel(storeAddress);
            administration.reloadIfRequired();
            ModelNodeResult tcpNoDelay = operations.readAttribute(storeAddress, TCP_NO_DELAY);
            tcpNoDelay.assertSuccess();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(REMOTE_STORE_LABEL);
            new ConfigChecker.Builder(client, storeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, TCP_NO_DELAY, !tcpNoDelay.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(TCP_NO_DELAY, !tcpNoDelay.booleanValue());
        } finally {
            operations.removeIfExists(storeAddress);
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

    private void addRemoteStoreInModel(Address storeAddress) throws IOException {
        operations.headers(Values.of(ALLOW_RESOURCE_SERVICE_RESTART, true))
                .add(storeAddress, Values.of(REMOTE_SERVERS, new ModelNodeGenerator.ModelNodeListBuilder()
                        .addAll(MAIL_SMTP).build()))
                .assertSuccess();
    }
}
