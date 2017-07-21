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
public class WebServicesClientConfigurationTestCase extends WebServicesTestCaseAbstract {

    @AfterClass
    public static void afterClass() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void testAddClientConfiguration() throws Exception {
        final String clientConfigurationName = "my_client_config_" + RandomStringUtils.randomAlphanumeric(7);
        final Address clientConfigAddress = WEBSERVICES_ADDRESS.and(CLIENT_CONFIGURATION, clientConfigurationName);
        try {
            page.navigate();
            page.switchToClientConfiguration();
            page.getResourceManager()
                    .addResource(WebServicesConfigurationWizard.class)
                    .name(clientConfigurationName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(clientConfigAddress, client).verifyExists();
        } finally {
            operations.removeIfExists(clientConfigAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddClientConfigurationWithoutNameShowsError() throws Exception {
        page.navigate();
        page.switchToClientConfiguration();
        page.getResourceManager()
                .addResource(WebServicesConfigurationWizard.class)
                .saveWithState().assertWindowOpen();
        Assert.assertTrue("Validation error regarding empty client configuration name should be shown", page.getWindowFragment().isErrorShownInForm());
    }

    @Test
    public void testAddClientConfigurationDuplicateShowsError() throws Exception {
        final String clientConfigurationName = "client_config_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(CLIENT_CONFIGURATION, clientConfigurationName);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToClientConfiguration();
            page.createConfigurationInUI(clientConfigurationName);
            Assert.assertNotNull("An alert regarding duplicate resource should be shown", page.getAlertArea());
        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddClientConfigurationProperty() throws Exception {
        final String clientConfigurationName = "endpoint_config_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(CLIENT_CONFIGURATION, clientConfigurationName);
        final String propertyKey = RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = "value_" + RandomStringUtils.randomAlphanumeric(5);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToClientConfiguration();
            page.getResourceManager().selectByName(clientConfigurationName);
            page.switchToProperties();
            page.getConfig()
                    .getResourceManager()
                    .addResource(WebServicesConfigurationPropertyWizard.class)
                    .key(propertyKey)
                    .value(propertyValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(createdAddress.and(PROPERTY, propertyKey), client)
                    .verifyExists()
                    .verifyAttribute("value", propertyValue);

        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddClientConfigurationPropertyDuplicateKeyShowsError() throws Exception {
        final String clientConfigurationName = "my_client_configuration_with_properties_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(CLIENT_CONFIGURATION, clientConfigurationName);
        final String propertyKey = RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = "value_" + RandomStringUtils.randomAlphanumeric(5);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            operations.add(createdAddress.and(PROPERTY, propertyKey), Values.of("value", propertyValue)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToClientConfiguration();
            page.getResourceManager().selectByName(clientConfigurationName);
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
    public void testAddClientConfigurationPropertyWithoutKeyShowsError() throws Exception {
        final String clientConfigurationName = "client_config_" + RandomStringUtils.randomAlphanumeric(7);
        final Address createdAddress = WEBSERVICES_ADDRESS.and(CLIENT_CONFIGURATION, clientConfigurationName);
        try {
            operations.add(createdAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToClientConfiguration();
            page.getResourceManager().selectByName(clientConfigurationName);
            page.switchToProperties();
            page.getConfig()
                    .getResourceManager()
                    .addResource(WebServicesConfigurationPropertyWizard.class)
                    .saveWithState().assertWindowOpen();
            Assert.assertTrue("Validation error regarding empty property name should be visible", page.getWindowFragment().isErrorShownInForm());

        } finally {
            operations.removeIfExists(createdAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testRemoveClientConfiguration() throws Exception {
        final String clientConfigurationName = "my_client_configuration_" + RandomStringUtils.randomAlphanumeric(7);
        final Address clientConfigurationAddress = WEBSERVICES_ADDRESS.and(CLIENT_CONFIGURATION, clientConfigurationName);
        try {
            operations.add(clientConfigurationAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToClientConfiguration();
            ResourceManager clientConfigurationsManager = page.getResourceManager();
            clientConfigurationsManager
                    .removeResource(clientConfigurationName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(clientConfigurationAddress, client).verifyDoesNotExist();
            Assert.assertFalse(clientConfigurationsManager.isResourcePresent(clientConfigurationName));

        } finally {
            operations.removeIfExists(clientConfigurationAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testRemoveClientConfigurationProperty() throws Exception {
        final String clientConfigurationName = "my_client_configuration_with_properties_" + RandomStringUtils.randomAlphanumeric(7);
        final Address clientConfigurationAddress = WEBSERVICES_ADDRESS.and(CLIENT_CONFIGURATION, clientConfigurationName);
        final String propertyKey = RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = "value_" + RandomStringUtils.randomAlphanumeric(5);
        try {
            operations.add(clientConfigurationAddress).assertSuccess();
            administration.reloadIfRequired();
            operations.add(clientConfigurationAddress.and(PROPERTY, propertyKey), Values.of("value", propertyValue)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToClientConfiguration();
            page.getResourceManager().selectByName(clientConfigurationName);
            page.switchToProperties();
            ResourceManager propertiesManager = page.getConfig().getResourceManager();
            propertiesManager
                    .removeResource(propertyKey)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(clientConfigurationAddress.and(PROPERTY, propertyKey), client).verifyDoesNotExist();
            Assert.assertFalse(propertiesManager.isResourcePresent(propertyKey));
        } finally {
            operations.removeIfExists(clientConfigurationAddress);
            administration.reloadIfRequired();
        }
    }
}
