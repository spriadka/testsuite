package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class TransactionsTestCaseAbstract {

    protected static final Address TRANSACTIONS_ADDRESS = Address.subsystem("transactions");

    protected static final String JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR = "journal-store-enable-async-io";
    protected static final String USE_JDBC_STORE_ATTR = "use-jdbc-store";
    protected static final String USE_JOURNAL_STORE_ATTR = "use-journal-store";

    private static final String PROCESS_ID_SOCKET_BINDING_ATTR = "process-id-socket-binding";
    private static final String JDBC_STORE_DATASOURCE_ATTR = "jdbc-store-datasource";

    private static BackupAndRestoreAttributes backup;
    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Administration administration = new Administration(client);
    protected static final Operations operations = new Operations(client);
    protected static final TransactionsOperations transactionsOps = new TransactionsOperations(client);

    @Drone
    public WebDriver browser;

    @Page
    public TransactionsPage page;

    @BeforeClass
    public static void beforeClass() throws CommandFailedException {
        backup = new BackupAndRestoreAttributes.Builder(Address.of("subsystem", "transactions"))
                .excluded(PROCESS_ID_SOCKET_BINDING_ATTR)
                .dependency(USE_JDBC_STORE_ATTR, JDBC_STORE_DATASOURCE_ATTR)
                .build();
        client.apply(backup.backup());
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        try {
            client.apply(backup.restore());
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    //helper methods
    protected void editTextAndVerify(Address address, String identifier, String value) throws Exception {
        page.getConfigFragment().editTextAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void editCheckboxAndVerify(Address address, String identifier, Boolean value) throws Exception {
        page.getConfigFragment().editCheckboxAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void editTextAndVerify(Address address, String identifier, int value) throws Exception {
        page.getConfigFragment().editTextAndSave(identifier, String.valueOf(value));
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void selectOptionAndVerify(Address address, String identifier, String value) throws Exception {
        page.getConfigFragment().selectOptionAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShownInForm());
        config.cancel();
    }
}
