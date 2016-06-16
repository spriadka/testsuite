package org.jboss.hal.testsuite.test.configuration.JCA;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 24.8.15.
 *
 * CAN NOT TEST WRONG PROPERTY
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DatasourceNonXAPolicyTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration adminOps = new Administration(client);

    private FinderNavigation navigation;

    private final Address datasourceAddress = Address.subsystem("datasources").and("data-source", "ExampleDS");
    private final ResourceVerifier verifier = new ResourceVerifier(datasourceAddress, client);
    private final ModelNodeGenerator nodeGenerator = new ModelNodeGenerator();

    @AfterClass
    public static void tearDown() {
        try {
            adminOps.reloadIfRequired();
        } catch (Exception e) {
            IOUtils.closeQuietly(client);
        }
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
    public void setDecrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacityDecrementerClass", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute("capacity-decrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");
    }

    @Test
    @InSequence(1)
    public void setDecrementerProperty() throws Exception {
        final String propertyKey = "Watermark", propertyValue = "9";

        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityDecrementerProperties", propertyKey + "=" + propertyValue);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute("capacity-decrementer-properties", expectedPropertiesNode);
    }

    @Test
    @InSequence(2)
    public void unsetDecrementerProperty() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityDecrementerProperties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttributeIsUndefined("capacity-decrementer-properties");
    }

    @Test
    @InSequence(3)
    public void unsetDecrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select("capacityDecrementerClass", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttributeIsUndefined("capacity-decrementer-class");
    }

    @Test
    @InSequence(4)
    public void setIncrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacityIncrementerClass", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute("capacity-incrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");
    }

    @Test
    @InSequence(5)
    public void setIncrementerProperty() throws Exception {
        final String propertyKey = "Size", propertyValue = "7";

        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityIncrementerProperties", propertyKey + "=" + propertyValue);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute("capacity-incrementer-properties", expectedPropertiesNode);
    }

    @Test
    @InSequence(6)
    public void unsetIncrementerProperty() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacityIncrementerProperties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttributeIsUndefined("capacity-incrementer-properties");
    }

    @Test
    @InSequence(7)
    public void unsetIncrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select("capacityIncrementerClass", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttributeIsUndefined("capacity-incrementer-class");
    }

}
