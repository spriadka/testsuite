package org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.AddJMSBridge;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.JMSBridgePage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.messaging.RemoveQueue;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.ADD_MESSAGE_ID_IN_HEADER_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.CLIENT_ID_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.FAILURE_RETRY_INTERVAL_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.MAX_BATCH_SIZE_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.MAX_BATCH_TIME_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.MAX_RETRIES_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.MODULE_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.QUALITY_OF_SERVICE_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SELECTOR_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SOURCE_CONNECTION_FACTORY_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SOURCE_DESTINATION_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SOURCE_PASSWORD_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SOURCE_USER_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SUBSCRIPTION_NAME_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.TARGET_CONNECTION_FACTORY_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.TARGET_DESTINATION_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.TARGET_PASSWORD_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.TARGET_USER_IDENTIFIER;

/**
 * Add a new JMS bridge and test if setting of its attributes in UI is propagated to model.
 *
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9/19/16.
 */
@RunWith(Arquillian.class)
public class JMSBridgeAttributesTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String
            IN_QUEUE_NAME = "InQueue",
            IN_QUEUE_JNDI = "java:/jms/queue/" + IN_QUEUE_NAME,
            OUT_QUEUE_NAME = "OutQueue",
            OUT_QUEUE_JNDI = "java:/jms/queue/" + OUT_QUEUE_NAME,
            LOCAL_CONNECTION_FACTORY = "java:/ConnectionFactory",
            JMS_BRIDGE_NAME = "test";

    private static final Address JMS_BRIDGE_ADDRESS = Address.subsystem("messaging-activemq")
            .and("jms-bridge", JMS_BRIDGE_NAME);

    @Drone
    private WebDriver browser;

    @Page
    private JMSBridgePage page;

    @BeforeClass
    public static void beforeClass() throws CommandFailedException {
        client.apply(new AddQueue.Builder(IN_QUEUE_NAME)
                .jndiEntries(Collections.singletonList(IN_QUEUE_JNDI))
                .replaceExisting()
                .build());
        client.apply(new AddQueue.Builder(OUT_QUEUE_NAME)
                .jndiEntries(Collections.singletonList(OUT_QUEUE_JNDI))
                .replaceExisting()
                .build());

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
    }

    @AfterClass
    public static void afterClass() throws IOException, CommandFailedException {
        try {
            operations.remove(JMS_BRIDGE_ADDRESS);
            client.apply(new RemoveQueue(IN_QUEUE_NAME));
            client.apply(new RemoveQueue(OUT_QUEUE_NAME));
        } finally {
            client.close();
        }
    }

    @Before
    public void before() {
        page.navigate();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void toggleAddMessageIDInHeader() throws Exception {
        new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, ADD_MESSAGE_ID_IN_HEADER_IDENTIFIER, true)
                .verifyFormSaved()
                .verifyAttribute(ADD_MESSAGE_ID_IN_HEADER_IDENTIFIER, true);

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, ADD_MESSAGE_ID_IN_HEADER_IDENTIFIER, false)
                .verifyFormSaved()
                .verifyAttribute(ADD_MESSAGE_ID_IN_HEADER_IDENTIFIER, false);
    }

    @Test
    public void editClientID() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, CLIENT_ID_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CLIENT_ID_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(CLIENT_ID_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, CLIENT_ID_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editFailureRetryInterval() throws Exception {
        final long VALUE = 4242;

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, FAILURE_RETRY_INTERVAL_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, FAILURE_RETRY_INTERVAL_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(FAILURE_RETRY_INTERVAL_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, FAILURE_RETRY_INTERVAL_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editFailureRetryIntervalInvalid() throws Exception {
        final String VALUE = "invalid123";

        new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, FAILURE_RETRY_INTERVAL_IDENTIFIER, VALUE)
                .verifyFormNotSaved();
    }

    @Test
    public void editMaxBatchSize() throws Exception {
        final int VALUE = 4242;

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, MAX_BATCH_SIZE_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MAX_BATCH_SIZE_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(MAX_BATCH_SIZE_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, MAX_BATCH_SIZE_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editMaxBatchSizeInvalid() throws Exception {
        final String VALUE = "invalid123";

        new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MAX_BATCH_SIZE_IDENTIFIER, VALUE)
                .verifyFormNotSaved();
    }

    @Test
    public void editMaxBatchTime() throws Exception {
        final long VALUE = 4242;

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, MAX_BATCH_TIME_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MAX_BATCH_TIME_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(MAX_BATCH_TIME_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, MAX_BATCH_TIME_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editMaxBatchTimeInvalid() throws Exception {
        final String VALUE = "invalid123";

        new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MAX_BATCH_TIME_IDENTIFIER, VALUE)
                .verifyFormNotSaved();
    }

    @Test
    public void editMaxRetries() throws Exception {
        final int VALUE = 42;

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, MAX_RETRIES_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MAX_RETRIES_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(MAX_RETRIES_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, MAX_RETRIES_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editMaxRetriesInvalid() throws Exception {
        final String VALUE = "invalid123";

        new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, MAX_RETRIES_IDENTIFIER, VALUE)
                .verifyFormNotSaved();
    }

    @Test
    public void editModule() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, MODULE_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MODULE_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(MODULE_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, MODULE_IDENTIFIER, origValue);
        }
    }

    @Test
    public void selectQualityOfService() throws Exception {
        final String VALUE = AddJMSBridge.QualityOfService.DUPLICATES_OK.getStringValue();

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, QUALITY_OF_SERVICE_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.SELECT, QUALITY_OF_SERVICE_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(QUALITY_OF_SERVICE_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, QUALITY_OF_SERVICE_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editSelector() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, SELECTOR_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SELECTOR_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(SELECTOR_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, SELECTOR_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editSourceConnectionFactory() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, SOURCE_CONNECTION_FACTORY_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOURCE_CONNECTION_FACTORY_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(SOURCE_CONNECTION_FACTORY_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, SOURCE_CONNECTION_FACTORY_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editSourceDestination() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, SOURCE_DESTINATION_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOURCE_DESTINATION_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(SOURCE_DESTINATION_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, SOURCE_DESTINATION_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editSourcePassword() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, SOURCE_PASSWORD_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOURCE_PASSWORD_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(SOURCE_PASSWORD_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, SOURCE_PASSWORD_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editSourceUser() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, SOURCE_USER_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOURCE_USER_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(SOURCE_USER_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, SOURCE_USER_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editTargetConnectionFactory() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, TARGET_CONNECTION_FACTORY_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TARGET_CONNECTION_FACTORY_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(TARGET_CONNECTION_FACTORY_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, TARGET_CONNECTION_FACTORY_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editTargetDestination() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, TARGET_DESTINATION_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TARGET_DESTINATION_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(TARGET_DESTINATION_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, TARGET_DESTINATION_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editTargetPassword() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, TARGET_PASSWORD_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TARGET_PASSWORD_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(TARGET_PASSWORD_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, TARGET_PASSWORD_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editTargetUser() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, TARGET_USER_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TARGET_USER_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(TARGET_USER_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, TARGET_USER_IDENTIFIER, origValue);
        }
    }

    @Test
    public void editSubscriptionName() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult origValueResult = operations.readAttribute(JMS_BRIDGE_ADDRESS, SUBSCRIPTION_NAME_IDENTIFIER, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        origValueResult.assertSuccess();
        ModelNode origValue = origValueResult.value();
        try {
            new ConfigChecker.Builder(client, JMS_BRIDGE_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SUBSCRIPTION_NAME_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(SUBSCRIPTION_NAME_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JMS_BRIDGE_ADDRESS, SUBSCRIPTION_NAME_IDENTIFIER, origValue);
        }
    }

    @Category(Elytron.class)
    @Test
    public void editSourceCredentialReferenceClearText() throws Exception {
        page.switchToSourceCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("source-credential-reference")
                .build()
                .setClearTextCredentialReferenceAndVerify();
    }

    @Category(Elytron.class)
    @Test
    public void editSourceCredentialReferenceStoreReference() throws Exception {
        page.switchToSourceCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("source-credential-reference")
                .build()
                .setCredentialStoreCredentialReferenceAndVerify();
    }

    @Category(Elytron.class)
    @Test
    public void editSourceCredentialReferenceIllegalCombination() throws Exception {
        page.switchToSourceCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("source-credential-reference")
                .build()
                .testIllegalCombinationCredentialReferenceAttributes();
    }

    @Category(Elytron.class)
    @Test
    public void editTargetCredentialReferenceClearText() throws Exception {
        page.switchToTargetCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("target-credential-reference")
                .build()
                .setClearTextCredentialReferenceAndVerify();
    }
    @Category(Elytron.class)
    @Test
    public void editTargetCredentialReferenceStoreReference() throws Exception {
        page.switchToTargetCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("target-credential-reference")
                .build()
                .setCredentialStoreCredentialReferenceAndVerify();
    }

    @Category(Elytron.class)
    @Test
    public void editTargetCredentialReferenceIllegalCombination() throws Exception {
        page.switchToTargetCredentialReference();
        new ElytronIntegrationChecker.Builder(client)
                .address(JMS_BRIDGE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("target-credential-reference")
                .build()
                .testIllegalCombinationCredentialReferenceAttributes();
    }
}
