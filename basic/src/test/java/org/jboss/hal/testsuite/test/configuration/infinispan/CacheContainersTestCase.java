package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheContainerWizard;
import org.jboss.hal.testsuite.page.config.CacheContainersPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ConfigChecker.InputType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class CacheContainersTestCase {

    private static final String CACHE_CONTAINER_NAME = "cc_" + RandomStringUtils.randomAlphabetic(5);
    private static final String JNDI_NAME = "java:/" + CACHE_CONTAINER_NAME;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private static final Administration adminOps = new Administration(client);

    private static final Address SUBSYSTEM_ADDRESS = client.options().isDomain ? Address.of("profile", "full-ha")
            .and("subsystem", "infinispan") : Address.subsystem("infinispan");
    private static final Address CACHE_CONTAINER_ADDRESS = SUBSYSTEM_ADDRESS.and("cache-container",
            CACHE_CONTAINER_NAME);
    private static final Address TRANSPORT_ADDRESS = CACHE_CONTAINER_ADDRESS.and("transport", "TRANSPORT");

    @Drone
    public WebDriver browser;

    @Page
    public CacheContainersPage page;

    @After
    public void after() throws OperationException, IOException, TimeoutException, InterruptedException {
        deleteCacheContainer();
        adminOps.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException {
        client.close();
    }

    @Test
    public void createCacheContainer() throws Exception {
        page.navigate();
        CacheContainerWizard wizard = page.invokeAddCacheContainer();
        boolean result = wizard.name(CACHE_CONTAINER_NAME).finish();

        assertTrue("Window should be closed.", result);
        new ResourceVerifier(CACHE_CONTAINER_ADDRESS, client).verifyExists();
    }


    @Test
    public void removeCacheContainer() throws Exception {
        addCacheContainer();
        page.navigateAndRemoveCacheContainer(CACHE_CONTAINER_NAME);
        new ResourceVerifier(CACHE_CONTAINER_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editJndiName() throws Exception {
        String attributeName = "jndi-name";
        String attributeValue = JNDI_NAME;
        addCacheContainer();
        page.invokeContainerSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, CACHE_CONTAINER_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.TEXT, attributeName, attributeValue)
            .verifyFormSaved()
            .verifyAttribute(attributeName, attributeValue);
    }

    @Test
    public void editDefaultCache() throws Exception {
        String attributeName = "default-cache";
        String attributeValue = "infDefCache_" + RandomStringUtils.randomAlphanumeric(5);
        addCacheContainer();
        page.invokeContainerSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, CACHE_CONTAINER_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.TEXT, attributeName, attributeValue)
            .verifyFormSaved()
            .verifyAttribute(attributeName, attributeValue);
    }

    @Test
    public void editModule() throws Exception {
        String attributeName = "default-cache";
        String attributeValue = "infModule" + RandomStringUtils.randomAlphanumeric(5);
        addCacheContainer();
        page.invokeContainerSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, CACHE_CONTAINER_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.TEXT, attributeName, attributeValue)
            .verifyFormSaved()
            .verifyAttribute(attributeName, attributeValue);
    }

    @Test
    public void setStatisticsEnabledToTrue() throws Exception {
        String attributeName = "statistics-enabled";
        final boolean attributeValue = true;
        addCacheContainer();
        page.invokeContainerSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, CACHE_CONTAINER_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.CHECKBOX, attributeName, attributeValue)
            .verifyFormSaved()
            .verifyAttribute(attributeName, attributeValue);
    }

    @Test
    public void setStatisticsEnabledToFalse() throws InterruptedException, TimeoutException, Exception {
        String attributeName = "statistics-enabled";
        boolean attributeValue = false;
        addCacheContainer();
        page.invokeContainerSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, CACHE_CONTAINER_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.CHECKBOX, attributeName, attributeValue)
            .verifyFormSaved()
            .verifyAttribute(attributeName, attributeValue);
    }

    @Test
    public void editAliases() throws Exception {
        String attributeName = "aliases";
        addCacheContainer();
        page.invokeContainerSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, CACHE_CONTAINER_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.TEXT, attributeName, "this\nthat")
            .verifyFormSaved()
            .verifyAttribute(attributeName, new ModelNode().add("this").add("that"));
    }

    @Test
    public void editLockTimeout() throws Exception {
        String attributeName = "lock-timeout";
        long attributeValue = 3600000;
        addCacheContainer();
        addJGroupsTransport();
        page.invokeTransportSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, TRANSPORT_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.TEXT, attributeName, attributeValue)
            .verifyFormSaved()
            .verifyAttribute(attributeName, attributeValue);
    }

    @Test
    public void editChannel() throws Exception {
        String attributeName = "channel";
        String attributeValue = "ee";
        addCacheContainer();
        addJGroupsTransport();
        page.invokeTransportSettings(CACHE_CONTAINER_NAME);
        new ConfigChecker.Builder(client, TRANSPORT_ADDRESS).configFragment(page.getSettingsConfig())
            .editAndSave(InputType.TEXT, attributeName, attributeValue)
            .verifyFormSaved()
            .verifyAttribute(attributeName, attributeValue);
    }

    private void addCacheContainer() throws Exception {
        ops.add(CACHE_CONTAINER_ADDRESS).assertSuccess();
        new ResourceVerifier(CACHE_CONTAINER_ADDRESS, client).verifyExists();
    }

    private void deleteCacheContainer() throws IOException, OperationException {
        ops.removeIfExists(CACHE_CONTAINER_ADDRESS);
    }

    private void addJGroupsTransport() throws IOException {
        ops.add(TRANSPORT_ADDRESS).assertSuccess();
    }

}
