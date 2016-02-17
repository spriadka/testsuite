package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.messaging.RemoveQueue;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class BridgesTestCase extends AbstractMessagingTestCase {

    private static final String BRIDGE = "bridge_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BRIDGE_TBA = "bridge-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BRIDGE_TBR = "bridge-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address BRIDGE_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", BRIDGE);
    private static final Address BRIDGE_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", BRIDGE_TBR);
    private static final Address BRIDGE_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", BRIDGE_TBA);

    private static final String CONNECTOR = "http-connector";
    private static final String QUEUE = "testQueue_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String QUEUE_EDIT = "testQueue_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String DISCOVERY_GROUP_EDIT = "discoveryGroupBridges_" + RandomStringUtils.randomAlphanumeric(5);

    @BeforeClass
    public static void setUp() throws Exception {
        List<String> jndiEntries = Arrays.asList(QUEUE);
        client.apply(new AddQueue.Builder(QUEUE).jndiEntries(jndiEntries).build());
        client.apply(new AddQueue.Builder(QUEUE_EDIT).jndiEntries(jndiEntries).build());
        createBridge(BRIDGE_ADDRESS);
        createBridge(BRIDGE_TBR_ADDRESS);
        operations.add(DEFAULT_MESSAGING_SERVER.and("discovery-group", DISCOVERY_GROUP_EDIT));
        administration.reloadIfRequired();
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectConnectionsView();
        page.switchToBridges();
        page.selectInTable(BRIDGE);
    }
    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, CommandFailedException {
        operations.removeIfExists(BRIDGE_ADDRESS);
        operations.removeIfExists(BRIDGE_TBA_ADDRESS);
        operations.removeIfExists(BRIDGE_TBR_ADDRESS);
        client.apply(new RemoveQueue(QUEUE));
        client.apply(new RemoveQueue(QUEUE_EDIT));
        operations.remove(DEFAULT_MESSAGING_SERVER.and("discovery-group", DISCOVERY_GROUP_EDIT));
    }

    @Test
    public void addBridge() throws Exception {
        page.addBridge(BRIDGE_TBA, QUEUE, "testAddress", CONNECTOR);
        new ResourceVerifier(BRIDGE_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateBridgeFilter() throws Exception {
        editTextAndVerify(BRIDGE_ADDRESS, "filter", "testFilter");
    }

    @Test
    public void updateBridgeQueue() throws Exception {
        editTextAndVerify(BRIDGE_ADDRESS, "queueName", "queue-name", QUEUE_EDIT);
    }

    @Test
    public void updateBridgeForwardingAddress() throws Exception {
        editTextAndVerify(BRIDGE_ADDRESS, "forwardingAddress", "forwarding-address", "127.0.0.1");
    }

    @Test
    public void updateBridgeTransformerClass() throws Exception {
        BackupAndRestoreAttributes backup = new BackupAndRestoreAttributes.Builder(BRIDGE_ADDRESS)
                .excluded("discovery-group")
                .build();
        try {
            client.apply(backup.backup());
            editTextAndVerify(BRIDGE_ADDRESS, "transformerClass", "transformer-class-name",
                    "org.apache.activemq.artemis.jms.example.HatColourChangeTransformer");
        } finally {
            client.apply(backup.restore());
        }

    }

    @Test
    public void updateBridgeDiscoveryGroup() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.edit();

        editor.text("staticConnectors", "");
        editor.text("discoveryGroup", DISCOVERY_GROUP_EDIT);
        boolean finished = editPanelFragment.save();

        Assert.assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(BRIDGE_ADDRESS, client).verifyAttribute("discovery-group", DISCOVERY_GROUP_EDIT);
    }

    @Test
    public void updateBridgePassword() throws Exception {
        page.switchToConnectionManagementTab();
        editTextAndVerify(BRIDGE_ADDRESS, "password", "pwd1");
    }

    @Test
    public void updateBridgeRetryInterval() throws Exception {
        page.switchToConnectionManagementTab();
        editTextAndVerify(BRIDGE_ADDRESS, "retryInterval", "retry-interval", 1L);
    }

    @Test
    public void updateBridgeRetryIntervalWrongValue() {
        page.switchToConnectionManagementTab();
        verifyIfErrorAppears("retryInterval", "-10");
    }

    @Test
    public void updateBridgeReconnectAttempts() throws Exception {
        page.switchToConnectionManagementTab();
        editTextAndVerify(BRIDGE_ADDRESS, "reconnectAttempts", "reconnect-attempts", -1);
    }

    @Test
    public void removeBridge() throws Exception {
        page.remove(BRIDGE_TBR);
        new ResourceVerifier(BRIDGE_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private static void createBridge(Address address) throws Exception {
        operations.add(address, Values.empty()
                .andList("static-connectors", CONNECTOR)
                .and("queue-name", QUEUE));
        new ResourceVerifier(address, client).verifyExists();
    }

}
