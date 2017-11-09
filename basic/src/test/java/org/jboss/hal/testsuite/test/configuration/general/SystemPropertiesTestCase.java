package org.jboss.hal.testsuite.test.configuration.general;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.SystemPropertiesPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SystemPropertiesTestCase {

    private static final String PROPERTY_KEY_TBR = "key-tbr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_KEY_TBA = "key-tba_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_VALUE = "val_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_INVALID_KEY = "čřč_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_KEY2 = "key2_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_INVALID_VALUE = "čěš_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address DMR_KEY_TBR = Address.of("system-property", PROPERTY_KEY_TBR);
    private static final Address DMR_KEY_TBA = Address.of("system-property", PROPERTY_KEY_TBA);
    private static final Address DMR_KEY2 = Address.of("system-property", PROPERTY_KEY2);
    private static final Address DMR_KEY_INVALID = Address.of("system-property", PROPERTY_INVALID_KEY);

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static Administration administration = new Administration(client);
    private static Operations operations = new Operations(client);

    @Drone
    public WebDriver browser;

    @Page
    public SystemPropertiesPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        operations.add(DMR_KEY_TBR);
        new ResourceVerifier(DMR_KEY_TBR, client).verifyExists();
    }

    @Before
    public void before() {
        page.navigate();
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(DMR_KEY_INVALID);
            operations.removeIfExists(DMR_KEY_TBR);
            operations.removeIfExists(DMR_KEY_TBA);
            operations.removeIfExists(DMR_KEY2);

            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void createProperty() throws Exception {
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();

        editor.text("key", PROPERTY_KEY_TBA);
        editor.text("value", PROPERTY_VALUE);
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        new ResourceVerifier(DMR_KEY_TBA, client)
                .verifyExists()
                .verifyAttribute("value", PROPERTY_VALUE);
    }

    @Test
    public void removeProperty() throws Exception {
        page.getResourceManager().removeResourceAndConfirm(PROPERTY_KEY_TBR);

        new ResourceVerifier(DMR_KEY_TBR, client).verifyDoesNotExist();
    }

    @Test
    public void createPropertyWithInvalidKey() throws Exception {
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("key", PROPERTY_INVALID_KEY);
        editor.text("value", PROPERTY_VALUE);

        boolean result = wizard.finish();

        assertFalse("Window should not be closed", result);
        new ResourceVerifier(DMR_KEY_INVALID, client).verifyDoesNotExist();
    }

    @Test
    public void createPropertyWithInvalidValue() throws Exception {
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("value", PROPERTY_INVALID_VALUE);
        editor.text("key", PROPERTY_KEY2);

        boolean result = wizard.finish();

        assertFalse("Window should not be closed", result);
        new ResourceVerifier(DMR_KEY2, client).verifyDoesNotExist();
    }

}
