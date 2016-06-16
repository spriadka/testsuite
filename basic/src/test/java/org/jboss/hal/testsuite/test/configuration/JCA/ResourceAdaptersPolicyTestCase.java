package org.jboss.hal.testsuite.test.configuration.JCA;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 24.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ResourceAdaptersPolicyTestCase {

    private static final String
        RESOURCE_ADAPTER_NAME = "RA_ResourceAdaptersPolicyTestCase",
        CONNECTION_DEFINITIONS_NAME = "CD_test",
        JNDINAME = "java:/" + CONNECTION_DEFINITIONS_NAME,
        CAPACITY_DECREMENTER_CLASS = "capacity-decrementer-class",
        CAPACITY_DECREMENTER_PROPERTIES = "capacity-decrementer-properties",
        CAPACITY_INCREMENTER_CLASS = "capacity-incrementer-class",
        CAPACITY_INCREMENTER_PROPERTIES = "capacity-incrementer-properties";
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration adminOps = new Administration(client);
    private static final Operations ops = new Operations(client);
    private static final Address
        resourceAdapterAddress = Address.subsystem("resource-adapters").and("resource-adapter", RESOURCE_ADAPTER_NAME),
        connectionDefinitionsAddress = resourceAdapterAddress.and("connection-definitions", CONNECTION_DEFINITIONS_NAME);

    private  FinderNavigation navigation;
    private final ResourceVerifier verifier = new ResourceVerifier(connectionDefinitionsAddress, client);
    private final ModelNodeGenerator nodeGenerator = new ModelNodeGenerator();

    @BeforeClass
    public static void setUp() throws IOException {
        ops.add(resourceAdapterAddress, Values.of("archive", "archive_test").and("transaction-support", "NoTransaction"));
        ops.add(connectionDefinitionsAddress,
                Values.of("jndi-name", JNDINAME).and("class-name", "test-class").and("enabled", false));
    }

    @AfterClass
    public static void tearDown() throws IOException, InterruptedException, TimeoutException, OperationException {
        try {
            ops.removeIfExists(resourceAdapterAddress);
            adminOps.reloadIfRequired();
        } finally {
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
                .step(FinderNames.SUBSYSTEM, "Resource Adapters")
                .step("Resource Adapter", RESOURCE_ADAPTER_NAME);

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();

        jcaPage.switchToConnectionDefinitions();

        ConfigPropertiesFragment fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
    }


    @Test
    @InSequence(0)
    public void setDecrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select(CAPACITY_DECREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(CAPACITY_DECREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");
    }

    @Test //property is not saved for next test .. https://issues.jboss.org/browse/HAL-809
    @InSequence(1)
    public void setDecrementerProperty() throws Exception {
        final String propertyKey = "Watermark", propertyValue = "9";

        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text(CAPACITY_DECREMENTER_PROPERTIES,  propertyKey + "=" + propertyValue);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        adminOps.reloadIfRequired();
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute(CAPACITY_DECREMENTER_PROPERTIES, expectedPropertiesNode);

    }

    @Test
    @InSequence(2)
    public void unsetDecrementerProperty() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text(CAPACITY_DECREMENTER_PROPERTIES, "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        adminOps.reloadIfRequired();
        verifier.verifyAttributeIsUndefined(CAPACITY_DECREMENTER_PROPERTIES, "See https://issues.jboss.org/browse/HAL-1112 ");
    }

    @Test
    @InSequence(3)
    public void unsetDecrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select(CAPACITY_DECREMENTER_CLASS, "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttributeIsUndefined(CAPACITY_DECREMENTER_CLASS);
    }

    @Test
    @InSequence(4)
    public void setIncrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select(CAPACITY_INCREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(CAPACITY_INCREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");
    }

    @Test //property is not saved for next test .. https://issues.jboss.org/browse/HAL-809
    @InSequence(5)
    public void setIncrementerProperty() throws Exception {
        final String propertyKey = "Size", propertyValue = "7";

        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text(CAPACITY_INCREMENTER_PROPERTIES,  propertyKey + "=" + propertyValue);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        adminOps.reloadIfRequired();
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute(CAPACITY_INCREMENTER_PROPERTIES, expectedPropertiesNode);
    }

    @Test
    @InSequence(6)
    public void unsetIncrementerProperty() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text(CAPACITY_INCREMENTER_PROPERTIES, "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        adminOps.reloadIfRequired();
        verifier.verifyAttributeIsUndefined(CAPACITY_INCREMENTER_PROPERTIES, "See https://issues.jboss.org/browse/HAL-1112 ");
    }

    @Test
    @InSequence(7)
    public void unsetIncrementerClass() throws Exception {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select(CAPACITY_INCREMENTER_CLASS, "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttributeIsUndefined(CAPACITY_INCREMENTER_CLASS);
    }
}
