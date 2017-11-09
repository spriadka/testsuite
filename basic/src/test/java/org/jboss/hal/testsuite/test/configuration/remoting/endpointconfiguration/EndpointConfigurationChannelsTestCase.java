package org.jboss.hal.testsuite.test.configuration.remoting.endpointconfiguration;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;


@RunWith(Arquillian.class)
@RunAsClient
public class EndpointConfigurationChannelsTestCase extends EndpointConfigurationTestCaseAbstract {

    private final String MAX_INBOUND_CHANNELS = "max-inbound-channels",
            MAX_INBOUND_MESSAGE_SIZE = "max-inbound-message-size",
            MAX_INBOUND_MESSAGES = "max-inbound-messages",
            MAX_OUTBOUND_CHANNELS = "max-outbound-channels",
            MAX_OUTBOUND_MESSAGE_SIZE = "max-outbound-message-size",
            MAX_OUTBOUND_MESSAGES = "max-outbound-messages";


    @Before
    public void switchToTab() {
        page.switchToEndpointChannels();
    }

    @Test
    public void editMaxInboundChannels() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_CHANNELS);
        try {
            enterTextAndVerify(MAX_INBOUND_CHANNELS, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_CHANNELS, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxInboundChannelsInvalidValue() throws Exception {
        final String invalidMaxInboundChannels = "invalid_max_inbound_channels";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(MAX_INBOUND_CHANNELS, invalidMaxInboundChannels);
        Assert.assertTrue("Validation error should be shown", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editMaxInboundMessageSize() throws Exception {
        final long value = 1024L;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_MESSAGE_SIZE);
        originalModelNodeResult.assertSuccess();
        try {
            enterTextAndVerify(MAX_INBOUND_MESSAGE_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_MESSAGE_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxInboundMessageSizeInvalidValue() throws Exception {
        final String invalidMaxInboundMessageSize = "invalid_max_inbound_message_size";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(MAX_INBOUND_MESSAGE_SIZE, invalidMaxInboundMessageSize);
        Assert.assertTrue("Validation error should be shown", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editMaxInboundMessages() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_MESSAGES);
        originalModelNodeResult.assertSuccess();
        try {
            enterTextAndVerify(MAX_INBOUND_MESSAGES, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_INBOUND_MESSAGES, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxInboundMessagesInvalidValue() throws Exception {
        final String invalidMaxInboundMessages = "invalid_max_inbound_messages";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(MAX_INBOUND_MESSAGES, invalidMaxInboundMessages);
        Assert.assertTrue("Validation error should be shown", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editMaxOutboundChannels() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_CHANNELS);
        originalModelNodeResult.assertSuccess();
        try {
            enterTextAndVerify(MAX_OUTBOUND_CHANNELS, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_CHANNELS, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxOutboundChannelsInvalidValue() throws Exception {
        final String invalidMaxOutboundChannels = "invalid_max_outbound_channels";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(MAX_OUTBOUND_CHANNELS, invalidMaxOutboundChannels);
        Assert.assertTrue("Validation error should be shown", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editMaxOutboundMessageSize() throws Exception {
        final long value = 1024L;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_MESSAGE_SIZE);
        originalModelNodeResult.assertSuccess();
        try {
            enterTextAndVerify(MAX_OUTBOUND_MESSAGE_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_MESSAGE_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxOutboundMessageSizeInvalidValue() throws Exception {
        final String invalidMaxOutboundMessageSize = "invalid_max_outbound_message_size";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(MAX_OUTBOUND_MESSAGE_SIZE, invalidMaxOutboundMessageSize);
        Assert.assertTrue("Validation error should be shown", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editMaxOutboundMessages() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_MESSAGES);
        originalModelNodeResult.assertSuccess();
        try {
            enterTextAndVerify(MAX_OUTBOUND_MESSAGES, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, MAX_OUTBOUND_MESSAGES, originalModelNodeResult.value());
        }
    }

    @Test
    public void editMaxOutboundMessagesInvalidValue() throws Exception {
        final String invalidMaxOutboundMessages = "invalid_max_outbound_messages";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(MAX_OUTBOUND_MESSAGES, invalidMaxOutboundMessages);
        Assert.assertTrue("Validation error should be shown", config.isErrorShownInForm());
        config.cancel();
    }

}
