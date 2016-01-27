package org.jboss.hal.testsuite.test.configuration.datasources;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.DatasourcesPage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * Created by jcechace on 21/02/14.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class TestConnectionTestCase extends AbstractTestConnectionTestCase {

    private static String dsNameValid = "TestConnectionDSNameValid" + RandomStringUtils.randomAlphanumeric(5);
    private static String xaDsNameValid = "TestConnectionXADsNameValid" + RandomStringUtils.randomAlphanumeric(5);
    private static String dsNameInvalid = "TestConnectionDSNameInvalid" + RandomStringUtils.randomAlphanumeric(5);
    private static String xaDsNameInvalid = "TestConnectionXADsNameInvalid" + RandomStringUtils.randomAlphanumeric(5);

    private static final String VALID_URL = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1";
    private static final String INVALID_URL = "invalidUrl";

    private static OnlineManagementClient client;
    private static Administration administration;
    private static DataSourcesOperations dsOps;

    // Setup

    @BeforeClass
    public static void setup() throws CommandFailedException, InterruptedException, TimeoutException, IOException {  // create needed datasources
        client = ManagementClientProvider.createOnlineManagementClient();
        administration = new Administration(client);
        dsOps = new DataSourcesOperations(client);
        dsNameValid = dsOps.createDataSource(VALID_URL);
        dsNameInvalid = dsOps.createDataSource(INVALID_URL);
        xaDsNameInvalid = dsOps.createXADataSource(INVALID_URL);
        xaDsNameValid = dsOps.createXADataSource(VALID_URL);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException { // remove datasources when finished
        dsOps.removeDataSource(dsNameValid);
        dsOps.removeDataSource(dsNameInvalid);
        dsOps.removeXADataSource(xaDsNameValid);
        dsOps.removeXADataSource(xaDsNameInvalid);
    }

    @Before
    public void before() {
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Graphene.goTo(DatasourcesPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        datasourcesPage.selectMenu("Non-XA");
        Console.withBrowser(browser).waitUntilLoaded();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }

    @After
    public void after() {
        browser.navigate().refresh();
    }

    // Regular DS tests
    @Test
    public void validDatasource() {
        testConnection(dsNameValid, true);
    }

    @Test
    public void invalidDatasource() {
        testConnection(dsNameInvalid, false);
    }

    @Test
    public void validInWizard() throws IOException, OperationException {
        String name = "TestConnectionValidInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testConnectionInWizard(dsOps, name, VALID_URL, true);
    }

    @Test
    public void invalidInWizard() throws IOException, OperationException {
        String name = "TestConnectionInvalidInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testConnectionInWizard(dsOps, name, INVALID_URL, false);
    }


    // XA DS tests
    @Test
    public void validXADatasource() {
        datasourcesPage.switchToXA();

        testConnection(xaDsNameValid, true);
    }

    @Test
    public void invalidXADatasource() {
        datasourcesPage.switchToXA();

        testConnection(xaDsNameInvalid, false);
    }

    @Test
    public void validXAInWizard() throws IOException, OperationException {
        datasourcesPage.switchToXA();

        String name = "TestConnectionValidXAInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testXAConnectionInWizard(dsOps, name, VALID_URL, true);
    }

    @Test
    public void invalidXAInWizard() throws IOException, OperationException {
        datasourcesPage.switchToXA();

        String name = "TestConnectionInvalidXAInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testXAConnectionInWizard(dsOps, name, "invalidUrl", false);
    }
}
