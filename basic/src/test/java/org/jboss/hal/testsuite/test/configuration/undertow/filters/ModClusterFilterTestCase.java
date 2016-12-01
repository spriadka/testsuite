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
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
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

            ADVERTISE_SOCKET_BINDING = "advertise-socket-binding",
            MANAGEMENT_SOCKET_BINDING = "management-socket-binding",
            HTTP2_MAX_FRAME_SIZE = "http2-max-frame-size",
            ENABLE_HTTP2 = "enable-http2",
            MAX_RETRIES = "max-retries",
            REQUEST_QUEUE_SIZE = "request-queue-size",

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

    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException {
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_1)
                .multicastAddress("224.0.0.1")
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_2)
                .multicastAddress("224.0.0.1")
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_3)
                .multicastAddress("224.0.0.1")
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_4)
                .multicastAddress("224.0.0.1")
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_5)
                .multicastAddress("224.0.0.1")
                .build());
        client.apply(new AddSocketBinding.Builder(SOCKET_BINDING_RESOURCE_6)
                .multicastAddress("224.0.0.1")
                .build());

        operations.add(FILTER_ADDRESS,
                Values.of(ADVERTISE_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_1)
                        .and(MANAGEMENT_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_2))
                .assertSuccess();

        operations.add(FILTER_TBR_ADDRESS,
                Values.of(ADVERTISE_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_3)
                        .and(MANAGEMENT_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_4))
                .assertSuccess();
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
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_1));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_2));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_3));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_4));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_5));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_RESOURCE_6));

            operations.removeIfExists(FILTER_ADDRESS);
            operations.removeIfExists(FILTER_TBA_ADDRESS);
            operations.removeIfExists(FILTER_TBR_ADDRESS);
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
        editor.text(ADVERTISE_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_5);
        editor.text(MANAGEMENT_SOCKET_BINDING, SOCKET_BINDING_RESOURCE_6);

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
    public void toggleEnableHTTP2() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        boolean originalValue = operations.readAttribute(FILTER_ADDRESS, ENABLE_HTTP2).booleanValue();

        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, ENABLE_HTTP2, !originalValue)
                .verifyFormSaved()
                .verifyAttribute(ENABLE_HTTP2, !originalValue);

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        administration.reload();

        //workaround for https://issues.jboss.org/browse/HAL-1235
        page.navigate();
        page.selectFilterType("ModCluster");
        page.getResourceManager().selectByName(FILTER_NAME);

        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, ENABLE_HTTP2, originalValue)
                .verifyFormSaved();

        administration.reload();

        new ResourceVerifier(FILTER_ADDRESS, client).verifyAttribute(ENABLE_HTTP2, originalValue);
    }

}