package org.jboss.hal.testsuite.test.configuration.transactions;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.datasources.AddDataSource;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Batch;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class JDBCStoreTestCase extends StoreTestCaseAbstract {

    private static final SnapshotBackup snapshotBackup = new SnapshotBackup();

    private static final String
            USE_JDBC_STORE = "use-jdbc-store",
            JDBC_ACTION_STORE_DROP_TABLE = "jdbc-action-store-drop-table",
            JDBC_ACTION_STORE_TABLE_PREFIX = "jdbc-action-store-table-prefix",
            JDBC_COMMUNICATION_STORE_DROP_TABLE = "jdbc-communication-store-drop-table",
            JDBC_COMMUNICATION_STORE_TABLE_PREFIX = "jdbc-communication-store-table-prefix",
            JDBC_STATE_STORE_DROP_TABLE = "jdbc-state-store-drop-table",
            JDBC_STATE_STORE_TABLE_PREFIX = "jdbc-state-store-table-prefix",
            JDBC_STORE_DATASOURCE = "jdbc-store-datasource";


    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        client.apply(snapshotBackup.backup());

        operations.batch(new Batch()
                .writeAttribute(TRANSACTIONS_ADDRESS, JDBC_STORE_DATASOURCE, createDatasource())
                .writeAttribute(TRANSACTIONS_ADDRESS, USE_JDBC_STORE, true)
        ).assertSuccess();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException, CommandFailedException {
        client.apply(snapshotBackup.restore());
        administration.restartIfRequired();
    }

    @Test
    public void toggleUseJdbcStore() throws Exception {
        page.navigate();
        page.switchToStore();

        final ModelNodeResult originalModelNodeResult = operations.readAttribute(TRANSACTIONS_ADDRESS, USE_JDBC_STORE);
        originalModelNodeResult.assertSuccess();
        final boolean originalValue = originalModelNodeResult.booleanValue();

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, USE_JDBC_STORE, !originalValue)
                .verifyFormSaved()
                .verifyAttribute(USE_JDBC_STORE, !originalValue);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, USE_JDBC_STORE, originalValue)
                .verifyFormSaved()
                .verifyAttribute(USE_JDBC_STORE, originalValue);
    }

    @Test
    public void toggleJdbcActionStoreDropTable() throws Exception {
        page.navigate();
        page.switchToStore();

        final ModelNodeResult originalModelNodeResult = operations.readAttribute(TRANSACTIONS_ADDRESS, JDBC_ACTION_STORE_DROP_TABLE);
        originalModelNodeResult.assertSuccess();
        final boolean originalValue = originalModelNodeResult.booleanValue();

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JDBC_ACTION_STORE_DROP_TABLE, !originalValue)
                .verifyFormSaved()
                .verifyAttribute(JDBC_ACTION_STORE_DROP_TABLE, !originalValue);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JDBC_ACTION_STORE_DROP_TABLE, originalValue)
                .verifyFormSaved()
                .verifyAttribute(JDBC_ACTION_STORE_DROP_TABLE, originalValue);
    }

    @Test
    public void editJdbcActionStoreTablePrefix() throws Exception {
        page.navigate();
        page.switchToStore();

        final String value = RandomStringUtils.randomAlphabetic(7);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, JDBC_ACTION_STORE_TABLE_PREFIX, value)
                .verifyFormSaved()
                .verifyAttribute(JDBC_ACTION_STORE_TABLE_PREFIX, value);
    }

    @Test
    public void toggleJdbcCommunicationStoreDropTable() throws Exception {
        page.navigate();
        page.switchToStore();

        final ModelNodeResult originalModelNodeResult = operations.readAttribute(TRANSACTIONS_ADDRESS, JDBC_COMMUNICATION_STORE_DROP_TABLE);
        originalModelNodeResult.assertSuccess();
        final boolean originalValue = originalModelNodeResult.booleanValue();

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JDBC_COMMUNICATION_STORE_DROP_TABLE, !originalValue)
                .verifyFormSaved()
                .verifyAttribute(JDBC_COMMUNICATION_STORE_DROP_TABLE, !originalValue);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JDBC_COMMUNICATION_STORE_DROP_TABLE, originalValue)
                .verifyFormSaved()
                .verifyAttribute(JDBC_COMMUNICATION_STORE_DROP_TABLE, originalValue);
    }

    @Test
    public void editJdbcCommunicationStoreTablePrefix() throws Exception {
        page.navigate();
        page.switchToStore();

        final String value = RandomStringUtils.randomAlphabetic(7);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, JDBC_COMMUNICATION_STORE_TABLE_PREFIX, value)
                .verifyFormSaved()
                .verifyAttribute(JDBC_COMMUNICATION_STORE_TABLE_PREFIX, value);
    }

    @Test
    public void toggleJdbcStateStoreDropTable() throws Exception {
        page.navigate();
        page.switchToStore();

        final ModelNodeResult originalModelNodeResult = operations.readAttribute(TRANSACTIONS_ADDRESS, JDBC_STATE_STORE_DROP_TABLE);
        originalModelNodeResult.assertSuccess();
        final boolean originalValue = originalModelNodeResult.booleanValue();

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JDBC_STATE_STORE_DROP_TABLE, !originalValue)
                .verifyFormSaved()
                .verifyAttribute(JDBC_STATE_STORE_DROP_TABLE, !originalValue);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.CHECKBOX, JDBC_STATE_STORE_DROP_TABLE, originalValue)
                .verifyFormSaved()
                .verifyAttribute(JDBC_STATE_STORE_DROP_TABLE, originalValue);
    }

    @Test
    public void editJdbcStateStoreTablePrefix() throws Exception {
        page.navigate();
        page.switchToStore();

        final String value = RandomStringUtils.randomAlphabetic(7);

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, JDBC_STATE_STORE_TABLE_PREFIX, value)
                .verifyFormSaved()
                .verifyAttribute(JDBC_STATE_STORE_TABLE_PREFIX, value);
    }

    @Test
    public void editJdbcStoreDatasource() throws Exception {
        page.navigate();
        page.switchToStore();

        final String datasource = createDatasource();

        new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, JDBC_STORE_DATASOURCE, datasource)
                .verifyFormSaved()
                .verifyAttribute(JDBC_STORE_DATASOURCE, datasource);
    }

    private static String createDatasource() throws CommandFailedException {
        final String datasourceName = RandomStringUtils.randomAlphanumeric(7);
        client.apply(new AddDataSource.Builder(datasourceName)
                .jndiName("java:/" + datasourceName)
                .driverName("h2")
                .connectionUrl("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1")
                .enableAfterCreate()
                .build());
        return datasourceName;
    }
}
