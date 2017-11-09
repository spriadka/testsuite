package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.logging.Logging;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class ConsoleHandlerTestCase extends LoggingAbstractTestCase {

    private static final String CONSOLE_HANDLER = "CONSOLE_HANDLER_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONSOLE_HANDLER_TBA = "CONSOLE_HANDLER_TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONSOLE_HANDLER_TB_REMOVED = "C_HANDLER_REMOVE_ME_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address C_HANDLER_ADDRESS = LOGGING_SUBSYSTEM.and("console-handler", CONSOLE_HANDLER);
    private static final Address C_HANDLER_TB_REMOVED_ADDRESS = LOGGING_SUBSYSTEM
            .and("console-handler", CONSOLE_HANDLER_TB_REMOVED);
    private static final Address C_HANDLER_TBA_ADDRESS = LOGGING_SUBSYSTEM
            .and("console-handler", CONSOLE_HANDLER_TBA);

    @BeforeClass
    public static void setUp() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        List<OnlineCommand> addConsoleHandlers = new LinkedList<>();
        addConsoleHandlers.add(Logging.handler().console().add(CONSOLE_HANDLER).build());
        addConsoleHandlers.add(Logging.handler().console().add(CONSOLE_HANDLER_TB_REMOVED).build());
        client.apply(addConsoleHandlers);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(C_HANDLER_ADDRESS);
        operations.removeIfExists(C_HANDLER_TBA_ADDRESS);
        operations.removeIfExists(C_HANDLER_TB_REMOVED_ADDRESS);
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToHandlerTab();
        page.selectHandler(CONSOLE_HANDLER);
    }

    @Test
    public void addConsoleHandler() throws Exception {
        page.addConsoleHandler(CONSOLE_HANDLER_TBA, "ALL");

        new ResourceVerifier(C_HANDLER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateConsoleHandlerLevel() throws Exception {
        selectOptionAndVerify(C_HANDLER_ADDRESS, "level", "DEBUG");
    }

    @Test
    public void updateConsoleHandlerNamedFormatter() throws Exception {
        new ConfigChecker.Builder(client, C_HANDLER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .edit(ConfigChecker.InputType.TEXT, FORMATTER, "")
                .edit(ConfigChecker.InputType.TEXT, NAMED_FORMATTER, "COLOR-PATTERN")
                .andSave()
                .verifyFormSaved()
                .verifyAttribute(NAMED_FORMATTER, "COLOR-PATTERN",
                        "Probably fails because of https://issues.jboss.org/browse/WFCORE-2958");
    }

    @Test
    public void updateConsoleHandlerTarget() throws Exception {
        selectOptionAndVerify(C_HANDLER_ADDRESS, "target", "console");
    }

    @Test
    public void updateConsoleHandlerAutoflush() throws Exception {
        editCheckboxAndVerify(C_HANDLER_ADDRESS, "autoflush", false);
    }

    @Test
    public void updateConsoleHandlerFormatter() throws Exception {
        editTextAndVerify(C_HANDLER_ADDRESS, "formatter", RandomStringUtils.randomAlphanumeric(6));
    }

    @Test
    public void removeConsoleHandler() throws Exception {
        page.removeInTable(CONSOLE_HANDLER_TB_REMOVED);

        new ResourceVerifier(C_HANDLER_TB_REMOVED_ADDRESS, client).verifyDoesNotExist();
    }

}
