package org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.AddJMSBridge;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.JMSBridgePage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
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
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.SOURCE_CONTEXT_IDENTIFIER;
import static org.jboss.hal.testsuite.test.configuration.messaging.jmsbridge.JMSBridgeConstants.TARGET_CONTEXT_IDENTIFIER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9/19/16.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class JMSBridgeContextTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String
            IN_QUEUE_NAME = "InQueue",
            IN_QUEUE_JNDI = "java:/jms/queue/" + IN_QUEUE_NAME,
            OUT_QUEUE_NAME = "OutQueue",
            OUT_QUEUE_JNDI = "java:/jms/queue/" + OUT_QUEUE_NAME,
            LOCAL_CONNECTION_FACTORY = "java:/ConnectionFactory",
            JMS_BRIDGE_NAME = "test" + RandomStringUtils.randomAlphanumeric(5),
            SOURCE_KEY_TBA = "testKeySource",
            SOURCE_VALUE_TBA = "testValueSource",
            SOURCE_KEY_TBR = "testKeySource",
            SOURCE_VALUE_TBR = "testValueSource",
            TARGET_KEY_TBA = "testKeyTarget",
            TARGET_VALUE_TBA = "testValueTarget",
            TARGET_KEY_TBR = "testKeyTarget",
            TARGET_VALUE_TBR = "testValueTarget";

    private static final Address
            MESSAGING_SUBSYSTEM_ADDRESS = Address.subsystem("messaging-activemq"),
            JMS_BRIDGE_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS.and("jms-bridge", JMS_BRIDGE_NAME);

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
                .sourceContext(Values.empty())
                .sourceDestination(IN_QUEUE_JNDI)
                .targetConnectionFactory(LOCAL_CONNECTION_FACTORY)
                .targetContext(Values.empty())
                .targetDestination(OUT_QUEUE_JNDI)
                .replaceExisting()
                .build()
        );
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        try {
            operations.remove(JMS_BRIDGE_ADDRESS);
            client.apply(new RemoveQueue(IN_QUEUE_NAME));
            client.apply(new RemoveQueue(OUT_QUEUE_NAME));
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addSourceContextProperty() throws Exception {
        page.navigate();

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        page.selectJMSBridgeInTable(JMS_BRIDGE_NAME);
        page.switchToSourceContext();

        WizardWindow wizard = page.openAddContextPropertyWindow();

        Editor editor = wizard.getEditor();

        editor.text("key", SOURCE_KEY_TBA);
        editor.text("value", SOURCE_VALUE_TBA);
        boolean result = wizard.finishAndDismissReloadRequiredWindow();

        assertTrue("Window should be closed", result);

        assertTrue("Attribute '" + SOURCE_CONTEXT_IDENTIFIER + "' should have defined property in list: <'" +
                        SOURCE_KEY_TBA + "','" + SOURCE_VALUE_TBA + ">.",
                hasListAttributeGivenProperty(SOURCE_CONTEXT_IDENTIFIER, SOURCE_KEY_TBA, SOURCE_VALUE_TBA));
    }

    @Test
    public void removeSourceContextProperty() throws Exception {
        operations.writeAttribute(JMS_BRIDGE_ADDRESS,
                    SOURCE_CONTEXT_IDENTIFIER,
                    new ModelNode().add(SOURCE_KEY_TBR, SOURCE_VALUE_TBR))
                .assertSuccess();

        page.navigate();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        page.selectJMSBridgeInTable(JMS_BRIDGE_NAME);
        page.switchToSourceContext();

        page.selectContextParameterAndClickRemove(SOURCE_KEY_TBR).confirmAndDismissReloadRequiredMessage();

        assertFalse("Attribute '" + SOURCE_CONTEXT_IDENTIFIER + "' should NOT have defined property: <'" +
                        SOURCE_KEY_TBR + "','" + SOURCE_VALUE_TBR + ">.",
                hasListAttributeGivenProperty(SOURCE_CONTEXT_IDENTIFIER, SOURCE_KEY_TBR, SOURCE_VALUE_TBR));
    }

    @Test
    public void addTargetContextProperty() throws Exception {
        page.navigate();

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        page.selectJMSBridgeInTable(JMS_BRIDGE_NAME);
        page.switchToTargetContext();

        WizardWindow wizard = page.openAddContextPropertyWindow();

        Editor editor = wizard.getEditor();

        editor.text("key", TARGET_KEY_TBA);
        editor.text("value", TARGET_VALUE_TBA);
        boolean result = wizard.finishAndDismissReloadRequiredWindow();

        assertTrue("Window should be closed", result);

        assertTrue("Attribute '" + TARGET_CONTEXT_IDENTIFIER + "' should have defined property in list: <'" +
                        TARGET_KEY_TBA + "','" + TARGET_VALUE_TBA + ">.",
                hasListAttributeGivenProperty(TARGET_CONTEXT_IDENTIFIER, TARGET_KEY_TBA, TARGET_VALUE_TBA));
    }

    @Test
    public void removeTargetContextProperty() throws Exception {
        operations.writeAttribute(JMS_BRIDGE_ADDRESS,
                TARGET_CONTEXT_IDENTIFIER,
                new ModelNode().add(TARGET_KEY_TBR, TARGET_VALUE_TBR))
                .assertSuccess();

        page.navigate();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        page.selectJMSBridgeInTable(JMS_BRIDGE_NAME);
        page.switchToTargetContext();

        page.selectContextParameterAndClickRemove(TARGET_KEY_TBR).confirmAndDismissReloadRequiredMessage();

        assertFalse("Attribute '" + TARGET_CONTEXT_IDENTIFIER + "' should NOT have defined property: <'" +
                        TARGET_KEY_TBR + "','" + TARGET_VALUE_TBR + ">.",
                hasListAttributeGivenProperty(TARGET_CONTEXT_IDENTIFIER, TARGET_KEY_TBR, TARGET_VALUE_TBR));
    }

    private boolean hasListAttributeGivenProperty(String identifier, String key, String value) throws IOException {
        ModelNodeResult result = operations.readAttribute(JMS_BRIDGE_ADDRESS, identifier);
        result.assertSuccess();
        if (!result.hasDefinedValue()) {
            return false;
        }
        for (Property property : result.value().asPropertyList()) {
            if (property.getName().equals(key) && property.getValue().asString().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
