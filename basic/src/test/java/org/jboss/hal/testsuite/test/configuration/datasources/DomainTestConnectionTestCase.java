package org.jboss.hal.testsuite.test.configuration.datasources;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author jcechace
 */
@Ignore("Test connection button has been removed from datasource wizard - JBEAP-2821")
@Category(Domain.class)
@RunWith(Arquillian.class)
public class DomainTestConnectionTestCase extends AbstractTestConnectionTestCase {

    @Page
    private DomainConfigurationPage domainConfigurationPage;

    private static String dsNameValid;
    private static String dsSameNameValid;
    private static String dsSameNameInvalid;


    private static final String VALID_URL = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1";
    private static final String INVALID_URL = "invalidUrl";

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static OnlineManagementClient fullHaClient = ManagementClientProvider.withProfile("full-ha");
    private static Administration administration = new Administration(client);
    private static DataSourcesOperations dsOps = new DataSourcesOperations(client);
    private static DomainManager manager = new DomainManager(CliClientFactory.getClient());

    // Setup
    @BeforeClass
    public static void setup() throws CommandFailedException, InterruptedException, TimeoutException, IOException {  // create needed datasources
        dsNameValid = dsOps.createDataSource(VALID_URL);
        dsSameNameValid = new DataSourcesOperations(fullHaClient, "full-ha").createDataSource(VALID_URL); ///
        dsSameNameInvalid = dsOps.createDataSource(dsSameNameValid, INVALID_URL);
        administration.reload();
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException { // remove datasources when finished
        dsOps.removeDataSource(dsNameValid);
        new DataSourcesOperations(fullHaClient, "full-ha").removeDataSource(dsSameNameValid); ///
        dsOps.removeDataSource(dsSameNameInvalid);
    }

    @Before
    public void before() {
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Graphene.goTo(DomainConfigurationPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        domainConfigurationPage.selectProfile(ConfigUtils.getDefaultProfile()).selectMenu("Datasources").selectMenu("Non-XA");
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
        domainConfigurationPage.selectProfile("full-ha").selectMenu("Datasources").selectMenu("Non-XA");
        Console.withBrowser(browser).waitUntilFinished();
        manager.startAllServers(10L);
        testConnection(dsSameNameValid, true);
    }
}
