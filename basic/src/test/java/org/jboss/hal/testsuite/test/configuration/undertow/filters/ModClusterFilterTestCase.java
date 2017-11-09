package org.jboss.hal.testsuite.test.configuration.undertow.filters;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.UndertowFiltersPage;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunAsClient
@RunWith(Arquillian.class)
public class ModClusterFilterTestCase {

    @Page
    private UndertowFiltersPage page;

    @Drone
    private WebDriver browser;

    private static final String
            SOCKET_BINDING_RESOURCE_1 = "socket-binding_1_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_RESOURCE_2 = "socket-binding_2_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_RESOURCE_3 = "socket-binding_3_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_RESOURCE_4 = "socket-binding_4_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_RESOURCE_5 = "socket-binding_5_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_RESOURCE_6 = "socket-binding_6_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_RESOURCE_7 = "socket-binding_7_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_RESOURCE_8 = "socket-binding_8_" + RandomStringUtils.randomAlphanumeric(7),

            ADVERTISE_FREQUENCY = "advertise-frequency",
            ADVERTISE_PATH = "advertise-path",
            ADVERTISE_PROTOCOL = "advertise-protocol",
            ADVERTISE_SOCKET_BINDING = "advertise-socket-binding",
            BROKEN_NODE_TIMEOUT = "broken-node-timeout",
            CACHED_CONNECTIONS_PER_THREAD = "cached-connections-per-thread",
            CONNECTION_IDLE_TIMEOUT = "connection-idle-timeout",
            CONNECTIONS_PER_THREAD = "connections-per-thread",
            ENABLE_HTTP2 = "enable-http2",
            HEALTH_CHECK_INTERVAL = "health-check-interval",
            HTTP2_ENABLE_PUSH = "http2-enable-push",
            HTTP2_HEADER_TABLE_SIZE = "http2-header-table-size",
            HTTP2_INITIAL_WINDOW_SIZE = "http2-header-table-size",
            HTTP2_MAX_CONCURRENT_STREAMS = "http2-max-concurrent-streams",
            HTTP2_MAX_FRAME_SIZE = "http2-max-frame-size",
            HTTP2_MAX_HEADER_LIST_SIZE = "http2-max-header-list-size",
            MANAGEMENT_ACCESS_PREDICATE = "management-access-predicate",
            MANAGEMENT_SOCKET_BINDING = "management-socket-binding",
            MAX_AJP_ACCESS_SIZE = "max-ajp-packet-size",
            MAX_REQUEST_TIME = "max-request-time",
            MAX_RETRIES = "max-retries",
            REQUEST_QUEUE_SIZE = "request-queue-size",
            SECURITY_KEY = "security-key",
            SECURITY_REALM = "security-realm",
            SSL_CONTEXT = "ssl-context",
            USE_ALIAS = "use-alias",
            WORKER = "worker",

