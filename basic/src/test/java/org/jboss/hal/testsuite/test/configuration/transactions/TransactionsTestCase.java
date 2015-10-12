package org.jboss.hal.testsuite.test.configuration.transactions;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.Composite;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class TransactionsTestCase extends TransactionsAbstractTestCase {

    private final String DEFAULT_TIMEOUT = "default-timeout";
    private final String ENABLE_TSM_STATUS = "enable-tsm-status";
    private final String JOURNAL_STORE_ENABLE_ASYNC_IO = "journal-store-enable-async-io";
    private final String JTS = "jts";
    private final String NODE_IDENTIFIER = "node-identifier";
    private final String STATISTICS_ENABLED = "statistics-enabled";
    private final String USE_JOURNAL_STORE = "use-journal-store";
    private final String SOCKET_BINDING = "socket-binding";
    private final String STATUS_SOCKET_BINDING = "status-socket-binding";
    private final String RECOVERY_LISTENER = "recovery-listener";
    private final String OBJECT_STORE_PATH = "object-store-path";
    private final String OBJECT_STORE_RELATIVE_TO = "object-store-relative-to";
    private final String USE_JDBC_STORE = "use-jdbc-store";
    private final String JDBC_ACTION_STORE_DROP_TABLE = "jdbc-action-store-drop-table";
    private final String JDBC_ACTION_STORE_TABLE_PREFIX = "jdbc-action-store-table-prefix";
    private final String JDBC_COMMUNICATION_STORE_DROP_TABLE = "jdbc-communication-store-drop-table";
    private final String JDBC_COMMUNICATION_STORE_TABLE_PREFIX = "jdbc-communication-store-table-prefix";
    private final String JDBC_STATE_STORE_DROP_TABLE = "jdbc-state-store-drop-table";
    private final String JDBC_STATE_STORE_TABLE_PREFIX = "jdbc-state-store-table-prefix";
    private final String JDBC_STORE_DATASOURCE = "jdbc-store-datasource";

    private final String DEFAULT_TIMEOUT_ATTR = "default-timeout";
    private final String ENABLE_TSM_STATUS_ATTR = "enable-tsm-status";
    private final String JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR = "journal-store-enable-async-io";
    private final String JTS_ATTR = "jts";
    private final String NODE_IDENTIFIER_ATTR = "node-identifier";
    private final String STATISTICS_ENABLED_ATTR = "statistics-enabled";
    private final String USE_JOURNAL_STORE_ATTR = "use-journal-store";
    private final String SOCKET_BINDING_ATTR = "socket-binding";
    private final String STATUS_SOCKET_BINDING_ATTR = "status-socket-binding";
    private final String RECOVERY_LISTENER_ATTR = "recovery-listener";
    private final String OBJECT_STORE_PATH_ATTR = "object-store-path";
    private final String OBJECT_STORE_RELATIVE_TO_ATTR = "object-store-relative-to";
    private final String USE_JDBC_STORE_ATTR = "use-jdbc-store";
    private final String JDBC_ACTION_STORE_DROP_TABLE_ATTR = "jdbc-action-store-drop-table";
    private final String JDBC_ACTION_STORE_TABLE_PREFIX_ATTR = "jdbc-action-store-table-prefix";
    private final String JDBC_COMMUNICATION_STORE_DROP_TABLE_ATTR = "jdbc-communication-store-drop-table";
    private final String JDBC_COMMUNICATION_STORE_TABLE_PREFIX_ATTR = "jdbc-communication-store-table-prefix";
    private final String JDBC_STATE_STORE_DROP_TABLE_ATTR = "jdbc-state-store-drop-table";
    private final String JDBC_STATE_STORE_TABLE_PREFIX_ATTR = "jdbc-state-store-table-prefix";
    private final String JDBC_STORE_DATASOURCE_ATTR = "jdbc-store-datasource";

    @Drone
    public WebDriver browser;

    @Page
    public TransactionsPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @After
    public void after() {
        //client.reload(false);
    }

    @Test
    public void setStatisticsToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, STATISTICS_ENABLED, STATISTICS_ENABLED_ATTR, true);
    }

    @Test
    public void setStatisticsToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, STATISTICS_ENABLED, STATISTICS_ENABLED_ATTR, true);
    }

    @Test
    public void setJTSToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, JTS, JTS_ATTR, true);
    }

    @Test
    public void setJTSToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, JTS, JTS_ATTR, false);
    }

    @Test
    public void setUseJournalStoreToTrue() throws IOException, InterruptedException {
        setEnableToUseJDBCStore(false);
        editCheckboxAndVerify(address, USE_JOURNAL_STORE, USE_JOURNAL_STORE_ATTR, true);
    }

    @Test
    public void setUseJournalStoreToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, USE_JOURNAL_STORE, USE_JOURNAL_STORE_ATTR, false);
    }

    @Test
    public void setJournalStoreEnableAsyncIOToTrue() throws IOException, InterruptedException {
        prepareForJournalStoreConfiguration();
        editCheckboxAndVerify(address, JOURNAL_STORE_ENABLE_ASYNC_IO, JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, true);
    }

    @Test
    public void setJournalStoreEnableAsyncIOToFalse() throws IOException, InterruptedException {
        prepareForJournalStoreConfiguration();
        editCheckboxAndVerify(address, JOURNAL_STORE_ENABLE_ASYNC_IO, JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR, false);
    }

    @Test
    public void setTSMStatusToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, ENABLE_TSM_STATUS, ENABLE_TSM_STATUS_ATTR, true);
    }

    @Test
    public void setTSMStatusToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, ENABLE_TSM_STATUS, ENABLE_TSM_STATUS_ATTR, false);
    }

    @Test
    public void editNodeIdentifier() throws IOException, InterruptedException {
        editTextAndVerify(address, NODE_IDENTIFIER, NODE_IDENTIFIER_ATTR);
    }

    @Test
    public void editDefaultTimeout() throws IOException, InterruptedException {
        editTextAndVerify(address, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_ATTR, "500");
    }

    @Test
    public void editDefaultTimeoutNegative() {
        verifyIfErrorAppears(DEFAULT_TIMEOUT, "-500");
    }
    @Test
    public void editDefaultTimeoutInvalid() {
        verifyIfErrorAppears(DEFAULT_TIMEOUT, "asdf");
    }



    @Test
    public void editSocketBinding() throws IOException, InterruptedException {
        page.getConfig().switchTo("Recovery");
        editTextAndVerify(address, SOCKET_BINDING, SOCKET_BINDING_ATTR);
    }

    @Test
    public void editStatusSocketBinding() throws IOException, InterruptedException {
        page.getConfig().switchTo("Recovery");
        editTextAndVerify(address, STATUS_SOCKET_BINDING, STATUS_SOCKET_BINDING_ATTR);
    }

    @Test
    public void setRecoveryListenerToTrue() throws IOException, InterruptedException {
        page.getConfig().switchTo("Recovery");
        editCheckboxAndVerify(address, RECOVERY_LISTENER, RECOVERY_LISTENER_ATTR, true);
    }

    @Test
    public void setRecoveryListenerToFalse() throws IOException, InterruptedException {
        page.getConfig().switchTo("Recovery");
        editCheckboxAndVerify(address, RECOVERY_LISTENER, RECOVERY_LISTENER_ATTR, false);
    }

    @Test
    public void editObjectStorePath() throws IOException, InterruptedException {
        page.getConfig().switchTo("Path");
        editTextAndVerify(address, OBJECT_STORE_PATH, OBJECT_STORE_PATH_ATTR);
    }

    @Test
    public void editObjectStoreRelativeTo() throws IOException, InterruptedException {
        page.getConfig().switchTo("Path");
        editTextAndVerify(address, OBJECT_STORE_RELATIVE_TO, OBJECT_STORE_RELATIVE_TO_ATTR);
    }

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
