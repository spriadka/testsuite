package org.jboss.hal.testsuite.test.configuration.JCA;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 24.8.15.
 */
public class DatasourceXAPolicyTestCASe {
    private static final String SIZE = "Size=7";
    private static final String WATERMARK = "Watermark=9";
    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    JCAPage jcaPage;
    @Page
    StandaloneConfigEntryPoint page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Datasources")
                .addAddress("Type","Non-XA")
                .addAddress("Datasource","ExampleDS");

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }

    @Test
    @InSequence(0)
    public void setDecrementerClass(){
        ConfigPropertiesFragment fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacityDecrementerClass", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);
    }

    @Test //property is not saved for next test!?
    @InSequence(1)
    public void setDecrementerProperty() {
        ConfigPropertiesFragment fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityDecrementerProperties", WATERMARK);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

    }

    @Test
    @InSequence(2)
    public void unsetDecrementerProperty(){
        ConfigPropertiesFragment  fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityDecrementerProperties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);
    }

    @Test
    @InSequence(3)
    public void unsetDecrementerClass(){
        ConfigPropertiesFragment  fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();
        editPanelFragment.getEditor().select("capacityDecrementerClass", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);
    }

    @Test
    @InSequence(4)
    public void setIncrementerClass(){
        ConfigPropertiesFragment  fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacityIncrementerClass", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);
    }

    @Test
    @InSequence(5)
    public void setIncrementerProperty() {
        ConfigPropertiesFragment fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityIncrementerProperties", SIZE);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

    }

    @Test
    @InSequence(6)
    public void unsetIncrementerProperty(){
        ConfigPropertiesFragment  fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityIncrementerProperties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);
    }

    @Test
    @InSequence(7)
    public void unsetIncrementerClass(){
        ConfigPropertiesFragment  fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select("capacityIncrementerClass", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);
    }
}
