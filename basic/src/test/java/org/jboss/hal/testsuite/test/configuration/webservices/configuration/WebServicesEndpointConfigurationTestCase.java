package org.jboss.hal.testsuite.test.configuration.webservices.configuration;


import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.webservices.WebServicesConfigurationPropertyWizard;
import org.jboss.hal.testsuite.fragment.config.webservices.WebServicesConfigurationWizard;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.test.configuration.webservices.WebServicesTestCaseAbstract;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class WebServicesEndpointConfigurationTestCase extends WebServicesTestCaseAbstract {

    @AfterClass
    public static void afterClass() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }


    @Test
    public void testAddEndpointConfiguration() throws Exception {
        final String endpointConfigurationName = "my_endpoint_configuration_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);
        try {
            page.navigate();
            page.switchToEndpointConfiguration();
            page.createConfigurationInUI(endpointConfigurationName);
            administration.reloadIfRequired();
            new ResourceVerifier(createdAddress, client).verifyExists();
        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }


    @Test
    public void testAddEndpointConfigurationWithoutNameShowsError() {
        page.navigate();
        page.switchToEndpointConfiguration();
        page.getResourceManager().addResource(WebServicesConfigurationWizard.class)
                .saveWithState().assertWindowOpen();
        Assert.assertTrue("An validation error regarding missing name should be visible", page.getWindowFragment().isErrorShownInForm());
    }

    @Test
    public void testAddEndpointConfigurationDuplicateShowsError() throws Exception {
        final String endpointConfigurationName = "endpoint_config_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfiguration();
            page.createConfigurationInUI(endpointConfigurationName);
            Assert.assertNotNull("An alert regarding duplicate resource should be shown", page.getAlertArea());
        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddEndpointConfigurationProperty() throws Exception {
        final String endpointConfigurationName = "my_endpoint_configuration_with_properties_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);
        final String propertyKey = RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = "value_" + RandomStringUtils.randomAlphanumeric(5);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfiguration();
            page.switchToProperties();
            page.getResourceManager().selectByName(endpointConfigurationName);
            page.getConfig()
                    .getResourceManager()
                    .addResource(WebServicesConfigurationPropertyWizard.class)
                    .key(propertyKey)
                    .value(propertyValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(createdAddress.and(PROPERTY, propertyKey), client).verifyAttribute("value", propertyValue);
        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddEndpointConfigurationPropertyDuplicateKey() throws Exception {
        final String endpointConfigurationName = "my_endpoint_configuration_with_properties_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);
        final String propertyKey = RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = "value_" + RandomStringUtils.randomAlphanumeric(5);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            operations.add(createdAddress.and(PROPERTY, propertyKey), Values.of("value", propertyValue)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfiguration();
            page.getResourceManager().selectByName(endpointConfigurationName);
            page.switchToProperties();
            page.getConfig()
                    .getResourceManager()
                    .addResource(WebServicesConfigurationPropertyWizard.class)
                    .key(propertyKey)
                    .value(propertyValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertNotNull("Error regarding duplicate property key should be shown", page.getAlertArea());
        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddEndpointConfigurationPropertyWithoutNameShowsError() {
        page.navigate();
        page.switchToEndpointConfiguration();
        page.switchToProperties();
        page.getConfig().getResourceManager()
                .addResource(WebServicesConfigurationPropertyWizard.class)
                .save();
        Assert.assertTrue("Validation error regarding empty property name should be visible", page.getWindowFragment().isErrorShownInForm());
    }

    @Test
    public void testRemoveEndpointConfiguration() throws Exception {
        final String endpointConfigurationName = "my_endpoint_configuration_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfiguration();
            ResourceManager endpointConfigurationsManager = page.getResourceManager();
            endpointConfigurationsManager
                    .removeResource(endpointConfigurationName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(createdAddress, client).verifyDoesNotExist();
            Assert.assertFalse(endpointConfigurationsManager.isResourcePresent(endpointConfigurationName));

        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testRemoveEndpointConfigurationProperty() throws Exception {
        final String endpointConfigurationName = "my_endpoint_configuration_with_properties_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);
        final String propertyKey = RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = "value_" + RandomStringUtils.randomAlphanumeric(5);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            operations.add(createdAddress.and(PROPERTY, propertyKey), Values.of("value", propertyValue)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfiguration();
            page.getResourceManager().selectByName(endpointConfigurationName);
            page.switchToProperties();
            ResourceManager propertiesManager = page.getConfig().getResourceManager();
            propertiesManager
                    .removeResource(propertyKey)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(createdAddress.and(PROPERTY, propertyKey), client).verifyDoesNotExist();
            Assert.assertFalse(propertiesManager.isResourcePresent(propertyKey));
        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }
}
