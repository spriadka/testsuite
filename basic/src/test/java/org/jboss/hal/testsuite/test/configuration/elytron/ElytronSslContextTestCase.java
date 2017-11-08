package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@Category(Elytron.class)
public class ElytronSslContextTestCase extends AbstractElytronTestCase {

    private static final String
        KEY_STORE = "key-store",
        CLEAR_TEXT = "clear-text",
        TYPE = "type",
        JKS = "jks",
        CREDENTIAL_REFERENCE = "credential-reference",
        ALGORITH_VALUE_1 = "PKIX",
            ALGORITH_VALUE_2 = "SunX509",
        TRUST_MANAGER_LABEL = "Trust Manager",
        TRUST_MANAGER = "trust-manager",
        ALGORITHM = "algorithm",
        ALIAS_FILTER = "alias-filter",
        PROVIDERS = "providers",
        CERTIFICATE_REVOCATION_LIST = "certificate-revocation-list",
        CERTIFICATE_REVOCATION_LIST_LABEL = "Certificate Revocation List",
        PATH = "path",
        RELATIVE_TO = "relative-to",
        MAXIMUM_CERT_PATH = "maximum-cert-path";

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
        Address keyStoreAddress = createKeyStore();
        String trustManagerName = randomAlphanumeric(5), keyStoreName = keyStoreAddress.getLastPairValue();
        Address trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);

        page.navigateToApplication().selectResource(TRUST_MANAGER_LABEL);

        try {
            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            wizard.maximizeWindow();
            Editor editor = wizard.getEditor();
            editor.text(NAME, trustManagerName);
            wizard.openOptionalFieldsTab();
            editor.text(ALGORITHM, ALGORITH_VALUE_1);
            editor.text(KEY_STORE, keyStoreName);

            assertTrue("Dialog should be closed!", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(trustManagerName));
            new ResourceVerifier(trustManagerAddress, client).verifyExists()
                .verifyAttribute(ALGORITHM, ALGORITH_VALUE_1)
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
        String trustManagerName = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        ResourceVerifier trustManagerVerifier = new ResourceVerifier(trustManagerAddress, client);

        try {
            ops.add(trustManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue()));
            trustManagerVerifier.verifyExists();

            page.navigateToApplication().selectResource(TRUST_MANAGER_LABEL).getResourceManager()
                    .removeResource(trustManagerName).confirmAndDismissReloadRequiredMessage().assertClosed();
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
        String trustManagerName = randomAlphanumeric(5), newAliasFilter = randomAlphanumeric(5),
                newProviderName = randomAlphanumeric(5), newProviders = randomAlphanumeric(5);
        Address originalKeyStoreAddress = createKeyStore(),
                newKeyStoreAddress = createKeyStore(),
                trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);

        try {
            elyOps.addProviderLoader(newProviders);
            ops.add(trustManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1)
                    .and(KEY_STORE, originalKeyStoreAddress.getLastPairValue())).assertSuccess();

            page.navigateToApplication().selectResource(TRUST_MANAGER_LABEL).getResourceManager()
                    .selectByName(trustManagerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, trustManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, ALGORITHM, ALGORITH_VALUE_2)
                    .edit(TEXT, ALIAS_FILTER, newAliasFilter)
                    .edit(TEXT, KEY_STORE, newKeyStoreAddress.getLastPairValue())
                    .andSave().verifyFormSaved()
                    .verifyAttribute(ALGORITHM, ALGORITH_VALUE_2)
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
        String
            trustManagerName = randomAlphanumeric(5),
            pathValue = randomAlphanumeric(5),
            newPathValue = randomAlphanumeric(5),
            relativeToValue = randomAlphanumeric(5);
        long maximumCertPathValue = 123L;
        Address
            keyStoreAddress = createKeyStore(),
            trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        ModelNode
            expectedFirstCertificateRevocationListNode = new ModelNodePropertiesBuilder()
                .addProperty(PATH, pathValue)
                .addProperty(RELATIVE_TO, relativeToValue)
                .build(),
            expectedSecondCertificateRevocationListNode = new ModelNodePropertiesBuilder()
                .addProperty(PATH, newPathValue)
                .addProperty(RELATIVE_TO, relativeToValue)
                .addProperty(MAXIMUM_CERT_PATH, new ModelNode(maximumCertPathValue))
                .build();

        try {
            ops.add(trustManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue())).assertSuccess();

            page.navigateToApplication().selectResource(TRUST_MANAGER_LABEL).getResourceManager()
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

    private Address createKeyStore() throws Exception {
        String keyStoreName = randomAlphanumeric(5), password = randomAlphanumeric(5);
        Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build();
        ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode)).assertSuccess();
        return keyStoreAddress;
    }
}
