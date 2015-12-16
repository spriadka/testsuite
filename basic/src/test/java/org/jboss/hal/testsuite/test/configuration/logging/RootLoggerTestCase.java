package org.jboss.hal.testsuite.test.configuration.logging;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 25.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class RootLoggerTestCase extends LoggingAbstractTestCase {
    //TODO add command for attribute backup and restore
    private static final Address ROOT_LOGGER_ADDRESS = LOGGING_SUBSYSTEM.and("root-logger", "ROOT");

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void updateRootLoggerAttributes() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.edit();

        editor.text("handlers", "");
        editor.select("level", "WARN");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(ROOT_LOGGER_ADDRESS, client).verifyAttributeIsUndefined("handlers");
        new ResourceVerifier(ROOT_LOGGER_ADDRESS, client).verifyAttribute("level", "WARN");
    }

    @Test
    public void setRootLoggerAttributesToDefault() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.edit();

        editor.text("handlers", "CONSOLE\nFILE");
        editor.select("level", "INFO");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(ROOT_LOGGER_ADDRESS, client).verifyAttribute("handlers", "[\"CONSOLE\",\"FILE\"]");
        new ResourceVerifier(ROOT_LOGGER_ADDRESS, client).verifyAttribute("level", "INFO");
    }
}
