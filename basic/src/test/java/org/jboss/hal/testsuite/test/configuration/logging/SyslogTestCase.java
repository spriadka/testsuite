package org.jboss.hal.testsuite.test.configuration.logging;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 27.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SyslogTestCase {
    private static final String SYSLOGHANDLER = "syslogHandler";

    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=logging/syslog-handler=" + SYSLOGHANDLER);
    private ResourceAddress address = new ResourceAddress(path);
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Logging");

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();

        page.switchToHandlerTab();
        page.switchToSyslog();
    }

    @Test
    @InSequence(0)
    public void addSyslogHandler() {
        page.addSyslogHandler(SYSLOGHANDLER);

        verifier.verifyResource(address, true);
    }

    @Test //https://issues.jboss.org/browse/HAL-813
    @InSequence(1)
    public void updateSyslogHandlerPortWrongValue() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("port", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed. -1 is invalid value", finished);
        verifier.verifyAttribute(address, "port", "514", 500);
    }

    @Test
    @InSequence(2)
    public void updateSyslogHandlerPort() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("port", "0");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should not be saved and closed.", finished);
        verifier.verifyAttribute(address, "port", "0", 500);
    }

    @Test
    @InSequence(3)
    public void updateSyslogHandlerFacolity() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().select("facility", "local-use-1");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "facility", "local-use-1", 500);
    }


    @Test
    @InSequence(4)
    public void updateSyslogHandlerFormat() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().select("syslog-format", "RFC3164");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "syslog-format", "RFC3164", 500);
    }

    @Test
    @InSequence(5)
    public void removeSyslogHandler() {
        page.remove();

        verifier.verifyResource(address, false);
    }
}
