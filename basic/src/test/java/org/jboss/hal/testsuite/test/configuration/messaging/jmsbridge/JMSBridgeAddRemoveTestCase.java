package org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.AddJMSBridge;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.messaging.AddJMSBridgeDialogFragment;
import org.jboss.hal.testsuite.page.config.JMSBridgePage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.messaging.RemoveQueue;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.Collections;

import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.FAILURE_RETRY_INTERVAL_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.MAX_BATCH_SIZE_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.MAX_BATCH_TIME_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.MAX_RETRIES_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.QUALITY_OF_SERVICE_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SOURCE_CONNECTION_FACTORY_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SOURCE_DESTINATION_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.TARGET_CONNECTION_FACTORY_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.TARGET_DESTINATION_IDENTIFIER;

/**
 * Tests which verifies if it is possible to add and remove JMS bridge in Web Console UI correctly
 *
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9/19/16.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class JMSBridgeAddRemoveTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String
            IN_QUEUE_NAME = "InQueue",
            IN_QUEUE_JNDI = "java:/jms/queue/" + IN_QUEUE_NAME,
            OUT_QUEUE_NAME = "OutQueue",
            OUT_QUEUE_JNDI = "java:/jms/queue/" + OUT_QUEUE_NAME,
            LOCAL_CONNECTION_FACTORY = "java:/ConnectionFactory";

    private static final Address MESSAGING_SUBSYSTEM_ADDRESS = Address.subsystem("messaging-activemq");

    @AfterClass
    public static void afterClass() throws IOException {
        client.close();
    }

    @Drone
    private WebDriver browser;

    @Page
    private JMSBridgePage page;

    @Test
    public void addJMSBridge() throws Exception {
        //constants
        final String NAME = RandomStringUtils.randomAlphanumeric(5);
        final Address JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS.and("jms-bridge", NAME);
        final int MAX_BATCH_SIZE = 1000;
        final int MAX_RETRIES = 42;
        final long MAX_BATCH_TIME = 1000;
        final long FAILURE_RETRY_INTERVAL = 4242;
        final AddJMSBridge.QualityOfService QUALITY_OF_SERVICE = AddJMSBridge.QualityOfService.DUPLICATES_OK;

        try {
            client.apply(new AddQueue.Builder(IN_QUEUE_NAME).jndiEntries(Collections.singletonList(IN_QUEUE_JNDI)).build());

            client.apply(new AddQueue.Builder(OUT_QUEUE_NAME).jndiEntries(Collections.singletonList(OUT_QUEUE_JNDI)).build());

            page.navigate();

            page.clickButton("Add");

            AddJMSBridgeDialogFragment dialog = Console.withBrowser(browser).openedWindow(AddJMSBridgeDialogFragment.class);

            dialog.name(NAME)
                    .failureRetryInterval(FAILURE_RETRY_INTERVAL)
                    .maxBatchSize(MAX_BATCH_SIZE)
                    .maxBatchTime(MAX_BATCH_TIME)
                    .maxRetries(MAX_RETRIES)
                    .qualityOfService(QUALITY_OF_SERVICE)
                    .sourceDestination(IN_QUEUE_JNDI)
                    .sourceConnectionFactory(LOCAL_CONNECTION_FACTORY)
                    .targetDestination(OUT_QUEUE_JNDI)
                    .targetConnectionFactory(LOCAL_CONNECTION_FACTORY)
                    .clickSave();

            new ResourceVerifier(JMS_BRIDGE_ADDRESS, client)
                    .verifyAttribute(FAILURE_RETRY_INTERVAL_IDENTIFIER, FAILURE_RETRY_INTERVAL)
                    .verifyAttribute(MAX_BATCH_SIZE_IDENTIFIER, MAX_BATCH_SIZE)
                    .verifyAttribute(MAX_BATCH_TIME_IDENTIFIER, MAX_BATCH_TIME)
                    .verifyAttribute(MAX_RETRIES_IDENTIFIER, MAX_RETRIES)
                    .verifyAttribute(QUALITY_OF_SERVICE_IDENTIFIER, QUALITY_OF_SERVICE.getStringValue())
                    .verifyAttribute(SOURCE_DESTINATION_IDENTIFIER, IN_QUEUE_JNDI)
                    .verifyAttribute(SOURCE_CONNECTION_FACTORY_IDENTIFIER, LOCAL_CONNECTION_FACTORY)
                    .verifyAttribute(TARGET_DESTINATION_IDENTIFIER, OUT_QUEUE_JNDI)
                    .verifyAttribute(TARGET_CONNECTION_FACTORY_IDENTIFIER, LOCAL_CONNECTION_FACTORY);
        } finally {
            operations.removeIfExists(JMS_BRIDGE_ADDRESS);
            client.apply(new RemoveQueue(IN_QUEUE_NAME));
            client.apply(new RemoveQueue(OUT_QUEUE_NAME));
            administration.reloadIfRequired();
        }
    }

    @Test
    public void removeJMSBridge() throws Exception {
        final String JMS_BRIDGE_NAME = "JMSBridgeTBR_" + RandomStringUtils.randomAlphanumeric(5);
        final Address JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS.and("jms-bridge", JMS_BRIDGE_NAME);

        try {
            client.apply(new AddQueue.Builder(IN_QUEUE_NAME).jndiEntries(Collections.singletonList(IN_QUEUE_JNDI)).build());

            client.apply(new AddQueue.Builder(OUT_QUEUE_NAME).jndiEntries(Collections.singletonList(OUT_QUEUE_JNDI)).build());

            client.apply(new AddJMSBridge.Builder(JMS_BRIDGE_NAME)
                    .failureRetryInterval(1000)
                    .maxBatchSize(1000)
                    .maxBatchTime(1000)
                    .maxRetries(1)
                    .qualityOfService(AddJMSBridge.QualityOfService.AT_MOST_ONCE)
                    .sourceConnectionFactory(LOCAL_CONNECTION_FACTORY)
                    .sourceDestination(IN_QUEUE_JNDI)
                    .targetConnectionFactory(LOCAL_CONNECTION_FACTORY)
                    .targetDestination(OUT_QUEUE_JNDI)
                    .replaceExisting()
                    .build()
            );

            new ResourceVerifier(JMS_BRIDGE_ADDRESS, client).verifyExists();

            page.navigate();

            page.removeJMSBridge(JMS_BRIDGE_NAME);

        } finally {
            operations.removeIfExists(JMS_BRIDGE_ADDRESS);
            client.apply(new RemoveQueue(IN_QUEUE_NAME));
            client.apply(new RemoveQueue(OUT_QUEUE_NAME));
            administration.reloadIfRequired();
        }
    }
}
