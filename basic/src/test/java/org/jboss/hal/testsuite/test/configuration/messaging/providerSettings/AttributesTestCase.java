package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.config.messaging.ProviderSettingsWindow;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;

@RunWith(Arquillian.class)
public class AttributesTestCase extends AbstractMessagingTestCase {

    private static final String
            MANAGEMENT_NOTIFICATION_ADDRESS = "management-notification-address",
            WILD_CARD_ROUTING_ENABLED = "wild-card-routing-enabled",
            PERSISTENCE_ENABLED = "persistence-enabled",
            PERSIST_ID_CACHE = "persist-id-cache",
            THREAD_POOL_MAX_SIZE = "thread-pool-max-size",
            SCHEDULED_THREAD_POOL_MAX_SIZE = "scheduled-thread-pool-max-size",
            TRANSACTION_TIMEOUT_SCAN_PERIOD = "transaction-timeout-scan-period",
            MANAGEMENT_ADDRESS = "management-address",
            STATISTICS_ENABLED = "statistics-enabled",
            TRANSACTION_TIMEOUT = "transaction-timeout";

    private static final Address SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and("server", RandomStringUtils.randomAlphanumeric(5));

    private ProviderSettingsWindow wizardWindow;

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(SERVER_ADDRESS).assertSuccess();
    }

    @Before
    public void before() {
        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        wizardWindow = page.providerSettingsWindow();
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
        final String value = "management";

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(TEXT, MANAGEMENT_ADDRESS, value)
                .verifyFormSaved()
                .verifyAttribute(MANAGEMENT_ADDRESS, value);
    }

    @Test
    public void updateNotificationAddress() throws Exception {
        final String value = "jms.topic.randomTopic";

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(TEXT, MANAGEMENT_NOTIFICATION_ADDRESS, value)
                .verifyFormSaved()
                .verifyAttribute(MANAGEMENT_NOTIFICATION_ADDRESS, value);
    }

    @Test
    public void updateThreadPollMaxSize() throws Exception {
        final int value = -1;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(TEXT, THREAD_POOL_MAX_SIZE, value)
                .verifyFormSaved()
                .verifyAttribute(THREAD_POOL_MAX_SIZE, value);
    }

    @Test
    public void updateScheduledThreadPollMaxSize() throws Exception {
        final int value = 10;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(TEXT, SCHEDULED_THREAD_POOL_MAX_SIZE, value)
                .verifyFormSaved()
                .verifyAttribute(SCHEDULED_THREAD_POOL_MAX_SIZE, value);
    }

    @Test
    public void updateTransactionTimeout() throws Exception {
        final long value = 1;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(TEXT, TRANSACTION_TIMEOUT, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(TRANSACTION_TIMEOUT, value);
    }

    @Test
    public void updateTransactionTimeoutWrongValue() throws Exception {
        final long value = -2;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(TEXT, TRANSACTION_TIMEOUT, String.valueOf(value))
                .verifyFormNotSaved();
    }

    @Test
    public void updateTransactionTimeoutScanPeriod() throws Exception {
        final long value = 100000;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(TEXT, TRANSACTION_TIMEOUT_SCAN_PERIOD, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(TRANSACTION_TIMEOUT_SCAN_PERIOD, value);
    }

    @Test
    public void updateStatisticsEnabled() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(CHECKBOX, STATISTICS_ENABLED, true)
                .verifyFormSaved()
                .verifyAttribute(STATISTICS_ENABLED, true);
    }

    @Test
    public void updateWildCardRoutingEnabled() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(CHECKBOX, WILD_CARD_ROUTING_ENABLED, false)
                .verifyFormSaved()
                .verifyAttribute(WILD_CARD_ROUTING_ENABLED, false);
    }

    @Test
    public void updatePersistenceEnabled() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(CHECKBOX, PERSISTENCE_ENABLED, false)
                .verifyFormSaved()
                .verifyAttribute(PERSISTENCE_ENABLED, false);
    }

    @Test
    public void updatePersistIdCache() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, wizardWindow)
                .editAndSave(CHECKBOX, PERSIST_ID_CACHE, false)
                .verifyFormSaved()
                .verifyAttribute(PERSIST_ID_CACHE, false);
    }
}
