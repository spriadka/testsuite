package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddAcceptor;
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
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;


@RunWith(Arquillian.class)
@Category(Shared.class)
public class InVMAcceptorTestCase extends AbstractMessagingTestCase {

    private static final String IN_VM_ACCEPTOR = "in-vm-acceptor_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String IN_VM_ACCEPTOR_TBA = "in-vm-acceptor-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String IN_VM_ACCEPTOR_TBR = "in-vm-acceptor-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address IN_VM_ACCEPTOR_ADDRESS = DEFAULT_MESSAGING_SERVER
            .and("in-vm-acceptor", IN_VM_ACCEPTOR);
    private static final Address IN_VM_ACCEPTOR_ADDRESS_TBA = DEFAULT_MESSAGING_SERVER
            .and("in-vm-acceptor", IN_VM_ACCEPTOR_TBA);
    private static final Address IN_VM_ACCEPTOR_ADDRESS_TBR = DEFAULT_MESSAGING_SERVER
            .and("in-vm-acceptor", IN_VM_ACCEPTOR_TBR);

    private static final String PROPERTY_TBR_KEY = "prop42";
    private static final int SERVER_ID_BOUND = 1000000;

    @BeforeClass
    public static void setUp() throws CommandFailedException {
        client.apply(new AddAcceptor.InVmBuilder(IN_VM_ACCEPTOR)
                .serverId(ThreadLocalRandom.current().nextInt(SERVER_ID_BOUND))
                .param(PROPERTY_TBR_KEY, "test")
                .build());
        client.apply(new AddAcceptor.InVmBuilder(IN_VM_ACCEPTOR_TBR)
                .serverId(ThreadLocalRandom.current().nextInt(SERVER_ID_BOUND))
                .build());
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(IN_VM_ACCEPTOR_ADDRESS);
        operations.removeIfExists(IN_VM_ACCEPTOR_ADDRESS_TBA);
        operations.removeIfExists(IN_VM_ACCEPTOR_ADDRESS_TBR);
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
        page.switchToInVmType();
        page.selectInTable(IN_VM_ACCEPTOR);
    }
    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addInVmAcceptor() throws Exception {
        page.addInVmAcceptor(IN_VM_ACCEPTOR_TBA, "123456");
        new ResourceVerifier(IN_VM_ACCEPTOR_ADDRESS_TBA, client).verifyExists();
    }

    @Test
    public void updateAcceptorServerID() throws Exception {
        editTextAndVerify(IN_VM_ACCEPTOR_ADDRESS, "serverId", "server-id", ThreadLocalRandom.current().nextInt(SERVER_ID_BOUND));
    }

    @Test
    public void updateAcceptorProperties() throws IOException {
        boolean isClosed = page.addProperty("prop", "test");
        Assert.assertTrue("Property should be added and wizard closed.", isClosed);
        Assert.assertTrue(PropertiesOps.isPropertyPresentInParams(IN_VM_ACCEPTOR_ADDRESS, client, "prop"));
    }

    @Test
    public void removeAcceptorProperties() throws IOException {
        page.removeProperty(PROPERTY_TBR_KEY);
        Assert.assertFalse(PropertiesOps.isPropertyPresentInParams(IN_VM_ACCEPTOR_ADDRESS, client, PROPERTY_TBR_KEY));
    }

    @Test
    public void removeInVmAcceptor() throws Exception {
        page.selectInTable(IN_VM_ACCEPTOR_TBR, 0);
        page.remove();

        new ResourceVerifier(IN_VM_ACCEPTOR_ADDRESS_TBR, client).verifyDoesNotExist();
    }
}
