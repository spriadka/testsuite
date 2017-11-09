package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.messaging.AddMessagingConnector;
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
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class RemoteConnectorTestCase extends AbstractMessagingTestCase {

    private static final String REMOTE_CONNECTOR = "remote-connector_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String REMOTE_CONNECTOR_TBR = "remote-connector-TBR_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String REMOTE_CONNECTOR_TBA = "remote-connector-TBA_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address REMOTE_CONNECTOR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("remote-connector", REMOTE_CONNECTOR);
    private static final Address REMOTE_CONNECTOR_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("remote-connector", REMOTE_CONNECTOR_TBR);
    private static final Address REMOTE_CONNECTOR_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("remote-connector", REMOTE_CONNECTOR_TBA);

    private static final String PROP_TBR_KEY = "prop123";


    @BeforeClass
    public static void setUp() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        client.apply(new AddMessagingConnector.RemoteBuilder(REMOTE_CONNECTOR)
                .param(PROP_TBR_KEY, "test")
                .socketBinding(createSocketBinding())
                .build());
        client.apply(new AddMessagingConnector.RemoteBuilder(REMOTE_CONNECTOR_TBR)
                .socketBinding(createSocketBinding())
                .build());
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, InterruptedException, TimeoutException {
        operations.removeIfExists(REMOTE_CONNECTOR_ADDRESS);
        operations.removeIfExists(REMOTE_CONNECTOR_TBR_ADDRESS);
        operations.removeIfExists(REMOTE_CONNECTOR_TBA_ADDRESS);
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.viewConnectionSettings("default");
        page.switchToConnector();
        page.selectInTable(REMOTE_CONNECTOR);
    }
    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addRemoteConnector() throws Exception {
        page.addRemoteAcceptor(REMOTE_CONNECTOR_TBA, createSocketBinding());
        new ResourceVerifier(REMOTE_CONNECTOR_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateConnectorSocketBinding() throws Exception {
        editTextAndVerify(REMOTE_CONNECTOR_ADDRESS, "socketBinding", "socket-binding", createSocketBinding());
    }

    @Test
    public void updateConnectorProperties() throws IOException {
        String propKey = "prop123_" + RandomStringUtils.randomAlphanumeric(5);
        boolean isClosed = page.addProperty(propKey, "yipeeeee");
        Assert.assertTrue("Property should be added and wizard closed.", isClosed);
        Assert.assertTrue(PropertiesOps.isPropertyPresentInParams(REMOTE_CONNECTOR_ADDRESS, client, propKey));
    }

    @Test
    public void removeConnectorProperties() throws IOException {
        page.removeProperty(PROP_TBR_KEY);
        Assert.assertFalse(PropertiesOps.isPropertyPresentInParams(REMOTE_CONNECTOR_ADDRESS, client, PROP_TBR_KEY));
    }

    @Test
    public void removeRemoteConnector() throws Exception {
        page.remove(REMOTE_CONNECTOR_TBR);
        new ResourceVerifier(REMOTE_CONNECTOR_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
