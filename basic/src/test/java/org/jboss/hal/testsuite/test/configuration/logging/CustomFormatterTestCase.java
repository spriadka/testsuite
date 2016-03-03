package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 1.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class CustomFormatterTestCase extends LoggingAbstractTestCase {

    private static final String CUSTOM_FORMATTER = "customFormatter" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CUSTOM_FORMATTER_TBR = "customFormatterTBR" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CUSTOM_FORMATTER_TBA = "customFormatterTBR" + RandomStringUtils.randomAlphanumeric(5);

    private static final String CLASS = "java.util.logging.XMLFormatter";
    private static final String MODULE = "org.jboss.logmanager";
    private static final Address CUSTOM_FORMATTER_ADDRESS = LOGGING_SUBSYSTEM
            .and("custom-formatter", CUSTOM_FORMATTER);
    private static final Address CUSTOM_FORMATTER_TBR_ADDRESS = LOGGING_SUBSYSTEM
            .and("custom-formatter", CUSTOM_FORMATTER_TBR);
    private static final Address CUSTOM_FORMATTER_TBA_ADDRESS = LOGGING_SUBSYSTEM
            .and("custom-formatter", CUSTOM_FORMATTER_TBA);

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(CUSTOM_FORMATTER_ADDRESS, Values.of("class", CLASS).and("module", MODULE));
        new ResourceVerifier(CUSTOM_FORMATTER_ADDRESS, client).verifyExists();
        operations.add(CUSTOM_FORMATTER_TBR_ADDRESS, Values.of("class", CLASS).and("module", MODULE));
        new ResourceVerifier(CUSTOM_FORMATTER_TBR_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(CUSTOM_FORMATTER_ADDRESS);
        operations.removeIfExists(CUSTOM_FORMATTER_TBR_ADDRESS);
        operations.removeIfExists(CUSTOM_FORMATTER_TBA_ADDRESS);
        administration.reloadIfRequired();
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToFormatterTab();
        page.switchToCustomPattern();
        page.selectFormatter(CUSTOM_FORMATTER);
    }

    @Test
    public void addCustomFormatter() throws Exception {
        page.addCustomFormatter(CUSTOM_FORMATTER_TBA, CLASS, MODULE);

        new ResourceVerifier(CUSTOM_FORMATTER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateCustomFormatterClass() throws Exception {
        editTextAndVerify(CUSTOM_FORMATTER_ADDRESS, "class", "org.jboss.logmanager.formatters.PatternFormatter");
    }

    @Test
    public void updateCustomFormatterModule() throws Exception {
        editTextAndVerify(CUSTOM_FORMATTER_ADDRESS, "module", "org.jboss.logmanager");
    }

    @Test
    public void updateCustomFormatterProperties() throws Exception {
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave("class", "org.jboss.logmanager.formatters.PatternFormatter"); //need to set formatter class which has needed setter method
        config.editTextAndSave("properties", "pattern=%s%E%n");
        ModelNode expected = new ModelNode().add(new Property("pattern", new ModelNode("%s%E%n")));
        new ResourceVerifier(CUSTOM_FORMATTER_ADDRESS, client).verifyAttribute("properties", expected);
    }

    @Test
    public void removeCustomFormatter() throws Exception {
        page.removeInTable(CUSTOM_FORMATTER_TBR);

        new ResourceVerifier(CUSTOM_FORMATTER_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
