package org.jboss.hal.testsuite.test.configuration.datasources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.KnownIssue;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
@RunAsClient
public class TestConnectionTestCase extends AbstractTestConnectionTestCase {

    private static final String VALID_URL = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1";
    private static final String INVALID_URL = "invalidUrl";

    private static String
            DS_NAME_VALID,
            XA_DS_NAME_VALID,
            DS_NAME_INVALID,
            XA_DS_NAME_INVALID;

    private static OnlineManagementClient client;
    private static Administration administration;
    private static Operations operations;
    private static DataSourcesOperations dsOps;

    // Setup

    @BeforeClass
    public static void setup() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        client = ManagementClientProvider.createOnlineManagementClient();
        administration = new Administration(client);
        operations = new Operations(client);
        dsOps = new DataSourcesOperations(client);
        DS_NAME_VALID = dsOps.createDataSource(VALID_URL);
        DS_NAME_INVALID = dsOps.createDataSource(INVALID_URL);
        XA_DS_NAME_INVALID = dsOps.createXADataSource(INVALID_URL);
        XA_DS_NAME_VALID = dsOps.createXADataSource(VALID_URL);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, CommandFailedException {
        try {
            dsOps.removeDataSource(DS_NAME_VALID);
            dsOps.removeDataSource(DS_NAME_INVALID);
            dsOps.removeXADataSource(XA_DS_NAME_VALID);
            dsOps.removeXADataSource(XA_DS_NAME_INVALID);
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    // Regular DS tests
    @Test
    public void validDatasource() {
        datasourcesPage.invokeViewDatasource(DS_NAME_VALID);
        testConnectionInDatasourceView(true);
    }

    @Test
    public void invalidDatasource() {
        datasourcesPage.invokeViewDatasource(DS_NAME_INVALID);
        testConnectionInDatasourceView(false);
    }

    /**
     * Test connection in wizard, which should be successful and then save it. Datasource is expected to be saved in
     * model after reload.
     */
    @Test
    public void testValidConnectionInWizardAndSave() throws Exception {
        datasourcesPage.invokeAddDatasource();

        String name = "TestConnectionValidSaveInWizard_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getDsAddress(name);
        try {
            testConnectionInWizardAndSave(name, VALID_URL, true);

            administration.reloadIfRequired();
            new ResourceVerifier(address, client).verifyExists();
        } finally {
            operations.removeIfExists(address);
        }
    }

    /**
     * Test connection in wizard, which should be successful and then close wizard. Datasource is expected not to be
     * saved in model after reload.
     */
    @Test
    public void testValidConnectionInWizardAndClose() throws Exception {
        datasourcesPage.invokeAddDatasource();

        String name = "TestConnectionValidSaveInWizard_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getDsAddress(name);
        try {
            testConnectionInWizardAndClose(name, VALID_URL, true);

            administration.reloadIfRequired();
            new ResourceVerifier(address, client).verifyDoesNotExist("Probably fails because of https://issues.jboss.org/browse/HAL-1257");
        } finally {
            operations.removeIfExists(address);
        }
    }

    /**
     * Test connection in wizard, which should be successful and then cancel it. Datasource is expected not to be saved
     * in model after reload.
     */
    @Test
    public void testValidConnectionInWizardAndCancel() throws Exception {
        datasourcesPage.invokeAddDatasource();

        String name = "TestConnectionValidCancelInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testConnectionInWizardAndCancel(name, VALID_URL, true);

        administration.reloadIfRequired();
        new ResourceVerifier(DataSourcesOperations.getDsAddress(name), client).verifyDoesNotExist();
    }

    /**
     * Test connection in wizard, which should be unsuccessful and then cancel it. Datasource is expected not to be
     * saved in model after reload.
     */
    @Test
    public void testInvalidConnectionInWizardAndCancel() throws Exception {
        datasourcesPage.invokeAddDatasource();

        String name = "TestConnectionInvalidInWizardCancel_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getDsAddress(name);
        try {
            testConnectionInWizardAndCancel(name, INVALID_URL, false);

            administration.reloadIfRequired();
            new ResourceVerifier(DataSourcesOperations.getDsAddress(name), client).verifyDoesNotExist();
        } finally {
            operations.removeIfExists(address);
        }
    }


    // XA DS tests
    @Test
    public void validXADatasource() {
        datasourcesPage.invokeViewXADatasource(XA_DS_NAME_VALID);
        testConnectionInDatasourceView(true);
    }

    @Test
    public void invalidXADatasource() {
        datasourcesPage.invokeViewXADatasource(XA_DS_NAME_INVALID);
        testConnectionInDatasourceView(false);
    }

    @Test
    @Category(KnownIssue.class)
    public void testValidXAConnectionInWizardAndCancel() throws Exception {
        datasourcesPage.invokeAddXADatasource();

        String name = "TestConnectionValidXAInWizardCancel_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getXADsAddress(name);
        try {
            testXAConnectionInWizardAndCancel(name, VALID_URL, true);

            administration.reloadIfRequired();
            new ResourceVerifier(address, client).verifyDoesNotExist();
        } finally {
            operations.removeIfExists(address);
        }
    }

    @Test
    public void testValidXAConnectionInWizardAndClose() throws Exception {
        datasourcesPage.invokeAddXADatasource();

        String name = "TestConnectionValidXAInWizardCancel_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getXADsAddress(name);
        try {
            testXAConnectionInWizardAndClose(name, VALID_URL, true);

            administration.reloadIfRequired();
            new ResourceVerifier(address, client).verifyDoesNotExist("Probably fails because of https://issues.jboss.org/browse/HAL-1257");
        } finally {
            operations.removeIfExists(address);
        }
    }

    @Test
    public void testValidXAConnectionInWizardAndSave() throws Exception {
        datasourcesPage.invokeAddXADatasource();

        String name = "TestConnectionValidXAInWizard_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getXADsAddress(name);
        try {
            testXAConnectionInWizardAndSave(name, VALID_URL, true);

            administration.reloadIfRequired();
            new ResourceVerifier(address, client).verifyExists();
        } finally {
            operations.removeIfExists(address);
        }
    }

    @Test
    public void testInvalidXAConnectionInWizardAndCancel() throws Exception {
        datasourcesPage.invokeAddXADatasource();

        String name = "TestConnectionInvalidXAInWizardCancel_" + RandomStringUtils.randomAlphabetic(6);
        Address address = DataSourcesOperations.getXADsAddress(name);
        try {
            testXAConnectionInWizardAndCancel(name, "invalidUrl", false);

            administration.reloadIfRequired();
            new ResourceVerifier(address, client).verifyDoesNotExist();
        } finally {
            operations.removeIfExists(address);
        }
    }
}
