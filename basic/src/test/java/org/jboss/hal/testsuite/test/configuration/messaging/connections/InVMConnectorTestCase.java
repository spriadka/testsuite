package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
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
import org.wildfly.extras.creaper.commands.messaging.AddConnector;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;


@RunWith(Arquillian.class)
@Category(Shared.class)
public class InVMConnectorTestCase extends AbstractMessagingTestCase {

    private static final String IN_VM_CONNECTOR = "in-vm-connector_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String IN_VM_CONNECTOR_TBA = "in-vm-connector-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String IN_VM_CONNECTOR_TBR = "in-vm-connector-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address IN_VM_CONNECTOR_ADDRESS = DEFAULT_MESSAGING_SERVER
            .and("in-vm-connector", IN_VM_CONNECTOR);
    private static final Address IN_VM_CONNECTOR_ADDRESS_TBA = DEFAULT_MESSAGING_SERVER
            .and("in-vm-connector", IN_VM_CONNECTOR_TBA);
    private static final Address IN_VM_CONNECTOR_ADDRESS_TBR = DEFAULT_MESSAGING_SERVER
            .and("in-vm-connector", IN_VM_CONNECTOR_TBR);

    private static final String PROPERTY_TBR_KEY = "prop42";

    @BeforeClass
    public static void setUp() throws CommandFailedException {
        client.apply(new AddConnector.InVmBuilder(IN_VM_CONNECTOR)
                .serverId(ThreadLocalRandom.current().nextInt(100000))
                .param(PROPERTY_TBR_KEY, "test")
                .build());
        client.apply(new AddConnector.InVmBuilder(IN_VM_CONNECTOR_TBR)
                .serverId(ThreadLocalRandom.current().nextInt(100000))
                .build());
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(IN_VM_CONNECTOR_ADDRESS);
        operations.removeIfExists(IN_VM_CONNECTOR_ADDRESS_TBA);
        operations.removeIfExists(IN_VM_CONNECTOR_ADDRESS_TBR);
    }

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectConnectionsView();
        page.switchToConnector();
        page.switchToInVmType();
        page.selectInTable(IN_VM_CONNECTOR);
    }
    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addInVmAcceptor() throws Exception {
        page.addInVmAcceptor(IN_VM_CONNECTOR_TBA, "123456");
        new ResourceVerifier(IN_VM_CONNECTOR_ADDRESS_TBA, client).verifyExists();
    }

    @Test
    public void updateConnectorServerID() throws Exception {
        editTextAndVerify(IN_VM_CONNECTOR_ADDRESS, "serverId", "server-id", ThreadLocalRandom.current().nextInt(100000));
    }

    @Test
    public void updateConnectorProperties() throws IOException {
        boolean isClosed = page.addProperty("prop", "test");
        Assert.assertTrue("Property should be added and wizard closed.", isClosed);
        Assert.assertTrue(PropertiesOps.isPropertyPresentInParams(IN_VM_CONNECTOR_ADDRESS, client, "prop"));
    }

    @Test
    public void removeConnectorProperties() throws IOException {
        page.removeProperty(PROPERTY_TBR_KEY);
        Assert.assertFalse(PropertiesOps.isPropertyPresentInParams(IN_VM_CONNECTOR_ADDRESS, client, PROPERTY_TBR_KEY));
    }

    @Test
    public void removeInVmConnector() throws Exception {
        page.selectInTable(IN_VM_CONNECTOR_TBR, 0);
        page.remove();

        new ResourceVerifier(IN_VM_CONNECTOR_ADDRESS_TBR, client).verifyDoesNotExist();
    }
}
