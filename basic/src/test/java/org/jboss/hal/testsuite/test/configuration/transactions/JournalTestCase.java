package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.Composite;
import org.jboss.hal.testsuite.dmr.Operation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 12.10.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class JournalTestCase extends TransactionsTestCaseAbstract {

    //TODO prepare for JournalStore Config
    @BeforeClass
    public static void setUp() {
        prepareForJournalConfiguration();
    }

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void setUseJournalStoreToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, USE_JOURNAL_STORE, USE_JOURNAL_STORE_ATTR, true);
    }

    @Test
    public void setUseJournalStoreToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, USE_JOURNAL_STORE, USE_JOURNAL_STORE_ATTR, false);
    }

    @Test
    public void setJournalStoreEnableAsyncIOToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, JOURNAL_STORE_ENABLE_ASYNC_IO, JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, true);
    }

    @Test
    public void setJournalStoreEnableAsyncIOToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, JOURNAL_STORE_ENABLE_ASYNC_IO, JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, false);
    }

    private static void prepareForJournalConfiguration() {
        Operation undefineUseJournalStore = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, USE_JOURNAL_STORE_ATTR)
                .param(VALUE, true)
                .build();
        Operation enableUseJDBCStore = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, USE_JDBC_STORE_ATTR)
                .param(VALUE, false)
                .build();
        dispatcher.execute(new Composite(undefineUseJournalStore, enableUseJDBCStore));
    }

}
