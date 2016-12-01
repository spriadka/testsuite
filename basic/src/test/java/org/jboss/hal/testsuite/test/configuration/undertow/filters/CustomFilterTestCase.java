package org.jboss.hal.testsuite.test.configuration.undertow.filters;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
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
public class CustomFilterTestCase {

    @Page
    private UndertowFiltersPage page;

    @Drone
    private WebDriver browser;

    private static final String
            PARAMETERS = "parameters",
            CLASS_NAME = "class-name",
            MODULE = "module",
            FILTER_NAME = "custom-filter_" + RandomStringUtils.randomAlphanumeric(5),
            FILTER_TBA_NAME = "custom-filter_TBA_" + RandomStringUtils.randomAlphanumeric(5),
            FILTER_TBR_NAME = "custom-filter_TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address
            UNDERTOW_SUBSYSTEM_ADDRESS = Address.subsystem("undertow"),
            FILTERS_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS.and("configuration", "filter"),
            FILTER_ADDRESS = FILTERS_ADDRESS.and("custom-filter", FILTER_NAME),
            FILTER_TBA_ADDRESS = FILTERS_ADDRESS.and("custom-filter", FILTER_TBA_NAME),
            FILTER_TBR_ADDRESS = FILTERS_ADDRESS.and("custom-filter", FILTER_TBR_NAME);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void beforeClass() throws IOException {
        operations.add(FILTER_TBR_ADDRESS, Values.of(CLASS_NAME, "quz")
                .and(MODULE, "qiz"))
                .assertSuccess();

        operations.add(FILTER_ADDRESS, Values.of(CLASS_NAME, "quz")
                .and(MODULE, "qiz"))
                .assertSuccess();
    }

    @Before
    public void before() {
        page.navigate();
        page.selectFilterType("Custom Filter");
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
        editor.text(CLASS_NAME, RandomStringUtils.randomAlphanumeric(7));
        editor.text(MODULE, RandomStringUtils.randomAlphanumeric(5));

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
    public void editClassName() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = RandomStringUtils.randomAlphanumeric(7);
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, CLASS_NAME, value)
                .verifyFormSaved()
                .verifyAttribute(CLASS_NAME, value);
    }

    @Test
    public void editModule() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String value = RandomStringUtils.randomAlphanumeric(7);
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MODULE, value)
                .verifyFormSaved()
                .verifyAttribute(MODULE, value);
    }

    @Test
    public void editParameters() throws Exception {
        page.getResourceManager().selectByName(FILTER_NAME);

        String key = "foo";
        String value = RandomStringUtils.randomAlphanumeric(7);
        new ConfigChecker.Builder(client, FILTER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, PARAMETERS, key + "=" + value)
                .verifyFormSaved()
                .verifyAttribute(PARAMETERS, new ModelNode().set(new Property(key, new ModelNode().set(value))).asObject());
    }

}
