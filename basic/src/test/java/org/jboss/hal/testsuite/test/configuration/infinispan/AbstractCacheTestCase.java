package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.jboss.hal.testsuite.page.config.HibernateCachePage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public abstract class AbstractCacheTestCase {

    protected static String
            STORE = "store",
            STORE_LABEL = "File Store",
            LOCKING = "locking",
            INFINISPAN = "infinispan",
            CACHE_TBA_NAME = RandomStringUtils.randomAlphabetic(7);

    protected static Address
            ABSTRACT_CACHE_ADDRESS = Address.subsystem(INFINISPAN).and("cache-container", "hibernate"),
            CACHE_ADDRESS,
            CACHE_TBA_ADDRESS,
            CACHE_TBR_ADDRESS,
            LOCKING_ADDRESS,
            TRANSACTION_ADDRESS,
            STORE_ADDRESS;

    protected static OnlineManagementClient client;
    protected static Administration administration;
    protected static Operations operations;


    @Drone
    public WebDriver browser;

    @Page
    public HibernateCachePage page;

    @BeforeClass
    public static void beforeClass() {
        client = ConfigUtils.isDomain() ? ManagementClientProvider.withProfile("full-ha") : ManagementClientProvider.createOnlineManagementClient();
        operations = new Operations(client);
        administration = new Administration(client);
    }

    @Before
    public void before() throws InterruptedException, TimeoutException, IOException {
        //add resources
        CACHE_ADDRESS = createCache();
        CACHE_TBR_ADDRESS = createCache();

        //initialize addresses
        LOCKING_ADDRESS = CACHE_ADDRESS.and(LOCKING, "LOCKING");
        TRANSACTION_ADDRESS = CACHE_ADDRESS.and("transaction", "TRANSACTION");

        //add singleton resources
        STORE_ADDRESS = addStoreToCache(CACHE_ADDRESS);
        operations.add(LOCKING_ADDRESS).assertSuccess();
        operations.add(TRANSACTION_ADDRESS).assertSuccess();

        administration.reloadIfRequired();
        page.navigate();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(CACHE_ADDRESS);
        operations.removeIfExists(CACHE_TBA_ADDRESS);
        operations.removeIfExists(CACHE_TBR_ADDRESS);
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    protected static void initializeTBAAddress(CacheType type) {
        CACHE_TBA_ADDRESS = ABSTRACT_CACHE_ADDRESS.and(type.getAddressName(), CACHE_TBA_NAME);
    }


    @Test
    public void testCreateCache() throws Exception {
        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(CACHE_TBA_NAME)
                .mode("SYNC")
                .finish();

        Assert.assertTrue("Window should be closed", result);
        new ResourceVerifier(CACHE_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeCache() throws Exception {
        page.content().getResourceManager()
                .removeResource(CACHE_TBR_ADDRESS.getLastPairValue())
                .confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(CACHE_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    //ATTRIBUTES
    @Test
    public void editJndiName() throws Exception {
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editTextAndVerify(CACHE_ADDRESS, "jndi-name", "java:/" + CACHE_ADDRESS.getLastPairValue());
    }

    //STORE
    @Test
    public void editShared() throws Exception {
        page.getConfig().switchTo(STORE_LABEL);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "shared", true);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "shared", false);
    }

    @Test
    public void editPassivation() throws Exception {
        page.getConfig().switchTo(STORE_LABEL);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "passivation", true);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "passivation", false);
    }

    @Test
    public void editPreload() throws Exception {
        page.getConfig().switchTo(STORE_LABEL);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "preload", true);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "preload", false);
    }

    @Test
    public void editFetchState() throws Exception {
        page.getConfig().switchTo(STORE_LABEL);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "fetch-state", true);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "fetch-state", false);
    }

    @Test
    public void editPurge() throws Exception {
        page.getConfig().switchTo(STORE_LABEL);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "purge", true);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "purge", false);
    }

    @Test
    public void editSingleton() throws Exception {
        page.getConfig().switchTo(STORE_LABEL);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "singleton", true);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(STORE_ADDRESS, "singleton", false);
    }

    //LOCKING
    @Test
    public void editConcurrencyLevel() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editTextAndVerify(LOCKING_ADDRESS, "concurrency-level", 1);
        verifyIfErrorAppears("concurrency-level", "-1");
    }

    @Test
    public void editIsolation() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        selectOptionAndVerify(LOCKING_ADDRESS, "isolation", "REPEATABLE_READ");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        selectOptionAndVerify(LOCKING_ADDRESS, "isolation", "NONE");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        selectOptionAndVerify(LOCKING_ADDRESS, "isolation", "SERIALIZABLE");
    }


    @Test
    public void editStriping() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(LOCKING_ADDRESS, "striping", true);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editCheckboxAndVerify(LOCKING_ADDRESS, "striping", false);
    }

    @Test
    public void editAcquireTimeout() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        verifyIfErrorAppears("acquire-timeout", "-1");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editTextAndVerify(LOCKING_ADDRESS, "acquire-timeout", 5000L);
    }

    //TRANSACTION
    @Test
    public void editMode() throws Exception {
        page.getConfig().switchTo("Transaction");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        selectOptionAndVerify(TRANSACTION_ADDRESS, "mode", "NONE");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        selectOptionAndVerify(TRANSACTION_ADDRESS, "mode", "NON_XA");
    }

    @Test
    public void editLocking() throws Exception {
        page.getConfig().switchTo("Transaction");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        selectOptionAndVerify(TRANSACTION_ADDRESS, "locking", "OPTIMISTIC");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        selectOptionAndVerify(TRANSACTION_ADDRESS, "locking", "PESSIMISTIC");
    }

    @Test
    public void editStopTimeout() throws Exception {
        page.getConfig().switchTo("Transaction");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        editTextAndVerify(TRANSACTION_ADDRESS, "stop-timeout", 13400L);
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        verifyIfErrorAppears("stop-timeout", "150asdf50");
        page.selectCache(CACHE_ADDRESS.getLastPairValue());
        verifyIfErrorAppears("stop-timeout", "-15000");
    }

    protected void editTextAndVerify(Address address, String identifier, String value) throws Exception {
        page.editTextAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void editTextAndVerify(Address address, String identifier, int value) throws Exception {
        page.editTextAndSave(identifier, String.valueOf(value));
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void editTextAndVerify(Address address, String identifier, long value) throws Exception {
        page.editTextAndSave(identifier, String.valueOf(value));
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void selectOptionAndVerify(Address address, String identifier, String value) throws Exception {
        page.selectOptionAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        page.editTextAndSave(identifier, value);
        Assert.assertTrue(page.isErrorShownInForm());
        config.cancel();
    }

    protected void editCheckboxAndVerify(Address address, String identifier, Boolean value) throws Exception {
        page.editCheckboxAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(identifier, value);
    }

    /**
     * Method to add specific cache type in model - not a test
     */
    protected abstract Address createCache() throws IOException, TimeoutException, InterruptedException;

    private Address addStoreToCache(Address address) throws IOException, TimeoutException, InterruptedException {
        final Address storeAddress = address.and(STORE, "file");
        //service restart is needed, because default service 'none' is removed and to install new restart must take place
        operations.headers(Values.of("allow-resource-service-restart", true))
                .add(storeAddress)
                .assertSuccess();
        administration.reloadIfRequired();
        return storeAddress;
    }

}
