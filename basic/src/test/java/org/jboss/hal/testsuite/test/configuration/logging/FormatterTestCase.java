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
 * Created by pcyprian on 27.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class FormatterTestCase {
    private static final String NAME = "TEST-PATTERN";
    private static final String PATTERN = "%d{yyyy-MM-dd} %-5p [%c] (%t) %s%e%n";

    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=logging/pattern-formatter=" + NAME);
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

        page.switchToFormatterTab();
    }

    @Test
    @InSequence(0)
    public void addFormatter(){
        page.addFormatter(NAME, PATTERN);

        verifier.verifyResource(address, true);
    }

    @Test
    @InSequence(1)
    public void updateFormatterPattern(){
        page.getResourceManager().getResourceTable().selectRowByText(0,NAME);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("pattern", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "pattern", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
    }

    @Test
    @InSequence(2)
    public void updateFormatterColorMap(){
        page.getResourceManager().getResourceTable().selectRowByText(0,NAME);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("color-map", "fatal:black");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "color-map", "fatal:black");
    }

    @Test
    @InSequence(3)
    public void removeFormatter(){
        page.getResourceManager().getResourceTable().selectRowByText(0,NAME);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
