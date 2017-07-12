package org.jboss.hal.testsuite.test.configuration.remoting.endpointconfiguration;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.fragment.AlertFragment;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;


@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class EndpointConfigurationAttributesTestCase extends EndpointConfigurationTestCaseAbstract {

    private static final String
            BUFFER_REGION_SIZE = "buffer-region-size",
            HEARTBEAT_INTERVAL = "heartbeat-interval",
            RECEIVE_BUFFER_SIZE = "receive-buffer-size",
            RECEIVE_WINDOW_SIZE = "receive-window-size",
            SEND_BUFFER_SIZE = "send-buffer-size",
            SERVER_NAME = "server-name",
            TRANSMIT_WINDOW_SIZE = "transmit-window-size",
            WORKER = "worker";


    @Test
    public void editBufferRegionSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, BUFFER_REGION_SIZE);
        try {
            enterTextAndVerify(BUFFER_REGION_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, BUFFER_REGION_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editBufferRegionSizeInvalidValue() throws Exception {
        final String invalidBufferRegionSize = "invalid_buffer_region_size";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(BUFFER_REGION_SIZE, invalidBufferRegionSize);
        Assert.assertTrue("There should be validation error", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editHeartbeatInterval() throws Exception {
        ModelNodeResult originalHeartbeatIntervalNode = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, HEARTBEAT_INTERVAL);
        final int value = 2648;
        try {
            enterTextAndVerify(HEARTBEAT_INTERVAL, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, HEARTBEAT_INTERVAL, originalHeartbeatIntervalNode.value());
        }
    }

    @Test
    public void editHeartbeatIntervalInvalidValue() throws Exception {
        final String invalidHeartbeat = "invalid_heartbeat";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(HEARTBEAT_INTERVAL, invalidHeartbeat);
        Assert.assertTrue("There should be validation error", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editReceiveBufferSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, RECEIVE_BUFFER_SIZE);
        try {
            enterTextAndVerify(RECEIVE_BUFFER_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, RECEIVE_BUFFER_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editReceiveBufferSizeInvalidValue() throws Exception {
        final String invalidReceiveBufferSize = "invalid_receive_buffer_size";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(RECEIVE_BUFFER_SIZE, invalidReceiveBufferSize);
        Assert.assertTrue("There should be validation error", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editReceiveWindowSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, RECEIVE_WINDOW_SIZE);
        try {
            enterTextAndVerify(RECEIVE_WINDOW_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, RECEIVE_WINDOW_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editReceiveWindowSizeInvalidValue() throws Exception {
        final String invalidReceiveWindowSize = "invalid_receive_window_size";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(RECEIVE_WINDOW_SIZE, invalidReceiveWindowSize);
        Assert.assertTrue("There should be validation error", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editSendBufferSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SEND_BUFFER_SIZE);
        try {
            enterTextAndVerify(SEND_BUFFER_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SEND_BUFFER_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editSendBufferSizeInvalidValue() throws Exception {
        final String invalidSendBufferSize = "invalid_send_buffer_size";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(SEND_BUFFER_SIZE, invalidSendBufferSize);
        Assert.assertTrue("There should be validation error", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editServerName() throws Exception {
        final String value = "random_server_name_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SERVER_NAME);
        try {
            enterTextAndVerify(SERVER_NAME, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SERVER_NAME, originalModelNodeResult.value());
        }
    }

    @Test
    public void editTransmitWindowSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, TRANSMIT_WINDOW_SIZE);
        try {
            enterTextAndVerify(TRANSMIT_WINDOW_SIZE, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, TRANSMIT_WINDOW_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void editTransmitWindowSizeInvalidValue() throws Exception {
        final String invalidTransmitWindowSize = "invalid_transmit_window_size";
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(TRANSMIT_WINDOW_SIZE, invalidTransmitWindowSize);
        Assert.assertTrue("There should be validation error", config.isErrorShownInForm());
        config.cancel();
    }

    @Test
    public void editWorkerShowsAlert() throws Exception {
        final String value = "my_worker_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, WORKER);
        try {
            ConfigFragment configFragment = page.getConfigFragment();
            configFragment.editTextAndSave(WORKER, value);
            AlertFragment alert = page.getAlertArea();
            Assert.assertNotNull(alert);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, WORKER, originalModelNodeResult.value());
        }
    }
}