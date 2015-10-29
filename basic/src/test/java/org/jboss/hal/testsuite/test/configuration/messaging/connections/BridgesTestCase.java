package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 7.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class BridgesTestCase {
    private static final String NAME = "test-bridge";
    private static final String CONNECTOR = "http-connector";
    private static final String QUEUE = "queue";
    private static final String ADD = "/subsystem=messaging-activemq/server=default/bridge=" + NAME + ":add(static-connectors=[" +
            CONNECTOR + "],queue-name=" + QUEUE + ")";
    private static final String DOMAIN = "/profile=full-ha" ;

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/bridge=" + NAME + ":remove";

    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/bridge=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/bridge=" + NAME);
    private ResourceAddress address;
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;
    CliClient cliClient = CliClientFactory.getClient();

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
        verifier = new ResourceVerifier(dispatcher);
    }

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            address = new ResourceAddress(domainPath);
            command = DOMAIN + ADD;
            remove = DOMAIN + remove;
        } else {
            address = new ResourceAddress(path);
            command = ADD;
        }
    }
    @After
    public void after() {
        cliClient.executeCommand(remove);
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
    }

    //https://issues.jboss.org/browse/HAL-831 https://issues.jboss.org/browse/HAL-880

    @Test
    public void addBridge() {
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();

        page.addBridge(NAME, QUEUE, "faddress", CONNECTOR);


        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateBridgeFilter() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("filter", "filter");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "filter", "filter");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBridgeQueue() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("queueName", "q");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "queue-name", "q");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBridgeForwardingAddress() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("forwardingAddress", "address");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "forwarding-address", "address");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBridgeTransformerClass() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("transformerClass", "transClass");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "transformer-class-name", "transClass");

        cliClient.executeCommand(remove);
    }

    @Test //https://issues.jboss.org/browse/HAL-903
    public void updateBridgeDiscoveryGroup() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("staticConnectors", "");
        editPanelFragment.getEditor().text("discoveryGroup", "dq-group1");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "discovery-group", "dg-group1");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBridgePassword() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.switchToConnectionManagementTab();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("password", "pwd1");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "password", "pwd1");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBridgeRetryInterval() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.switchToConnectionManagementTab();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("retryInterval", "1");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "retry-interval", "1");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBridgeRetryIntervalWrongValue() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.switchToConnectionManagementTab();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("retryInterval", "-10");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed. Wrong value.", finished);
        verifier.verifyAttribute(address, "retry-interval", "2000");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBridgeReconnectAttempts() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();
        page.switchToConnectionManagementTab();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("reconnectAttempts", "-1");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "reconnect-attempts", "-1");

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeBridge() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToBridges();

        verifier.verifyResource(address, true);

        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
