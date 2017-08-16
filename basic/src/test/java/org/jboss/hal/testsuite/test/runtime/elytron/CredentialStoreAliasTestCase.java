package org.jboss.hal.testsuite.test.runtime.elytron;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.runtime.elytron.AliasWizard;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.page.runtime.ElytronRuntimePage;
import org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.IOUtils;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.online.operations.admin.DomainAdministration;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class CredentialStoreAliasTestCase {

    private static final String CREDENTIAL_STORE_NAME = "credential_store_" + RandomStringUtils.randomAlphanumeric(7);
    private static final String PARENT_CREDENTIAL_STORE_NAME = "parent_credential_store_" + RandomStringUtils.randomAlphanumeric(7);
    private static final String CHILD_CREDENTIAL_STORE_NAME = "child_credential_store_" + RandomStringUtils.randomAlphanumeric(7);

    private static final String TEST_SERVER = "test_server_" + RandomStringUtils.randomAlphanumeric(7);
    private static final String TEST_SERVER_GROUP = "test_server_group_" + RandomStringUtils.randomAlphanumeric(7);
    private static final String TEST_PROFILE = "test_profile_" + RandomStringUtils.randomAlphanumeric(7);

    private static Address CREDENTIAL_STORE_ADDRESS;
    private static Address PARENT_CREDENTIAL_STORE_ADDRESS;
    private static Address CHILD_CREDENTIAL_STORE_ADDRESS;

    private static Address TEST_SERVER_ADDRESS = Address.host(ConfigUtils.getDefaultHost()).and(Constants.SERVER_CONFIG, TEST_SERVER);
    private static Address TEST_SERVER_GROUP_ADDRESS = Address.root().and(Constants.SERVER_GROUP, TEST_SERVER_GROUP);
    private static Address TEST_PROFILE_ADDRESS = Address.root().and(Constants.PROFILE, TEST_PROFILE);

    private static final String ALIAS = "alias";
    private static final String SECRET_VALUE = "secret-value";
    private static final String ENTRY_TYPE = "entry-type";

    private static final String ADD_ALIAS_OPERATION = "add-alias";
    private static final String READ_ALIASES_OPERATION = "read-aliases";
    private static final String REMOVE_ALIAS_OPERATION = "remove-alias";

    private static final String STATE = "state";
    private static final String STATE_START_FAILED = "START_FAILED";
    private static final String STATE_UP = "UP";

    private static final String SET_SECRET_LABEL = "Set secret";

    private static OnlineManagementClient client;
    private static Operations operations;
    private static ElytronOperations elytronOperations;
    private static Administration administration;
    private static DomainAdministration domainAdministration;

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException {
        if (ConfigUtils.isDomain()) {
            client = ManagementClientProvider.withProfile(TEST_PROFILE);
            operations = new Operations(client);
            domainAdministration = new DomainAdministration(client);
            elytronOperations = new ElytronOperations(client);
            setUpTestProfile();
            setUpCredentialStores();
            setUpTestServerGroup();
            setUpTestServer();
            domainAdministration.reloadIfRequired();
        } else {
            client = ManagementClientProvider.createOnlineManagementClient();
            operations = new Operations(client);
            administration = new Administration(client);
            elytronOperations = new ElytronOperations(client);
            setUpCredentialStores();
            administration.reloadIfRequired();
        }
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            if (ConfigUtils.isDomain()) {
                domainAdministration.shutdownServer(TEST_SERVER);
            }
            operations.removeIfExists(CREDENTIAL_STORE_ADDRESS);
            operations.removeIfExists(CHILD_CREDENTIAL_STORE_ADDRESS);
            operations.removeIfExists(PARENT_CREDENTIAL_STORE_ADDRESS);
            operations.removeIfExists(TEST_SERVER_ADDRESS);
            operations.removeIfExists(TEST_SERVER_GROUP_ADDRESS);
            operations.removeIfExists(TEST_PROFILE_ADDRESS);
            reloadAdministrationIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Page
    private ElytronRuntimePage page;

    @Drone
    private WebDriver browser;

    /**
     * @tpTestDetails Try to add alias in Web Console to credential store instance filling only required fields
     * Validate added alias is present in aliases table in the UI
     * Validate added alias is present in model
     */
    @Test
    public void testAddAliasRequiredFieldsOnly() throws IOException, TimeoutException, InterruptedException {
        final String aliasName = "my_alias_" + RandomStringUtils.randomNumeric(7);
        try {
            navigateToRuntimeServer();
            page.selectCredentialStore(CREDENTIAL_STORE_NAME);
            page.getAliases()
                    .addResource(AliasWizard.class)
                    .alias(aliasName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Created alias should be present in table", page.getAliases().isResourcePresent(aliasName));
            ModelNodeResult aliases = readAliases(CREDENTIAL_STORE_ADDRESS);
            aliases.assertSuccess();
            Assert.assertTrue("Created alias should be present in model", aliases.stringListValue().contains(aliasName));
        } finally {
            operations.invoke(REMOVE_ALIAS_OPERATION, CREDENTIAL_STORE_ADDRESS, Values.of(ALIAS, aliasName));
            reloadAdministrationIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to add alias in Web Console to credential store instance filling required and optional fields
     * Validate added alias is present in aliases table in the UI
     * Validate added alias is present in model
     */
    @Test
    public void testAddAliasWithOptionalFields() throws IOException, TimeoutException, InterruptedException {
        final String aliasName = "my_alias_" + RandomStringUtils.randomNumeric(7);
        final String aliasSecretValue = RandomStringUtils.randomAlphanumeric(7);
        try {
            navigateToRuntimeServer();
            page.selectCredentialStore(CREDENTIAL_STORE_NAME);
            page.getAliases()
                    .addResource(AliasWizard.class)
                    .alias(aliasName)
                    .secretValue(aliasSecretValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Created alias should be present in table", page.getAliases().isResourcePresent(aliasName));
            ModelNodeResult aliases = readAliases(CREDENTIAL_STORE_ADDRESS);
            aliases.assertSuccess();
            Assert.assertTrue("Created alias should be present in model", aliases.stringListValue().contains(aliasName));
        } finally {
            operations.invoke(REMOVE_ALIAS_OPERATION, CREDENTIAL_STORE_ADDRESS, Values.of(ALIAS, aliasName));
            reloadAdministrationIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to add alias in Web Console without filling required fields.
     * Validate an error is shown in the Web Console
     */
    @Test
    public void testAddAliasWithoutRequiredFieldsShowsError() {
        navigateToRuntimeServer();
        page.selectCredentialStore(CREDENTIAL_STORE_NAME);
        page.getAliases()
                .addResource(AliasWizard.class)
                .saveWithState()
                .assertWindowOpen();
        Assert.assertTrue("An validation error regarding alias name should be visible",
                page.getWindowFragment().isErrorShownInForm());
    }

    /**
     * @tpTestDetails Add alias in model and try to remove it in Web Console
     * Validate removed alias is not present in the aliases table anymore
     * Validate removed alias is not present in the model
     */
    @Test
    public void testRemoveAlias() throws IOException, TimeoutException, InterruptedException {
        final String aliasName = "my_alias_" + RandomStringUtils.randomNumeric(7);
        final Address addAliasAddress = getCredentialStoreAddressForAddingAlias(CREDENTIAL_STORE_NAME);
        try {
            operations.invoke(ADD_ALIAS_OPERATION, addAliasAddress, Values.of(ALIAS, aliasName));
            navigateToRuntimeServer();
            page.selectCredentialStore(CREDENTIAL_STORE_NAME);
            page.getAliases()
                    .removeResource(aliasName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Removed alias should not be present in table anymore", page.getAliases().isResourcePresent(aliasName));
            ModelNodeResult aliases = readAliases(CREDENTIAL_STORE_ADDRESS);
            aliases.assertSuccess();
            Assert.assertFalse("Removed alias should not be present in model anymore", aliases.stringListValue().contains(aliasName));
        } finally {
            operations.invoke(REMOVE_ALIAS_OPERATION, CREDENTIAL_STORE_ADDRESS, Values.of(ALIAS, aliasName));
            reloadAdministrationIfRequired();
        }
    }

    /**
     * @tpTestDetails Navigate to credential store page, add a credential store in the model
     * and try to see newly added credential store in credential stores table in the Web Console
     * Validate added credential store is present in credential stores table
     */
    @Test
    public void testRefreshCredentialStores() throws IOException, TimeoutException, InterruptedException, OperationException {
        final String credentialStoreName = "created_credential_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address credentialStoreAddress = elytronOperations.getElytronAddress(ElytronOperations.CREDENTIAL_STORE, credentialStoreName);
        try {
            navigateToRuntimeServer();
            ResourceManager credentialStoresManager = page.getResourceManager();
            Assert.assertFalse("Credential store should not be visible in the table as it has not been created yet"
                    , credentialStoresManager.isResourcePresent(credentialStoreName));
            elytronOperations.createCredentialStoreWithCredentialReferenceClearText(credentialStoreAddress);
            reloadServer();
            page.refreshCredentialStores();
            Assert.assertTrue("Credential store should pre present in the table", credentialStoresManager.isResourcePresent(credentialStoreName));
        } finally {
            operations.removeIfExists(credentialStoreAddress);
            reloadAdministrationIfRequired();
        }
    }

    /**
     * @tpTestDetails Create parent credential store in the model with an alias,
     * create child credential store depending on parent credential store and its alias.
     * Try to change secret value of alias in the Web Console, and child credential store should not be running
     * Validate child credential store is not running after secret value change in the UI and model.
     * Validate child credential store is running after resetting secret value to previous one in the UI and model
     */
    @Test
    public void testSetSecret() throws Exception {
        final String aliasName = "alias_" + RandomStringUtils.randomNumeric(7);
        final String aliasSecret = RandomStringUtils.randomAlphanumeric(7);
        final Values aliasValues = Values.of(ALIAS, aliasName)
                .and(SECRET_VALUE, aliasSecret);
        final Address addAliasAddress = getCredentialStoreAddressForAddingAlias(PARENT_CREDENTIAL_STORE_NAME);
        operations.invoke(ADD_ALIAS_OPERATION, addAliasAddress, aliasValues).assertSuccess();
        operations.add(CHILD_CREDENTIAL_STORE_ADDRESS, Values.of(ElytronOperations.CREATE, true)
                .and(ElytronOperations.CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(ElytronOperations.STORE, PARENT_CREDENTIAL_STORE_NAME)
                        .addProperty(ALIAS, aliasName).build())).assertSuccess();
        navigateToRuntimeServer();
        page.selectCredentialStore(PARENT_CREDENTIAL_STORE_NAME);
        page.selectAlias(aliasName);
        page.getAliases().addResource(AliasWizard.class, SET_SECRET_LABEL)
                .secretValue(RandomStringUtils.randomAlphanumeric(10))
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowClosed();
        reloadServer();
        page.refreshCredentialStores();
        Assert.assertEquals("Depending credential store should have \"" + STATE_START_FAILED + "\" state in the UI after alias secret change"
                , STATE_START_FAILED, page.getResourceManager().selectByName(CHILD_CREDENTIAL_STORE_NAME).getCellValue(1));
        verifyCredentialStoreState(CHILD_CREDENTIAL_STORE_NAME, STATE_START_FAILED);
        page.selectCredentialStore(PARENT_CREDENTIAL_STORE_NAME);
        page.selectAlias(aliasName);
        page.getAliases()
                .addResource(AliasWizard.class, SET_SECRET_LABEL)
                .secretValue(aliasSecret)
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowClosed();
        reloadServer();
        page.refreshCredentialStores();
        Assert.assertEquals("Depending credential store should have \"" + STATE_UP + "\" state in the UI after reverting alias secret change"
                , STATE_UP, page.getResourceManager().selectByName(CHILD_CREDENTIAL_STORE_NAME).getCellValue(1));
        verifyCredentialStoreState(CHILD_CREDENTIAL_STORE_NAME, STATE_UP);
    }

    private ModelNodeResult readAliases(Address credentialStoreAddress) throws IOException {
        if (ConfigUtils.isDomain()) {
            return operations.invoke(READ_ALIASES_OPERATION, credentialStoreAddress)
                    .forServer(ConfigUtils.getDefaultHost(), TEST_SERVER);
        }
        return operations.invoke(READ_ALIASES_OPERATION, credentialStoreAddress);
    }

    private Address getCredentialStoreAddressForAddingAlias(String credentialStoreName) {
        if (ConfigUtils.isDomain()) {
            return Address.host(ConfigUtils.getDefaultHost())
                    .and(Constants.SERVER, TEST_SERVER)
                    .and(Constants.SUBSYSTEM, "elytron")
                    .and(ElytronOperations.CREDENTIAL_STORE, credentialStoreName);
        }
        return elytronOperations.getElytronAddress(ElytronOperations.CREDENTIAL_STORE, credentialStoreName);
    }

    private void verifyCredentialStoreState(String credentialStoreName, String expectedState) throws Exception {
        Address credentialStoreAddress;
        if (ConfigUtils.isDomain()) {
            credentialStoreAddress = Address.host(ConfigUtils.getDefaultHost())
                    .and(Constants.SERVER, TEST_SERVER)
                    .and(Constants.SUBSYSTEM, "elytron")
                    .and(ElytronOperations.CREDENTIAL_STORE, credentialStoreName);
        } else {
            credentialStoreAddress = elytronOperations.getElytronAddress(ElytronOperations.CREDENTIAL_STORE, credentialStoreName);
        }
        new ResourceVerifier(credentialStoreAddress, client).verifyExists().verifyAttribute(STATE, expectedState);
    }

    private static void setUpTestProfile() throws IOException {
        operations.add(TEST_PROFILE_ADDRESS);
        operations.add(TEST_PROFILE_ADDRESS.and(Constants.SUBSYSTEM, "elytron"));
    }

    private static void setUpTestServerGroup() throws IOException, CommandFailedException {
        final String socketBindingGroup = client.options().isDomain ? "full-sockets" : "standard-sockets";
        operations.add(TEST_SERVER_GROUP_ADDRESS, Values.of(Constants.PROFILE, TEST_PROFILE)
                .and(Constants.SOCKET_BINDING_GROUP, socketBindingGroup));
    }

    private static void setUpTestServer() throws IOException, TimeoutException, InterruptedException {
        operations.add(TEST_SERVER_ADDRESS, Values.of(Constants.GROUP, TEST_SERVER_GROUP)).assertSuccess();
        domainAdministration.startServer(TEST_SERVER);
        domainAdministration.waitUntilServersRunning(ConfigUtils.getDefaultHost(), Collections.singletonList(TEST_SERVER));
    }

    private static void setUpCredentialStores() throws IOException {
        CREDENTIAL_STORE_ADDRESS = elytronOperations.createCredentialStoreWithCredentialReferenceClearText(CREDENTIAL_STORE_NAME);
        PARENT_CREDENTIAL_STORE_ADDRESS = elytronOperations.createCredentialStoreWithCredentialReferenceClearText(PARENT_CREDENTIAL_STORE_NAME);
        CHILD_CREDENTIAL_STORE_ADDRESS = elytronOperations.getElytronAddress(ElytronOperations.CREDENTIAL_STORE, CHILD_CREDENTIAL_STORE_NAME);
    }

    private void navigateToRuntimeServer() {
        if (ConfigUtils.isDomain()) {
            page.navigateToServer(TEST_SERVER);
        } else {
            page.navigate();
        }
    }

    private static void reloadAdministrationIfRequired() throws InterruptedException, TimeoutException, IOException {
        if (ConfigUtils.isDomain()) {
            domainAdministration.reloadIfRequired();
        } else {
            administration.reloadIfRequired();
        }
    }

    private static void reloadServer() throws IOException, InterruptedException, TimeoutException {
        if (ConfigUtils.isDomain()) {
            operations.invoke("reload-servers", TEST_SERVER_GROUP_ADDRESS).assertSuccess();
            domainAdministration.waitUntilServersRunning(ConfigUtils.getDefaultHost(), Collections.singletonList(TEST_SERVER));
        } else {
            administration.reload();
        }
    }

}
