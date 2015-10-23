package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

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
 * Created by pcyprian on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class AttributesTestCase {
    private static final String NAME = "test-provider";
    private static final String ADD = "/subsystem=messaging-activemq/server=" + NAME + ":add()";
    private static final String DOMAIN = "/profile=full-ha";
    private static final String REMOVE = "/subsystem=messaging-activemq/server=" + NAME + ":remove";

    private String command;
    private String removeCmd;
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=" + NAME);
    private ResourceAddress address;
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;
    CliClient cliClient = CliClientFactory.getClient();

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
        verifier = new ResourceVerifier(dispatcher);
    }

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            address = new ResourceAddress(domainPath);
            command = DOMAIN + ADD;
            removeCmd = DOMAIN + REMOVE;
        } else {
            address = new ResourceAddress(path);
            command = ADD;
            removeCmd = REMOVE;
        }
        cliClient.executeCommand(command);
    }

    @After
    public void after() {
        cliClient.executeCommand(removeCmd);
        cliClient.reload();
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
    }

    @Test
    public void updateManagementAddress() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("management-address", "management");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "management-address", "management");
    }

    @Test
    public void updateNotificationAddress() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("management-notification-address", "a.n");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "management-notification-address", "a.n");
    }

    @Test //https://issues.jboss.org/browse/HAL-837
    public void updateThreadPollMaxSize() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("thread-pool-max-size", "-1");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "thread-pool-max-size", "-1");
    }

    @Test
    public void updateScheduledThreadPollMaxSize() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("scheduled-thread-pool-max-size", "10");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "scheduled-thread-pool-max-size", "10");
    }

    @Test
    public void updateTransacitonTimeout() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("transaction-timeout", "1");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "transaction-timeout", "1");
    }

    @Test
    public void updateTransacitonTimeoutWrongValue() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("transaction-timeout", "-2");
        boolean finished =  page.getWindowFragment().save();

        assertFalse("Config should not be saved and closed.Invalid value.", finished);
        verifier.verifyAttribute(address, "transaction-timeout", "300000");
    }

    @Test
    public void updateTransacitonTimeoutScanPeriod() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("transaction-timeout-scan-period", "100000");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "transaction-timeout-scan-period", "100000");
    }

    @Test
    public void updateStatisticsEnabled() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("statistics-enabled", true);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "statistics-enabled", true);
    }

    @Test
    public void updateWildCardRoutingEnabled() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("wild-card-routing-enabled", false);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "wild-card-routing-enabled", false);
    }

    @Test
    public void updatePersistenceEnabled() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("persistence-enabled", false);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "persistence-enabled", false);
    }

    @Test
    public void updatePersistIdCache() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("persist-id-cache", false);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "persist-id-cache", false);
    }
}
