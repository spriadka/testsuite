package org.jboss.hal.testsuite.test.configuration.messaging.destinations;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Created by pcyprian on 8.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ConnectionFactoriesTestCase {
    private static final String NAME = "test-cf";
    private static final String JNDINAME = "java:/jndi-cf";
    private static final String CONNECTOR = "http-connector";
    private static final String ADD = "/subsystem=messaging-activemq/server==default/connection-factory=" + NAME + ":add(entries=["
            + JNDINAME + "],connectors=[" + CONNECTOR + "])";
    private static final String DOMAIN = "/profile=full-ha";

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/connection-factory=" + NAME + ":remove";
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/connection-factory=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/connection-factory=" + NAME);
    private ResourceAddress address;
    private ResourceAddress topicsAddress;
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

    @Test //https://issues.jboss.org/browse/HAL-832
    public void addConnectionFactory() {
        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToConnectionFactories();

        page.addFactory(NAME, JNDINAME, CONNECTOR);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void removeConnectionFactory() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToConnectionFactories();

        verifier.verifyResource(address, true);
        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
