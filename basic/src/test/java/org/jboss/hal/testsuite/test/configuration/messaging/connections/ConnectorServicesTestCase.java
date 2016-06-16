package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

@Ignore("After validation on presence of selected ConnectorServiceFactory was added it's now hard to tests this "
        + "since no implementation of ConnectorServiceFactory is part of ActiveMQ module so far. Moreover this feature "
        + "is proposed to be removed from console as useless - see https://issues.jboss.org/browse/JBEAP-4467. "
        + "Untill this test case will be completelly rewritten it will be ignored. "
        + "In case this feature will be per JBEAP-4467 removed the testcase will be removed as well.")
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ConnectorServicesTestCase extends AbstractMessagingTestCase {
    /* There should be no reload performed in this test case because it is error prone and possibly causes crash of
     * ActiveMQ. See note below.
     * After validation of factory-class value added and due to https://issues.jboss.org/browse/JBEAP-4467 this will be ignored.*/

    private static final String CONNECTOR_SERVICE = "connector-service_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTOR_SERVICE_TBA = "connector-service-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTOR_SERVICE_TBR = "connector-service-TBR_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address CONNECTOR_SERVICE_ADDRESS = DEFAULT_MESSAGING_SERVER.and("connector-service", CONNECTOR_SERVICE);
    private static final Address CONNECTOR_SERVICE_ADDRESS_TBR = DEFAULT_MESSAGING_SERVER.and("connector-service", CONNECTOR_SERVICE_TBR);
    private static final Address CONNECTOR_SERVICE_ADDRESS_TBA = DEFAULT_MESSAGING_SERVER.and("connector-service", CONNECTOR_SERVICE_TBA);

    private static final String PROPERTY_TBR_KEY = "key4";

    /* No implementation of org.apache.activemq.artemis.core.server.ConnectorServiceFactory is currently present in
     * ActiveMQ module so filling value is used instead. This value should be only used to verify if value is correctly
     * propagated to model from web console configuration.*/
    private static final String FACTORY_CLASS = "clazz";

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(CONNECTOR_SERVICE_ADDRESS, Values.of("factory-class", FACTORY_CLASS)).assertSuccess();
        PropertiesOps.addProperty(CONNECTOR_SERVICE_ADDRESS, client, PROPERTY_TBR_KEY, "test");
        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS, client).verifyExists();
        operations.add(CONNECTOR_SERVICE_ADDRESS_TBR, Values.of("factory-class", FACTORY_CLASS));
        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS_TBR, client).verifyExists();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(CONNECTOR_SERVICE_ADDRESS);
        operations.removeIfExists(CONNECTOR_SERVICE_ADDRESS_TBA);
        operations.removeIfExists(CONNECTOR_SERVICE_ADDRESS_TBR);
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectConnectionsView();
        page.switchToConnectorServices();
        page.selectInTable(CONNECTOR_SERVICE);
    }

    @Test
    public void addConnectorServices() throws Exception {
        page.addConnetorServices(CONNECTOR_SERVICE_TBA, FACTORY_CLASS);
        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS_TBA, client).verifyExists();
    }

    @Test
    public void updateConnectorServicesServicesFactoryClass() throws Exception {
        new ConfigChecker.Builder(client, CONNECTOR_SERVICE_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, "factoryClass", FACTORY_CLASS)
                .verifyFormSaved()
                .verifyAttribute("factory-class", FACTORY_CLASS);
    }

    @Test
    public void addConnectorServicesProperty() throws Exception {
        boolean isClosed = page.addProperty("prop", "test");
        assertTrue("Property should be added and wizard closed.", isClosed);

        Assert.assertTrue(PropertiesOps.isPropertyPresentInParams(CONNECTOR_SERVICE_ADDRESS, client, "prop"));
    }

    @Test
    public void removeConnectorServicesProperties() throws Exception {
        page.removeProperty(PROPERTY_TBR_KEY);
        Assert.assertFalse(PropertiesOps.isPropertyPresentInParams(CONNECTOR_SERVICE_ADDRESS, client, PROPERTY_TBR_KEY));
    }

    @Test
    public void removeConnectorServices() throws Exception {
        page.selectInTable(CONNECTOR_SERVICE_TBR, 0);
        page.remove();

        new ResourceVerifier(CONNECTOR_SERVICE_ADDRESS_TBR, client).verifyDoesNotExist();
    }
}
