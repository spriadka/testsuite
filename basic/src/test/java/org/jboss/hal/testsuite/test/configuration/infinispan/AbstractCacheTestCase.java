package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.jboss.hal.testsuite.page.config.HibernateCachePage;
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
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public abstract class AbstractCacheTestCase {

    protected static final Address ABSTRACT_CACHE_ADDRESS = Address.subsystem("infinispan").and("cache-container", "hibernate");

    protected final String cacheName = "cache_" + RandomStringUtils.randomAlphanumeric(5);
    protected final String cacheTypeAddress = getCacheType().getAddressName();
    protected final Address cacheAddress = ABSTRACT_CACHE_ADDRESS.and(cacheTypeAddress, cacheName);

    protected final Address lockingAddress = cacheAddress.and("locking", "LOCKING");
    protected final Address transactionAddress = cacheAddress.and("transaction", "TRANSACTION");
    protected final Address storeAddress = cacheAddress.and("store", "STORE");

    protected static OnlineManagementClient client;
    protected static Administration administration;
    protected static Operations operations;


    @Drone
    public WebDriver browser;

    @Page
    public HibernateCachePage page;

    @BeforeClass
    public static void beforeClass() {
        client = ManagementClientProvider.createOnlineManagementClient();
        operations = new Operations(client);
        administration = new Administration(client);
    }

    @Before
    public void before() throws InterruptedException, TimeoutException, IOException {
        addCache();
        administration.reloadIfRequired();
        page.navigate();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException, OperationException {
        deleteCache();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        client.close();
    }


    @Test
    public void createCache() throws Exception {
        String name = "cn_" + RandomStringUtils.randomAlphanumeric(5);

        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(name)
                .mode("SYNC")
                .finish();

        Assert.assertTrue("Window should be closed", result);
        new ResourceVerifier(ABSTRACT_CACHE_ADDRESS.and(cacheTypeAddress, name), client).verifyExists();

        page.content().getResourceManager().removeResourceAndConfirm(name);
        new ResourceVerifier(ABSTRACT_CACHE_ADDRESS.and(cacheTypeAddress, name), client).verifyDoesNotExist();
    }

    //ATTRIBUTES
    @Test
    public void editJndiName() throws Exception {
        page.selectCache(cacheName);
        editTextAndVerify(cacheAddress, "jndi-name", "java:/" + cacheName);
    }

    //STORE
    @Test
    public void editShared() throws Exception {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "shared", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "shared", false);
    }

    @Test
    public void editPassivation() throws Exception {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "passivation", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "passivation", false);
    }

    @Test
    public void editPreload() throws Exception {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "preload", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "preload", false);
    }

    @Test
    public void editFetchState() throws Exception {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "fetch-state", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "fetch-state", false);
    }

    @Test
    public void editPurge() throws Exception {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "purge", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "purge", false);
    }

    @Test
    public void editSingleton() throws Exception {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "singleton", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(storeAddress, "singleton", false);
    }

    //LOCKING
    @Test
    public void editConcurrencyLevel() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        editTextAndVerify(lockingAddress, "concurrency-level", 1);
        verifyIfErrorAppears("concurrency-level", "-1");
    }

    @Test
    public void editIsolation() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        selectOptionAndVerify(lockingAddress, "isolation", "REPEATABLE_READ");
        page.selectCache(cacheName);
        selectOptionAndVerify(lockingAddress, "isolation", "NONE");
        page.selectCache(cacheName);
        selectOptionAndVerify(lockingAddress, "isolation", "SERIALIZABLE");
    }


    @Test
    public void editStriping() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        editCheckboxAndVerify(lockingAddress, "striping", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(lockingAddress, "striping", false);
    }

    @Test
    public void editAcquireTimeout() throws Exception {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        verifyIfErrorAppears("acquire-timeout", "-1");
        page.selectCache(cacheName);
        editTextAndVerify(lockingAddress, "acquire-timeout", 5000L);
    }

    //TRANSACTION
    @Test
    public void editMode() throws Exception {
        page.getConfig().switchTo("Transaction");
        page.selectCache(cacheName);
        selectOptionAndVerify(transactionAddress, "mode", "NONE");
        page.selectCache(cacheName);
        selectOptionAndVerify(transactionAddress, "mode", "NON_XA");
    }

    @Test
    public void editLocking() throws Exception {
        page.getConfig().switchTo("Transaction");
        page.selectCache(cacheName);
        selectOptionAndVerify(transactionAddress, "locking", "OPTIMISTIC");
        page.selectCache(cacheName);
        selectOptionAndVerify(transactionAddress, "locking", "PESSIMISTIC");
    }

    @Test
    public void editStopTimeout() throws Exception {
        page.getConfig().switchTo("Transaction");
        page.selectCache(cacheName);
        editTextAndVerify(transactionAddress, "stop-timeout", 13400L);
        page.selectCache(cacheName);
        verifyIfErrorAppears("stop-timeout", "150asdf50");
        page.selectCache(cacheName);
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

    protected abstract CacheType getCacheType();

    public abstract void addCache() throws IOException;

    public abstract void deleteCache() throws IOException, OperationException;

}
