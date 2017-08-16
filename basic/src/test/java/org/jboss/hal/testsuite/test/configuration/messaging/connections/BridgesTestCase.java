package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.messaging.RemoveQueue;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class BridgesTestCase extends AbstractMessagingTestCase {

    private static final Logger log = LoggerFactory.getLogger(BridgesTestCase.class);

    private static final String BRIDGE = "bridge_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BRIDGE_TBA = "bridge-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BRIDGE_TBR = "bridge-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address BRIDGE_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", BRIDGE);
    private static final Address BRIDGE_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", BRIDGE_TBR);
    private static final Address BRIDGE_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", BRIDGE_TBA);
    private static final Address BRIDGE_TBA_2_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", "bridge-TBA_2_" + RandomStringUtils.randomAlphanumeric(5));
    private static final Address BRIDGE_TBA_3_ADDRESS = DEFAULT_MESSAGING_SERVER.and("bridge", "bridge-TBA_3_" + RandomStringUtils.randomAlphanumeric(5));

    private static final String
            CONNECTOR = "http-connector",
            QUEUE_CREATE_BRIDGE = "testQueue_" + RandomStringUtils.randomAlphanumeric(5),
            QUEUE_EDIT_BRIDGE = "testQueue_" + RandomStringUtils.randomAlphanumeric(5),
            DISCOVERY_GROUP_EDIT = "discoveryGroupBridges_" + RandomStringUtils.randomAlphanumeric(5),
            HAL1317_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1317",
            HAL1318_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1318",
            HAL1324_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1324",
            HAL1325_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1325";

    @BeforeClass
    public static void setUp() throws Exception {
        //add queues
        List<String> entriesQueueCreateBridge = Collections.singletonList(QUEUE_CREATE_BRIDGE);
        List<String> entriesQueueEditBridge = Collections.singletonList(QUEUE_EDIT_BRIDGE);
        client.apply(new AddQueue.Builder(QUEUE_CREATE_BRIDGE).jndiEntries(entriesQueueCreateBridge).build());
        client.apply(new AddQueue.Builder(QUEUE_EDIT_BRIDGE).jndiEntries(entriesQueueEditBridge).build());
        //add bridges
        createBridge(BRIDGE_ADDRESS);
        createBridge(BRIDGE_TBR_ADDRESS);

        administration.reloadIfRequired();
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.viewConnectionSettings("default");
        page.switchToBridges();
        page.selectInTable(BRIDGE);
    }
    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, CommandFailedException, TimeoutException, InterruptedException {
        //remove bridges
        operations.removeIfExists(BRIDGE_ADDRESS);
        operations.removeIfExists(BRIDGE_TBA_ADDRESS);
        operations.removeIfExists(BRIDGE_TBA_2_ADDRESS);
        operations.removeIfExists(BRIDGE_TBA_3_ADDRESS);
        operations.removeIfExists(BRIDGE_TBR_ADDRESS);
        //remove queues
        client.apply(new RemoveQueue(QUEUE_CREATE_BRIDGE));
        client.apply(new RemoveQueue(QUEUE_EDIT_BRIDGE));
    }

    @Test
    public void addBridgeWithDiscoveryGroup() throws Exception {
        page.addBridge()
                .name(BRIDGE_TBA)
                .queueName(QUEUE_CREATE_BRIDGE)
                .discoveryGroup("foobar")
                .saveAndDismissReloadRequiredWindow();
        new ResourceVerifier(BRIDGE_TBA_ADDRESS, client).verifyExists(HAL1317_FAIL_MESSAGE);
    }

    @Test
    public void addBridgeWithStaticConnectors() throws Exception {
        page.addBridge()
                .name(BRIDGE_TBA_2_ADDRESS.getLastPairValue())
                .queueName(QUEUE_CREATE_BRIDGE)
                .staticConnectors("foo", "bar", "qux", "qiz")
                .saveAndDismissReloadRequiredWindow();
        new ResourceVerifier(BRIDGE_TBA_2_ADDRESS, client).verifyExists(HAL1317_FAIL_MESSAGE);
    }

    @Test
    public void addBridgeWithDiscoveryGroupAndForwardAddressDefined() throws Exception {
        page.addBridge()
                .name(BRIDGE_TBA_3_ADDRESS.getLastPairValue())
                .queueName(QUEUE_CREATE_BRIDGE)
                .discoveryGroup("foobar")
                .forwardAddress("quz")
                .saveAndDismissReloadRequiredWindow();
        new ResourceVerifier(BRIDGE_TBA_3_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateBridgeFilter() throws Exception {
        editTextAndVerify(BRIDGE_ADDRESS, "filter", "testFilter");
    }

    @Test
    public void updateBridgeQueue() throws Exception {
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, "queue-name", QUEUE_EDIT_BRIDGE)
                .verifyFormSaved()
                .verifyAttribute("queue-name", QUEUE_EDIT_BRIDGE, HAL1324_FAIL_MESSAGE);
    }

    @Test
    public void updateBridgeForwardingAddress() throws Exception {
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, "forwarding-address", "127.0.0.1")
                .verifyFormSaved()
                .verifyAttribute("forwarding-address", "127.0.0.1", HAL1324_FAIL_MESSAGE);
    }

    @Test
    public void updateBridgeTransformerClass() throws Exception {
        try {
            new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, "transformer-class-name", "clazz")
                    .verifyFormSaved()
                    .verifyAttribute("transformer-class-name", "clazz", HAL1324_FAIL_MESSAGE);
        } finally {
            operations.undefineAttribute(BRIDGE_ADDRESS, "transformer-class-name");
        }

    }

    @Test
    public void updateBridgeDiscoveryGroup() throws Exception {
        try {
            new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, "static-connectors", "")
                    .edit(ConfigChecker.InputType.TEXT, "discovery-group", DISCOVERY_GROUP_EDIT)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute("discovery-group", DISCOVERY_GROUP_EDIT, HAL1324_FAIL_MESSAGE);
        } finally {
            operations.undefineAttribute(BRIDGE_ADDRESS, "discovery-group");
        }
    }

    @Test
    public void updateBridgePassword() throws Exception {
        page.switchToConnectionManagementTab();
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(BRIDGE_ADDRESS, "password");
        originalModelNodeResult.assertSuccess();
        try {
            editTextAndVerify(BRIDGE_ADDRESS, "password", "pwd1");
        } finally {
            operations.writeAttribute(BRIDGE_ADDRESS, "password", originalModelNodeResult.value());
        }
    }

    @Test
    public void updateBridgeRetryInterval() throws Exception {
        page.switchToConnectionManagementTab();
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, "retry-interval", 1L)
                .verifyFormSaved()
                .verifyAttribute("retry-interval", 1L, HAL1324_FAIL_MESSAGE);
    }

    @Test
    public void updateBridgeRetryIntervalWrongValue() {
        page.switchToConnectionManagementTab();
        verifyIfErrorAppears("retry-interval", "-10");
    }

    @Test
    public void updateBridgeReconnectAttempts() throws Exception {
        page.switchToConnectionManagementTab();
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, "reconnect-attempts", -1)
                .verifyFormSaved(HAL1325_FAIL_MESSAGE)
                .verifyAttribute("reconnect-attempts", -1);
    }

    @Category(Elytron.class)
    @Test
    public void setCredentialReferenceToClearText() throws Exception {
        page.switchToCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .build()
                .setClearTextCredentialReferenceAndVerify(HAL1318_FAIL_MESSAGE);
    }

    @Category(Elytron.class)
    @Test
    public void setCredentialReferenceToCredentialStore() throws Exception {
        page.switchToCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .build()
                .setCredentialStoreCredentialReferenceAndVerify(HAL1318_FAIL_MESSAGE);
    }

    @Category(Elytron.class)
    @Test
    public void testIllegalCombinationsForCredentialReference() throws Exception {
        page.switchToCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .build()
                .testIllegalCombinationCredentialReferenceAttributes();
    }

    @Test
    public void removeBridge() throws Exception {
        page.remove(BRIDGE_TBR);
        new ResourceVerifier(BRIDGE_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private static void createBridge(Address address) throws Exception {
        operations.add(address, Values.empty()
                .andList("static-connectors", CONNECTOR)
                .and("queue-name", QUEUE_CREATE_BRIDGE)).assertSuccess();
    }

}
