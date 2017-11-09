package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.config.messaging.ProviderSettingsWindow;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.SELECT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;

@RunWith(Arquillian.class)
public class JournalTestCase extends AbstractMessagingTestCase {

    private ProviderSettingsWindow providerSettingsWindow;

    private static final String
            SERVER = "server",
            JOURNAL_TYPE = "journal-type",
            CREATE_JOURNAL_DIR = "create-journal-dir",
            JOURNAL_SYNC_NON_TRANSACTIONAL = "journal-sync-non-transactional",
            JOURNAL_SYNC_TRANSACTIONAL = "journal-sync-transactional",
            JOURNAL_MIN_FILES = "journal-min-files",
            JOURNAL_MAX_IO = "journal-max-io",
            JOURNAL_BUFFER_SIZE = "journal-buffer-size",
            JOURNAL_BUFFER_TIMEOUT = "journal-buffer-timeout",
            JOURNAL_COMPACT_MIN_FILES = "journal-compact-min-files",
            JOURNAL_COMPACT_PERCENTAGE = "journal-compact-percentage",
            JOURNAL_FILE_SIZE = "journal-file-size";

    private static final Address
            SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and(SERVER, randomAlphanumeric(7));

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(SERVER_ADDRESS).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, InterruptedException, TimeoutException {
        operations.removeIfExists(SERVER_ADDRESS);
        administration.reloadIfRequired();
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();
    }

