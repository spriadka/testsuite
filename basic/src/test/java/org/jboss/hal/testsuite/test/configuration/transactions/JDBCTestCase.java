package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.hal.testsuite.dmr.Operation;
import org.junit.Test;

import java.io.IOException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 12.10.15.
 */
public class JDBCTestCase extends TransactionsTestCaseAbstract {

    private final String USE_JDBC_STORE = "use-jdbc-store";
    private final String JDBC_ACTION_STORE_DROP_TABLE = "jdbc-action-store-drop-table";
    private final String JDBC_ACTION_STORE_TABLE_PREFIX = "jdbc-action-store-table-prefix";
    private final String JDBC_COMMUNICATION_STORE_DROP_TABLE = "jdbc-communication-store-drop-table";
    private final String JDBC_COMMUNICATION_STORE_TABLE_PREFIX = "jdbc-communication-store-table-prefix";
    private final String JDBC_STATE_STORE_DROP_TABLE = "jdbc-state-store-drop-table";
    private final String JDBC_STATE_STORE_TABLE_PREFIX = "jdbc-state-store-table-prefix";
    private final String JDBC_STORE_DATASOURCE = "jdbc-store-datasource";

    private final String USE_JDBC_STORE_ATTR = "use-jdbc-store";
    private final String JDBC_ACTION_STORE_DROP_TABLE_ATTR = "jdbc-action-store-drop-table";
    private final String JDBC_ACTION_STORE_TABLE_PREFIX_ATTR = "jdbc-action-store-table-prefix";
    private final String JDBC_COMMUNICATION_STORE_DROP_TABLE_ATTR = "jdbc-communication-store-drop-table";
    private final String JDBC_COMMUNICATION_STORE_TABLE_PREFIX_ATTR = "jdbc-communication-store-table-prefix";
    private final String JDBC_STATE_STORE_DROP_TABLE_ATTR = "jdbc-state-store-drop-table";
    private final String JDBC_STATE_STORE_TABLE_PREFIX_ATTR = "jdbc-state-store-table-prefix";
    private final String JDBC_STORE_DATASOURCE_ATTR = "jdbc-store-datasource";

    @Test
    public void setUseJDBCStoreToTrue() throws IOException, InterruptedException {
        setEnableToUseJournalStore(false);
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, USE_JDBC_STORE, USE_JDBC_STORE_ATTR, true);
    }

    @Test
    public void setUseJDBCStoreToFalse() throws IOException, InterruptedException {
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, USE_JDBC_STORE, USE_JDBC_STORE_ATTR, false);
    }

    @Test
    public void setJDBCActionStoreDropTableToTrue() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, JDBC_ACTION_STORE_DROP_TABLE, JDBC_ACTION_STORE_DROP_TABLE_ATTR, true);
    }

    @Test
    public void setJDBCActionStoreDropTableToFalse() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, JDBC_ACTION_STORE_DROP_TABLE, JDBC_ACTION_STORE_DROP_TABLE_ATTR, false);
    }

    @Test
    public void setJDBCCommunicationStoreDropTableToTrue() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, JDBC_COMMUNICATION_STORE_DROP_TABLE, JDBC_COMMUNICATION_STORE_DROP_TABLE_ATTR, true);
    }

    @Test
    public void setJDBCCommunicationStoreDropTableToFalse() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, JDBC_COMMUNICATION_STORE_DROP_TABLE, JDBC_COMMUNICATION_STORE_DROP_TABLE_ATTR, false);
    }

    @Test
    public void setJDBCStateStoreDropTableToTrue() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, JDBC_STATE_STORE_DROP_TABLE, JDBC_STATE_STORE_DROP_TABLE_ATTR, true);
    }

    @Test
    public void setJDBStateStoreDropTableToFalse() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editCheckboxAndVerify(address, JDBC_STATE_STORE_DROP_TABLE, JDBC_STATE_STORE_DROP_TABLE_ATTR, false);
    }

    @Test
    public void editJDBCStateStoreTablePrefix() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editTextAndVerify(address, JDBC_STATE_STORE_TABLE_PREFIX, JDBC_STATE_STORE_TABLE_PREFIX_ATTR);
    }

    @Test
    public void editJDBCCommunicationStoreTablePrefix() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editTextAndVerify(address, JDBC_COMMUNICATION_STORE_TABLE_PREFIX, JDBC_COMMUNICATION_STORE_TABLE_PREFIX_ATTR);
    }

    @Test
    public void editJDBCActionStoreTablePrefix() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editTextAndVerify(address, JDBC_ACTION_STORE_TABLE_PREFIX, JDBC_ACTION_STORE_TABLE_PREFIX_ATTR);
    }

    @Test
    public void editJDBCStoreDataSource() throws IOException, InterruptedException {
        prepareForJDBCConfiguration();
        page.getConfig().switchTo("JDBC");
        editTextAndVerify(address, JDBC_STORE_DATASOURCE, JDBC_STORE_DATASOURCE_ATTR);
    }



    private void prepareForJDBCConfiguration() {
        setEnableToUseJournalStore(false);
        setEnableToUseJDBCStore(true);
    }

    private void setEnableToUseJournalStore(boolean enable) {
        dispatcher.execute(new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, USE_JOURNAL_STORE_ATTR)
                .param(VALUE, enable)
                .build());
        reloadIfRequiredAndWaitForRunning();
    }

    private void setEnableToUseJDBCStore(boolean enable) {
        dispatcher.execute(new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                .param(NAME, USE_JDBC_STORE_ATTR)
                .param(VALUE, enable)
                .build());
        reloadIfRequiredAndWaitForRunning();
    }

    private void prepareForJournalStoreConfiguration() {
        setEnableToUseJDBCStore(false);
        setEnableToUseJournalStore(true);
    }
}
