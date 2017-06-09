package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.junit.AfterClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Abstract class for store test cases. They are divided because restart is required when enabling either use-jdbc-store
 * or use-journal-store.
 */
public abstract class StoreTestCaseAbstract {

    @Page
    public TransactionsPage page;

    @Drone
    protected WebDriver browser;

    protected static final Address TRANSACTIONS_ADDRESS = Address.subsystem("transactions");

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Operations operations = new Operations(client);
    protected static final Administration administration = new Administration(client);

    @AfterClass
    public static void afterClass_() throws InterruptedException, TimeoutException, IOException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }
}