            FILTER_NAME = "mod-cluster-filter_" + RandomStringUtils.randomAlphanumeric(5),
            FILTER_TBA_NAME = "mod-cluster-filter_TBA_" + RandomStringUtils.randomAlphanumeric(5),
            FILTER_TBR_NAME = "mod-cluster-filter_TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address
            UNDERTOW_SUBSYSTEM_ADDRESS = Address.subsystem("undertow"),
            FILTERS_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS.and("configuration", "filter"),
            FILTER_ADDRESS = FILTERS_ADDRESS.and("mod-cluster", FILTER_NAME),
            FILTER_TBA_ADDRESS = FILTERS_ADDRESS.and("mod-cluster", FILTER_TBA_NAME),
            FILTER_TBR_ADDRESS = FILTERS_ADDRESS.and("mod-cluster", FILTER_TBR_NAME);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);
    private static final UndertowOperations undertowOps = new UndertowOperations(client);

    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_1)
                .multicastAddress("224.0.0.1")
                .multicastPort(4567)
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_2).build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_3)
                .multicastAddress("224.0.0.1")
                .multicastPort(4567)
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_4).build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_5)
                .multicastAddress("224.0.0.1")
                .multicastPort(4567)
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_6)
                .multicastAddress("224.0.0.1")
                .multicastPort(4567)
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_7)
                .multicastAddress("224.0.0.1")
                .multicastPort(4567)
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_8).build());

        operations.add(FILTER_ADDRESS,
                Values.of(ADVERTISE_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_1)
                        .and(MANAGEMENT_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_2))
                .assertSuccess();

        operations.add(FILTER_TBR_ADDRESS,
                Values.of(ADVERTISE_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_3)
                        .and(MANAGEMENT_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_4))
                .assertSuccess();

        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.selectFilterType("ModCluster");
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException, CommandFailedException {
        try {
            operations.removeIfExists(FILTER_ADDRESS);
            operations.removeIfExists(FILTER_TBA_ADDRESS);
            operations.removeIfExists(FILTER_TBR_ADDRESS);

            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_1));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_2));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_3));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_4));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_5));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_6));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_7));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_8));

            undertowOps.cleanupReferences();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void testAddingFilter() throws Exception {
        WizardWindow wizardWindow = page.getResourceManager().addResource();

        Editor editor = wizardWindow.getEditor();

        editor.text("name", FILTER_TBA_NAME);
        editor.text(MANAGEMENT_SOCKET_BINDING, undertowOps.createSocketBinding());

        wizardWindow.finish();

        new ResourceVerifier(FILTER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void testRemovingFilter() throws Exception {
        new ResourceVerifier(FILTER_TBR_ADDRESS, client).verifyExists();

        page.getResourceManager().removeResource(FILTER_TBR_NAME).confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(FILTER_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editAdvertiseFrequency() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, ADVERTISE_FREQUENCY, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(ADVERTISE_FREQUENCY, value);
    }

    @Test
    public void editAdvertiseFrequencyInvalid() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = "foobar";
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, ADVERTISE_FREQUENCY, value)
                .verifyFormNotSaved();
    }

    @Test
    public void editAdvertiseFrequencyNegative() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = -10;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, ADVERTISE_FREQUENCY, String.valueOf(value))
                .verifyFormNotSaved();
    }

    @Test
    public void editAdvertisePath() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = "foobar" + RandomStringUtils.randomAlphanumeric(7);
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, ADVERTISE_PATH, value)
                .verifyFormSaved()
                .verifyAttribute(ADVERTISE_PATH, value);
    }

    @Test
    public void editAdvertiseProtocol() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = "foobar" + RandomStringUtils.randomAlphanumeric(7);
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, ADVERTISE_PROTOCOL, value)
                .verifyFormSaved()
                .verifyAttribute(ADVERTISE_PROTOCOL, value);
    }

    @Test
    public void editAdvertiseSocketBinding() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = SOCKET_BINDING_RESOURCE_7;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, ADVERTISE_SOCKET_BINDING, value)
                .verifyFormSaved()
                .verifyAttribute(ADVERTISE_SOCKET_BINDING, value);
    }

    @Test
    public void editBrokenNodeTimeout() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, BROKEN_NODE_TIMEOUT, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(BROKEN_NODE_TIMEOUT, value);
    }

    @Test
    public void editBrokenNodeTimeoutInvalid() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = "foo";
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, BROKEN_NODE_TIMEOUT, value)
                .verifyFormNotSaved();
    }

    @Test
    public void editCachedConnectionsPerThread() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, CACHED_CONNECTIONS_PER_THREAD, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(CACHED_CONNECTIONS_PER_THREAD, value);
    }

    @Test
    public void editCachedConnectionIdleTimeout() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, CONNECTION_IDLE_TIMEOUT, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(CONNECTION_IDLE_TIMEOUT, value);
    }

    @Test
    public void editConnectionsPerThread() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, CONNECTIONS_PER_THREAD, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(CONNECTIONS_PER_THREAD, value);
    }

    @Test
    public void toggleEnableHTTP2() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        toggleCheckbox(ENABLE_HTTP2);
    }

    @Test
    public void editHealthCheckInterval() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, HEALTH_CHECK_INTERVAL, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(HEALTH_CHECK_INTERVAL, value);
    }

    @Test
    public void toggleHTTP2EnablePush() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        toggleCheckbox(HTTP2_ENABLE_PUSH);
    }

    @Test
    public void editHTTP2HeaderTableSize() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, HTTP2_HEADER_TABLE_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(HTTP2_HEADER_TABLE_SIZE, value);
    }

    @Test
    public void editHTTP2InitialWindowSize() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, HTTP2_INITIAL_WINDOW_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(HTTP2_INITIAL_WINDOW_SIZE, value);
    }

    @Test
    public void editHTTP2MaxConcurrentStreams() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, HTTP2_MAX_CONCURRENT_STREAMS, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(HTTP2_MAX_CONCURRENT_STREAMS, value);
    }

    @Test
    public void editHTTP2MaxFrameSize() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, HTTP2_MAX_FRAME_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(HTTP2_MAX_FRAME_SIZE, value);
    }

    @Test
    public void editHTTP2MaxHeaderListSize() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, HTTP2_MAX_HEADER_LIST_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(HTTP2_MAX_HEADER_LIST_SIZE, value);
    }

    @Test
    public void editManagementAccessPredicate() throws Exception {
        /*known predicates are [auth-required, method, secure, path-suffix, directory, max-content-size, idempotent,
         path, path-prefix, contains, regex, min-content-size, file, equals, exists, path-template, dispatcher]*/
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = "auth-required";
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MANAGEMENT_ACCESS_PREDICATE, value)
                .verifyFormSaved();

        administration.reloadIfRequired();

       new ResourceVerifier(FILTER_ADDRESS, client).verifyAttribute(MANAGEMENT_ACCESS_PREDICATE, value);
    }

    @Test
    public void editManagementSocketBinding() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = SOCKET_BINDING_RESOURCE_8;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MANAGEMENT_SOCKET_BINDING, value)
                .verifyFormSaved()
                .verifyAttribute(MANAGEMENT_SOCKET_BINDING, value);
    }

    @Test
    public void editMaxAJPPacketSize() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MAX_AJP_ACCESS_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(MAX_AJP_ACCESS_SIZE, value);
    }

    @Test
    public void editMaxRequestTime() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MAX_REQUEST_TIME, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(MAX_REQUEST_TIME, value);
    }

    @Test
    public void editMaxRetries() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MAX_RETRIES, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(MAX_RETRIES, value);
    }

    @Test
    public void editRequestQueueSize() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, REQUEST_QUEUE_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(REQUEST_QUEUE_SIZE, value);
    }

    @Test
    public void editRequestQueueSizeInvalid() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String invalidValue = "4sadf65";
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, REQUEST_QUEUE_SIZE, invalidValue)
                .verifyFormNotSaved();
    }

    @Test
    public void editSecurityKey() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = "top-secret-key" + RandomStringUtils.randomAlphanumeric(7);
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_KEY, value)
                .verifyFormSaved()
                .verifyAttribute(SECURITY_KEY, value);
    }

    @Test
    public void editSecurityRealm() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String securityRealm = undertowOps.createSecurityRealm();

        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_REALM, securityRealm)
                .verifyFormSaved()
                .verifyAttribute(SECURITY_REALM, securityRealm);
    }

    @Test
    public void editSSLContext() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        Address keyStoreAddress = null,
            keyManagerAddress = null,
            sslContextAddress = null;


        final ModelNodeResult originalModelNodeResult = operations.readAttribute(FILTER_ADDRESS, SSL_CONTEXT);

        originalModelNodeResult.assertSuccess();

        try {
            final UndertowElytronOperations undertowElyOps = new UndertowElytronOperations(client);

            keyStoreAddress = undertowElyOps.createKeyStore();
            keyManagerAddress = undertowElyOps.createKeyManager(keyStoreAddress);
            sslContextAddress = undertowElyOps.createClientSSLContext(keyManagerAddress);

            final String value = sslContextAddress.getLastPairValue();

            new ConfigChecker.Builder(client, FILTER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SSL_CONTEXT, value)
                    .verifyFormSaved()
                    .verifyAttribute(SSL_CONTEXT, value);
        } finally {
            operations.writeAttribute(FILTER_ADDRESS, SSL_CONTEXT, originalModelNodeResult.value()).assertSuccess();
            if (sslContextAddress != null) {
                operations.removeIfExists(sslContextAddress);
            }
            if (keyManagerAddress != null) {
                operations.removeIfExists(keyManagerAddress);
            }
            if (keyStoreAddress != null) {
                operations.removeIfExists(keyStoreAddress);
            }
        }
    }

    @Test
    public void toggleUseAlias() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        toggleCheckbox(USE_ALIAS);
    }

    @Test
    public void editWorker() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);
        String value = undertowOps.createWorker();
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, WORKER, value)
                .verifyFormSaved()
                .verifyAttribute(WORKER, value);
    }

    public void toggleCheckbox(String identifier) throws Exception {
        boolean originalValue = operations.readAttribute(FILTER_ADDRESS, identifier).booleanValue();

        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, identifier, !originalValue)
                .verifyFormSaved()
                .verifyAttribute(identifier, !originalValue);

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        administration.reloadIfRequired();

        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, identifier, originalValue)
                .verifyFormSaved();

        administration.reloadIfRequired();

        new ResourceVerifier(FILTER_ADDRESS, client).verifyAttribute(identifier, originalValue,
                "Setting back to original value failed probably because of https://issues.jboss.org/browse/HAL-1235");
    }
}