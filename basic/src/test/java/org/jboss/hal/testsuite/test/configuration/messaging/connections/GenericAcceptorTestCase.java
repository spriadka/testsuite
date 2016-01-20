package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.mina.util.AvailablePortFinder;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

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
        addGenericAcceptor(GENERIC_ACCEPTOR_ADDRESS);
        new ResourceVerifier(GENERIC_ACCEPTOR_ADDRESS, client);
        operations.writeAttribute(GENERIC_ACCEPTOR_ADDRESS, "params." + PROPERTY_TBR_KEY, "marvin");
        addGenericAcceptor(GENERIC_ACCEPTOR_ADDRESS_TBR);
        new ResourceVerifier(GENERIC_ACCEPTOR_ADDRESS_TBR, client);
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
        page.selectView("Connections");
        page.switchType("Type: Generic");
        page.selectInTable(GENERIC_ACCEPTOR, 0);
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
        String key = "prop";
        String value = "test";
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        ConfigPropertyWizard wizard = properties.addProperty();
        wizard.name(key).value(value).clickSave();

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        assertTrue("Property should be added and wizard closed.", wizard.isClosed());
        Assert.assertTrue(isPropertyPresentInParams(GENERIC_ACCEPTOR_ADDRESS, key));
    }

    @Test
    public void removeAcceptorProperty() throws Exception {
        ConfigPropertiesFragment config = page.getConfig().propertiesConfig();
        ResourceManager properties = config.getResourceManager();
        properties.removeResource(PROPERTY_TBR_KEY).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(isPropertyPresentInParams(GENERIC_ACCEPTOR_ADDRESS, PROPERTY_TBR_KEY));
    }

    @Test
    public void removeGenericAcceptor() throws Exception {
        page.selectInTable(GENERIC_ACCEPTOR_TBR, 0);
        page.remove();

        new ResourceVerifier(GENERIC_ACCEPTOR_ADDRESS_TBR, client).verifyDoesNotExist();
    }

    private static boolean addGenericAcceptor(Address address) throws IOException, CommandFailedException {
        String socketBinding = "genericAcceptorSocketBinding_" + RandomStringUtils.randomAlphanumeric(5);
        createSocketBinding(socketBinding);
        return operations.add(address, Values
                .of("socket-binding", socketBinding)
                .and("factory-class", FACTORY_CLASS)).isSuccess();
    }


}
