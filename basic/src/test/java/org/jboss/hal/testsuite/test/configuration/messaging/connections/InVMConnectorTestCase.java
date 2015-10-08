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
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 7.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class InVMConnectorTestCase {
    private static final String NAME = "in-vm-test-connector";
    private static final String SERVER = "1";
    private static final String ADD = "/subsystem=messaging-activemq/server=default/in-vm-connector=" + NAME + ":add(server-id=" + SERVER + ")";
    private static final String DOMAIN = "/profile=full-ha" ;

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/in-vm-connector=" + NAME + ":remove";
    private String addProperty = "/subsystem=messaging-activemq/server=default/in-vm-connector=" + NAME + ":write-attribute(name=params.prop,value=test)";
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/in-vm-connector=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/in-vm-connector=" + NAME);
    private ResourceAddress address;
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    CliClient cliClient = CliClientFactory.getClient();

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
            addProperty = DOMAIN + addProperty;
        } else {
            address = new ResourceAddress(path);
            command = ADD;
        }
    }
    @After
    public void after() {
        cliClient.executeCommand(remove);
    }

    @Test
    public void addInVmConnector() {
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToConnector();
        page.switchType("Type: In-VM");
        page.addInVmAcceptor(NAME, SERVER);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateConnectorServerID() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToConnector();
        page.switchType("Type: In-VM");
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("serverId", "0");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "server-id", "0");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateConnectorProperties() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToConnector();
        page.switchType("Type: In-VM");
        page.selectInTable(NAME, 0);

        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        ConfigPropertyWizard wizard = properties.addProperty();
        boolean result = wizard.name("prop").value("test").finish();

        assertTrue("Property should be added and wizard closed.", result);
        verifier.verifyAttribute(address, "params", "{\"prop\" => \"test\"}");

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeConnectorProperties() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addProperty);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToConnector();
        page.switchType("Type: In-VM");
        page.selectInTable(NAME, 0);
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        properties.removeProperty("prop");

        verifier.verifyAttribute(address, "params", "undefined");

        cliClient.executeCommand(remove);
    }

    @Test //https://issues.jboss.org/browse/HAL-830
    public void removeInVmConnector() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToConnector();
        page.switchType("Type: In-VM");

        verifier.verifyResource(address, true);

        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
