package org.jboss.hal.testsuite.test.naming;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.NamingPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class NamingTestCase {
    private static final Logger logger = LoggerFactory.getLogger(NamingTestCase.class);

    @Page
    private NamingPage page;

    @Drone
    private WebDriver browser;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String
            DEFAULT_BINDING_NAME = "java:/NamingBindingDef_" + RandomStringUtils.randomAlphanumeric(3),
            BINDING_TBR_NAME = "java:/NamingBindingTbr_" + RandomStringUtils.randomAlphanumeric(3),
            BINDING_TBA_NAME = "java:/NamingBindingTba_" + RandomStringUtils.randomAlphanumeric(3),
            BINDING = "binding",
            SERVICE = "service",
            BINDING_TYPE = "binding-type",
            CACHE = "cache",
            NAME = "name",
            VALUE = "value";

    private static final Address
            SUBSYSTEM_ADDRESS = Address.subsystem("naming"),
            DEFAULT_BINDING_ADDRESS = SUBSYSTEM_ADDRESS.and(BINDING, DEFAULT_BINDING_NAME),
            BINDING_TBR_ADDRESS = SUBSYSTEM_ADDRESS.and(BINDING, BINDING_TBR_NAME),
            BINDING_TBA_ADDRESS = SUBSYSTEM_ADDRESS.and(BINDING, BINDING_TBA_NAME);



    @BeforeClass
    public static void beforeClass() throws IOException {
        operations.add(DEFAULT_BINDING_ADDRESS, Values.of(BINDING_TYPE, "simple").and(VALUE, "foo")).assertSuccess();
        operations.add(BINDING_TBR_ADDRESS, Values.of(BINDING_TYPE, "simple").and(VALUE, "foo")).assertSuccess();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(DEFAULT_BINDING_ADDRESS);
            operations.removeIfExists(BINDING_TBA_ADDRESS);
            operations.removeIfExists(BINDING_TBR_ADDRESS);
        } finally {
            administration.reloadIfRequired();
            client.close();
        }
    }

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void verifyIfNewlyAddedBindingInCLIIsAddedAfterRefresh() throws IOException, OperationException {
        page.getTreeNavigation()
                .step(BINDING)
                .navigateToTreeItem().clickLabel();

        String name = "java:/NamingBinding_" + RandomStringUtils.randomAlphanumeric(3);

        try {
            operations.add(SUBSYSTEM_ADDRESS.and(BINDING, name),
                    Values.of(BINDING_TYPE, "simple").and(VALUE, "foo"))
                    .assertSuccess();

            page.refreshTreeNavigation();

            assertTrue(page.getTreeNavigation()
                    .step(BINDING)
                    .navigateToTreeItem()
                    .hasChild(name));
        } finally {
            operations.removeIfExists(SUBSYSTEM_ADDRESS.and(BINDING, name));
        }
    }

    @Test
    public void addBindingInUI() throws Exception {
        page.getTreeNavigation()
                .step(BINDING)
                .navigateToTreeItem().clickLabel();

        WizardWindowWithOptionalFields window = page.getResourceManager()
                .addResource(WizardWindowWithOptionalFields.class);
        window.openAdvancedOptionsTab();

        Editor editor = window.getEditor();
        editor.text(NAME, BINDING_TBA_NAME);
        editor.select(BINDING_TYPE, "simple");
        editor.text("value", "foo");

        window.finishAndDismissReloadRequiredWindow();
        administration.reloadIfRequired();

        new ResourceVerifier(BINDING_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeBindingInUI() throws Exception {
        page.getTreeNavigation()
                .step(BINDING)
                .navigateToTreeItem().clickLabel();

        page.getResourceManager()
                .removeResource(BINDING_TBR_NAME)
                .confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(BINDING_TBR_ADDRESS, client)
                .verifyDoesNotExist();
    }

    @Test
    public void verifyRemoteNamingServiceExists() {
        assertTrue(page.getTreeNavigation()
                .step(SERVICE)
                .navigateToTreeItem()
                .clickLabel()
                .hasChild("remote-naming"));
    }

    @Ignore("Not testable because of https://issues.jboss.org/browse/HAL-1199 . Ignore until resolved.")
    @Test
    public void toggleCache() throws Exception {
        page.getTreeNavigation()
                .step(BINDING)
                .step(DEFAULT_BINDING_NAME)
                .navigateToTreeItem().clickLabel();

        ModelNodeResult result = operations.readAttribute(DEFAULT_BINDING_ADDRESS, CACHE, ReadAttributeOption.INCLUDE_DEFAULTS);
        result.assertSuccess();

        final ModelNode defaultModelValue = result.value();
        //Even with INCLUDE_DEFAULTS 'cache' attribute remains undefined
        final boolean defaultValue = false;

        try {
            new ConfigChecker.Builder(client, DEFAULT_BINDING_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, CACHE, !defaultValue)
                    .verifyFormSaved()
                    .verifyAttribute(CACHE, !defaultValue);

            new ConfigChecker.Builder(client, DEFAULT_BINDING_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, CACHE, defaultValue)
                    .verifyFormSaved()
                    .verifyAttribute(CACHE, defaultValue);
        } finally {
            operations.writeAttribute(DEFAULT_BINDING_ADDRESS, CACHE, defaultModelValue);
        }

    }
}
