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

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 27.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class PeriodicTestCase extends LoggingAbstractTestCase {
    private static final String PERIODIC_HANDLER = "PeriodicHandler" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address PERIODIC_HANDLER_ADDRESS = LOGGING_SUBSYSTEM.and("periodic-rotating-file-handler", PERIODIC_HANDLER);

    @BeforeClass
    public static void setUp() throws Exception {
        createPeriodicFileHandler(PERIODIC_HANDLER_ADDRESS, "periodic-handler.log");
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
        page.selectHandler(PERIODIC_HANDLER);
    }

    @Test
    public void updatePeriodicHandlerNamedFormatter() throws Exception {
        editTextAndVerify(PERIODIC_HANDLER_ADDRESS, "named-formatter", "COLOR-PATTERN");
    }

    @Test
    public void updatePeriodicHandlerSuffix() throws Exception {
        editTextAndVerify(PERIODIC_HANDLER_ADDRESS, "suffix", ".yyyy-MM-dd,HH:mm");
    }

    @Test
    public void updatePeriodicHandlerAppend() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, "append", false);
    }

    @Test
    public void updatePeriodicHandlerAutoflush() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, "autoflush", false);
    }

    @Test
    public void disablePeriodicHandler() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, "enabled", false);
    }

    @Test
    public void enablePeriodicHandler() throws Exception {
        editCheckboxAndVerify(PERIODIC_HANDLER_ADDRESS, "enabled", true);
    }

    @Test
    public void updatePeriodicHandlerLevel() throws Exception {
        selectOptionAndVerify(PERIODIC_HANDLER_ADDRESS, "level", "CONFIG");
    }

    @Test
    public void updatePeriodicHandlerToDefaultSettings() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.edit();

        editor.text("named-formatter", "PATTERN");
        editor.text("suffix", ".yyyy-MM-dd");
        editor.checkbox("append", true);
        editor.checkbox("autoflush", true);
        editor.checkbox("enabled", true);
        editor.select("level", "ALL");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        ResourceVerifier verifier = new ResourceVerifier(PERIODIC_HANDLER_ADDRESS, client, 500);
        verifier.verifyAttribute("named-formatter", "PATTERN");
        verifier.verifyAttribute("suffix", ".yyyy-MM-dd");
        verifier.verifyAttribute("append", true);
        verifier.verifyAttribute("autoflush", true);
        verifier.verifyAttribute("enabled", true);
        verifier.verifyAttribute("level", "ALL");
    }
}
