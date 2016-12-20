package org.jboss.hal.testsuite.test.configuration.datasources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.DatasourcesPage;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Category(Domain.class)
@RunWith(Arquillian.class)
public class DomainTestConnectionTestCase extends AbstractTestConnectionTestCase {

    @Page
    private DatasourcesPage datasourcesPage;

    private static final String
            VALID_URL = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1",
            INVALID_URL = "invalidUrl";

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static OnlineManagementClient fullHaClient = ManagementClientProvider.withProfile("full-ha");
    private static Administration administration = new Administration(client);
    private static Operations fullHaOperations = new Operations(fullHaClient);
    private static DataSourcesOperations dsOps = new DataSourcesOperations(client);

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException {
        try {
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
            IOUtils.closeQuietly(fullHaClient);
        }
    }

    @Test
    public void testConnectionOnServerGroupWithNoRunningServers() throws Exception {
        datasourcesPage.invokeAddDatasourceOnProfile("full-ha");

        String name = "ConnectionOnProfileWithNoRunningServers_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getDsAddress(name);
        try {
            testConnectionInWizardAndCancel(name, VALID_URL, false);

            administration.reloadIfRequired();
            new ResourceVerifier(address, fullHaClient).verifyDoesNotExist();
        } finally {
            fullHaOperations.removeIfExists(address);
        }
    }

    @Test
    public void testInvalidConnectionOnServerGroupWithNoRunningServers() throws Exception {
        datasourcesPage.invokeAddDatasourceOnProfile("full-ha");

        String name = "InvalidConnectionOnProfileWithNoRunningServers_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getDsAddress(name);
        try {
            testConnectionInWizardAndCancel(name, INVALID_URL, false);

            administration.reloadIfRequired();
            new ResourceVerifier(address, fullHaClient).verifyDoesNotExist();
        } finally {
            fullHaOperations.removeIfExists(address);
        }
    }

}
