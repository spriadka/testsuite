package org.jboss.hal.testsuite.test.configuration.logging;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 27.8.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ConsoleHandlerTestCase {
    private static final String CONSOLEHANDLER = "consoleHandler";

    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=logging/console-handler=" + CONSOLEHANDLER);
    private ModelNode domainPath = new ModelNode("/profile=default/subsystem=logging/console-handler=" + CONSOLEHANDLER);
    private ResourceAddress address;
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "default")
                    .addAddress(FinderNames.SUBSYSTEM, "Logging");
            address = new ResourceAddress(domainPath);
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Logging");
            address = new ResourceAddress(path);
        }

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();

        page.switchToHandlerTab();
    }

    @Test
    @InSequence(0)
    public void addConsoleHandler() {
        page.addConsoleHandler(CONSOLEHANDLER, "PATTERN");

        verifier.verifyResource(address, true);
    }

    @Test
    @InSequence(1)
    public void updateConsoleHandlerLevel() {
        page.getResourceManager().getResourceTable().selectRowByText(0, CONSOLEHANDLER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().select("level", "DEBUG");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "level", "DEBUG");
    }

    @Test
    @InSequence(2)
    public void updateConsoleHandlerNamedFormatter() {
        page.getResourceManager().getResourceTable().selectRowByText(0, CONSOLEHANDLER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("named-formatter", "");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "named-formatter" , "undefined");
    }

    @Test
    @InSequence(3)
    public void updateConsoleHandlerTarget() {
        page.getResourceManager().getResourceTable().selectRowByText(0, CONSOLEHANDLER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().select("target", "console");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "target" , "console");
    }

    @Test
    @InSequence(4)
    public void updateConsoleHandlerAutoflush() {
        page.getResourceManager().getResourceTable().selectRowByText(0, CONSOLEHANDLER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("autoflush", false);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "autoflush" , false);
    }

    @Test
    @InSequence(5)
    public void updateConsoleHandlerFormatter() {
        page.getResourceManager().getResourceTable().selectRowByText(0, CONSOLEHANDLER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("formatter", "");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "formatter" , "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
    }

    @Test
    @InSequence(6)
    public void removeConsoleHandler() {
        page.getResourceManager().getResourceTable().selectRowByText(0, CONSOLEHANDLER);
        page.remove();

        verifier.verifyResource(address, false);
    }

}
