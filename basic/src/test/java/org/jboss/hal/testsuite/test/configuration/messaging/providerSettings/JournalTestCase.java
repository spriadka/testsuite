package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class JournalTestCase extends AbstractMessagingTestCase {
    private static final String SERVER_NAME = "test-provider_" + randomAlphanumeric(5);

    private static final Address SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and("server", SERVER_NAME);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(SERVER_ADDRESS);
        Address journalPathAddress = SERVER_ADDRESS.and("path", "journal-directory");
        operations.add(journalPathAddress, Values.of("path", "test-journal").and("relative-to", "jboss.server.data.dir"));
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, InterruptedException, TimeoutException {
        operations.removeIfExists(SERVER_ADDRESS);
        administration.restart();
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.invokeProviderSettings(SERVER_NAME);
        page.switchToJournalTab();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void updateJournalType() throws Exception {
        selectOptionAndVerify(SERVER_ADDRESS, "journal-type", "NIO");
    }

    @Test
    public void updateCreateJournalDir() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "create-journal-dir", false);
    }

    @Test
    public void updateJournalSyncNonTransactional() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "journal-sync-non-transactional", false);
    }

    @Test
    public void updateJournalSyncTransactional() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "journal-sync-transactional", false);
    }

    @Test
    public void updateJournalMinFilesInvalid() throws Exception {
        verifyIfErrorAppears("journal-min-files", "0");
    }

    @Test
    public void updateJournalMaxFilesWrongValue() {
        verifyIfErrorAppears("journal-max-io", "-1");
    }

    @Test
    public void updateJournalBufferSize() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-buffer-size", 1024L);
        undefineAndVerify(SERVER_ADDRESS, "journal-buffer-size");
    }

    @Test
    public void updateJournalBufferTimeout() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-buffer-timeout", 1L);
    }

    @Test
    public void updateJournalCompactMinFiles() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-compact-min-files", 1);
    }

    @Test
    public void updateJournalCompactPercentage() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-compact-percentage", 99);
    }

    @Test
    public void updateJournalFileSize() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-file-size", 4096L);
    }

    @Test
    public void updateJournalDatasource() throws Exception {
        String journalDatasourceAttr = "journal-datasource",
                journalDatasourceName = "journalDatasource_" + randomAlphanumeric(5);
        try {
            new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, journalDatasourceAttr, journalDatasourceName).verifyFormSaved()
                    .verifyAttribute(journalDatasourceAttr, journalDatasourceName);
            new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, journalDatasourceAttr, "").verifyFormSaved() // undefine
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
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalDatabaseAttr, journalDatabaseName).verifyFormSaved()
                .verifyAttribute(journalDatabaseAttr, journalDatabaseName);
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalDatabaseAttr, "").verifyFormSaved() // undefine
                .verifyAttributeIsUndefined(journalDatabaseAttr);
    }

    @Test
    public void updateJournalMessagesTable() throws Exception {
        String journalMessagesTableAttr = "journal-messages-table",
                journalMessagesTableName = "journalMessagesTable_" + randomAlphanumeric(5);
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalMessagesTableAttr, journalMessagesTableName).verifyFormSaved()
                .verifyAttribute(journalMessagesTableAttr, journalMessagesTableName);
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalMessagesTableAttr, "").verifyFormSaved() // undefine
                .verifyAttribute(journalMessagesTableAttr, "MESSAGES"); // default name
    }

    @Test
    public void updateJournalLargeMessagesTable() throws Exception {
        String journalLargeMessagesTableAttr = "journal-large-messages-table",
                journalLargeMessagesTableName = "journalLargeMessagesTable_" + randomAlphanumeric(5);
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalLargeMessagesTableAttr, journalLargeMessagesTableName).verifyFormSaved()
                .verifyAttribute(journalLargeMessagesTableAttr, journalLargeMessagesTableName);
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalLargeMessagesTableAttr, "").verifyFormSaved() // undefine
                .verifyAttribute(journalLargeMessagesTableAttr, "LARGE_MESSAGES"); // default name
    }

    @Test
    public void updateJournalBindingsTable() throws Exception {
        String journalBindingsTableAttr = "journal-bindings-table",
                journalBindingsTableName = "journalBindingsTable_" + randomAlphanumeric(5);
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalBindingsTableAttr, journalBindingsTableName).verifyFormSaved()
                .verifyAttribute(journalBindingsTableAttr, journalBindingsTableName);
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalBindingsTableAttr, "").verifyFormSaved() // undefine
                .verifyAttribute(journalBindingsTableAttr, "BINDINGS"); // default name
    }

    @Test
    public void updateJournalPageStoreTable() throws Exception {
        String journalPageStoreTableAttr = "journal-page-store-table",
                journalPageStoreTableName = "journalPageStoreTable_" + randomAlphanumeric(5);
        try {
            new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, journalPageStoreTableAttr, journalPageStoreTableName).verifyFormSaved()
                    .verifyAttribute(journalPageStoreTableAttr, journalPageStoreTableName);
        } catch (NoSuchElementException e) {
            if (e.getMessage().contains(journalPageStoreTableAttr)) {
                Assert.fail("HAL-1281: " + journalPageStoreTableAttr + " should be visible and configurable!");
            } else {
                throw e;
            }
        }
        new ConfigChecker.Builder(client, SERVER_ADDRESS).configFragment(page.getConfigFragment())
                .editAndSave(TEXT, journalPageStoreTableAttr, "").verifyFormSaved() // undefine
                .verifyAttribute(journalPageStoreTableAttr, "PAGE_STORE"); // default name
    }
}
