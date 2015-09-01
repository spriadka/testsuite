package org.jboss.hal.testsuite.test.configuration.general;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.SystemPropertiesPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.SYSTEM_PROPERTY_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SystemPropertiesTestCase {

    private static final String PROPERTY_KEY = "key_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_VALUE = "val_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_INVALID_KEY = "čřč_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_KEY2 = "key2_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROPERTY_INVALID_VALUE = "čěš_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String DMR_KEY = SYSTEM_PROPERTY_ADDRESS + "=" + PROPERTY_KEY;
    private static final String DMR_KEY2 = SYSTEM_PROPERTY_ADDRESS + "=" + PROPERTY_KEY2;
    private static final String DMR_KEY_INVALID = SYSTEM_PROPERTY_ADDRESS + "=" + PROPERTY_INVALID_KEY;

    private static CliClient client = CliClientFactory.getClient();
    private static ResourceVerifier verifier = new ResourceVerifier(DMR_KEY, client);

    @Drone
    public WebDriver browser;

    @Page
    public SystemPropertiesPage page;

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(SystemPropertiesPage.class);
    }

    @AfterClass
    public static void cleanUp() {
        client.removeSystemProperty(PROPERTY_INVALID_KEY);
        client.removeSystemProperty(PROPERTY_KEY);
        client.removeSystemProperty(PROPERTY_KEY2);
    }

    @Test
    @InSequence(0)
    public void createProperty() {
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();

        editor.text("key", PROPERTY_KEY);
        editor.text("value", PROPERTY_VALUE);
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        verifier.verifyResource(true);
        assertEquals(PROPERTY_VALUE, client.getSystemProperty(PROPERTY_KEY));
    }

    @Test
    @InSequence(1)
    public void removeProperty() {
        page.getResourceManager().removeResourceAndConfirm(PROPERTY_KEY);

        verifier.verifyResource(false);
    }

    @Test
    public void createPropertyWithInvalidKey() {
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("key", PROPERTY_INVALID_KEY);
        editor.text("value", PROPERTY_VALUE);

        boolean result = wizard.finish();

        assertFalse("Window should not be closed", result);
        verifier.verifyResource(DMR_KEY_INVALID, false);
    }

    @Test
    public void createPropertyWithInvalidValue() {
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("value", PROPERTY_INVALID_VALUE);
        editor.text("key", PROPERTY_KEY2);

        boolean result = wizard.finish();

        assertFalse("Window should not be closed", result);
        verifier.verifyResource(DMR_KEY2, false);
    }

}
