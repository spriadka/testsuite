package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class JournalStoreTestCase extends StoreTestCaseAbstract {

    private static final SnapshotBackup snapshotBackup = new SnapshotBackup();

    private static final String
            USE_JOURNAL_STORE = "use-journal-store",
            USE_JDBC_STORE = "use-jdbc-store",
            JOURNAL_STORE_ENABLE_ASYNC_IO = "journal-store-enable-async-io";

    @BeforeClass
    public static void beforeClass() throws CommandFailedException, TimeoutException, InterruptedException, IOException {
        client.apply(snapshotBackup.backup());

        operations.writeAttribute(TRANSACTIONS_ADDRESS, USE_JOURNAL_STORE, true).assertSuccess();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException, CommandFailedException {
        client.apply(snapshotBackup.restore());
        administration.restartIfRequired();
    }

    @Test
    public void testSettingBothStoresAtOnce() throws Exception {
        page.navigate();
        page.switchToStore();

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, USE_JDBC_STORE, true)
                .verifyFormNotSaved();
    }

    @Test
    public void toggleJournalStoreEnableAsyncIO() throws Exception {
        page.navigate();
        page.switchToStore();

        final ModelNodeResult originalModelNodeResult = operations.readAttribute(TRANSACTIONS_ADDRESS, JOURNAL_STORE_ENABLE_ASYNC_IO);
        originalModelNodeResult.assertSuccess();
        final boolean originalValue = originalModelNodeResult.booleanValue();

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JOURNAL_STORE_ENABLE_ASYNC_IO, !originalValue)
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_STORE_ENABLE_ASYNC_IO, !originalValue);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JOURNAL_STORE_ENABLE_ASYNC_IO, originalValue)
                .verifyFormSaved()
                .verifyAttribute(JOURNAL_STORE_ENABLE_ASYNC_IO, originalValue);
    }

}
