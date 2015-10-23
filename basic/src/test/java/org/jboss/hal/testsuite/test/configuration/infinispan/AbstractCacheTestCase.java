package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.jboss.hal.testsuite.page.config.HibernateCachePage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public abstract class AbstractCacheTestCase {

    protected static final AddressTemplate ABSTRACT_CACHE_TEMPLATE = AddressTemplate.of("{default.profile}/subsystem=infinispan/cache-container=hibernate");

    protected final String cacheName = "cache_" + RandomStringUtils.randomAlphanumeric(5);
    protected final AddressTemplate cacheAddress = getCacheTemplate();

    protected final AddressTemplate lockingTemplate = cacheAddress.append("/locking=LOCKING");
    protected final AddressTemplate transactionTemplate = cacheAddress.append("/transaction=TRANSACTION");
    protected final AddressTemplate storeTemplate = cacheAddress.append("/store=STORE");

    protected CliClient client = CliClientFactory.getClient();

    protected Dispatcher dispatcher = new Dispatcher();
    protected org.jboss.hal.testsuite.dmr.ResourceVerifier validVerifier = new org.jboss.hal.testsuite.dmr.ResourceVerifier(dispatcher);
    protected StatementContext context = new DefaultContext();

    @Drone
    public WebDriver browser;

    @Page
    public HibernateCachePage page;

    @Before
    public void before() {
        addCache();
        reloadIfRequiredAndWaitForRunning();
        page.navigate();
        page.selectCache(cacheName);
    }

    @After
    public void after() {
        deleteCache();
        reloadIfRequiredAndWaitForRunning();
    }


    @Test
    public void createCache() {
        String name = "cn_" + RandomStringUtils.randomAlphanumeric(5);

        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(name)
                .mode("SYNC")
                .finish();

        Assert.assertTrue("Window should be closed", result);
        validVerifier.verifyResource(cacheAddress.resolve(context, name));

        page.content().getResourceManager().removeResourceAndConfirm(name);
        validVerifier.verifyResource(cacheAddress.resolve(context, name), false);
    }

    //ATTRIBUTES
    @Test
    public void editJndiName() {
        ResourceAddress address = cacheAddress.resolve(context, cacheName);
        editTextAndVerify(address, "jndi-name", "java:/" + cacheName);
    }

    //STORE
    @Test
    public void editShared() {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        ResourceAddress address = storeTemplate.resolve(context, cacheName);
        editCheckboxAndVerify(address, "shared", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(address, "shared", false);
    }

    @Test
    public void editPassivation() {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        ResourceAddress address = storeTemplate.resolve(context, cacheName);
        editCheckboxAndVerify(address, "passivation", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(address, "passivation", false);
    }

    @Test
    public void editPreload() {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        ResourceAddress address = storeTemplate.resolve(context, cacheName);
        editCheckboxAndVerify(address, "preload", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(address, "preload", false);
    }

    @Test
    public void editFetchState() {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        ResourceAddress address = storeTemplate.resolve(context, cacheName);
        editCheckboxAndVerify(address, "fetch-state", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(address, "fetch-state", false);
    }

    @Test
    public void editPurge() {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        ResourceAddress address = storeTemplate.resolve(context, cacheName);
        editCheckboxAndVerify(address, "purge", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(address, "purge", false);
    }

    @Test
    public void editSingleton() {
        page.getConfig().switchTo("Store");
        page.selectCache(cacheName);
        ResourceAddress address = storeTemplate.resolve(context, cacheName);
        editCheckboxAndVerify(address, "singleton", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(address, "singleton", false);
    }

    //LOCKING
    @Test
    public void editConcurrencyLevel() {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        ResourceAddress address = lockingTemplate.resolve(context, cacheName);
        editTextAndVerify(address, "concurrency-level", "1");
        verifyIfErrorAppears("concurrency-level", "-1");
    }

    @Test
    public void editIsolation() {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        ResourceAddress address = lockingTemplate.resolve(context, cacheName);
        selectOptionAndVerify(address, "isolation", "REPEATABLE_READ");
        page.selectCache(cacheName);
        selectOptionAndVerify(address, "isolation", "NONE");
        page.selectCache(cacheName);
        selectOptionAndVerify(address, "isolation", "SERIALIZABLE");
    }


    @Test
    public void editStriping() {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        ResourceAddress address = lockingTemplate.resolve(context, cacheName);
        editCheckboxAndVerify(address, "striping", true);
        page.selectCache(cacheName);
        editCheckboxAndVerify(address, "striping", false);
    }

    @Test
    public void editAcquireTimeout() {
        page.getConfig().switchTo("Locking");
        page.selectCache(cacheName);
        ResourceAddress address = lockingTemplate.resolve(context, cacheName);
        verifyIfErrorAppears("acquire-timeout", "-1");
        page.selectCache(cacheName);
        editTextAndVerify(address, "acquire-timeout", "5000");
    }

    //TRANSACTION
    @Test
    public void editMode() {
        page.getConfig().switchTo("Transaction");
        page.selectCache(cacheName);
        ResourceAddress address = transactionTemplate.resolve(context, cacheName);
        selectOptionAndVerify(address, "mode", "NONE");
        page.selectCache(cacheName);
        selectOptionAndVerify(address, "mode", "NON_XA");
    }

    @Test
    public void editLocking() {
        page.getConfig().switchTo("Transaction");
        page.selectCache(cacheName);
        ResourceAddress address = transactionTemplate.resolve(context, cacheName);
        selectOptionAndVerify(address, "locking", "OPTIMISTIC");
        page.selectCache(cacheName);
        selectOptionAndVerify(address, "locking", "PESSIMISTIC");
    }

    @Test
    public void editStopTimeout() {
        page.getConfig().switchTo("Transaction");
        page.selectCache(cacheName);
        ResourceAddress address = transactionTemplate.resolve(context, cacheName);
        editTextAndVerify(address, "stop-timeout", "13400");
        page.selectCache(cacheName);
        verifyIfErrorAppears("stop-timeout", "150asdf50");
        page.selectCache(cacheName);
        verifyIfErrorAppears("stop-timeout", "-15000");
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String value) {
        page.editTextAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        validVerifier.verifyAttribute(address, identifier, value);
    }

    protected void selectOptionAndVerify(ResourceAddress address, String identifier, String value) {
        page.selectOptionAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        validVerifier.verifyAttribute(address, identifier, value);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        page.editTextAndSave(identifier, value);
        Assert.assertTrue(page.isErrorShownInForm());
        config.cancel();
    }

    protected void editCheckboxAndVerify(ResourceAddress address, String identifier, Boolean value) {
        page.editCheckboxAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        validVerifier.verifyAttribute(address, identifier, value.toString());
    }

    protected abstract AddressTemplate getCacheTemplate();

    public abstract void addCache();

    public abstract void deleteCache();

    protected void reloadIfRequiredAndWaitForRunning() {
        int timeout = 60000;
        if (ConfigUtils.isDomain()) {
            new DomainManager(client).reloadAndWaitUntilRunning(timeout);
        } else {
            client.reload(false);
        }
    }
}
