package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class JournalTestCase extends TransactionsTestCaseAbstract {

    @Test
    public void toggleUseJournalStore() throws Exception {
        navigate2store();
        boolean originalUseJournalStoreValue = isUseJournalStore();
        try {
            page.getConfigFragment().editCheckboxAndSave(USE_JOURNAL_STORE_ATTR, true);
            new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(USE_JOURNAL_STORE_ATTR, true);
            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
            page.getConfigFragment().editCheckboxAndSave(USE_JOURNAL_STORE_ATTR, false);
            new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(USE_JOURNAL_STORE_ATTR, false);
        } finally {
            writeUseJournalStore(originalUseJournalStoreValue);
        }
    }

    @Test
    public void toggleJournalStoreEnableAsyncIO() throws Exception {
        boolean originalUseJournalStoreValue = isUseJournalStore();
        try {
            if (!originalUseJournalStoreValue) {
                writeUseJournalStore(true);
            }
            navigate2store();
            page.getConfigFragment().editCheckboxAndSave(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, true);
            new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, true);
            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
            page.getConfigFragment().editCheckboxAndSave(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, false);
            new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, false);
        } finally {
            writeUseJournalStore(originalUseJournalStoreValue);
        }
    }

    private void navigate2store() {
        page.navigate();
        page.getConfig().switchTo("Store");
    }

    private boolean isUseJournalStore() throws IOException {
        return operations.readAttribute(TRANSACTIONS_ADDRESS, USE_JOURNAL_STORE_ATTR).booleanValue();
    }

    private void writeUseJournalStore(boolean value) throws IOException, InterruptedException, TimeoutException {
        if (isUseJournalStore() != value) {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, USE_JOURNAL_STORE_ATTR, value);
            administration.restartIfRequired();
            administration.reloadIfRequired();
        }
    }

}
