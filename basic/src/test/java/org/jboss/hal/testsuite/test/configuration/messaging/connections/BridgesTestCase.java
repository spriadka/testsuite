package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.messaging.RemoveQueue;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class BridgesTestCase extends AbstractMessagingTestCase {

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

    private static final String QUEUE_NAME = "queue-name";
    private static final String DISCOVERY_GROUP = "discovery-group";
    private static final String STATIC_CONNECTORS = "static-connectors";
    private static final String FORWARDING_ADDRESS = "forwarding-address";
    private static final String FILTER = "filter";
    private static final String PASSWORD = "password";
    private static final String RETRY_INTERVAL = "retry-interval";
    private static final String RECONNECT_ATTEMPTS = "reconnect-attempts";
    private static final String TRANSFORMER_CLASS_NAME = "transformer-class-name";

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

    private void navigateToBridgeTab() {
        page.viewConnectionSettings("default");
        page.switchToBridges();
        page.selectInTable(BRIDGE);
    }

    /**
     * @tpTestDetails
     * Tries to create new bridge in the Web Console messaging subsystem with given discovery group.
     * Validate newly created bridge is present in bridges table
     * Validate newly created bridge is present in the model
     */
    @Test
    public void addBridgeWithDiscoveryGroup() throws Exception {
        final String discoveryGroup = "foobar";
        navigateToBridgeTab();
        page.addBridge()
            .name(BRIDGE_TBA)
            .queueName(QUEUE_CREATE_BRIDGE)
            .discoveryGroup(discoveryGroup)
            .saveAndDismissReloadRequiredWindow();
        Assert.assertTrue("Newly added bridge should be present in the bridges table",
                page.getResourceManager().isResourcePresent(BRIDGE_TBA));
        new ResourceVerifier(BRIDGE_TBA_ADDRESS, client)
            .verifyExists(HAL1317_FAIL_MESSAGE)
            .verifyAttribute(DISCOVERY_GROUP, discoveryGroup);
    }

    /**
     * @tpTestDetails
     * Tries to create new bridge in the Web Console messaging subsystem with given static connectors.
     * Validate newly created bridge is present in bridges table
     * Validate newly created bridge is present in the model
     */
    @Test
    public void addBridgeWithStaticConnectors() throws Exception {
        final String[] staticConnectors = new String[] {"foo", "bar", "qux", "qiz"};
        navigateToBridgeTab();
        page.addBridge()
                .name(BRIDGE_TBA_2_ADDRESS.getLastPairValue())
                .queueName(QUEUE_CREATE_BRIDGE)
                .staticConnectors(staticConnectors)
                .saveAndDismissReloadRequiredWindow();
        Assert.assertTrue("Newly added bridge should be present in the bridges table",
                page.getResourceManager().isResourcePresent(BRIDGE_TBA_2_ADDRESS.getLastPairValue()));
        new ResourceVerifier(BRIDGE_TBA_2_ADDRESS, client)
                .verifyExists(HAL1317_FAIL_MESSAGE)
                .verifyAttribute(STATIC_CONNECTORS, new ModelNodeGenerator.ModelNodeListBuilder()
                        .addAll(staticConnectors).build());
    }

    /**
     * @tpTestDetails
     * Tries to create new bridge in the Web Console messaging subsystem
     * with given discovery group and forwarding address
     * Validate newly created bridge is present in bridges table
     * Validate newly created bridge is present in the model
     */
    @Test
    public void addBridgeWithDiscoveryGroupAndForwardingAddressDefined() throws Exception {
        final String discoveryGroup = "foobar";
        final String forwardingAddress = "quz";
        navigateToBridgeTab();
        page.addBridge()
                .name(BRIDGE_TBA_3_ADDRESS.getLastPairValue())
                .queueName(QUEUE_CREATE_BRIDGE)
                .discoveryGroup(discoveryGroup)
                .forwardingAddress(forwardingAddress)
                .saveAndDismissReloadRequiredWindow();
        Assert.assertTrue("Newly added bridge should be present in the bridges table",
                page.getResourceManager().isResourcePresent(BRIDGE_TBA_3_ADDRESS.getLastPairValue()));
        new ResourceVerifier(BRIDGE_TBA_3_ADDRESS, client)
                .verifyExists()
                .verifyAttribute(DISCOVERY_GROUP, discoveryGroup)
                .verifyAttribute(FORWARDING_ADDRESS, forwardingAddress);
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge filter attribute in the Web Console messaging subsystem.
     * Validate edited filter attribute in model
     */
    @Test
    public void updateBridgeFilter() throws Exception {
        final String filter = "testFilter";
        navigateToBridgeTab();
        editTextAndVerify(BRIDGE_ADDRESS, FILTER, filter);
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge queue attribute in the Web Console messaging subsystem.
     * Validate edited queue attribute in model
     */
    @Test
    public void updateBridgeQueue() throws Exception {
        navigateToBridgeTab();
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
            .configFragment(page.getConfigFragment())
            .editAndSave(ConfigChecker.InputType.TEXT, QUEUE_NAME, QUEUE_EDIT_BRIDGE)
            .verifyFormSaved()
            .verifyAttribute(QUEUE_NAME, QUEUE_EDIT_BRIDGE, HAL1324_FAIL_MESSAGE);
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge forwarding address attribute in the Web Console messaging subsystem.
     * Validate edited forwarding address in model
     */
    @Test
    public void updateBridgeForwardingAddress() throws Exception {
        final String forwardingAddress = "127.0.0.1";
        navigateToBridgeTab();
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, FORWARDING_ADDRESS, forwardingAddress)
                .verifyFormSaved()
                .verifyAttribute(FORWARDING_ADDRESS, forwardingAddress, HAL1324_FAIL_MESSAGE);
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge transformer class attribute in the Web Console messaging subsystem.
     * Validate edited transformer class attribute in model
     */
    @Test
    public void updateBridgeTransformerClass() throws Exception {
        final String transformerClass = "clazz";
        navigateToBridgeTab();
        try {
            new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TRANSFORMER_CLASS_NAME, transformerClass)
                    .verifyFormSaved()
                    .verifyAttribute(TRANSFORMER_CLASS_NAME, transformerClass, HAL1324_FAIL_MESSAGE);
        } finally {
            operations.undefineAttribute(BRIDGE_ADDRESS, TRANSFORMER_CLASS_NAME);
        }

    }

    /**
     * @tpTestDetails
     * Tries to edit bridge discovery group attribute in the Web Console messaging subsystem.
     * Validate edited discovery group attribute in model
     */
    @Test
    public void updateBridgeDiscoveryGroup() throws Exception {
        navigateToBridgeTab();
        try {
            new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, STATIC_CONNECTORS, "")
                    .edit(ConfigChecker.InputType.TEXT, DISCOVERY_GROUP, DISCOVERY_GROUP_EDIT)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(DISCOVERY_GROUP, DISCOVERY_GROUP_EDIT, HAL1324_FAIL_MESSAGE);
        } finally {
            operations.undefineAttribute(BRIDGE_ADDRESS, DISCOVERY_GROUP);
        }
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge password attribute in the Web Console messaging subsystem.
     * Validate edited password attribute in model
     */
    @Test
    public void updateBridgePassword() throws Exception {
        final String password = "pwd1";
        navigateToBridgeTab();
        page.switchToConnectionManagementTab();
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(BRIDGE_ADDRESS, PASSWORD,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        originalModelNodeResult.assertSuccess();
        try {
            editTextAndVerify(BRIDGE_ADDRESS, PASSWORD, password);
        } finally {
            operations.writeAttribute(BRIDGE_ADDRESS, PASSWORD, originalModelNodeResult.value());
        }
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge retry interval attribute in the Web Console messaging subsystem.
     * Validate edited retry interval attribute in model
     */
    @Test
    public void updateBridgeRetryInterval() throws Exception {
        long retryInterval = 1L;
        navigateToBridgeTab();
        page.switchToConnectionManagementTab();
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, RETRY_INTERVAL, retryInterval)
                .verifyFormSaved()
                .verifyAttribute(RETRY_INTERVAL, retryInterval, HAL1324_FAIL_MESSAGE);
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge retry interval attribute with incorrect value in the Web Console messaging subsystem.
     * Validate a validation error is shown when entered invalid value for retry interval attribute
     */
    @Test
    public void updateBridgeRetryIntervalWrongValue() {
        int retryInterval = -10;
        navigateToBridgeTab();
        page.switchToConnectionManagementTab();
        verifyIfErrorAppears(RETRY_INTERVAL, String.valueOf(retryInterval));
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge reconnect attempts attribute in the Web Console messaging subsystem.
     * Validate edited reconnect attempts attribute in model
     */
    @Test
    public void updateBridgeReconnectAttempts() throws Exception {
        int reconnectAttempts = -1;
        navigateToBridgeTab();
        page.switchToConnectionManagementTab();
        new ConfigChecker.Builder(client, BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, RECONNECT_ATTEMPTS, reconnectAttempts)
                .verifyFormSaved(HAL1325_FAIL_MESSAGE)
                .verifyAttribute(RECONNECT_ATTEMPTS, reconnectAttempts);
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge credential reference attribute
     * to clear text in the Web Console messaging subsystem.
     * Validate edited credential reference attribute in model
     */
    @Category(Elytron.class)
    @Test
    public void setCredentialReferenceToClearText() throws Exception {
        final ModelNodeResult bridgePassword = operations.readAttribute(BRIDGE_ADDRESS, PASSWORD,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        try {
            undefineBridgePassword();
            navigateToBridgeTab();
            page.switchToCredentialReference();
            new ElytronIntegrationChecker.Builder(client)
                    .address(BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .build()
                    .setClearTextCredentialReferenceAndVerify(HAL1318_FAIL_MESSAGE);
        } finally {
            operations.writeAttribute(BRIDGE_ADDRESS, PASSWORD, bridgePassword.value());
        }
    }

    private void undefineBridgePassword() throws IOException, TimeoutException, InterruptedException {
        operations.undefineAttribute(BRIDGE_ADDRESS, PASSWORD);
        administration.reloadIfRequired();
    }

    /**
     * @tpTestDetails
     * Tries to edit bridge credential reference attribute
     * to credential store in the Web Console messaging subsystem.
     * Validate edited credential reference attribute in model
     */
    @Category(Elytron.class)
    @Test
    public void setCredentialReferenceToCredentialStore() throws Exception {
        final ModelNodeResult bridgePassword = operations.readAttribute(BRIDGE_ADDRESS, PASSWORD,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        try {
            undefineBridgePassword();
            navigateToBridgeTab();
            page.switchToCredentialReference();
            new ElytronIntegrationChecker.Builder(client)
                    .address(BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .build()
                    .setCredentialStoreCredentialReferenceAndVerify(HAL1318_FAIL_MESSAGE);
        } finally {
            operations.writeAttribute(BRIDGE_ADDRESS, PASSWORD, bridgePassword.value());
        }
    }

    /**
     * @tpTestDetails
     * Tries to enter invalid combination for bridge credential reference attribute
     * Validate that a validation error is shown when entering invalid combination
     * for bridge credential reference attribute
     */
    @Category(Elytron.class)
    @Test
    public void testIllegalCombinationsForCredentialReference() throws Exception {
        final ModelNodeResult bridgePassword = operations.readAttribute(BRIDGE_ADDRESS, PASSWORD,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        try {
            undefineBridgePassword();
            navigateToBridgeTab();
            page.switchToCredentialReference();
            new ElytronIntegrationChecker.Builder(client)
                    .address(BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .build()
                    .testIllegalCombinationCredentialReferenceAttributes();
        } finally {
            operations.writeAttribute(BRIDGE_ADDRESS, PASSWORD, bridgePassword.value());
        }
    }

    /**
     * @tpTestDetails
     * Tries to remove bridge in the Web Console messaging subsystem
     * Validate removed bridge is not present in the bridges table
     * Validate removed bridge is not present in the model
     */
    @Test
    public void removeBridge() throws Exception {
        navigateToBridgeTab();
        page.remove(BRIDGE_TBR);
        Assert.assertFalse("Removed bridge should not be present in the bridges table",
                page.getResourceManager().isResourcePresent(BRIDGE_TBR));
        new ResourceVerifier(BRIDGE_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private static void createBridge(Address address) throws Exception {
        operations.add(address, Values.empty()
                .andList(STATIC_CONNECTORS, CONNECTOR)
                .and(QUEUE_NAME, QUEUE_CREATE_BRIDGE)).assertSuccess();
    }

}
