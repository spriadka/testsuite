package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
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

    private static final String CONNECTOR = "http-connector";
    private static final String QUEUE_CREATE_BRIDGE = "testQueue_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String QUEUE_EDIT_BRIDGE = "testQueue_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String DISCOVERY_GROUP_EDIT = "discoveryGroupBridges_" + RandomStringUtils.randomAlphanumeric(5);

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
        new ResourceVerifier(BRIDGE_TBA_ADDRESS, client).verifyExists("Probably fails because of https://issues.jboss.org/browse/HAL-1317");
    }

    @Test
    public void addBridgeWithStaticConnectors() throws Exception {
        page.addBridge()
                .name(BRIDGE_TBA_2_ADDRESS.getLastPairValue())
                .queueName(QUEUE_CREATE_BRIDGE)
                .staticConnectors("foo", "bar", "qux", "qiz")
                .saveAndDismissReloadRequiredWindow();
        new ResourceVerifier(BRIDGE_TBA_2_ADDRESS, client).verifyExists("Probably fails because of https://issues.jboss.org/browse/HAL-1317");
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
        editTextAndVerify(BRIDGE_ADDRESS, "queue-name", QUEUE_EDIT_BRIDGE);
    }

    @Test
    public void updateBridgeForwardingAddress() throws Exception {
        editTextAndVerify(BRIDGE_ADDRESS, "forwarding-address", "127.0.0.1");
    }

    @Test
    public void updateBridgeTransformerClass() throws Exception {
        try {
            new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, "transformer-class-name", "clazz")
                    .verifyFormSaved()
                    .verifyAttribute("transformer-class-name", "clazz");
        } finally {
            operations.undefineAttribute(BRIDGE_ADDRESS, "transformer-class-name");
        }

    }

    @Test
    public void updateBridgeDiscoveryGroup() throws Exception {
        try {
            ConfigFragment editPanelFragment = page.getConfigFragment();
            Editor editor = editPanelFragment.edit();

            editor.text("static-connectors", "");
            editor.text("discovery-group", DISCOVERY_GROUP_EDIT);
            boolean finished = editPanelFragment.save();

            Assert.assertTrue("Config should be saved and closed.", finished);
            new ResourceVerifier(BRIDGE_ADDRESS, client).verifyAttribute("discovery-group", DISCOVERY_GROUP_EDIT);
        } finally {
            operations.undefineAttribute(BRIDGE_ADDRESS, "discovery-group");
        }
    }

    @Test
    public void updateBridgePassword() throws Exception {
        page.switchToConnectionManagementTab();
        editTextAndVerify(BRIDGE_ADDRESS, "password", "pwd1");
    }

    @Test
    public void updateBridgeRetryInterval() throws Exception {
        page.switchToConnectionManagementTab();
        editTextAndVerify(BRIDGE_ADDRESS, "retry-interval", 1L);
    }

    @Test
    public void updateBridgeRetryIntervalWrongValue() {
        page.switchToConnectionManagementTab();
        verifyIfErrorAppears("retry-interval", "-10");
    }

    @Test
    public void updateBridgeReconnectAttempts() throws Exception {
        page.switchToConnectionManagementTab();
        editTextAndVerify(BRIDGE_ADDRESS, "reconnect-attempts", -1);
    }

    @Test
    public void setCredentialReferenceToClearText() throws Exception {
        page.switchToCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .build()
                .setClearTextCredentialReferenceAndVerify("Probably fails because of https://issues.jboss.org/browse/HAL-1318");
    }

    @Test
    public void setCredentialReferenceToCredentialStore() throws Exception {
        page.switchToCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .build()
                .setCredentialStoreCredentialReferenceAndVerify("Probably fails because of https://issues.jboss.org/browse/HAL-1318");
    }

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
