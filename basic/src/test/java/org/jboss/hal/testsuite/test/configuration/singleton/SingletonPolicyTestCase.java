package org.jboss.hal.testsuite.test.configuration.singleton;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.SingletonSubsystemPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class SingletonPolicyTestCase {

    private static final String
            SINGLETON_POLICY_RESOURCE = "s-pol_" + RandomStringUtils.randomAlphanumeric(5),
            SINGLETON_POLICY_RESOURCE_TBA = "s-pol_tba_" + RandomStringUtils.randomAlphanumeric(5),
            SINGLETON_POLICY_RESOURCE_TBR = "s-pol_tbr_" + RandomStringUtils.randomAlphanumeric(5),
            CACHE_CONTAINER_RESOURCE = "cache-container-singleton_" + RandomStringUtils.randomAlphanumeric(5),
            CACHE_CONTAINER_RESOURCE_EDIT = "cache-container-singleton-edit_" + RandomStringUtils.randomAlphanumeric(5),
            SINGLETON_POLICY = "singleton-policy",
            CACHE_CONTAINER = "cache-container",
            CACHE = "cache",
            QUORUM = "quorum",
            NAME = "name";

    private static final Address
            SINGLETON_SUBSYSTEM_ADDRESS = Address.subsystem("singleton"),
            INFINISPAN_SUBSYSTEM_ADDRESS = Address.subsystem("infinispan"),
            SINGLETON_POLICY_ADDRESS = SINGLETON_SUBSYSTEM_ADDRESS.and(SINGLETON_POLICY, SINGLETON_POLICY_RESOURCE),
            SINGLETON_POLICY_TBR_ADDRESS = SINGLETON_SUBSYSTEM_ADDRESS.and(SINGLETON_POLICY, SINGLETON_POLICY_RESOURCE_TBR),
            SINGLETON_POLICY_TBA_ADDRESS = SINGLETON_SUBSYSTEM_ADDRESS.and(SINGLETON_POLICY, SINGLETON_POLICY_RESOURCE_TBA),
            CACHE_CONTAINER_RESOURCE_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS.and(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE),
            CACHE_CONTAINER_RESOURCE_EDIT_ADDRESS = INFINISPAN_SUBSYSTEM_ADDRESS.and(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE_EDIT);


    private static final OnlineManagementClient client = ConfigUtils.isDomain() ?
            ManagementClientProvider.withProfile("full-ha") :
            ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final SingletonSubsystemOperations singletonOperations = new SingletonSubsystemOperations(client);

    @Drone
    private WebDriver browser;

    @Page
    private SingletonSubsystemPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        singletonOperations.prepareCacheContainer(CACHE_CONTAINER_RESOURCE_ADDRESS);
        singletonOperations.prepareCacheContainer(CACHE_CONTAINER_RESOURCE_EDIT_ADDRESS);
        operations.add(SINGLETON_POLICY_ADDRESS, Values.of(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE)).assertSuccess();
        operations.add(SINGLETON_POLICY_TBR_ADDRESS, Values.of(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE)).assertSuccess();
    }

    @AfterClass
    public static void afterClass() throws IOException, TimeoutException, InterruptedException, OperationException {
        try {
            operations.removeIfExists(SINGLETON_POLICY_ADDRESS);
            operations.removeIfExists(SINGLETON_POLICY_TBA_ADDRESS);
            operations.removeIfExists(SINGLETON_POLICY_TBR_ADDRESS);
            singletonOperations.removeCacheContainer(CACHE_CONTAINER_RESOURCE_ADDRESS);
            singletonOperations.removeCacheContainer(CACHE_CONTAINER_RESOURCE_EDIT_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addSingletonPolicy() throws Exception {
        page.treeNavigation()
                .step("singleton-policy")
                .navigateToTreeItem()
                .clickLabel();

        WizardWindow window = page.getResourceManager().addResource();
        Editor editor = window.getEditor();

        editor.text(NAME, SINGLETON_POLICY_RESOURCE_TBA);
        editor.text(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE);

        window.finish();

        new ResourceVerifier(SINGLETON_POLICY_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeSingletonPolicy() throws Exception {
        page.treeNavigation()
                .step("singleton-policy")
                .navigateToTreeItem()
                .clickLabel();

        page.getResourceManager()
                .removeResource(SINGLETON_POLICY_RESOURCE_TBR)
                .confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(SINGLETON_POLICY_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editCacheContainer() throws Exception {
        page.treeNavigation()
                .step("singleton-policy")
                .step(SINGLETON_POLICY_RESOURCE)
                .navigateToTreeItem()
                .clickLabel();

        try {
            new ConfigChecker.Builder(client, SINGLETON_POLICY_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE_EDIT)
                    .verifyFormSaved()
                    .verifyAttribute(CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE_EDIT);
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("This is probably failing because of https://issues.jboss.org/browse/HAL-1255", e);
        }
    }

    @Test
    public void editCache() throws Exception {
        final String CACHE_RESOURCE = "singleton-cache_edit_" + RandomStringUtils.randomAlphanumeric(5);

        operations.add(CACHE_CONTAINER_RESOURCE_ADDRESS.and("local-cache", CACHE_RESOURCE));
        operations.writeAttribute(SINGLETON_POLICY_ADDRESS, CACHE_CONTAINER, CACHE_CONTAINER_RESOURCE);

        page.treeNavigation()
                .step(SINGLETON_POLICY)
                .step(SINGLETON_POLICY_RESOURCE)
                .navigateToTreeItem()
                .clickLabel();

        //value has to be set back, since it is dependent on set cache-container
        ModelNodeResult resultValue = operations.readAttribute(SINGLETON_POLICY_ADDRESS, CACHE, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        resultValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, SINGLETON_POLICY_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CACHE, CACHE_RESOURCE)
                    .verifyFormSaved()
                    .verifyAttribute(CACHE, CACHE_RESOURCE);
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("This is probably failing because of https://issues.jboss.org/browse/HAL-1255", e);
        } finally {
            operations.writeAttribute(SINGLETON_POLICY_ADDRESS, CACHE, resultValue.value());
        }
    }

    @Test
    public void editCacheInvalid() throws Exception {
        page.treeNavigation()
                .step("singleton-policy")
                .step(SINGLETON_POLICY_RESOURCE)
                .navigateToTreeItem()
                .clickLabel();

        String value = "this-cache-does-not-exists-under-set-cache-container";
        new ConfigChecker.Builder(client, SINGLETON_POLICY_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, CACHE, value)
                .verifyFormSaved()
                .verifyAttributeNotEqual(CACHE, new ModelNode().set(value));
    }

    @Test
    public void editQuorum() throws Exception {
        page.treeNavigation()
                .step("singleton-policy")
                .step(SINGLETON_POLICY_RESOURCE)
                .navigateToTreeItem()
                .clickLabel();

        final int value = 5;

        try {
            new ConfigChecker.Builder(client, SINGLETON_POLICY_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, QUORUM, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(QUORUM, value);
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("This is probably failing because of https://issues.jboss.org/browse/HAL-1255", e);
        }
    }

    @Test
    public void editQuorumInvalid() throws Exception {
        page.treeNavigation()
                .step("singleton-policy")
                .step(SINGLETON_POLICY_RESOURCE)
                .navigateToTreeItem()
                .clickLabel();

        new ConfigChecker.Builder(client, SINGLETON_POLICY_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, QUORUM, "Kryton")
                .verifyFormNotSaved();
    }

    @Test
    public void editQuorumNegative() throws Exception {
        page.treeNavigation()
                .step("singleton-policy")
                .step(SINGLETON_POLICY_RESOURCE)
                .navigateToTreeItem()
                .clickLabel();

        new ConfigChecker.Builder(client, SINGLETON_POLICY_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, QUORUM, "-297")
                .verifyFormNotSaved();
    }
}
