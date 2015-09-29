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
import org.jboss.hal.testsuite.fragment.ConfigFragment;
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
 * Created by pcyprian on 8.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class QueuesTopicsTestCase {
    private static final String NAME = "test-queue";
    private static final String TOPICSNAME = "test-topics";
    private static final String JNDINAME = "java:/jndi-queue";
    private static final String JNDI_TOPICS_NAME = "java:/jndi-topics";
    private static final String ADD = "/subsystem=messaging-activemq/server=default/jms-queue=" + NAME + ":add(entries=[" + JNDINAME + "])";
    private static final String DOMAIN = "/profile=full-ha";

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/jms-queue=" + NAME + ":remove";
    private String removeTopics = "/subsystem=messaging-activemq/server=default/jms-topic=" + TOPICSNAME + ":remove";
    private String addTopics = "/subsystem=messaging-activemq/server=default/jms-topic=" + TOPICSNAME + ":add(entries=[" + JNDI_TOPICS_NAME + "])";
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/jms-queue=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/jms-queue=" + NAME);
    private ModelNode topicsPath = new ModelNode("/subsystem=messaging-activemq/server=default/jms-topic=" + TOPICSNAME);
    private ModelNode topicsDomainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/jms-topic=" + TOPICSNAME);
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
            topicsAddress = new ResourceAddress(topicsDomainPath);
            command = DOMAIN + ADD;
            remove = DOMAIN + remove;
            removeTopics = DOMAIN + removeTopics;
            addTopics = DOMAIN + addTopics;
        } else {
            address = new ResourceAddress(path);
            topicsAddress = new ResourceAddress(topicsPath);
            command = ADD;
        }
    }

    @After
    public void after() {
        cliClient.executeCommand(removeTopics);
        cliClient.executeCommand(remove);
    }

    @Test
    public void addJmsQueue() {
        page.navigateToMessaging();
        page.selectView("Destinations");
        page.addQueue(NAME, JNDINAME);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void removeJmsQueue() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");

        verifier.verifyResource(address, true);
        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }

    @Test
    public void addJmsTopics() {
        page.navigateToMessaging();
        page.selectView("Destinations");
        page.getConfig().topicsConfig();
        page.addQueue(TOPICSNAME, JNDI_TOPICS_NAME);

        verifier.verifyResource(topicsAddress, true);

        cliClient.executeCommand(removeTopics);

        verifier.verifyResource(topicsAddress, false);
    }

    @Test
    public void updateTopicsJndiNames() {
        cliClient.executeCommand(addTopics);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.getConfig().topicsConfig();
        page.selectInTable(TOPICSNAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("entries", "java:/jndi-name");
        boolean finished = editPanelFragment.save();
        cliClient.reload();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(topicsAddress, "entries", "[\"java:/jndi-name\"]");

        cliClient.executeCommand(removeTopics);
        //reload is required because of update JNDInames
        cliClient.reload();
    }

    @Test
    public void removeJmsTopics() {
        cliClient.executeCommand(addTopics);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.getConfig().topicsConfig();

        verifier.verifyResource(topicsAddress, true);
        page.selectInTable(TOPICSNAME, 0);
        page.remove();

        verifier.verifyResource(topicsAddress, false);
    }

}
