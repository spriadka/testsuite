package org.jboss.hal.testsuite.test.runtime.elytron;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.page.runtime.ElytronRuntimePage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class CredentialStoreAliasTestCase extends AbstractElytronTestCase {

    private static final String CREDENTIAL_STORE_NAME = "credential_store_" + RandomStringUtils.randomAlphanumeric(7);
    private static Address credentialStoreAddress;

    private static final String ALIAS = "alias";
    private static final String SECRET_VALUE = "secret-value";

    private static final String ALIASES_TAB_LABEL = "Aliases";

    private static final String ADD_ALIAS_OPERATION = "add-alias";
    private static final String READ_ALIASES_OPERATION = "read-aliases";
    private static final String REMOVE_ALIAS_OPERATION = "remove-alias";

    private static final String STATE = "state";
    private static final String START_FAILED_STATE = "START_FAILED";
    private static final String UP_STATE = "UP";

    private static final String SET_SECRET_LABEL = "Set secret";

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        credentialStoreAddress = elyOps.createCredentialStoreWithCredentialReferenceClearText(CREDENTIAL_STORE_NAME);
        adminOps.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        ops.removeIfExists(credentialStoreAddress);
        adminOps.reloadIfRequired();
    }

    @Page
    private ElytronRuntimePage page;

    @Test
    public void testAddAliasRequiredFieldsOnly() throws IOException, TimeoutException, InterruptedException {
        final String aliasName = "my_alias_" + RandomStringUtils.randomNumeric(7);
        try {
            page.navigate();
            page.getResourceManager().selectByName(CREDENTIAL_STORE_NAME);
            ResourceManager aliasManager = page.getConfig()
                    .switchTo(ALIASES_TAB_LABEL)
                    .getResourceManager();
            aliasManager
                    .addResource(AddResourceWizard.class)
                    .text(ALIAS, aliasName)
                    .saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();
            Assert.assertTrue("Created alias should be present in table", aliasManager.isResourcePresent(aliasName));
            ModelNodeResult aliases = ops.invoke(READ_ALIASES_OPERATION, credentialStoreAddress);
            aliases.assertSuccess();
            Assert.assertTrue("Created alias should be present in model", aliases.stringListValue().contains(aliasName));
        } finally {
            ops.invoke(REMOVE_ALIAS_OPERATION, credentialStoreAddress, Values.of(ALIAS, aliasName));
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void testAddAliasWithOptionalFields() throws IOException, TimeoutException, InterruptedException {
        final String aliasName = "my_alias_" + RandomStringUtils.randomNumeric(7);
        final String aliasSecretValue = RandomStringUtils.randomAlphanumeric(7);
        try {
            page.navigate();
            page.getResourceManager().selectByName(CREDENTIAL_STORE_NAME);
            ResourceManager aliasManager = page.getConfig()
                    .switchTo(ALIASES_TAB_LABEL)
                    .getResourceManager();
            AddResourceWizard wizard = aliasManager.addResource(AddResourceWizard.class);
            wizard.text(ALIAS, aliasName);
            wizard.openOptionalFieldsTab();
            wizard.text(SECRET_VALUE, aliasSecretValue)
                    .saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();
            Assert.assertTrue("Created alias should be present in table", aliasManager.isResourcePresent(aliasName));
            ModelNodeResult aliases = ops.invoke(READ_ALIASES_OPERATION, credentialStoreAddress);
            aliases.assertSuccess();
            Assert.assertTrue("Created alias should be present in model", aliases.stringListValue().contains(aliasName));
        } finally {
            ops.invoke(REMOVE_ALIAS_OPERATION, credentialStoreAddress, Values.of(ALIAS, aliasName));
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void testRemoveAlias() throws IOException, TimeoutException, InterruptedException {
        final String aliasName = "my_alias_" + RandomStringUtils.randomNumeric(7);
        try {
            ops.invoke(ADD_ALIAS_OPERATION, credentialStoreAddress, Values.of(ALIAS, aliasName)).assertSuccess();
            page.navigate();
            page.getResourceManager().selectByName(CREDENTIAL_STORE_NAME);
            ResourceManager aliasManager = page.getConfig()
                    .switchTo(ALIASES_TAB_LABEL)
                    .getResourceManager();
            aliasManager
                    .removeResource(aliasName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Removed alias should not be present in table anymore", aliasManager.isResourcePresent(aliasName));
            ModelNodeResult aliases = ops.invoke(READ_ALIASES_OPERATION, credentialStoreAddress);
            aliases.assertSuccess();
            Assert.assertFalse("Removed alias should not be present in model anymore", aliases.stringListValue().contains(aliasName));
        } finally {
            ops.invoke(REMOVE_ALIAS_OPERATION, credentialStoreAddress, Values.of(ALIAS, aliasName));
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void testRefreshCredentialStores() throws IOException, TimeoutException, InterruptedException, OperationException {
        final String credentialStoreName = "created_credential_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address credentialStoreAddress = ElytronOperations.getElytronSubsystemAddress()
                .and(ElytronOperations.CREDENTIAL_STORE, credentialStoreName);
        try {
            page.navigate();
            ResourceManager credentialStoresManager = page.getResourceManager();
            Assert.assertFalse("Credential store should not be visible in the table as it has not been created yet"
                    , credentialStoresManager.isResourcePresent(credentialStoreName));
            elyOps.createCredentialStoreWithCredentialReferenceClearText(credentialStoreAddress);
            adminOps.reloadIfRequired();
            page.refreshCredentialStores();
            Assert.assertTrue("Credential store should pre present in the table", credentialStoresManager.isResourcePresent(credentialStoreName));
        } finally {
            ops.removeIfExists(credentialStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void testSetSecret() throws Exception {
        final String credentialStoreName = "owning_credential_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address credentialStoreAddress = ElytronOperations.getElytronSubsystemAddress()
                .and(ElytronOperations.CREDENTIAL_STORE, credentialStoreName);
        final String dependingCredentialStoreName = "depending_credential_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dependingCredentialStoreAddress = ElytronOperations.getElytronSubsystemAddress()
                .and(ElytronOperations.CREDENTIAL_STORE, dependingCredentialStoreName);
        final String aliasName = "alias_" + RandomStringUtils.randomNumeric(7);
        final String aliasSecret = RandomStringUtils.randomAlphanumeric(7);
        try {
            elyOps.createCredentialStoreWithCredentialReferenceClearText(credentialStoreAddress);
            ops.invoke(ADD_ALIAS_OPERATION, credentialStoreAddress, Values.of(ALIAS, aliasName).and(SECRET_VALUE, aliasSecret))
                    .assertSuccess();
            ops.add(dependingCredentialStoreAddress, Values.of(ElytronOperations.CREATE, true)
                    .and(ElytronOperations.CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(ALIAS, aliasName)
                            .addProperty(ElytronOperations.STORE, credentialStoreName).build())).assertSuccess();
            page.navigate();
            page.getResourceManager().selectByName(credentialStoreName);
            ResourceManager aliasManager = page.getConfig().switchTo(ALIASES_TAB_LABEL).getResourceManager();
            aliasManager.selectByName(aliasName);
            AddResourceWizard wizard = aliasManager.addResource(AddResourceWizard.class, SET_SECRET_LABEL);
            wizard.openOptionalFieldsTab();
            wizard.text(SECRET_VALUE, RandomStringUtils.randomAlphanumeric(10));
            wizard.saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();
            adminOps.reload();
            page.refreshCredentialStores();
            Assert.assertEquals("Depending credential store should have \"" + START_FAILED_STATE + "\" state in the UI after alias secret change"
                    , START_FAILED_STATE, page.getResourceManager().selectByName(dependingCredentialStoreName).getCellValue(1));
            new ResourceVerifier(dependingCredentialStoreAddress, client).verifyAttribute(STATE, START_FAILED_STATE);
            page.getResourceManager().selectByName(credentialStoreName);
            page.getConfig().switchTo(ALIASES_TAB_LABEL).getResourceManager().selectByName(aliasName);
            wizard = aliasManager.addResource(AddResourceWizard.class, SET_SECRET_LABEL);
            wizard.openOptionalFieldsTab();
            wizard.text(SECRET_VALUE, aliasSecret);
            wizard.saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();
            adminOps.reload();
            page.refreshCredentialStores();
            Assert.assertEquals("Depending credential store should have \"" + UP_STATE + "\" state in the UI after reverting alias secret change"
                    , UP_STATE, page.getResourceManager().selectByName(dependingCredentialStoreName).getCellValue(1));
            new ResourceVerifier(dependingCredentialStoreAddress, client).verifyAttribute(STATE, UP_STATE);

        } finally {
            ops.removeIfExists(dependingCredentialStoreAddress);
            ops.removeIfExists(credentialStoreAddress);
        }
    }

}
