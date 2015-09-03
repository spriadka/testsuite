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
 * Created by pcyprian on 3.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class DiscoveryGroupsTestCase {
    private static final String NAME = "dg-group-test";
    private static final String BINDING = "socket-binding";
    private static final String ADD = "/subsystem=messaging-activemq/server=default/discovery-group=" + NAME + ":add(socket-binding=" + BINDING + ")";
    private static final String DOMAIN = "/profile=full-ha" ;

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/discovery-group=" + NAME + ":remove";

    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/discovery-group=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/discovery-group=" + NAME);
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
    public void addDiscoveryGroup() {
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();

        page.addBroadcastGroup(NAME,BINDING);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateDiscoveryGroupSocketBinding() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("socketBinding", "sb");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "socket-binding", "sb");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDiscoveryGroupRefreshTimeout() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("refreshTimeout", "2000");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "refresh-timeout", "2000");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBroadcastGroupRefreshTimeoutNegativeValue() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("refreshTimeout", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed.Negative value.", finished);
        verifier.verifyAttribute(address, "refresh-timeout", "10000");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDiscoveryGroupInitialTimeout() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("initialWaitTimeout", "200");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "initial-wait-timeout", "200");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateBroadcastGroupInitialTimeoutNegativeValue() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("initialWaitTimeout", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed.Negative value.", finished);
        verifier.verifyAttribute(address, "initial-wait-timeout", "10000");

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeDiscoveryGroup() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();

        verifier.verifyResource(address, true);

        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }

}
