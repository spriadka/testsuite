package org.jboss.hal.testsuite.test.configuration.remoting;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.RemotingSubsystemPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Testing settings of Remoting Endpoint
 * TODO: Expand tests
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class EndpointConfigurationTestCase {

    @Page
    private RemotingSubsystemPage page;

    @Drone
    private WebDriver browser;

    private static final String
            BUFFER_REGION_SIZE = "buffer-region-size",
            SEND_BUFFER_SIZE = "send-buffer-size",
            AUTHENTICATION_RETRIES = "authentication-retries",
            MAX_INBOUND_CHANNELS = "max-inbound-channels",
            MAX_OUTBOUND_CHANNELS = "max-outbound-channels",
            SERVER_NAME = "server-name",
            SASL_PROTOCOL = "sasl-protocol";

    private static final Address
            REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting"),
            ENDPOINT_CONFIGURATION_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("configuration", "endpoint");

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    @Before
    public void before() {
        page.navigate();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void editBufferRegionSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, BUFFER_REGION_SIZE);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, ENDPOINT_CONFIGURATION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, BUFFER_REGION_SIZE, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(BUFFER_REGION_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, BUFFER_REGION_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editSendBufferSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SEND_BUFFER_SIZE);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, ENDPOINT_CONFIGURATION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SEND_BUFFER_SIZE, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(SEND_BUFFER_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SEND_BUFFER_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editServerName() throws Exception {
        final String value = "random_server_name_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SERVER_NAME);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, ENDPOINT_CONFIGURATION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SERVER_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(SERVER_NAME, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SERVER_NAME, originalModelNodeResult.value());
        }
    }

    @Test
    public void editAuthenticationRetries() throws Exception {
        page.switchToEndpointSecurity();
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTHENTICATION_RETRIES);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, ENDPOINT_CONFIGURATION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_RETRIES, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_RETRIES, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTHENTICATION_RETRIES, originalModelNodeResult.value());
        }
    }

    @Test
    public void editSASLProtocol() throws Exception {
        page.switchToEndpointSecurity();
        final String value = "random_sasl_protocol_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SASL_PROTOCOL);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, ENDPOINT_CONFIGURATION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_PROTOCOL, value)
                    .verifyFormSaved()
                    .verifyAttribute(SASL_PROTOCOL, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SASL_PROTOCOL, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxInboundChannels() throws Exception {
        page.switchToEndpointChannels();
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_CHANNELS);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, ENDPOINT_CONFIGURATION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MAX_INBOUND_CHANNELS, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(MAX_INBOUND_CHANNELS, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_CHANNELS, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxOutboundChannels() throws Exception {
        page.switchToEndpointChannels();
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_CHANNELS);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, ENDPOINT_CONFIGURATION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MAX_OUTBOUND_CHANNELS, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(MAX_OUTBOUND_CHANNELS, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_CHANNELS, originalModelNodeResult.value());
        }
    }
}