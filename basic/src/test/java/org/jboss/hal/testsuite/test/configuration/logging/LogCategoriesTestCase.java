package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.logging.AddLogger;
import org.wildfly.extras.creaper.commands.logging.Logging;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 26.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class LogCategoriesTestCase extends LoggingAbstractTestCase {
    private static final String LOGGER = "logger_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String LOGGER_TO_BE_REMOVED = "logger-tbr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String LOGGER_TO_BE_ADDED = "logger-tba_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address LOGGER_ADDRESS = LOGGING_SUBSYSTEM.and("logger", LOGGER);
    private static final Address LOGGER_TO_BE_REMOVED_ADDRESS = LOGGING_SUBSYSTEM.and("logger", LOGGER_TO_BE_REMOVED);
    private static final Address LOGGER_TO_BE_ADDED_ADDRESS = LOGGING_SUBSYSTEM.and("logger", LOGGER_TO_BE_ADDED);

    @BeforeClass
    public static void beforeClass() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        AddLogger addLogger = Logging.logger().add(LOGGER).build();
        client.apply(addLogger);
        AddLogger addLoggerTbr = Logging.logger().add(LOGGER_TO_BE_REMOVED).build();
        client.apply(addLoggerTbr);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, TimeoutException, InterruptedException, OperationException, CommandFailedException {
        operations.removeIfExists(LOGGER_ADDRESS);
        operations.removeIfExists(LOGGER_TO_BE_ADDED_ADDRESS);
        operations.removeIfExists(LOGGER_TO_BE_REMOVED_ADDRESS);
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToCategoriesTab();
        page.selectLogger(LOGGER);
    }

    @Test
    public void addLoggerHandler() throws Exception {
        page.addLogger(LOGGER_TO_BE_ADDED, LOGGER_TO_BE_ADDED, "DEBUG");

        new ResourceVerifier(LOGGER_TO_BE_ADDED_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateLoggerLevel() throws Exception {
        selectOptionAndVerify(LOGGER_ADDRESS, "level", "WARN");
    }

    @Test
    public void updateLoggerUsingParentHandler() throws Exception {
        editCheckboxAndVerify(LOGGER_ADDRESS, "use-parent-handlers", false);
    }

    @Test
    public void updateLoggerHandler() throws Exception {
        addHandlers(LOGGER_ADDRESS, "handlers");
    }

    @Test
    public void removeLoggerHandler() throws Exception {
        page.removeInTable(LOGGER_TO_BE_REMOVED);

        administration.reloadIfRequired();

        new ResourceVerifier(LOGGER_TO_BE_REMOVED_ADDRESS, client).verifyDoesNotExist();
    }
}
