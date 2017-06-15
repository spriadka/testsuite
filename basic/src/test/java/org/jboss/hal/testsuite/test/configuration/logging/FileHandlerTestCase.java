package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 14.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class FileHandlerTestCase extends LoggingAbstractTestCase {
    private static final String FILE_HANDLER = "FILE_HANDLER_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String FILE_HANDLER_TO_BE_REMOVED = "FILE_HANDLER_REMOVE_ME_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address FILE_HANDLER_ADDRESS = LOGGING_SUBSYSTEM.and("file-handler", FILE_HANDLER);
    private static final Address FILE_HANDLER_TB_REMOVED_ADDRESS = LOGGING_SUBSYSTEM
            .and("file-handler", FILE_HANDLER_TO_BE_REMOVED);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        createFileHandler(FILE_HANDLER_ADDRESS, "tests_hal1.log");
        createFileHandler(FILE_HANDLER_TB_REMOVED_ADDRESS, "tests_hal2.log");
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(FILE_HANDLER_ADDRESS);
        operations.removeIfExists(FILE_HANDLER_TB_REMOVED_ADDRESS);
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToHandlerTab();
        page.switchToFile();
        page.selectHandler(FILE_HANDLER);
    }

    @Test
    public void addFileHandler() throws Exception {
        new ResourceVerifier(FILE_HANDLER_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateFileHandlerNamedFormatter() throws Exception {
        new ConfigChecker.Builder(client, FILE_HANDLER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .edit(ConfigChecker.InputType.TEXT, FORMATTER, "")
                .edit(ConfigChecker.InputType.TEXT, NAMED_FORMATTER, "COLOR-PATTERN")
                .andSave()
                .verifyFormSaved()
                .verifyAttribute(NAMED_FORMATTER, "COLOR-PATTERN",
                        "Probably fails because of https://issues.jboss.org/browse/WFCORE-2958");
    }

    @Test
    public void updateFileHandlerEncoding() throws Exception {
        editTextAndVerify(FILE_HANDLER_ADDRESS, "encoding", "UTF-8");
    }

    @Test
    public void updateFileHandlerAppend() throws Exception {
        editCheckboxAndVerify(FILE_HANDLER_ADDRESS, "append", false);
    }

    @Test
    public void updateFileHandlerAutoflush() throws Exception {
        editCheckboxAndVerify(FILE_HANDLER_ADDRESS, "autoflush", false);
    }

    @Test
    public void disableFileHandler() throws Exception {
        editCheckboxAndVerify(FILE_HANDLER_ADDRESS, "enabled", false);
    }

    @Test
    public void updateFileHandlerLevel() throws Exception {
        selectOptionAndVerify(FILE_HANDLER_ADDRESS, "level", "CONFIG");
    }

    @Test
    public void updateFileHandlerFilterSpec() throws Exception {
        editTextAndVerify(FILE_HANDLER_ADDRESS, "filter-spec", "match(\"JBEAP.*\")");
    }

    @Test
    public void updateFileHandlerFormatter() throws Exception {
        editTextAndVerify(FILE_HANDLER_ADDRESS, "formatter", "%d{HH:mm:ss,SSS}");
    }


    @Test
    public void updateFileHandlerToDefaultSettings() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.edit();

        editor.text("named-formatter", "");
        editor.text("encoding", "");
        editor.text("filter-spec", "");
        editor.text("formatter", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
        editor.checkbox("append", true);
        editor.checkbox("autoflush", true);
        editor.checkbox("enabled", true);
        editor.select("level", "ALL");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(FILE_HANDLER_ADDRESS, client, 500).verifyAttributeIsUndefined("named-formatter")
                .verifyAttributeIsUndefined("encoding")
                .verifyAttributeIsUndefined("filter-spec")
                .verifyAttribute("formatter", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n")
                .verifyAttribute("append", true)
                .verifyAttribute("autoflush", true)
                .verifyAttribute("enabled", true)
                .verifyAttribute("level", "ALL");
    }

    @Test
    public void removeFileHandler() throws Exception {
        page.removeInTable(FILE_HANDLER_TO_BE_REMOVED);

        administration.reloadIfRequired();

        new ResourceVerifier(FILE_HANDLER_TB_REMOVED_ADDRESS, client).verifyDoesNotExist();
    }
}
