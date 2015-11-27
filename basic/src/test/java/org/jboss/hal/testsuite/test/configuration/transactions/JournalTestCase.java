package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.dmr.Composite;
import org.jboss.hal.testsuite.dmr.Operation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

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
//Some test might fail due to the HAL-883
public class JournalTestCase extends TransactionsTestCaseAbstract {

    private static BackupAndRestoreAttributes backup;
    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();

    //TODO prepare for JournalStore Config
    @BeforeClass
    public static void setUp() throws CommandFailedException {
        backup = new BackupAndRestoreAttributes.Builder(Address.of("subsystem", "transactions"))
                .dependency(USE_JDBC_STORE, "jdbc-store-datasource")
                .excluded("process-id-socket-binding")
                .build();
        client.apply(backup.backup());
        prepareForJournalConfiguration();
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException {
        client.apply(backup.restore());
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
