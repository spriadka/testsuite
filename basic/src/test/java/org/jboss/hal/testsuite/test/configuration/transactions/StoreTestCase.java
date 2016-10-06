package org.jboss.hal.testsuite.test.configuration.transactions;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Batch;

import java.io.IOException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class StoreTestCase extends TransactionsTestCaseAbstract {

    private static final String JDBC_ACTION_STORE_DROP_TABLE_ATTR = "jdbc-action-store-drop-table";
    private static final String JDBC_ACTION_STORE_TABLE_PREFIX_ATTR = "jdbc-action-store-table-prefix";
    private static final String JDBC_COMMUNICATION_STORE_DROP_TABLE_ATTR = "jdbc-communication-store-drop-table";
    private static final String JDBC_COMMUNICATION_STORE_TABLE_PREFIX_ATTR = "jdbc-communication-store-table-prefix";
    private static final String JDBC_STATE_STORE_DROP_TABLE_ATTR = "jdbc-state-store-drop-table";
    private static final String JDBC_STATE_STORE_TABLE_PREFIX_ATTR = "jdbc-state-store-table-prefix";
    private static final String JDBC_STORE_DATASOURCE_ATTR = "jdbc-store-datasource";

    @Page
    public TransactionsPage page;

    @Before
    public void before() {
        page.navigate();
        page.getConfig().switchTo("Store");
    }

    @Test
    public void setUseJDBCStoreToTrue() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS,  USE_JDBC_STORE_ATTR, true);
    }

    @Test
    public void setUseJDBCStoreToFalse() throws Exception {
        try {
            page.getConfigFragment().editCheckboxAndSave(USE_JDBC_STORE_ATTR, false);
            new ResourceVerifier(TRANSACTIONS_ADDRESS, client).verifyAttribute(USE_JDBC_STORE_ATTR, false);
        } finally {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, USE_JDBC_STORE_ATTR, true);
        }
    }

    @Test
    public void setJDBCActionStoreDropTableToTrue() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JDBC_ACTION_STORE_DROP_TABLE_ATTR, true);
    }

    @Test
    public void setJDBCActionStoreDropTableToFalse() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JDBC_ACTION_STORE_DROP_TABLE_ATTR, false);
    }

    @Test
    public void setJDBCCommunicationStoreDropTableToTrue() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JDBC_COMMUNICATION_STORE_DROP_TABLE_ATTR, true);
    }

    @Test
    public void setJDBCCommunicationStoreDropTableToFalse() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JDBC_COMMUNICATION_STORE_DROP_TABLE_ATTR, false);
    }

    @Test
    public void setJDBCStateStoreDropTableToTrue() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JDBC_STATE_STORE_DROP_TABLE_ATTR, true);
    }

    @Test
    public void setJDBStateStoreDropTableToFalse() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JDBC_STATE_STORE_DROP_TABLE_ATTR, false);
    }

    @Test
    public void editJDBCStateStoreTablePrefix() throws Exception {
        editTextAndVerify(TRANSACTIONS_ADDRESS, JDBC_STATE_STORE_TABLE_PREFIX_ATTR,
                "prefix_" + RandomStringUtils.randomAlphanumeric(4));
    }

    @Test
    public void editJDBCCommunicationStoreTablePrefix() throws Exception {
        editTextAndVerify(TRANSACTIONS_ADDRESS, JDBC_COMMUNICATION_STORE_TABLE_PREFIX_ATTR,
                "prefix_" + RandomStringUtils.randomAlphanumeric(4));
    }

    @Test
    public void editJDBCActionStoreTablePrefix() throws Exception {
        editTextAndVerify(TRANSACTIONS_ADDRESS, JDBC_ACTION_STORE_TABLE_PREFIX_ATTR,
                "prefix_" + RandomStringUtils.randomAlphanumeric(4));
    }

    @Test
    public void editJDBCStoreDataSource() throws Exception {
        String datasource = operations.readAttribute(TRANSACTIONS_ADDRESS, JDBC_STORE_DATASOURCE_ATTR).value().asString();
        try {
            editTextAndVerify(TRANSACTIONS_ADDRESS, JDBC_STORE_DATASOURCE_ATTR,
                    "jdbc-store-datasource_" + RandomStringUtils.randomAlphanumeric(4));
        } finally {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, JDBC_STORE_DATASOURCE_ATTR, datasource);
        }
    }

    @BeforeClass
    public static void prepareForJDBCConfiguration() throws IOException {
        Batch batch = new Batch();
        batch.writeAttribute(TRANSACTIONS_ADDRESS, USE_JOURNAL_STORE_ATTR, false)
                .writeAttribute(TRANSACTIONS_ADDRESS, USE_JDBC_STORE_ATTR, true)
                .writeAttribute(TRANSACTIONS_ADDRESS, JDBC_STORE_DATASOURCE_ATTR, "ExampleDS");
        operations.batch(batch);
    }
}
