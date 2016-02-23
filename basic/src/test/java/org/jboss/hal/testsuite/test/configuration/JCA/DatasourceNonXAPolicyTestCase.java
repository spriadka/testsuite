package org.jboss.hal.testsuite.test.configuration.JCA;

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
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 24.8.15.
 *
 * CAN NOT TEST WRONG PROPERTY
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DatasourceNonXAPolicyTestCase {

    private static final String SIZE = "Size=7";
    private static final String WATERMARK = "Watermark=9";
    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=datasources/data-source=ExampleDS");
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
    private JCAPage jcaPage;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "Datasources")
                .step("Type", "Non-XA")
                .step("Datasource", "ExampleDS");

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();

        ConfigPropertiesFragment  fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
    }

    @Test
    @InSequence(0)
    public void setDecrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacityDecrementerClass", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer", 500);
    }

    @Test //property is not saved for next test .. https://issues.jboss.org/browse/HAL-809
    @InSequence(1)
    public void setDecrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityDecrementerProperties", WATERMARK);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-properties", "{\"Watermark\" => \"9\"}", 500);
    }

    @Test
    @InSequence(2)
    public void unsetDecrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityDecrementerProperties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-properties", "undefined", 500);
    }

    @Test
    @InSequence(3)
    public void unsetDecrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select("capacityDecrementerClass", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-class", "undefined", 500);
    }

    @Test
    @InSequence(4)
    public void setIncrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacityIncrementerClass", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer", 500);
    }

    @Test //property is not saved for next test .. https://issues.jboss.org/browse/HAL-809
    @InSequence(5)
    public void setIncrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityIncrementerProperties", SIZE);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-properties", "{\"Size\" => \"7\"}", 500);
    }

    @Test
    @InSequence(6)
    public void unsetIncrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityIncrementerProperties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-properties", "undefined", 500);
    }

    @Test
    @InSequence(7)
    public void unsetIncrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select("capacityIncrementerClass", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-class", "undefined", 500);
    }
}
