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
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 7.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class GenericConnectorTestCase extends AbstractMessagingTestCase {

    private static final String GENERIC_CONNECTOR = "generic-connector_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String GENERIC_CONNECTOR_TBA = "generic-connector-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String GENERIC_CONNECTOR_TBR = "generic-connector-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address GENERIC_CONNECTOR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("connector", GENERIC_CONNECTOR);
    private static final Address GENERIC_CONNECTOR_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("connector", GENERIC_CONNECTOR_TBA);
    private static final Address GENERIC_CONNECTOR_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("connector", GENERIC_CONNECTOR_TBR);

    private static final String PROPERTY_TBR_KEY = "prop42";
    private static final String FACTORY_CLASS = "factoryClass";

    @BeforeClass
    public static void setUp() throws Exception {
        addGenericConnector(GENERIC_CONNECTOR_ADDRESS);
        new ResourceVerifier(GENERIC_CONNECTOR_ADDRESS, client).verifyExists();
        operations.writeAttribute(GENERIC_CONNECTOR_ADDRESS, "params." + PROPERTY_TBR_KEY, "marvin");
        addGenericConnector(GENERIC_CONNECTOR_TBR_ADDRESS);
        new ResourceVerifier(GENERIC_CONNECTOR_TBR_ADDRESS, client).verifyExists();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(GENERIC_CONNECTOR_ADDRESS);
        operations.removeIfExists(GENERIC_CONNECTOR_TBA_ADDRESS);
        operations.removeIfExists(GENERIC_CONNECTOR_TBR_ADDRESS);
    }

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToConnector();
        page.switchType("Type: Generic");
        page.selectInTable(GENERIC_CONNECTOR, 0);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addGenericConnector() throws Exception {
        page.addGenericAcceptor(GENERIC_CONNECTOR_TBA, createSocketBinding(), FACTORY_CLASS);
        new ResourceVerifier(GENERIC_CONNECTOR_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void editSocketBinding() throws Exception {
       editTextAndVerify(GENERIC_CONNECTOR_ADDRESS, "socketBinding", "socket-binding", createSocketBinding());
    }

    @Test
    public void editFactoryClass() throws Exception {
        editTextAndVerify(GENERIC_CONNECTOR_ADDRESS, "factoryClass", "factory-class", "fc");
    }

    @Test
    public void addConnectorProperty() throws IOException {
        boolean isClosed = page.addProperty("prop", "test");
        assertTrue("Property should be added and wizard closed.", isClosed);

        Assert.assertTrue(PropertiesOps.isPropertyPresentInParams(GENERIC_CONNECTOR_ADDRESS, "prop"));
    }

    @Test
    public void removeConnectorProperty() throws IOException {
        page.removeProperty(PROPERTY_TBR_KEY);
        Assert.assertFalse(PropertiesOps.isPropertyPresentInParams(GENERIC_CONNECTOR_ADDRESS, PROPERTY_TBR_KEY));
    }

    @Test //https://issues.jboss.org/browse/HAL-830
    public void removeGenericConnector() throws Exception {
        page.selectInTable(GENERIC_CONNECTOR_TBR, 0);
        page.remove();
        System.out.print('a');
        new ResourceVerifier(GENERIC_CONNECTOR_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private static boolean addGenericConnector(Address address) throws IOException, CommandFailedException {
        String socketBinding = "genericAcceptorSocketBinding_" + RandomStringUtils.randomAlphanumeric(5);
        createSocketBinding(socketBinding);
        return operations.add(address, Values
                .of("socket-binding", socketBinding)
                .and("factory-class", FACTORY_CLASS)).isSuccess();
    }
}
