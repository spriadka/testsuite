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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 26.8.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class AsyncHandlerTestCase {
    private static final String ASYNCHANDLER = "asyncHandler";

    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=logging/async-handler=" + ASYNCHANDLER);
    private ModelNode domainPath = new ModelNode("/profile=default/subsystem=logging/async-handler=" + ASYNCHANDLER);
    private ResourceAddress address;
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before(){
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
        page.switchToAsync();
    }

    @Test
    @InSequence(0)
    public void addAsyncHandler(){
        page.addAsyncHandler(ASYNCHANDLER, "230");

        verifier.verifyResource(address, true);
    }

    @Test
    @InSequence(1)
    public void updateAsyncHandlerLevel(){
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().select("level", "WARN");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "level", "WARN");
    }

    @Test // https://issues.jboss.org/browse/HAL-811
    @InSequence(2)
    public void updateAsyncHandlerQueueLenWithWrongValue(){
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("queue-length", "0");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed,because 0 is illegal value.", finished);
        verifier.verifyAttribute(address, "queue-length", "230");
    }

    @Test
    @InSequence(3)
    public void updateAsyncHandlerQueueLen(){
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("queue-length", "240");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "queue-length", "240");
    }

    @Test
    @InSequence(4)
    public void disableAsyncHandler(){
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("enabled", false);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "enabled" , false);
    }


    @Test
    @InSequence(5)
    public void addAsyncHandlerSubhandlers(){
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("subhandlers", "CONSOLE\nFILE");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "subhandlers", "[\"CONSOLE\",\"FILE\"]");
    }

    @Test
    @InSequence(6) //https://issues.jboss.org/browse/HAL-819
    public void addAsyncHandlerWrongSubhandlers(){
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("subhandlers", "BLABLA");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed. But handlers are not really saved.", finished);
        verifier.verifyAttribute(address, "subhandlers", "[\"CONSOLE\",\"FILE\"]");
    }

    @Test
    @InSequence(7)
    public void removeAsyncHandler(){
        page.remove();

        verifier.verifyResource(address,false);
    }
}
