package org.jboss.hal.testsuite.test.configuration.messaging;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
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

/**
 * Created by pcyprian on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class MessagingProviderTestCase {
    private static final String NAME = "test-provider";
    private static final String SECURITYDOMAIN = "other";
    private static final String USER = "tester";
    private static final String PASSWORD = "12345";

    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=" + NAME );
    private ModelNode domainPath = new ModelNode("/profile=full/subsystem=messaging-activemq/server=" + NAME);
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
        } else {
            address = new ResourceAddress(path);
        }
    }
    @After
    public void after() {
        cliClient.reload();
    }

    @Test
    @InSequence(0)
    public void addMessagingProvider() {
        page.navigateToMessagingProvider();
        page.makeNavigation();
        page.createProvider(NAME, true, SECURITYDOMAIN, USER, PASSWORD);

        verifier.verifyResource(address, true);
    }

    @Test
    @InSequence(1)
    public void removeMessagingProvider() {
        page.selectProvider(NAME);
        page.removeProvider();

        verifier.verifyResource(address, false);
    }
}
