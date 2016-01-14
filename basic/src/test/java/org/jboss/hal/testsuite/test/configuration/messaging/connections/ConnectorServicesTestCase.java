package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
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
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
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
public class ConnectorServicesTestCase extends AbstractMessagingTestCase {

    private static final String CONNECTOR_SERVICE = "connector-service_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTOR_SERVICE_TBA = "connector-service-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTOR_SERVICE_TBR = "connector-service-TBR_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address CONNECTOR_SERVICE_ADDRESS = DEFAULT_MESSAGING_SERVER.and("connector-service", CONNECTOR_SERVICE);
    private static final Address CONNECTOR_SERVICE_ADDRESS_TBR = DEFAULT_MESSAGING_SERVER.and("connector-service", CONNECTOR_SERVICE_TBR);
    private static final Address CONNECTOR_SERVICE_ADDRESS_TBA = DEFAULT_MESSAGING_SERVER.and("connector-service", CONNECTOR_SERVICE_TBA);

    private static final String PROPERTY_TBR_KEY = "key4";

    private static final String FACTORY_CLASS = "factoryClass";

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(CONNECTOR_SERVICE_ADDRESS, Values.of("factory-class", FACTORY_CLASS));
        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS, client).verifyExists();
        operations.add(CONNECTOR_SERVICE_ADDRESS_TBR, Values.of("factory-class", FACTORY_CLASS));
        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS_TBR, client).verifyExists();
        administration.reloadIfRequired();
        addPropertyToConnectorService(PROPERTY_TBR_KEY, "value2"); //Property which will be removed
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(CONNECTOR_SERVICE_ADDRESS);
        operations.removeIfExists(CONNECTOR_SERVICE_ADDRESS_TBA);
        operations.removeIfExists(CONNECTOR_SERVICE_ADDRESS_TBR);
        administration.reloadIfRequired();
    }

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchToConnectorServices();
        page.selectInTable(CONNECTOR_SERVICE, 0);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addConnectorServices() throws Exception {
        page.addConnetorServices(CONNECTOR_SERVICE_TBA, FACTORY_CLASS);
        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS_TBA, client).verifyExists();
    }

    @Test
    public void updateConnectorServicesServicesFactoryClass() throws Exception {
        editTextAndVerify(CONNECTOR_SERVICE_ADDRESS, "factoryClass", "factory-class", "fc");
    }

    @Test
    public void updateConnectorServicesProperties() throws Exception {
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        ConfigPropertyWizard wizard = properties.addProperty();

        wizard.name("prop").value("test").clickSave();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        assertTrue("Property should be added and wizard closed.", wizard.isClosed());

        Assert.assertTrue(isPropertyPresentInParams("prop"));
    }

    @Test
    public void removeConnectorServicesProperties() throws Exception {
        ConfigPropertiesFragment config = page.getConfig().propertiesConfig();
        ResourceManager properties = config.getResourceManager();
        properties.removeResource(PROPERTY_TBR_KEY).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(isPropertyPresentInParams(PROPERTY_TBR_KEY));
    }

    @Test
    public void removeConnectorServices() throws Exception {
        page.selectInTable(CONNECTOR_SERVICE_TBR, 0);
        page.remove();

        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS_TBR, client).verifyDoesNotExist();
    }

    private static void addPropertyToConnectorService(String key, String value) throws IOException {
        ModelNode property = new ModelNode().set(key, value).asObject();
        operations.writeAttribute(CONNECTOR_SERVICE_ADDRESS, "params", property);
    }

    private boolean isPropertyPresentInParams(String key) throws IOException {
        ModelNodeResult result = operations.readAttribute(CONNECTOR_SERVICE_ADDRESS, "params");
        return result.value().keys().contains(key);
    }

}