    @Test
    public void updateJournalType() throws Exception {
        final String value = "NIO";

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(SELECT, JOURNAL_TYPE, value)
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_TYPE, value);
    }

    @Test
    public void updateCreateJournalDir() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(CHECKBOX, CREATE_JOURNAL_DIR, false)
                .verifyFormSaved()
                .verifyAttribute(CREATE_JOURNAL_DIR, false);
    }

    @Test
    public void updateJournalSyncNonTransactional() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(CHECKBOX, JOURNAL_SYNC_NON_TRANSACTIONAL, false)
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_SYNC_NON_TRANSACTIONAL, false);
    }

    @Test
    public void updateJournalSyncTransactional() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(CHECKBOX, JOURNAL_SYNC_TRANSACTIONAL, false)
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_SYNC_TRANSACTIONAL, false);
    }

    @Test
    public void updateJournalMinFilesInvalid() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, JOURNAL_MIN_FILES, "0")
                .verifyFormNotSaved();
    }

    @Test
    public void updateJournalMaxFilesWrongValue() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, JOURNAL_MAX_IO, "-1")
                .verifyFormNotSaved();
    }

    @Test
    public void updateJournalBufferSize() throws Exception {
        final String journalBufferSize = JOURNAL_BUFFER_SIZE;
        final long journalBufferSizeValue = 1024L;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalBufferSize, String.valueOf(journalBufferSizeValue))
                .verifyFormSaved()
                .verifyAttribute(journalBufferSize, journalBufferSizeValue);

        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalBufferSize, "")
                .verifyFormSaved() // undefine
                .verifyAttributeIsUndefined(journalBufferSize);
    }

    @Test
    public void updateJournalBufferTimeout() throws Exception {
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(ConfigChecker.InputType.TEXT, JOURNAL_BUFFER_TIMEOUT, String.valueOf(1L))
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_BUFFER_TIMEOUT, 1L);
    }

    @Test
    public void updateJournalCompactMinFiles() throws Exception {
        final int value = 1;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, JOURNAL_COMPACT_MIN_FILES, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_COMPACT_MIN_FILES, value);
    }

    @Test
    public void updateJournalCompactPercentage() throws Exception {
        final int value = 99;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, JOURNAL_COMPACT_PERCENTAGE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_COMPACT_PERCENTAGE, value);
    }

    @Test
    public void updateJournalFileSize() throws Exception {
        final long value = 4096;

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, JOURNAL_FILE_SIZE, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_FILE_SIZE, value);
    }

    @Test
    public void updateJournalDatasource() throws Exception {
        String journalDatasourceAttr = "journal-datasource",
                journalDatasourceName = "journalDatasource_" + randomAlphanumeric(5);
        try {
            createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                    .editAndSave(TEXT, journalDatasourceAttr, journalDatasourceName)
                    .verifyFormSaved()
                    .verifyAttribute(journalDatasourceAttr, journalDatasourceName);

            page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
            providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();

            createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                    .editAndSave(TEXT, journalDatasourceAttr, "")
                    .verifyFormSaved() // undefine
                    .verifyAttributeIsUndefined(journalDatasourceAttr);
        } finally {
            // needed to be sure it will be possible to remove JMS server in #tearDown()
            ModelNodeResult attrResult = operations.readAttribute(SERVER_ADDRESS, journalDatasourceAttr);
            if (attrResult.isSuccess() && attrResult.hasDefined(Constants.RESULT)) {
                operations.undefineAttribute(SERVER_ADDRESS, journalDatasourceAttr);
            }
        }
    }

    @Test
    public void updateJournalDatabase() throws Exception {
        String journalDatabaseAttr = "journal-database",
                journalDatabaseName = "journalDatabase_" + randomAlphanumeric(5);
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalDatabaseAttr, journalDatabaseName)
                .verifyFormSaved()
                .verifyAttribute(journalDatabaseAttr, journalDatabaseName);

        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalDatabaseAttr, "")
                .verifyFormSaved() // undefine
                .verifyAttributeIsUndefined(journalDatabaseAttr);
    }

    @Test
    public void updateJournalMessagesTable() throws Exception {
        String journalMessagesTableAttr = "journal-messages-table",
                journalMessagesTableName = "journalMessagesTable_" + randomAlphanumeric(5);
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalMessagesTableAttr, journalMessagesTableName)
                .verifyFormSaved()
                .verifyAttribute(journalMessagesTableAttr, journalMessagesTableName);

        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalMessagesTableAttr, "")
                .verifyFormSaved() // undefine
                .verifyAttribute(journalMessagesTableAttr, "MESSAGES"); // default name
    }

    @Test
    public void updateJournalLargeMessagesTable() throws Exception {
        String journalLargeMessagesTableAttr = "journal-large-messages-table",
                journalLargeMessagesTableName = "journalLargeMessagesTable_" + randomAlphanumeric(5);
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalLargeMessagesTableAttr, journalLargeMessagesTableName)
                .verifyFormSaved()
                .verifyAttribute(journalLargeMessagesTableAttr, journalLargeMessagesTableName);

        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalLargeMessagesTableAttr, "")
                .verifyFormSaved()
                .verifyAttribute(journalLargeMessagesTableAttr, "LARGE_MESSAGES");
    }

    @Test
    public void updateJournalBindingsTable() throws Exception {
        String journalBindingsTableAttr = "journal-bindings-table",
                journalBindingsTableName = "journalBindingsTable_" + randomAlphanumeric(5);
        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalBindingsTableAttr, journalBindingsTableName)
                .verifyFormSaved()
                .verifyAttribute(journalBindingsTableAttr, journalBindingsTableName);

        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();

        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalBindingsTableAttr, "")
                .verifyFormSaved() // undefine
                .verifyAttribute(journalBindingsTableAttr, "BINDINGS"); // default name
    }

    @Test
    public void updateJournalPageStoreTable() throws Exception {
        String journalPageStoreTableAttr = "journal-page-store-table",
                journalPageStoreTableName = "journalPageStoreTable_" + randomAlphanumeric(5);
        try {
            createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                    .editAndSave(TEXT, journalPageStoreTableAttr, journalPageStoreTableName)
                    .verifyFormSaved()
                    .verifyAttribute(journalPageStoreTableAttr, journalPageStoreTableName);
        } catch (NoSuchElementException e) {
            if (e.getMessage().contains(journalPageStoreTableAttr)) {
                Assert.fail("HAL-1281: " + journalPageStoreTableAttr + " should be visible and configurable!");
            } else {
                throw e;
            }
        }
        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToJournalTab();


        createConfigCheckerBuilderBasedOnServerMode(client, SERVER_ADDRESS, providerSettingsWindow)
                .editAndSave(TEXT, journalPageStoreTableAttr, "")
                .verifyFormSaved() // undefine
                .verifyAttribute(journalPageStoreTableAttr, "PAGE_STORE"); // default name
    }
}
