package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class CustomFormatterTestCase extends LoggingAbstractTestCase {

    private static final String
            CUSTOM_FORMATTER = "custom-formatter",
            PROPERTIES = "properties",
            CLASS = "class",
            PATTERN = "pattern",
            MODULE = "module";

    private static final String
            PATTERN_VALUE = "%s%E%n",
            CLASS_VALUE = "java.util.logging.XMLFormatter",
            MODULE_VALUE = "org.jboss.logmanager";

    private static final Address CUSTOM_FORMATTER_ADDRESS = LOGGING_SUBSYSTEM
            .and(CUSTOM_FORMATTER, RandomStringUtils.randomAlphanumeric(5));
    private static final Address CUSTOM_FORMATTER_TBR_ADDRESS = LOGGING_SUBSYSTEM
            .and(CUSTOM_FORMATTER, RandomStringUtils.randomAlphanumeric(5));
    private static final Address CUSTOM_FORMATTER_TBA_ADDRESS = LOGGING_SUBSYSTEM
            .and(CUSTOM_FORMATTER, RandomStringUtils.randomAlphanumeric(5));

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(CUSTOM_FORMATTER_ADDRESS, Values.of(CLASS, CLASS_VALUE).and(MODULE, MODULE_VALUE)).assertSuccess();
        operations.add(CUSTOM_FORMATTER_TBR_ADDRESS, Values.of(CLASS, CLASS_VALUE).and(MODULE, MODULE_VALUE)).assertSuccess();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(CUSTOM_FORMATTER_ADDRESS);
        operations.removeIfExists(CUSTOM_FORMATTER_TBR_ADDRESS);
        operations.removeIfExists(CUSTOM_FORMATTER_TBA_ADDRESS);
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    private BackupAndRestoreAttributes backupAndRestoreAttributes;

    @Before
    public void before() throws CommandFailedException {
        page.navigate();
        page.switchToFormatterTab();
        page.switchToCustomPattern();
        page.selectFormatter(CUSTOM_FORMATTER_ADDRESS.getLastPairValue());
        backupAndRestoreAttributes = new BackupAndRestoreAttributes.Builder(CUSTOM_FORMATTER_ADDRESS).build();
        client.apply(backupAndRestoreAttributes.backup());
    }



    @After
    public void restoreFormatterAndGetRidOfReloadRequired() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        if (backupAndRestoreAttributes != null) {
            client.apply(backupAndRestoreAttributes.restore());
        }
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomFormatter() throws Exception {
        page.addCustomFormatter(CUSTOM_FORMATTER_TBA_ADDRESS.getLastPairValue(), CLASS_VALUE, MODULE_VALUE);

        new ResourceVerifier(CUSTOM_FORMATTER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateCustomFormatterClass() throws Exception {
        editTextAndVerify(CUSTOM_FORMATTER_ADDRESS, CLASS, "org.jboss.logmanager.formatters.PatternFormatter");
    }

    @Test
    public void updateCustomFormatterModule() throws Exception {
        editTextAndVerify(CUSTOM_FORMATTER_ADDRESS, MODULE, "org.jboss.logmanager");
    }

    @Test
    public void updateCustomFormatterProperties() throws Exception {
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(CLASS, "org.jboss.logmanager.formatters.PatternFormatter"); //need to set formatter class which has needed setter method
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        administration.reloadIfRequired();
        config.editTextAndSave(PROPERTIES, PATTERN + "=" + PATTERN_VALUE);
        ModelNode expected = new ModelNode().add(new Property(PATTERN, new ModelNode(PATTERN_VALUE)));
        new ResourceVerifier(CUSTOM_FORMATTER_ADDRESS, client)
                .verifyAttribute(PROPERTIES, expected, "Failed probably due https://issues.jboss.org/browse/HAL-1174");
    }

    @Test
    public void removeCustomFormatter() throws Exception {
        page.removeInTable(CUSTOM_FORMATTER_TBR_ADDRESS.getLastPairValue());

        new ResourceVerifier(CUSTOM_FORMATTER_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
