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

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecurityTestCase {
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

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
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

    @Test
    public void updateClusterUser() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToSecurityTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("cluster-user", "TESTER");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "cluster-user", "TESTER");
    }

    @Test
    public void updateClusterPassword() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToSecurityTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("cluster-password", "TESTER.PASSWORD");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "cluster-password", "TESTER.PASSWORD");
    }

    @Test
    public void updateSecurityEnabled() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToSecurityTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("security-enabled", false);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "security-enabled", false);
    }

    @Test
    public void updateSecurityInvalidationInterval() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToSecurityTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("security-invalidation-interval", "10");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "security-invalidation-interval", "10");
    }

    @Test
    public void updateSecurityDomain() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToSecurityTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("security-domain", "jboss-web-policy");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "security-domain", "jboss-web-policy");
    }
}
