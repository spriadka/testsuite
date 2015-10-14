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
 * Created by pcyprian on 14.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class PeriodicSizeTestCase {
    private static final String PERIODIC_SIZE_HANDLER = "Periodic_Size_HANDLER";

    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=logging/periodic-size-rotating-file-handler=" + PERIODIC_SIZE_HANDLER);
    private ResourceAddress address = new ResourceAddress(path);
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);

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
        page.switchToPeriodicSize();
    }

    @Test
    @InSequence(0)
    public void addPeriodicSizeHandler() {
        page.addPeriodicSizeHandler(PERIODIC_SIZE_HANDLER, ".yyyy-MM-dd,HH:mm");

        verifier.verifyResource(address, true);
    }

    @Test
    @InSequence(1)
    public void updatePeriodicSizeHandlerNamedFormatter() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("named-formatter", "PATTERN");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "named-formatter", "PATTERN");

    }

    @Test
    @InSequence(2)
    public void updatePeriodicSizeHandlerEncoding() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("encoding", "UTF-8");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "encoding", "UTF-8");

    }

    @Test
    @InSequence(3)
    public void updatePeriodicSizeHandlerAppend() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("append", false);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "append", false);

    }

    @Test
    @InSequence(4)
    public void updatePeriodicSizeHandlerAutoflush() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("autoflush", false);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "autoflush", false);

    }

    @Test
    @InSequence(5)
    public void disablePeriodicSizeHandler() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("enabled", false);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "enabled", false);

    }

    @Test
    @InSequence(6)
    public void updatePeriodicSizeHandlerLevel() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().select("level", "CONFIG");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "level" , "CONFIG");
    }

    @Test
    @InSequence(7)
    public void updatePeriodicSizeHandlerFilterSpec() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("filter-spec", "match(\"JBEAP.*\")");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "filter-spec", "match(\"JBEAP.*\")");

    }

    @Test
    @InSequence(8)
    public void updatePeriodicSizeHandlerFormatter() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("formatter", "%d{HH:mm:ss,SSS}");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "formatter", "%d{HH:mm:ss,SSS}");

    }

    @Test
    @InSequence(9)
    public void updatePeriodicSizeHandlerRotateOnBoot() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("rotate-on-boot", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "rotate-on-boot", true);

    }

    @Test
    @InSequence(10)
    public void updatePeriodicSizeHandlerMaxBackupIndex() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("max-backup-index", "3");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "max-backup-index", "3");

    }

    @Test
    @InSequence(11)
    public void updatePeriodicSizeHandlerToDefualtSettings() {
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("named-formatter", "");
        editPanelFragment.getEditor().text("encoding", "");
        editPanelFragment.getEditor().text("filter-spec", "");
        editPanelFragment.getEditor().text("formatter", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
        editPanelFragment.getEditor().text("max-backup-index", "0");
        editPanelFragment.getEditor().checkbox("append", true);
        editPanelFragment.getEditor().checkbox("autoflush", true);
        editPanelFragment.getEditor().checkbox("enabled", true);
        editPanelFragment.getEditor().checkbox("rotate-on-boot", false);
        editPanelFragment.getEditor().select("level", "ALL");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "named-formatter", "undefined");
        verifier.verifyAttribute(address, "encoding", "undefined");
        verifier.verifyAttribute(address, "filter-spec", "undefined");
        verifier.verifyAttribute(address, "formatter", "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
        verifier.verifyAttribute(address, "max-backup-index", "1");
        verifier.verifyAttribute(address, "append", true);
        verifier.verifyAttribute(address, "autoflush", true);
        verifier.verifyAttribute(address, "enabled", true);
        verifier.verifyAttribute(address, "rotate-on-boot", false);
        verifier.verifyAttribute(address, "level", "ALL");
    }

    @Test
    @InSequence(12)
    public void removePeriodicSizeHandler() {
        page.remove();

        verifier.verifyResource(address, false);
    }
}
