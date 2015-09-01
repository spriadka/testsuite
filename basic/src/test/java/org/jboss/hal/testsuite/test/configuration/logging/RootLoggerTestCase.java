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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 25.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class RootLoggerTestCase {

    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=logging/root-logger=ROOT");
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
    }

    @Test
    @InSequence(0)
    public void updateRootLoggerAttributes(){
        page.getContentRoot();
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("handlers", "");
        editPanelFragment.getEditor().select("level", "WARN");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "handlers", "undefined");
        verifier.verifyAttribute(address, "level", "WARN");
    }

    @Test
    @InSequence(1)
    public void setRootLoggerAttributesToDefault(){
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("handlers", "CONSOLE\nFILE");
        editPanelFragment.getEditor().select("level", "INFO");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "handlers", "[\"CONSOLE\",\"FILE\"]");
        verifier.verifyAttribute(address,"level","INFO");
    }
}
