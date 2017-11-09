package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.SELECT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;

@RunWith(Arquillian.class)
@Category(Standalone.class)
public class PeriodicTestCase extends LoggingAbstractTestCase {

    private static final String
            PERIODIC_ROTATING_FILE_HANDLER = "periodic-rotating-file-handler",
            SUFFIX = "suffix",
            APPEND = "append",
            AUTOFLUSH = "autoflush",
            ENABLED = "enabled",
            LEVEL = "level";

    private static final Address PERIODIC_HANDLER_ADDRESS = LOGGING_SUBSYSTEM.and(PERIODIC_ROTATING_FILE_HANDLER, RandomStringUtils.randomAlphanumeric(5));

    @BeforeClass
    public static void setUp() throws Exception {
        createPeriodicFileHandler(PERIODIC_HANDLER_ADDRESS.getLastPairValue(), "periodic-handler.log");
        administration.reloadIfRequired();
        new ResourceVerifier(PERIODIC_HANDLER_ADDRESS, client).verifyExists();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(PERIODIC_HANDLER_ADDRESS);
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToHandlerTab();
        page.switchToPeriodic();
        page.selectHandler(PERIODIC_HANDLER_ADDRESS.getLastPairValue());
    }

    @Test
    public void updatePeriodicHandlerNamedFormatter() throws Exception {
        final String value = "COLOR-PATTERN";

        final ModelNodeResult
                formatterModelNodeResult = operations.readAttribute(PERIODIC_HANDLER_ADDRESS, FORMATTER),
                namedFormatterModelNodeResult = operations.readAttribute(PERIODIC_HANDLER_ADDRESS, NAMED_FORMATTER);

        formatterModelNodeResult.assertSuccess();
        namedFormatterModelNodeResult.assertSuccess();

        try {
            new ConfigChecker.Builder(client, PERIODIC_HANDLER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, FORMATTER, "")
                    .edit(TEXT, NAMED_FORMATTER, value)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(NAMED_FORMATTER, value,
                            "Probably fails because of https://issues.jboss.org/browse/WFCORE-2958");
        } finally {
            operations.batch(new Batch()
                    .writeAttribute(PERIODIC_HANDLER_ADDRESS, FORMATTER, formatterModelNodeResult.value())
                    .writeAttribute(PERIODIC_HANDLER_ADDRESS, NAMED_FORMATTER, namedFormatterModelNodeResult.value()))
            .assertSuccess();
            administration.reloadIfRequired();
        }
    }

    @Test
    public void updatePeriodicHandlerSuffix() throws Exception {
        final String value = ".yyyy-MM-dd,HH:mm";
        new ConfigChecker.Builder(client, PERIODIC_HANDLER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, SUFFIX, value)
                .andSave()
                .verifyFormSaved()
                .verifyAttribute(SUFFIX, value);
    }

    @Test
    public void updatePeriodicHandlerAppend() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, APPEND, false);
    }

    @Test
    public void updatePeriodicHandlerAutoflush() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, AUTOFLUSH, false);
    }

    @Test
    public void disablePeriodicHandler() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, ENABLED, false);
    }

    @Test
    public void enablePeriodicHandler() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, ENABLED, true);
    }

    @Test
    public void updatePeriodicHandlerLevel() throws Exception {
        selectOptionAndVerify(PERIODIC_HANDLER_ADDRESS, LEVEL, "CONFIG");
    }

    @Test
    public void updatePeriodicHandlerToDefaultSettings() throws Exception {
        new ConfigChecker.Builder(client, PERIODIC_HANDLER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, NAMED_FORMATTER, "PATTERN")
                .edit(TEXT, FORMATTER, "")
                .edit(TEXT, SUFFIX, ".yyyy-MM-dd")
                .edit(CHECKBOX, APPEND, true)
                .edit(CHECKBOX, AUTOFLUSH, true)
                .edit(CHECKBOX, ENABLED, true)
                .edit(SELECT, LEVEL, "ALL")
                .andSave()
                .verifyFormSaved()
                .verifyAttribute(NAMED_FORMATTER, "PATTERN",
                        "Probably failed because of https://issues.jboss.org/browse/WFCORE-2958")
                .verifyAttribute(SUFFIX, ".yyyy-MM-dd")
                .verifyAttribute(APPEND, true)
                .verifyAttribute(AUTOFLUSH, true)
                .verifyAttribute(ENABLED, true)
                .verifyAttribute(LEVEL, "ALL");
    }
}
