package org.jboss.hal.testsuite.test.configuration.datasources;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
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


/**
 * Created by jcechace on 21/02/14.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class TestConnectionTestCase extends AbstractTestConnectionTestCase {

    private static String dsNameValid = "dsNameValid" + RandomStringUtils.randomAlphanumeric(5);
    private static String xaDsNameValid = "xaDsNameValid" + RandomStringUtils.randomAlphanumeric(5);
    private static String dsNameInvalid = "dsNameInvalid" + RandomStringUtils.randomAlphanumeric(5);
    private static String xaDsNameInvalid = "xaDsNameInvalid" + RandomStringUtils.randomAlphanumeric(5);

    private static final String VALID_URL = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1";
    private static final String INVALID_URL = "invalidUrl";

    private static CliClient client;
    private static DataSourcesOperations dsOps;

    // Setup

    @BeforeClass
    public static void setup() {  // create needed datasources
        client = CliClientFactory.getClient();
        dsOps = new DataSourcesOperations(client);
        dsNameValid = dsOps.createDataSource(VALID_URL);
        dsNameInvalid = dsOps.createDataSource(INVALID_URL);
        xaDsNameInvalid = dsOps.createXADataSource(INVALID_URL);
        xaDsNameValid = dsOps.createXADataSource(VALID_URL);
        client.reload();
    }

    @AfterClass
    public static void tearDown() { // remove datasources when finished
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
    public void validInWizard() {
        String name = "validInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testConnectionInWizard(dsOps, name, VALID_URL, true);
    }

    @Test
    public void invalidInWizard() {
        String name = "invalidInWizard_" + RandomStringUtils.randomAlphabetic(6);
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
    public void validXAInWizard() {
        datasourcesPage.switchToXA();

        String name = "validXAInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testXAConnectionInWizard(dsOps, name, VALID_URL, true);
    }

    @Test
    public void invalidXAInWizard() {
        datasourcesPage.switchToXA();

        String name = "invalidXAInWizard_" + RandomStringUtils.randomAlphabetic(6);
        testXAConnectionInWizard(dsOps, name, "invalidUrl", false);
    }
}
