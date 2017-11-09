package org.jboss.hal.testsuite.test.configuration.undertow.filters;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
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
public class RequestLimitFilterTestCase {

    @Page
    private UndertowFiltersPage page;

    @Drone
    private WebDriver browser;

    private static final String
            MAX_CONCURRENT_REQUESTS = "max-concurrent-requests",
            QUEUE_SIZE = "queue-size",
            FILTER_NAME = "request-limit_" + RandomStringUtils.randomAlphanumeric(5),
            FILTER_TBA_NAME = "request-limit_TBA_" + RandomStringUtils.randomAlphanumeric(5),
            FILTER_TBR_NAME = "request-limit_TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address
            UNDERTOW_SUBSYSTEM_ADDRESS = Address.subsystem("undertow"),
            FILTERS_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS.and("configuration", "filter"),
            FILTER_ADDRESS = FILTERS_ADDRESS.and("request-limit", FILTER_NAME),
            FILTER_TBA_ADDRESS = FILTERS_ADDRESS.and("request-limit", FILTER_TBA_NAME),
            FILTER_TBR_ADDRESS = FILTERS_ADDRESS.and("request-limit", FILTER_TBR_NAME);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void beforeClass() throws IOException {
        operations.add(FILTER_TBR_ADDRESS, Values.of(MAX_CONCURRENT_REQUESTS, 42)).assertSuccess();

        operations.add(FILTER_ADDRESS, Values.of(MAX_CONCURRENT_REQUESTS, 42)).assertSuccess();
    }

    @Before
    public void before() {
        page.navigate();
        page.selectFilterType("Request Limit");
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
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
        editor.text(MAX_CONCURRENT_REQUESTS, String.valueOf(42));

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
    public void editMaxConcurrentRequests() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 85;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MAX_CONCURRENT_REQUESTS, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(MAX_CONCURRENT_REQUESTS, value);
    }

    @Test
    public void editQueueSize() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        int value = 42;
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, QUEUE_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(QUEUE_SIZE, value);
    }

    @Test
    public void editQueueSizeInvalid() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String invalidValue = "458df";
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, QUEUE_SIZE, invalidValue)
                .verifyFormNotSaved();
    }

}