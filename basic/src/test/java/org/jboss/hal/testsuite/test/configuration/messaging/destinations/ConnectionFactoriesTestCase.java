package org.jboss.hal.testsuite.test.configuration.messaging.destinations;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ConnectionFactoriesTestCase extends AbstractMessagingTestCase {

    private static final String CONNECTION_FACTORY_TBR = "connFactory-TBR_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTION_FACTORY_TBA = "connFactory-TBA_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address CONN_FACTORY_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER
            .and("connection-factory", CONNECTION_FACTORY_TBA);
    private static final Address CONN_FACTORY_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER
            .and("connection-factory", CONNECTION_FACTORY_TBR);


    private static final String JNDI_NAME_TBA = "java:/jndi-cf-" + RandomStringUtils.randomAlphanumeric(5);
    private static final String CONNECTOR_TBA = "http-connector";

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        createConnectionFactory(CONN_FACTORY_TBR_ADDRESS, "java:/" + CONNECTION_FACTORY_TBR, CONNECTOR_TBA);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectConnectionsView();
        page.switchToConnectionFactories();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(CONN_FACTORY_TBA_ADDRESS);
        operations.removeIfExists(CONN_FACTORY_TBR_ADDRESS);
    }

    @Test
    public void addConnectionFactory() throws Exception {
        page.addFactory(CONNECTION_FACTORY_TBA, JNDI_NAME_TBA, CONNECTOR_TBA);
        new ResourceVerifier(CONN_FACTORY_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeConnectionFactory() throws Exception {
        page.remove(CONNECTION_FACTORY_TBR);
        new ResourceVerifier(CONN_FACTORY_TBA_ADDRESS, client).verifyDoesNotExist();
    }

    private static void createConnectionFactory(Address address, String jndiName, String connector) throws Exception {
        operations.add(address, Values.empty()
                .andList("entries", jndiName)
                .andList("connectors", connector));
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyExists();
    }
}
