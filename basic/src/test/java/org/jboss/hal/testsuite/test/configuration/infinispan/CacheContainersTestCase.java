package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheContainerWizard;
import org.jboss.hal.testsuite.page.config.CacheContainersPage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
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
@Category(Shared.class)
public class CacheContainersTestCase {

    private static final String CACHE_CONTAINER_NAME = "cc_" + RandomStringUtils.randomAlphabetic(5);
    private static final String JNDI_NAME = "java:/" + CACHE_CONTAINER_NAME;
    private static final String START = "LAZY";
    private static final String EVICTION_EXECUTOR = "ee_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String LISTENER_EXECUTOR = "le_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String REPLICATION_QUEUE_EXECUTOR = "rqe_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TRANSPORT_VALUE = "1000";
    private static final String ALIASES_VALUE = "this\nthat";

    private final String cacheContainerName = "container_" + RandomStringUtils.randomAlphanumeric(5);
    private final String profile = ConfigUtils.isDomain() ? "/profile=" + ConfigUtils.getDefaultProfile() : "";
    private final String cacheContainerDmr = profile + CACHE_CONTAINER_ADDRESS + "=" + cacheContainerName;
    private final String transportDmr = cacheContainerDmr + "/transport=TRANSPORT";

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier attrVerifier = new ResourceVerifier(cacheContainerDmr, client);
    private ConfigAreaChecker attrChecker = new ConfigAreaChecker(attrVerifier);
    private ResourceVerifier transportVerifier = new ResourceVerifier(transportDmr, client);
    private ConfigAreaChecker transportChecker = new ConfigAreaChecker(transportVerifier);

    @Drone
    public WebDriver browser;

    @Page
    public CacheContainersPage page;

    @Before
    public void before_() {
        addCacheContainer();
        page.navigate();
    }

    @After
    public void after() {
        deleteCacheContainer();
    }

    @Test
    @InSequence(0)
    public void createCacheContainer() {
        CacheContainerWizard wizard = page.invokeAddCacheContainer();
        boolean result = wizard.name(CACHE_CONTAINER_NAME).finish();

        assertTrue("Window should be closed.", result);
        attrVerifier.verifyResource(CACHE_CONTAINER_ADDRESS + "=" + CACHE_CONTAINER_NAME, true);
    }


    @Test
    @InSequence(1)
    public void removeCacheContainer() {
        page.removeCacheContainer(CACHE_CONTAINER_NAME);

        attrVerifier.verifyResource(CACHE_CONTAINER_ADDRESS + "=" + CACHE_CONTAINER_NAME, false);

    }

    @Test
    public void editJndiName() {
        page.invokeContainerSettings(cacheContainerName);
        attrChecker.editTextAndAssert(page, "jndi-name", JNDI_NAME).invoke();
    }

    @Test
    public void editDefaultCache() {
        page.invokeContainerSettings(cacheContainerName);
        attrChecker.editTextAndAssert(page, "default-cache", "infDefCache_" + RandomStringUtils.randomAlphanumeric(5)).invoke();
    }

    @Test
    public void editModule() {
        page.invokeContainerSettings(cacheContainerName);
        attrChecker.editTextAndAssert(page, "module", "infModule" + RandomStringUtils.randomAlphanumeric(5)).invoke();
    }

    @Test
    public void setStatisticsEnabledToTrue() {
        page.invokeContainerSettings(cacheContainerName);
        attrChecker.editCheckboxAndAssert(page, "statistics-enabled", true).invoke();
    }

    @Test
    public void setStatisticsEnabledToFalse() {
        page.invokeContainerSettings(cacheContainerName);
        attrChecker.editCheckboxAndAssert(page, "statistics-enabled", false).invoke();
    }

    @Test
    public void editAliases() {
        page.invokeContainerSettings(cacheContainerName);
        attrChecker.editTextAndAssert(page, "aliases", ALIASES_VALUE).invoke();
    }

    @Test
    public void editLockTimeout() {
        page.invokeTransportSettings(cacheContainerName);
        transportChecker.editTextAndAssert(page, "lock-timeout", TRANSPORT_VALUE)
                .invoke();
    }

    @Test
    public void editChannel() {
        page.invokeTransportSettings(cacheContainerName);
        transportChecker.editTextAndAssert(page, "channel", TRANSPORT_VALUE)
                .invoke();
    }

    private void addCacheContainer() {
        String addContainer = CliUtils.buildCommand(cacheContainerDmr, ":add");
        String addTransport = CliUtils.buildCommand(transportDmr, ":add");
        client.executeCommand(addContainer);
        client.executeCommand(addTransport);
    }

    private void deleteCacheContainer() {
        String cmd = CliUtils.buildCommand(cacheContainerDmr, ":remove");
        client.executeCommand(cmd);
    }

}
