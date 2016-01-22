package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class AttributesTestCase extends AbstractMessagingTestCase {

    private static final String SERVER_NAME = "test-provider_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and("server", SERVER_NAME);

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(SERVER_ADDRESS);
    }

    @Before
    public void before() {
        page.selectProvider(SERVER_NAME);
        page.InvokeProviderSettings();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(SERVER_ADDRESS);
    }

    @Test
    public void updateManagementAddress() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "management-address", "management");
    }

    @Test
    public void updateNotificationAddress() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "management-notification-address", "a.n");
    }

    @Test
    public void updateThreadPollMaxSize() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "thread-pool-max-size", -1);
    }

    @Test
    public void updateScheduledThreadPollMaxSize() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "scheduled-thread-pool-max-size", 10);
    }

    @Test
    public void updateTransactionTimeout() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "transaction-timeout", 1L);
    }

    @Test
    public void updateTransactionTimeoutWrongValue() {
        verifyIfErrorAppears("transaction-timeout", "-2");
    }

    @Test
    public void updateTransactionTimeoutScanPeriod() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "transaction-timeout-scan-period", 100000L);
    }

    @Test
    public void updateStatisticsEnabled() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "statistics-enabled", true);
    }

    @Test
    public void updateWildCardRoutingEnabled() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "wild-card-routing-enabled", false);
    }

    @Test
    public void updatePersistenceEnabled() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "persistence-enabled", false);
    }

    @Test
    public void updatePersistIdCache() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "persist-id-cache", false);
    }
}
