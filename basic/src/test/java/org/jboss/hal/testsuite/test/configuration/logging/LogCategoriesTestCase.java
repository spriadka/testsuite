package org.jboss.hal.testsuite.test.configuration.logging;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 26.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class LogCategoriesTestCase {
    private static String LOGGER = "com.test.logger";
    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=logging/logger=" + LOGGER);
    private ResourceAddress address = new ResourceAddress(path);
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before(){
        navigation = new FinderNavigation(browser,StandaloneConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION,FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM,"Logging");

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();

        page.switchToCategoriesTab();
    }

    @Test
    @InSequence(0)
    public void addLoggerHandler(){
        page.addLogger(LOGGER, LOGGER, "DEBUG");

        verifier.verifyResource(address, true);
    }

    @Test
    @InSequence(1)
    public void updateLoggerLevel(){
        page.getResourceManager().getResourceTable().selectRowByText(0,LOGGER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().select("level", "WARN");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "level", "WARN");
    }

    @Test
    @InSequence(2)
    public void updateLoggerUsingParentHandler(){
        page.getResourceManager().getResourceTable().selectRowByText(0, LOGGER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("use-parent-handlers", false);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "use-parent-handlers",false);
    }

    @Test
    @InSequence(3)
    public void updateLoggerHandlesr(){
        page.getResourceManager().getResourceTable().selectRowByText(0, LOGGER);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("handlers", "CONSOLE\nFILE");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "handlers", "[\"CONSOLE\",\"FILE\"]");
    }

    @Test
    @InSequence(4)
    public void removeLoggerHandler(){
        page.getResourceManager().getResourceTable().selectRowByText(0,LOGGER);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
