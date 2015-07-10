package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheContainerWizard;
import org.jboss.hal.testsuite.page.config.ConfigurationPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.CACHE_CONTAINER_ADDRESS;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class CacheContainersTestCase {

    private static final String CACHE_CONTAINER_NAME = "cc_" + RandomStringUtils.randomAlphabetic(5);
    private static final String JNDI_NAME = "java:/" + CACHE_CONTAINER_NAME;
    private static final String START = "LAZY";
    private static final String EVICTION_EXECUTOR = "ee_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String LISTENER_EXECUTOR = "le_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String REPLICATION_QUEUE_EXECUTOR = "rqe_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TRANSPORT_VALUE = "1000";

    private final String cacheContainerName = "container_" + RandomStringUtils.randomAlphanumeric(5);
    private final String cacheContainerDmr = CACHE_CONTAINER_ADDRESS + "=" + cacheContainerName;
    private final String transportDmr = cacheContainerDmr + "/transport=TRANSPORT";

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier attrVerifier = new ResourceVerifier(cacheContainerDmr, client);
    private ConfigAreaChecker attrChecker = new ConfigAreaChecker(attrVerifier);
    private ResourceVerifier transportVerifier = new ResourceVerifier(transportDmr, client);
    private ConfigAreaChecker transportChecker = new ConfigAreaChecker(transportVerifier);

    @Drone
    public WebDriver browser;

    @Page
    public StandaloneConfigurationPage page;

    @Before
    public void before() {
        addCacheContainer();
        browser.navigate().refresh();
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Graphene.goTo(StandaloneConfigurationPage.class);
        Console.withBrowser(browser).waitUntilLoaded().maximizeWindow();
    }

    @After
    public void after() {
        deleteCacheContainer();
    }

    @Test
    @InSequence(0)
    public void createCacheContainer() {
        CacheContainerWizard wizard = page.subsystems().select("Infinispan").add(CacheContainerWizard.class);
        boolean result = wizard.name(CACHE_CONTAINER_NAME).finish();

        assertTrue("Window should be closed.", result);
        attrVerifier.verifyResource(CACHE_CONTAINER_ADDRESS + "=" + CACHE_CONTAINER_NAME, true);
    }


    @Test
    @InSequence(1)
    public void removeCacheContainer() {
        navigateToCache(CACHE_CONTAINER_NAME).remove();

        attrVerifier.verifyResource(CACHE_CONTAINER_ADDRESS + "=" + CACHE_CONTAINER_NAME, false);

    }

    @Test
    public void editStartMode() {
        navigateToCache(cacheContainerName).option("Container Settings");
        attrChecker.editSelectAndAssert(page, "start", START).invoke();
    }

    @Test
    public void editJndiName() {
        navigateToCache(cacheContainerName).option("Container Settings");
        attrChecker.editTextAndAssert(page, "jndi-name", JNDI_NAME).invoke();
    }

    @Test
    public void editEvictionExecutor() {
        navigateToCache(cacheContainerName).option("Container Settings");
        attrChecker.editTextAndAssert(page, "eviction-executor", EVICTION_EXECUTOR).invoke();
    }

    @Test
    public void editListenerExecutor() {
        navigateToCache(cacheContainerName).option("Container Settings");
        attrChecker.editTextAndAssert(page, "listener-executor", LISTENER_EXECUTOR).invoke();
    }

    @Test
    public void editReplicationQueueExecutor() {
        navigateToCache(cacheContainerName).option("Container Settings");
        attrChecker.editTextAndAssert(page, "replication-queue-executor", REPLICATION_QUEUE_EXECUTOR).invoke();
    }

    @Test
    public void editStack() {
        navigateToCache(cacheContainerName).option("Transport Settings");
        transportChecker.editTextAndAssert(page, "stack", TRANSPORT_VALUE)
                .invoke();
    }

    @Test
    public void editExecutor() {
        navigateToCache(cacheContainerName).option("Transport Settings");
        transportChecker.editTextAndAssert(page, "executor", TRANSPORT_VALUE)
                .invoke();
    }

    @Test
    public void editLockTimeout() {
        navigateToCache(cacheContainerName).option("Transport Settings");
        transportChecker.editTextAndAssert(page, "lock-timeout", TRANSPORT_VALUE)
                .invoke();
    }

    private ConfigurationPage navigateToCache(String cache) {
        return page.subsystems().select("Infinispan").select(cache);
    }

    private void addCacheContainer() {
        String addContainer = CliUtils.buildCommand(cacheContainerDmr, ":add");
        String addTransport = CliUtils.buildCommand(transportDmr, ":add", new String[]{"stack=500"});
        client.executeCommand(addContainer);
        client.executeCommand(addTransport);
    }

    private void deleteCacheContainer() {
        String cmd = CliUtils.buildCommand(cacheContainerDmr, ":remove");
        client.executeCommand(cmd);
    }

}
