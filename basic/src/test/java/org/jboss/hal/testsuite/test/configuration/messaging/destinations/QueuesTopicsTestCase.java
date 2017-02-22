package org.jboss.hal.testsuite.test.configuration.messaging.destinations;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.messaging.AddQueue;
import org.wildfly.extras.creaper.commands.messaging.AddTopic;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class QueuesTopicsTestCase extends AbstractMessagingTestCase {

    private static final String QUEUE_TBA = "test-queue-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String QUEUE_TBR = "test-queue-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address QUEUE_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("jms-queue", QUEUE_TBA);
    private static final Address QUEUE_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("jms-queue", QUEUE_TBR);

    private static final String TOPIC = "test-topic_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TOPIC_TBA = "test-topic-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TOPIC_TBR = "test-topic-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address TOPIC_ADDRESS = DEFAULT_MESSAGING_SERVER.and("jms-topic", TOPIC);
    private static final Address TOPIC_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("jms-topic", TOPIC_TBA);
    private static final Address TOPIC_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("jms-topic", TOPIC_TBR);

    private static final String JNDI_NAME = "java:/jndi-queue_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String JNDI_TOPICS_NAME = "java:/jndi-topics";

    @BeforeClass
    public static void setUp() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        client.apply(new AddQueue.Builder(QUEUE_TBR).jndiEntries(getJNDIEntriesList()).build());
        client.apply(new AddTopic.Builder(TOPIC).jndiEntries(getJNDIEntriesList()).build());
        client.apply(new AddTopic.Builder(TOPIC_TBR).jndiEntries(getJNDIEntriesList()).build());
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException, IOException, OperationException {
        operations.removeIfExists(QUEUE_TBA_ADDRESS);
        operations.removeIfExists(QUEUE_TBR_ADDRESS);
        operations.removeIfExists(TOPIC_ADDRESS);
        operations.removeIfExists(TOPIC_TBA_ADDRESS);
        operations.removeIfExists(TOPIC_TBR_ADDRESS);
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.viewQueuesAndTopics("default");
        page.switchToJmsQueuesTopics();
        page.selectInTable(TOPIC);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addJmsQueue() throws Exception {
        page.addQueue(QUEUE_TBA, JNDI_NAME);
        new ResourceVerifier(QUEUE_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeJmsQueue() throws Exception {
        page.remove(QUEUE_TBR);
        new ResourceVerifier(QUEUE_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void addJmsTopics() throws Exception {
        page.getConfig().topicsConfig();
        page.addTopic(TOPIC_TBA, JNDI_TOPICS_NAME);
        new ResourceVerifier(TOPIC_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateTopicsJndiNames() throws InterruptedException, TimeoutException, IOException {
        page.getConfig().topicsConfig();
        page.selectInTable(TOPIC);

        String jndiName = "java:/jndi-name" + RandomStringUtils.randomAlphanumeric(5);

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.edit().text("entries", jndiName);
        boolean finished = editPanelFragment.save();

        administration.reloadIfRequired();

        assertTrue("Config should be saved and closed.", finished);

        ModelNodeResult result = operations.readAttribute(TOPIC_ADDRESS, "entries");

        Assert.assertTrue(result.hasDefinedValue() && result.stringValue().contains(jndiName));
    }

    @Test
    public void removeJmsTopics() throws Exception {
        page.getConfig().topicsConfig();
        page.remove(TOPIC_TBR);
        new ResourceVerifier(TOPIC_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private static List<String> getJNDIEntriesList() {
        List<String> jndi = new LinkedList<>();
        jndi.add("java:/jndi-entry-dummy_" + RandomStringUtils.randomAlphanumeric(5));
        return jndi;
    }

}
