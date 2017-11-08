package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddKeyManagerWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class KeyManagerTestCase extends AbstractElytronTestCase {

    private static final String KEY_MANAGER_LABEL = "Key Manager";
    private static final String KEY_MANAGER = "key-manager";
    private static final String KEY_STORE = "key-store";
    private static final String CREDENTIAL_REFERENCE_LABEL = "Credential Reference";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String ALGORITHM = "algorithm";
    private static final String PKIX = "PKIX";
    private static final String TYPE = "type";
    private static final String JKS = "jks";
    private static final String ALIAS_FILTER = "alias-filter";
    private static final String PROVIDERS = "providers";
    private static final String SUN_X509 = "SunX509";


    @Page
    private SSLPage page;

    /**
     * @tpTestDetails Try to create Elytron Key Manager instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Key Manager table.
     * Validate created resource is in model.
     * Validate attribute values of created resource in model.
     */
    @Test
    public void addKeyManagerTest() throws Exception {
        final String keyManagerName = "key_manager_" + randomAlphanumeric(5);
        final String password = randomAlphanumeric(5);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        final ModelNode expectedCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password)
                .build();
        try {
            createKeyStore(keyStoreAddress);
            page.navigateToApplication()
                    .selectResource(KEY_MANAGER_LABEL)
                    .getResourceManager()
                    .addResource(AddKeyManagerWizard.class)
                    .name(keyManagerName)
                    .algorithm(PKIX)
                    .keyStore(keyStoreName)
                    .credentialReferenceClearText(password)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(keyManagerName));
            new ResourceVerifier(keyManagerAddress, client).verifyExists()
                    .verifyAttribute(ALGORITHM, PKIX)
                    .verifyAttribute(KEY_STORE, keyStoreName)
                    .verifyAttribute(CREDENTIAL_REFERENCE, expectedCredentialReferenceNode);
        } finally {
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createKeyStore(Address keyStoreAddress) throws IOException {
        final String password = RandomStringUtils.randomAlphanumeric(7);
        ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password).build()))
                .assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Key Manager instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Key Manager table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeKeyManagerTest() throws Exception {
        final String keyStoreName = "key_store_" + randomAlphanumeric(5);
        final String keyManagerName = "key_manager_" + randomAlphanumeric(5);
        final String password = randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build();
        final ResourceVerifier keyManagerVerifier = new ResourceVerifier(keyManagerAddress, client);

        try {
            createKeyStore(keyStoreAddress);
            ops.add(keyManagerAddress, Values.of(ALGORITHM, PKIX).and(KEY_STORE, keyStoreName)
                    .and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            keyManagerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(KEY_MANAGER_LABEL)
                    .getResourceManager()
                    .removeResource(keyManagerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(keyManagerName));
            keyManagerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Key Manager instance in model
     * and try to edit it's attributes in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editKeyManagerAttributesTest() throws Exception {
        final String initialKeyStoreName = "initial_key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyManagerName = "key_manager_" + randomAlphanumeric(5);
        final String password = randomAlphanumeric(5);
        final String newAliasFilter = randomAlphanumeric(5);
        final String newProviderName = randomAlphanumeric(5);
        final String newProviders = randomAlphanumeric(5);
        final Address originalKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, initialKeyStoreName);
        final Address newKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build();
        try {
            createKeyStore(originalKeyStoreAddress);
            createKeyStore(newKeyStoreAddress);
            elyOps.addProviderLoader(newProviders);
            ops.add(keyManagerAddress, Values.of(ALGORITHM, PKIX)
                    .and(KEY_STORE, originalKeyStoreAddress.getLastPairValue())
                    .and(CREDENTIAL_REFERENCE, credentialReferenceNode)).assertSuccess();
            page.navigateToApplication().selectResource(KEY_MANAGER_LABEL).getResourceManager()
                    .selectByName(keyManagerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, keyManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, ALGORITHM, SUN_X509)
                    .edit(TEXT, ALIAS_FILTER, newAliasFilter)
                    .edit(TEXT, KEY_STORE, newKeyStoreAddress.getLastPairValue())
                    .andSave().verifyFormSaved()
                    .verifyAttribute(ALGORITHM, SUN_X509)
                    .verifyAttribute(ALIAS_FILTER, newAliasFilter)
                    .verifyAttribute(KEY_STORE, newKeyStoreAddress.getLastPairValue());
            new ConfigChecker.Builder(client, keyManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, PROVIDER_NAME, newProviderName)
                    .edit(TEXT, PROVIDERS, newProviders)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(PROVIDER_NAME, newProviderName)
                    .verifyAttribute(PROVIDERS, newProviders);
        } finally {
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(originalKeyStoreAddress);
            ops.removeIfExists(newKeyStoreAddress);
            elyOps.removeProviderLoader(newProviders);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Key Manager instance in model
     * and try to edit it's credential reference in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     * Test setting <ul>
     * <li>store + alias</li>
     * <li>clear text</li>
     * <li>illegal combination of both</li></ul>
     */
    @Test
    public void editKeyManagerCredentialReferenceTest() throws Exception {
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyManagerName = "key_manager_" + randomAlphanumeric(5);
        final String password = randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        ModelNode originalCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        try {
            createKeyStore(keyStoreAddress);
            ops.add(keyManagerAddress, Values.of(ALGORITHM, PKIX)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue())
                    .and(CREDENTIAL_REFERENCE, originalCredentialReferenceNode)).assertSuccess();

            page.navigateToApplication()
                    .selectResource(KEY_MANAGER_LABEL)
                    .getResourceManager()
                    .selectByName(keyManagerName);
            page.switchToConfigAreaTab(CREDENTIAL_REFERENCE_LABEL);
            ElytronIntegrationChecker credentialReferenceChecker = new ElytronIntegrationChecker.Builder(client)
                    .address(keyManagerAddress).configFragment(page.getConfigFragment()).build();
            credentialReferenceChecker.setCredentialStoreCredentialReferenceAndVerify();
            credentialReferenceChecker.setClearTextCredentialReferenceAndVerify();
            credentialReferenceChecker.testIllegalCombinationCredentialReferenceAttributes();
        } finally {
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }
}
