package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.jboss.hal.testsuite.page.config.HibernateCachePage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.CACHE_CONTAINER_ADDRESS;
import static org.jboss.hal.testsuite.cli.CliConstants.DOMAIN_CACHE_CONTAINER_ADDRESS;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public abstract class AbstractCacheTestCase {


    protected static final String CACHE_CONTAINER = "hibernate";
    protected static final String ABSTRACT_DMR_BASE = (ConfigUtils.isDomain() ? DOMAIN_CACHE_CONTAINER_ADDRESS : CACHE_CONTAINER_ADDRESS) + "=" + CACHE_CONTAINER;

    protected final String cacheName = "cache_" + RandomStringUtils.randomAlphanumeric(5);
    protected final String cacheDmr = getDmrBase() + cacheName;
    protected final String lockingDmr = cacheDmr + "/locking=LOCKING";
    protected final String transactionDmr = cacheDmr + "/transaction=TRANSACTION";
    protected final String storeDmr = cacheDmr + "/store=STORE";

    protected CliClient client = CliClientFactory.getClient();
    protected ResourceVerifier verifier = new ResourceVerifier(cacheDmr, client);
    protected ResourceVerifier lockingVerifier = new ResourceVerifier(lockingDmr, client);
    protected ResourceVerifier transactionVerifier = new ResourceVerifier(transactionDmr, client);
    protected ResourceVerifier storeVerifier = new ResourceVerifier(storeDmr, client);
    protected ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public HibernateCachePage page;

    @Before
    public void before() {
        addCache();
        Console.withBrowser(browser).refreshAndNavigate(HibernateCachePage.class);
    }

    @After
    public void after() {
        deleteCache();
        client.reload(false);
    }

    @Test
    public void createCache() {
        String name = "cn_" + RandomStringUtils.randomAlphanumeric(5);

        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(name)
                .mode("SYNC")
                .finish();

        Assert.assertTrue("Window should be closed", result);
        verifier.verifyResource(getDmrBase() + name, true);

        page.content().getResourceManager().removeResourceAndConfirm(name);
        verifier.verifyResource(getDmrBase() + name, false);
    }

    //ATTRIBUTES
    @Test
    public void editStartMode() {
        checker.editSelectAndAssert(page, "start", "LAZY").rowName(cacheName).invoke();
    }

    //Fails due to [JBEAP-359]
    @Test
    public void editJndiName() {
        checker.editTextAndAssert(page, "jndi-name", "java:/" + cacheName).rowName(cacheName).invoke();
    }

    //Fails due to [JBEAP-359]
    @Test
    public void editBatching() {
        checker.editCheckboxAndAssert(page, "batching", true).rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "batching", false).rowName(cacheName).invoke();
    }

    //Fails due to [JBEAP-359]
    @Test
    public void editIndexing() {
        checker.editSelectAndAssert(page, "indexing", "LOCAL").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "indexing", "ALL").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "indexing", "NONE").rowName(cacheName).invoke();
    }

    //STORE
    //Fails due to [JBEAP-361]
    @Test
    public void editShared() {
        checker.editCheckboxAndAssert(page, "shared", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "shared", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
    }

    @Test
    public void editPassivation() {
        checker.editCheckboxAndAssert(page, "passivation", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "passivation", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
    }

    //Fails due to [JBEAP-361]
    @Test
    public void editPreload() {
        checker.editCheckboxAndAssert(page, "preload", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "preload", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
    }

    @Test
    public void editFetchState() {
        checker.editCheckboxAndAssert(page, "fetch-state", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "fetch-state", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
    }

    @Test
    public void editPurge() {
        checker.editCheckboxAndAssert(page, "purge", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "purge", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
    }

    //Fails due to [JBEAP-361]
    @Test
    public void editSingleton() {
        checker.editCheckboxAndAssert(page, "singleton", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "singleton", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).invoke();
    }

    //LOCKING
    @Test
    public void editConcurrencyLevel() {
        checker.editTextAndAssert(page, "concurrency-level", "1").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "concurrency-level", "-1").expectError()
                .tab("Locking").rowName(cacheName).invoke();
    }

    @Test
    public void editIsolation() {
        checker.editSelectAndAssert(page, "isolation", "REPEATABLE_READ").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "isolation", "NONE").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "isolation", "SERIALIZABLE").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
    }

    //Fails due to [JBEAP-361]
    @Test
    public void editStriping() {
        checker.editCheckboxAndAssert(page, "striping", true).withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "striping", false).withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
    }

    @Test
    public void editAcquireTimeout() {
        checker.editTextAndAssert(page, "acquire-timeout", "5000").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "acquire-timeout", "-1").expectError()
                .tab("Locking").rowName(cacheName).invoke();
    }

    //TRANSACTION
    @Test
    public void editMode() {
        checker.editSelectAndAssert(page, "mode", "NONE").withVerifier(transactionVerifier)
                .tab("Transaction").rowName(cacheName).dmrAttribute("mode").invoke();
        checker.editSelectAndAssert(page, "mode", "NON_XA").withVerifier(transactionVerifier)
                .tab("Transaction").rowName(cacheName).dmrAttribute("mode").invoke();
    }

    @Test
    public void editLocking() {
        checker.editSelectAndAssert(page, "locking", "OPTIMISTIC").withVerifier(transactionVerifier)
                .tab("Transaction").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "locking", "PESSIMISTIC").withVerifier(transactionVerifier)
                .tab("Transaction").rowName(cacheName).invoke();
    }

    @Test
    public void editStopTimeout() {
        checker.editTextAndAssert(page, "stop-timeout", "13400").withVerifier(transactionVerifier)
                .tab("Transaction").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "stop-timeout", "150asdf50").expectError()
                .tab("Transaction").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "stop-timeout", "-15000").expectError()
                .tab("Transaction").rowName(cacheName).invoke();
    }

    public abstract String getDmrBase();

    public abstract void addCache();

    public abstract void deleteCache();
}
