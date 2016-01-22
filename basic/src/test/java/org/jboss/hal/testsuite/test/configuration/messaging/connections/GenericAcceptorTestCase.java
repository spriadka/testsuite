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

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class GenericAcceptorTestCase extends AbstractMessagingTestCase {

    private static final String GENERIC_ACCEPTOR = "generic-acceptor_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String GENERIC_ACCEPTOR_TBR = "generic-acceptor-TBR_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String GENERIC_ACCEPTOR_TBA = "generic-acceptor-TBA_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address GENERIC_ACCEPTOR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("acceptor", GENERIC_ACCEPTOR);
    private static final Address GENERIC_ACCEPTOR_ADDRESS_TBR = DEFAULT_MESSAGING_SERVER.and("acceptor", GENERIC_ACCEPTOR_TBR);
    private static final Address GENERIC_ACCEPTOR_ADDRESS_TBA = DEFAULT_MESSAGING_SERVER.and("acceptor", GENERIC_ACCEPTOR_TBA);

    private static final String PROPERTY_TBR_KEY = "prop42";
    private static final String FACTORY_CLASS = "factoryClass";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        client.apply(new AddAcceptor.GenericBuilder(GENERIC_ACCEPTOR)
                .param(PROPERTY_TBR_KEY, "testThis")
                .socketBinding(createSocketBinding())
                .factoryClass(FACTORY_CLASS)
                .build());
        client.apply(new AddAcceptor.GenericBuilder(GENERIC_ACCEPTOR_TBR)
                .param(PROPERTY_TBR_KEY, "testThis")
                .socketBinding(createSocketBinding())
                .factoryClass(FACTORY_CLASS)
                .build());
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(GENERIC_ACCEPTOR_ADDRESS);
        operations.removeIfExists(GENERIC_ACCEPTOR_ADDRESS_TBA);
        operations.removeIfExists(GENERIC_ACCEPTOR_ADDRESS_TBR);
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
        page.switchToGenericType();
        page.selectInTable(GENERIC_ACCEPTOR);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addGenericAcceptor() throws Exception {
        String socketBinding = createSocketBinding();
        page.addGenericAcceptor(GENERIC_ACCEPTOR_TBA, socketBinding, FACTORY_CLASS);
        new ResourceVerifier(GENERIC_ACCEPTOR_ADDRESS_TBA, client).verifyExists();
    }

    @Test
    public void editSocketBinding() throws Exception {
        String socketBindingName = "genericAcceptorSocketBinding_" + RandomStringUtils.randomAlphanumeric(5);
        createSocketBinding(socketBindingName);
        editTextAndVerify(GENERIC_ACCEPTOR_ADDRESS, "socketBinding", "socket-binding", socketBindingName);
    }

    @Test
    public void editFactoryClass() throws Exception {
        editTextAndVerify(GENERIC_ACCEPTOR_ADDRESS, "factoryClass", "factory-class", "fc");
    }

    @Test
    public void addPropertyToAcceptor() throws Exception {
        boolean isClosed = page.addProperty("prop", "test");
        assertTrue("Property should be added and wizard closed.", isClosed);

        Assert.assertTrue(PropertiesOps.isPropertyPresentInParams(GENERIC_ACCEPTOR_ADDRESS, client, "prop"));
    }

    @Test
    public void removeAcceptorProperty() throws Exception {
        page.removeProperty(PROPERTY_TBR_KEY);
        Assert.assertFalse(PropertiesOps.isPropertyPresentInParams(GENERIC_ACCEPTOR_ADDRESS, client, PROPERTY_TBR_KEY));
    }

    @Test
    public void removeGenericAcceptor() throws Exception {
        page.remove(GENERIC_ACCEPTOR_TBR);
        new ResourceVerifier(GENERIC_ACCEPTOR_ADDRESS_TBR, client).verifyDoesNotExist();
    }

}
