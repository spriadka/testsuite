package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddTrustManagerWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class TrustManagerTestCase extends AbstractElytronTestCase {

    private static final String KEY_STORE = "key-store";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String TYPE = "type";
    private static final String JKS = "jks";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String PKIX = "PKIX";
    private static final String SUN_X509 = "SunX509";
    private static final String TRUST_MANAGER_LABEL = "Trust Manager";
    private static final String TRUST_MANAGER = "trust-manager";
    private static final String ALGORITHM = "algorithm";
    private static final String ALIAS_FILTER = "alias-filter";
    private static final String PROVIDERS = "providers";
    private static final String CERTIFICATE_REVOCATION_LIST = "certificate-revocation-list";
    private static final String CERTIFICATE_REVOCATION_LIST_LABEL = "Certificate Revocation List";
    private static final String PATH = "path";
    private static final String RELATIVE_TO = "relative-to";
    private static final String MAXIMUM_CERT_PATH = "maximum-cert-path";

    @Page
    private SSLPage page;

    /**
     * @tpTestDetails Try to create Elytron Trust Manager instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Trust Manager table.
     * Validate created resource is in model.
     * Validate attribute values of created resource in model.
     */
    @Test
    public void addTrustManagerTest() throws Exception {
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String trustManagerName = randomAlphanumeric(5);
        Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        Address trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        try {
            createKeyStore(keyStoreAddress);
            page.navigateToApplication()
                    .selectResource(TRUST_MANAGER_LABEL)
                    .getResourceManager()
                    .addResource(AddTrustManagerWizard.class)
                    .name(trustManagerName)
                    .algorithm(PKIX)
                    .keyStore(keyStoreName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(trustManagerName));
            new ResourceVerifier(trustManagerAddress, client).verifyExists()
                .verifyAttribute(ALGORITHM, PKIX)
                .verifyAttribute(KEY_STORE, keyStoreName);
        } finally {
            ops.removeIfExists(trustManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Trust Manager instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Trust Manager table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeTrustManagerTest() throws Exception {
        final String keyStoreName = "key_store_" + randomAlphanumeric(7);
        final String trustManagerName = "trust_manager_" + randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        final ResourceVerifier trustManagerVerifier = new ResourceVerifier(trustManagerAddress, client);
        try {
            createKeyStore(keyStoreAddress);
            ops.add(trustManagerAddress, Values.of(ALGORITHM, PKIX)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue()));
            trustManagerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(TRUST_MANAGER_LABEL)
                    .getResourceManager()
                    .removeResource(trustManagerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(trustManagerName));
            trustManagerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(trustManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Trust Manager instance in model
     * and try to edit it's attributes in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editTrustManagerAttributesTest() throws Exception {
        final String initialKeyStore = "initial_key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String trustManagerName = "trust_manager_" + randomAlphanumeric(5);
        final String newAliasFilter = randomAlphanumeric(5);
        final String newProviderName = randomAlphanumeric(5);
        final String newProviders = randomAlphanumeric(5);
        final Address originalKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, initialKeyStore);
        final Address newKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        try {
            createKeyStore(originalKeyStoreAddress);
            createKeyStore(newKeyStoreAddress);
            elyOps.addProviderLoader(newProviders);
            ops.add(trustManagerAddress, Values.of(ALGORITHM, PKIX)
                    .and(KEY_STORE, originalKeyStoreAddress.getLastPairValue())).assertSuccess();

            page.navigateToApplication()
                    .selectResource(TRUST_MANAGER_LABEL)
                    .getResourceManager()
                    .selectByName(trustManagerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, trustManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, ALGORITHM, SUN_X509)
                    .edit(TEXT, ALIAS_FILTER, newAliasFilter)
                    .edit(TEXT, KEY_STORE, newKeyStoreAddress.getLastPairValue())
                    .andSave().verifyFormSaved()
                    .verifyAttribute(ALGORITHM, SUN_X509)
                    .verifyAttribute(ALIAS_FILTER, newAliasFilter)
                    .verifyAttribute(KEY_STORE, newKeyStoreAddress.getLastPairValue());
            new ConfigChecker.Builder(client, trustManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, PROVIDER_NAME, newProviderName)
                    .edit(TEXT, PROVIDERS, newProviders)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(PROVIDER_NAME, newProviderName)
                    .verifyAttribute(PROVIDERS, newProviders);
        } finally {
            ops.removeIfExists(trustManagerAddress);
            ops.removeIfExists(originalKeyStoreAddress);
            ops.removeIfExists(newKeyStoreAddress);
            elyOps.removeProviderLoader(newProviders);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Trust Manager instance in model
     * and try to edit it's certificate-revocation-list attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editTrustManagerCertificateRevocationListTest() throws Exception {
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String trustManagerName = "trust_manager" + randomAlphanumeric(5);
        final String pathValue = randomAlphanumeric(5);
        final String newPathValue = randomAlphanumeric(5);
        final String relativeToValue = randomAlphanumeric(5);
        long maximumCertPathValue = 123L;
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        final ModelNode expectedFirstCertificateRevocationListNode = new ModelNodePropertiesBuilder()
                .addProperty(PATH, pathValue)
                .addProperty(RELATIVE_TO, relativeToValue)
                .build();
        final ModelNode expectedSecondCertificateRevocationListNode = new ModelNodePropertiesBuilder()
                .addProperty(PATH, newPathValue)
                .addProperty(RELATIVE_TO, relativeToValue)
                .addProperty(MAXIMUM_CERT_PATH, new ModelNode(maximumCertPathValue))
                .build();

        try {
            createKeyStore(keyStoreAddress);
            ops.add(trustManagerAddress, Values.of(ALGORITHM, PKIX)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue())).assertSuccess();
            page.navigateToApplication()
                    .selectResource(TRUST_MANAGER_LABEL)
                    .getResourceManager()
                    .selectByName(trustManagerName);
            page.switchToConfigAreaTab(CERTIFICATE_REVOCATION_LIST_LABEL);
            new ConfigChecker.Builder(client, trustManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, PATH, pathValue)
                    .edit(TEXT, RELATIVE_TO, relativeToValue)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(CERTIFICATE_REVOCATION_LIST, expectedFirstCertificateRevocationListNode);
            new ConfigChecker.Builder(client, trustManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, PATH, newPathValue)
                    .edit(TEXT, MAXIMUM_CERT_PATH, maximumCertPathValue)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(CERTIFICATE_REVOCATION_LIST, expectedSecondCertificateRevocationListNode);
        } finally {
            ops.removeIfExists(trustManagerAddress);
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
}
