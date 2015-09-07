package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 2.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class BroadcastGroupsTestCase {
    private static final String NAME = "bg-group-test";
    private static final String BINDING = "socket-binding";
    private static final String ADD = "/subsystem=messaging-activemq/server=default/broadcast-group=" + NAME + ":add(socket-binding=" + BINDING + ")";
    private static final String DOMAIN = "/profile=full-ha" ;

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/broadcast-group=" + NAME + ":remove";

    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/broadcast-group=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/broadcast-group=" + NAME);
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
    public void addBroadcastGroup() {
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.addBroadcastGroup(NAME, BINDING);
        
        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateBroadcastGroupPeriod() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("broadcastPeriod", "1000");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "broadcast-period", "1000");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBroadcastGroupPeriodNegativeValue() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("broadcastPeriod", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed.Negative value.", finished);
        verifier.verifyAttribute(address, "broadcast-period", "2000");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBroadcastGroupConnectors() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("connectors", "in-vm");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "connectors", "[\"in-vm\"]");

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeBroadcastGroup() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");

        verifier.verifyResource(address, true);

        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
