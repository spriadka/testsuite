package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class JournalTestCase extends TransactionsTestCaseAbstract {

    @BeforeClass
    public static void prepareForJournalConfiguration() throws IOException, TimeoutException, InterruptedException {
        Address address = Address.subsystem("transactions");
        Batch batch = new Batch();
        batch.writeAttribute(address, USE_JOURNAL_STORE_ATTR, true);
        batch.writeAttribute(address, USE_JDBC_STORE_ATTR, false);
        operations.batch(batch);
        administration.restartIfRequired();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void toggleUseJournalStore() throws Exception {
        ModelNode value = operations.readAttribute(TRANSACTIONS_ADDRESS, USE_JOURNAL_STORE_ATTR);
        try {
            page.getConfigFragment().editCheckboxAndSave(USE_JOURNAL_STORE_ATTR, true);
            new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(USE_JOURNAL_STORE_ATTR, true);
            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
            page.getConfigFragment().editCheckboxAndSave(USE_JOURNAL_STORE_ATTR, false);
            new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(USE_JOURNAL_STORE_ATTR, false);
        } finally {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, USE_JOURNAL_STORE_ATTR, value);
        }
    }

    @Test
    public void toggleJournalStoreEnableAsyncIO() throws Exception {
        page.getConfigFragment().editCheckboxAndSave(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, true);
        new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, true);
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        page.getConfigFragment().editCheckboxAndSave(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, false);
        new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, false);
    }

}
