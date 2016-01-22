package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
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
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.messaging.AddAcceptor;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class RemoteAcceptorTestCase extends AbstractMessagingTestCase {

    private static final String REMOTE_ACCEPTOR = "remote-acceptor_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String REMOTE_ACCEPTOR_TBR = "remote-acceptor-TBR_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String REMOTE_ACCEPTOR_TBA = "remote-acceptor-TBA_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address REMOTE_ACCEPTOR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("remote-acceptor", REMOTE_ACCEPTOR);
    private static final Address REMOTE_ACCEPTOR_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("remote-acceptor", REMOTE_ACCEPTOR_TBR);
    private static final Address REMOTE_ACCEPTOR_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("remote-acceptor", REMOTE_ACCEPTOR_TBA);

    private static final String PROP_TBR_KEY = "prop123";

    @BeforeClass
    public static void setUp() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        client.apply(new AddAcceptor.RemoteBuilder(REMOTE_ACCEPTOR)
                .socketBinding(createSocketBinding())
                .param(PROP_TBR_KEY, "test")
                .build());
        client.apply(new AddAcceptor.RemoteBuilder(REMOTE_ACCEPTOR_TBR)
                .socketBinding(createSocketBinding())
                .build());
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(REMOTE_ACCEPTOR_ADDRESS);
        operations.removeIfExists(REMOTE_ACCEPTOR_TBA_ADDRESS);
        operations.removeIfExists(REMOTE_ACCEPTOR_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectConnectionsView();
        page.switchToAcceptor();
        page.switchToRemoteType();
        page.selectInTable(REMOTE_ACCEPTOR);
    }
    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addRemoteAcceptor() throws Exception {
        page.addRemoteAcceptor(REMOTE_ACCEPTOR_TBA, createSocketBinding());
        new ResourceVerifier(REMOTE_ACCEPTOR_TBA_ADDRESS, client).verifyExists();
    }

    @Test

    public void updateAcceptorSocketBinding() throws Exception {
        editTextAndVerify(REMOTE_ACCEPTOR_ADDRESS, "socketBinding", "socket-binding", createSocketBinding());
    }

    @Test
    public void addAcceptorProperty() throws IOException {
        String propKey = "prop_" + RandomStringUtils.randomAlphanumeric(5);
        boolean isClosed = page.addProperty(propKey, "test");
        Assert.assertTrue("Property should be added and wizard closed.", isClosed);
        Assert.assertTrue(PropertiesOps.isPropertyPresentInParams(REMOTE_ACCEPTOR_ADDRESS, client, propKey));
    }

    @Test
    public void removeAcceptorProperty() throws IOException {
        page.removeProperty(PROP_TBR_KEY);
        Assert.assertFalse(PropertiesOps.isPropertyPresentInParams(REMOTE_ACCEPTOR_ADDRESS, client, PROP_TBR_KEY));
    }

    @Test
    public void removeRemoteAcceptor() throws Exception {
        page.remove(REMOTE_ACCEPTOR_TBR);
        new ResourceVerifier(REMOTE_ACCEPTOR_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
