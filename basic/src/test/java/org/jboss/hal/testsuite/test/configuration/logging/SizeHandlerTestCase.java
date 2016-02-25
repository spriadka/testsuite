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
public class SizeHandlerTestCase extends LoggingAbstractTestCase {

    private static final String SIZE_HANDLER = "Size_HANDLER" + RandomStringUtils.randomAlphanumeric(6);
    private static final String SIZE_HANDLER_TBA = "Size_HANDLER_TBA" + RandomStringUtils.randomAlphanumeric(6);
    private static final String SIZE_HANDLER_TBR = "Size_HANDLER_TBR" + RandomStringUtils.randomAlphanumeric(6);

    private static final Address SIZE_HANDLER_ADDRESS = LOGGING_SUBSYSTEM
            .and("size-rotating-file-handler", SIZE_HANDLER);
    private static final Address SIZE_HANDLER_TBA_ADDRESS = LOGGING_SUBSYSTEM
            .and("size-rotating-file-handler", SIZE_HANDLER_TBA);
    private static final Address SIZE_HANDLER_TBR_ADDRESS = LOGGING_SUBSYSTEM
            .and("size-rotating-file-handler", SIZE_HANDLER_TBR);

    @BeforeClass
    public static void setUp() throws Exception {
        createFileHandler(SIZE_HANDLER_ADDRESS, "sizehandler.log");
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyExists();
        createFileHandler(SIZE_HANDLER_TBR_ADDRESS, "sizehandler2.log");
        new ResourceVerifier(SIZE_HANDLER_TBR_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(SIZE_HANDLER_ADDRESS);
        operations.removeIfExists(SIZE_HANDLER_TBA_ADDRESS);
        operations.removeIfExists(SIZE_HANDLER_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToHandlerTab();
        page.switchToSize();
        page.selectHandler(SIZE_HANDLER);
    }

    @Test
    public void addSizeHandler() throws Exception {
        page.addFileHandler(SIZE_HANDLER_TBA, getTmpDirPath("logs"));
        new ResourceVerifier(SIZE_HANDLER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateSizeHandlerNamedFormatter() throws Exception {
        editTextAndVerify(SIZE_HANDLER_ADDRESS, "named-formatter", "PATTERN");
    }

    @Test
    public void updateSizeHandlerEncoding() throws Exception {
        editTextAndVerify(SIZE_HANDLER_ADDRESS, "encoding", "UTF-8");
    }

    @Test
    public void updateSizeHandlerAppend() throws Exception {
        editCheckboxAndVerify(SIZE_HANDLER_ADDRESS, "append", false);
    }

    @Test
    public void updateSizeHandlerAutoflush() throws Exception {
        editCheckboxAndVerify(SIZE_HANDLER_ADDRESS, "autoflush", false);
    }

    @Test
    public void disableSizeHandler() throws Exception {
        editCheckboxAndVerify(SIZE_HANDLER_ADDRESS, "enabled", false);
    }

    @Test
    public void updateSizeHandlerLevel() throws Exception {
        selectOptionAndVerify(SIZE_HANDLER_ADDRESS, "level", "CONFIG");
    }

    @Test
    public void updateSizeHandlerFilterSpec() throws Exception {
        editTextAndVerify(SIZE_HANDLER_ADDRESS, "filter-spec", "match(\"JBEAP.*\")");
    }

    @Test
    public void updateSizeHandlerFormatter() throws Exception {
        editTextAndVerify(SIZE_HANDLER_ADDRESS, "formatter", "%d{HH:mm:ss,SSS}");
    }

    @Test
    public void updateSizeHandlerRotateOnBoot() throws Exception {
        editCheckboxAndVerify(SIZE_HANDLER_ADDRESS, "rotate-on-boot", true);
    }

    @Test
    public void updateSizeHandlerMaxBackupIndex() throws Exception {
        editTextAndVerify(SIZE_HANDLER_ADDRESS, "max-backup-index", 3);
    }

    @Test
    public void updateSizeHandlerSuffix() throws Exception {
        editTextAndVerify(SIZE_HANDLER_ADDRESS, "suffix", ".yyyy-MM-dd,HH:mm");
    }

    @Test
    public void updateSizeHandlerToDefaultSettings() throws Exception {

        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.edit();

       editor.text("named-formatter", "");
       editor.text("encoding", "");
       editor.text("filter-spec", "");
       editor.text("formatter", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
       editor.text("max-backup-index", "1");
       editor.text("suffix", "");
       editor.checkbox("append", true);
       editor.checkbox("autoflush", true);
       editor.checkbox("enabled", true);
       editor.checkbox("rotate-on-boot", false);
       editor.select("level", "ALL");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttributeIsUndefined("named-formatter");
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttributeIsUndefined("encoding");
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttributeIsUndefined("filter-spec");
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttribute("formatter", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttributeIsUndefined("suffix");
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttribute("max-backup-index", 1);
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttribute("append", true);
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttribute("autoflush", true);
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttribute("enabled", true);
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttribute("rotate-on-boot", false);
        new ResourceVerifier(SIZE_HANDLER_ADDRESS, client).verifyAttribute("level", "ALL");
    }

    @Test
    public void removeSizeHandler() throws Exception {
        page.selectHandler(SIZE_HANDLER_TBR);
        page.remove();

        new ResourceVerifier(SIZE_HANDLER_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
