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

    private static final Address datasourceAddress = Address.subsystem("datasources").and("xa-data-source", DATASOURCE_NAME);
    private final ResourceVerifier verifier = new ResourceVerifier(datasourceAddress, client);
    private final ModelNodeGenerator nodeGenerator = new ModelNodeGenerator();

    private static final PoolOperations poolOperations = new PoolOperations(client, datasourceAddress);

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

    private void navigateToDatasource() {
        datasourcesPage.invokeViewXADatasource(DATASOURCE_NAME);
        datasourcesPage.getPoolConfig().edit();
    }

    @Test
    public void setDecrementerClass() throws Exception {
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        navigateToDatasource();
        datasourcesPage.setDecrementerClass(decrementerClass);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_DECREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");
    }

    @Test
    public void setDecrementerProperty() throws Exception {
        final String propertyKey = "Watermark";
        final String propertyValue = "9";
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        poolOperations.setCapacityDecrementerClassInModel(decrementerClass);
        navigateToDatasource();
        datasourcesPage.setDecrementerProperty(propertyKey, propertyValue);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_DECREMENTER_PROPERTIES, expectedPropertiesNode);
    }

    @Test
    public void unsetDecrementerProperty() throws Exception {
        final String propertyKey = "Watermark";
        final String propertyValue = "9";
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        poolOperations.setCapacityDecrementerClassInModel(decrementerClass);
        poolOperations.setCapacityDecrementerPropertyInModel(propertyKey, propertyValue);
        navigateToDatasource();
        datasourcesPage.unsetDecrementerProperty();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_DECREMENTER_PROPERTIES);
    }

    @Test
    public void unsetDecrementerClass() throws Exception {
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        poolOperations.setCapacityDecrementerClassInModel(decrementerClass);
        navigateToDatasource();
        datasourcesPage.unsetDecrementerClass();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_DECREMENTER_CLASS);
    }

    @Test
    public void setIncrementerClass() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        navigateToDatasource();
        datasourcesPage.setIncrementerClass(incrementerClass);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_INCREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");
    }

    @Test
    public void setIncrementerProperty() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        final String propertyKey = "Size";
        final String propertyValue = "7";
        poolOperations.setCapacityIncrementerClassInModel(incrementerClass);
        navigateToDatasource();
        datasourcesPage.setIncrementerProperty(propertyKey, propertyValue);
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_INCREMENTER_PROPERTIES, expectedPropertiesNode);
    }

    @Test
    public void unsetIncrementerProperty() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        final String propertyKey = "Size";
        final String propertyValue = "7";
        poolOperations.setCapacityIncrementerClassInModel(incrementerClass);
        poolOperations.setCapacityIncrementerPropertyInModel(propertyKey, propertyValue);
        navigateToDatasource();
        datasourcesPage.unsetIncrementerProperty();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_INCREMENTER_PROPERTIES);
    }

    @Test
    public void unsetIncrementerClass() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        poolOperations.setCapacityIncrementerClassInModel(incrementerClass);
        navigateToDatasource();
        datasourcesPage.unsetIncrementerClass();
        boolean finished = datasourcesPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_INCREMENTER_CLASS);
    }
}
