package org.jboss.hal.testsuite.test.configuration.JCA;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.page.config.DatasourcesPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.datasources.AddXADataSource;
import org.wildfly.extras.creaper.commands.datasources.RemoveXADataSource;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 24.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DatasourceXAPolicyTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration adminOps = new Administration(client);

    private static final String DATASOURCE_NAME = "DatasourceXAPolicyTestCase-DS";

    private final Address datasourceAddress = Address.subsystem("datasources").and("xa-data-source", DATASOURCE_NAME);
    private final ResourceVerifier verifier = new ResourceVerifier(datasourceAddress, client);
    private final ModelNodeGenerator nodeGenerator = new ModelNodeGenerator();

    @BeforeClass
    public static void setUpDatasource() throws CommandFailedException {
        client.apply(new AddXADataSource.Builder<>(DATASOURCE_NAME)
                .jndiName("java:/xa-datasources/" + DATASOURCE_NAME)
                .driverName("h2")
                .build());
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException, IOException, InterruptedException, TimeoutException {
        try {
            client.apply(new RemoveXADataSource(DATASOURCE_NAME));
            adminOps.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Drone
    private WebDriver browser;
    @Page
    private DatasourcesPage datasourcesPage;

    @Before
    public void before() {
        datasourcesPage.invokeViewXADatasource(DATASOURCE_NAME);
        datasourcesPage.getPoolConfig().edit();
    }

    @Test
    public void setDecrementerClass() throws Exception {
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        datasourcesPage.setDecrementerClass(decrementerClass);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute("capacity-decrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");
    }

    @Test
    public void setDecrementerProperty() throws Exception {
        final String propertyKey = "Watermark";
        final String propertyValue = "9";
        datasourcesPage.setDecrementerProperty(propertyKey, propertyValue);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute("capacity-decrementer-properties", expectedPropertiesNode);
    }

    @Test
    public void unsetDecrementerProperty() throws Exception {
        datasourcesPage.unsetDecrementerProperty();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined("capacity-decrementer-properties");
    }

    @Test
    public void unsetDecrementerClass() throws Exception {
        datasourcesPage.unsetDecrementerClass();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined("capacity-decrementer-class");
    }

    @Test
    public void setIncrementerClass() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        datasourcesPage.setIncrementerClass(incrementerClass);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute("capacity-incrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");
    }

    @Test
    public void setIncrementerProperty() throws Exception {
        final String propertyKey = "Size";
        final String propertyValue = "7";
        datasourcesPage.setIncrementerProperty(propertyKey, propertyValue);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute("capacity-incrementer-properties", expectedPropertiesNode);
    }

    @Test
    public void unsetIncrementerProperty() throws Exception {
        datasourcesPage.unsetIncrementerProperty();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined("capacity-incrementer-properties");
    }

    @Test
    public void unsetIncrementerClass() throws Exception {
        datasourcesPage.unsetIncrementerClass();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined("capacity-incrementer-class");
    }
}
