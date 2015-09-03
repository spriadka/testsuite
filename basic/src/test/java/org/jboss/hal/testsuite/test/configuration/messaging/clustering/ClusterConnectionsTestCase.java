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
public class ClusterConnectionsTestCase {
    private static final String NAME = "test-cluster";
    private static final String ADD = "/subsystem=messaging-activemq/server=default/cluster-connection=" + NAME + ":add(cluster-connection-address=jms,connector-name=http-connector)";
    private static final String DOMAIN = "/profile=full-ha" ;

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/cluster-connection=" + NAME + ":remove";

    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/cluster-connection=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/cluster-connection=" + NAME);
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

    @Test //https://issues.jboss.org/browse/HAL-827
    public void addClusterConnection() {
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToConnections();

        page.addClusterConnection(NAME, "dg-group1", "http-connector", "jms");

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateClusterConnectionCallTimeout() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToConnections();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("callTimeout", "200");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "call-timeout", "200");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateClusterConnectionCheckPeriod() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToConnections();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("checkPeriod", "1");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "check-period", "1");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateClusterConnectionTTLNegativiValue() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToConnections();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("connectionTTL", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed.Negative value.", finished);
        verifier.verifyAttribute(address, "connection-ttl", "60000");

        cliClient.executeCommand(remove);
    }

    @Test //https://issues.jboss.org/browse/HAL-828
    public void setClusterConnectionRetryIntervalToNull() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToConnections();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("retryInterval", "");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "retry-interval", "500");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateClusterConnectionReconnectAttempts() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToConnections();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("reconnectAttempts", "0");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "reconnect-attempts", "0");

        cliClient.executeCommand(remove);
    }
    @Test
    public void removeClusterConnection() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToConnections();

        verifier.verifyResource(address, true);

        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }

}
