package org.jboss.hal.testsuite.test.configuration.datasources;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.test.category.Domain;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author jcechace
 */
@Category(Domain.class)
@RunWith(Arquillian.class)
public class DomainTestConnectionTestCase extends AbstractTestConnectionTestCase {

    @Page
    DomainConfigurationPage domainConfigurationPage;

    private static String dsNameValid;
    private static String dsSameNameValid;
    private static String dsSameNameInvalid;


    private static final String VALID_URL = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1";
    private static final String INVALID_URL = "invalidUrl";

    private static CliClient client = CliClientFactory.getDomainClient("full");
    private static CliClient fullHaClient = CliClientFactory.getDomainClient("full-ha");
    private static DataSourcesOperations dsOps = new DataSourcesOperations(client);
    private static DomainManager manager = new DomainManager(client);

    // Setup
    @BeforeClass
    public static void setup() {  // create needed datasources
        dsNameValid = dsOps.createDataSource(VALID_URL);
        dsSameNameValid = new DataSourcesOperations(fullHaClient, "full-ha").createDataSource(VALID_URL);///
        dsSameNameInvalid = dsOps.createDataSource(dsSameNameValid, INVALID_URL);
        client.reload();
    }

    @AfterClass
    public static void tearDown() { // remove datasources when finished
        dsOps.removeDataSource(dsNameValid);
        new DataSourcesOperations(fullHaClient, "full-ha").removeDataSource(dsSameNameValid);///
        dsOps.removeDataSource(dsSameNameInvalid);
    }

    @Before
    public void before() {
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Graphene.goTo(DomainConfigurationPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        domainConfigurationPage.selectProfile(ConfigUtils.getDefaultProfile()).view("Datasources");
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @After
    public void after() {
        browser.navigate().refresh();
    }

    @Test
    public void testValidWithNoRunningServer() throws IOException {
        manager.stopAllServers(10L);
        testConnection(dsNameValid, false);
    }

    @Test
    public void testInvalidWithSameName() throws IOException {
        manager.startAllServers(10L);
        testConnection(dsSameNameValid, false);
    }

    @Test
    public void testValidWithSameNameInOtherGroup() throws IOException {
        Graphene.goTo(DomainConfigurationPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        domainConfigurationPage.selectProfile("full-ha").view("Datasources");
        Console.withBrowser(browser).waitUntilFinished();
        manager.startAllServers(10L);
        testConnection(dsSameNameValid, true);
    }
}
