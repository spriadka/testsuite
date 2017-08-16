package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
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
        KEY_MANAGER_LABEL = "Key Manager",
        KEY_MANAGER = "key-manager",
        TRUST_MANAGER_LABEL = "Trust Manager",
        TRUST_MANAGER = "trust-manager",
        ALGORITHM = "algorithm",
        CREDENTIAL_REFERENCE_CLEAR_TEXT_IDENTIFIER = "credential-reference-clear-text",
        ALIAS_FILTER = "alias-filter",
        PROVIDERS = "providers",
        CREDENTIAL_REFERENCE_LABEL = "Credential Reference",
        SERVER_SSL_CONTEXT = "server-ssl-context",
        SERVER_SSL_CONTEXT_LABEL = "Server SSL Context",
        CLIENT_SSL_CONTEXT = "client-ssl-context",
        CLIENT_SSL_CONTEXT_LABEL = "Client SSL Context",
        TLS_V11 = "TLSv1.1",
        TLS_V12 = "TLSv1.2",
        PROTOCOLS = "protocols",
        NEED_CLIENT_AUTH = "need-client-auth",
        CERTIFICATE_REVOCATION_LIST = "certificate-revocation-list",
        CERTIFICATE_REVOCATION_LIST_LABEL = "Certificate Revocation List",
        PATH = "path",
        RELATIVE_TO = "relative-to",
        MAXIMUM_CERT_PATH = "maximum-cert-path";

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
        Address keyStoreAddress = createKeyStore();
        String keyManagerName = randomAlphanumeric(5), password = randomAlphanumeric(5),
                keyStoreName = keyStoreAddress.getLastPairValue();
        Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        ModelNode expectedCredentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        page.navigateToApplication().selectResource(KEY_MANAGER_LABEL);

        try {
            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            wizard.maximizeWindow();
            Editor editor = wizard.getEditor();
            editor.text(NAME, keyManagerName);
            wizard.openOptionalFieldsTab();
            editor.text(ALGORITHM, ALGORITH_VALUE_1);
            editor.text(KEY_STORE, keyStoreName);
            editor.text(CREDENTIAL_REFERENCE_CLEAR_TEXT_IDENTIFIER, password);

            assertTrue("Dialog should be closed!", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(keyManagerName));
            new ResourceVerifier(keyManagerAddress, client).verifyExists()
                .verifyAttribute(ALGORITHM, ALGORITH_VALUE_1)
                .verifyAttribute(KEY_STORE, keyStoreName)
                .verifyAttribute(CREDENTIAL_REFERENCE, expectedCredentialReferenceNode);
        } finally {
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Key Manager instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Key Manager table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeKeyManagerTest() throws Exception {
        String keyManagerName = randomAlphanumeric(5), password = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build();
        ResourceVerifier keyManagerVerifier = new ResourceVerifier(keyManagerAddress, client);

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1).and(KEY_STORE, keyStoreAddress.getLastPairValue())
                    .and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            keyManagerVerifier.verifyExists();

            page.navigateToApplication().selectResource(KEY_MANAGER_LABEL).getResourceManager()
                    .removeResource(keyManagerName).confirmAndDismissReloadRequiredMessage().assertClosed();
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
        String keyManagerName = randomAlphanumeric(5), password = randomAlphanumeric(5),
                newAliasFilter = randomAlphanumeric(5), newProviderName = randomAlphanumeric(5),
                newProviders = randomAlphanumeric(5);
        Address originalKeyStoreAddress = createKeyStore(),
                newKeyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build();

        try {
            elyOps.addProviderLoader(newProviders);
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1)
                    .and(KEY_STORE, originalKeyStoreAddress.getLastPairValue())
                    .and(CREDENTIAL_REFERENCE, credentialReferenceNode)).assertSuccess();

            page.navigateToApplication().selectResource(KEY_MANAGER_LABEL).getResourceManager()
                    .selectByName(keyManagerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, keyManagerAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, ALGORITHM, ALGORITH_VALUE_2)
                    .edit(TEXT, ALIAS_FILTER, newAliasFilter)
                    .edit(TEXT, KEY_STORE, newKeyStoreAddress.getLastPairValue())
                    .andSave().verifyFormSaved()
                    .verifyAttribute(ALGORITHM, ALGORITH_VALUE_2)
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
        String keyManagerName = randomAlphanumeric(5), password = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        ModelNode originalCredentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue())
                    .and(CREDENTIAL_REFERENCE, originalCredentialReferenceNode)).assertSuccess();

            page.navigateToApplication().selectResource(KEY_MANAGER_LABEL).getResourceManager()
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

    /**
     * @tpTestDetails Try to create Elytron Server SSL Context instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Server SSL Context table.
     * Validate created resource is in model.
     * Validate attribute values of created resource in model.
     */
    @Test
    public void addServerSSLContextTest() throws Exception {
        String serverSSLContextName = randomAlphanumeric(5), keyManagerName = randomAlphanumeric(5),
                password = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName),
                serverSSLContextAddress = elyOps.getElytronAddress(SERVER_SSL_CONTEXT, serverSSLContextName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build();

        page.navigateToApplication().selectResource(SERVER_SSL_CONTEXT_LABEL);

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            WizardWindow wizard = page.getResourceManager().addResource();
            wizard.maximizeWindow();
            Editor editor = wizard.getEditor();
            editor.text(NAME, serverSSLContextName);
            editor.text(KEY_MANAGER, keyManagerName);

            assertTrue("Dialog should be closed!", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(serverSSLContextName));
            new ResourceVerifier(serverSSLContextAddress, client).verifyExists()
                .verifyAttribute(KEY_MANAGER, keyManagerName);
        } finally {
            ops.removeIfExists(serverSSLContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Server SSL Context instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Server SSL Context table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeServerSSLContextTest() throws Exception {
        String serverSSLContextName = randomAlphanumeric(5), keyManagerName = randomAlphanumeric(5),
                password = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName),
                serverSSLContextAddress = elyOps.getElytronAddress(SERVER_SSL_CONTEXT, serverSSLContextName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build(),
                protocolList = new ModelNodeListBuilder(new ModelNode(TLS_V11)).addNode(new ModelNode(TLS_V12))
                        .build();
        ResourceVerifier serverSSLContextVerifier = new ResourceVerifier(serverSSLContextAddress, client);

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            ops.add(serverSSLContextAddress, Values.of(KEY_MANAGER, keyManagerName).and(PROTOCOLS, protocolList));
            serverSSLContextVerifier.verifyExists();

            page.navigateToApplication().selectResource(SERVER_SSL_CONTEXT_LABEL).getResourceManager()
                    .removeResource(serverSSLContextName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(serverSSLContextName));
            serverSSLContextVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(serverSSLContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Server SSL Context instance in model
     * and try to edit it's attributes in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editServerSSLContextAttributesTest() throws Exception {
        String serverSSLContextName = randomAlphanumeric(5), keyManagerName = randomAlphanumeric(5),
                password = randomAlphanumeric(5), trustManagerName = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName),
                serverSSLContextAddress = elyOps.getElytronAddress(SERVER_SSL_CONTEXT, serverSSLContextName),
                trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build(),
                protocolList = new ModelNodeListBuilder(new ModelNode(TLS_V11)).addNode(new ModelNode(TLS_V12))
                        .build(),
                newProtocolList = new ModelNodeListBuilder(new ModelNode(TLS_V12)).build();

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(serverSSLContextAddress, Values.of(KEY_MANAGER, keyManagerName).and(PROTOCOLS, protocolList))
                    .assertSuccess();
            ops.add(trustManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue())).assertSuccess();

            page.navigateToApplication().selectResource(SERVER_SSL_CONTEXT_LABEL).getResourceManager()
                    .selectByName(serverSSLContextName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, serverSSLContextAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, PROTOCOLS, TLS_V12)
                    .edit(TEXT, TRUST_MANAGER, trustManagerName)
                    .edit(CHECKBOX, NEED_CLIENT_AUTH, true)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(PROTOCOLS, newProtocolList)
                    .verifyAttribute(TRUST_MANAGER, trustManagerName)
                    .verifyAttribute(NEED_CLIENT_AUTH, true);
        } finally {
            ops.removeIfExists(serverSSLContextAddress);
            ops.removeIfExists(trustManagerAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Client SSL Context instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Client SSL Context table.
     * Validate created resource is in model.
     * Validate attribute values of created resource in model.
     */
    @Test
    public void addClientSSLContextTest() throws Exception {
        String clientSSLContextName = randomAlphanumeric(5), keyManagerName = randomAlphanumeric(5),
                password = randomAlphanumeric(5), protocolValues = TLS_V11 + "\n" + TLS_V12;
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName),
                clientSSLContextAddress = elyOps.getElytronAddress(CLIENT_SSL_CONTEXT, clientSSLContextName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build(),
                expectedProtocolList = new ModelNodeListBuilder(new ModelNode(TLS_V11)).addNode(new ModelNode(TLS_V12))
                        .build();

        page.navigateToApplication().selectResource(CLIENT_SSL_CONTEXT_LABEL);

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            WizardWindow wizard = page.getResourceManager().addResource();
            wizard.maximizeWindow();
            Editor editor = wizard.getEditor();
            editor.text(NAME, clientSSLContextName);
            editor.text(KEY_MANAGER, keyManagerName);
            editor.text(PROTOCOLS, protocolValues);

            assertTrue("Dialog should be closed!", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(clientSSLContextName));
            new ResourceVerifier(clientSSLContextAddress, client).verifyExists()
                .verifyAttribute(KEY_MANAGER, keyManagerName)
                .verifyAttribute(PROTOCOLS, expectedProtocolList);
        } finally {
            ops.removeIfExists(clientSSLContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Client SSL Context instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Client SSL Context table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeClientSSLContextTest() throws Exception {
        String clientSSLContextName = randomAlphanumeric(5), keyManagerName = randomAlphanumeric(5),
                password = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName),
                clientSSLContextAddress = elyOps.getElytronAddress(CLIENT_SSL_CONTEXT, clientSSLContextName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build(),
                protocolList = new ModelNodeListBuilder(new ModelNode(TLS_V11)).addNode(new ModelNode(TLS_V12))
                        .build();
        ResourceVerifier clientSSLContextVerifier = new ResourceVerifier(clientSSLContextAddress, client);

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            ops.add(clientSSLContextAddress, Values.of(KEY_MANAGER, keyManagerName).and(PROTOCOLS, protocolList));
            clientSSLContextVerifier.verifyExists();

            page.navigateToApplication().selectResource(CLIENT_SSL_CONTEXT_LABEL).getResourceManager()
                    .removeResource(clientSSLContextName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(clientSSLContextName));
            clientSSLContextVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(clientSSLContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Client SSL Context instance in model
     * and try to edit it's attributes in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editClientSSLContextAttributesTest() throws Exception {
        String clientSSLContextName = randomAlphanumeric(5), keyManagerName = randomAlphanumeric(5),
                password = randomAlphanumeric(5), trustManagerName = randomAlphanumeric(5);
        Address keyStoreAddress = createKeyStore(),
                keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName),
                clientSSLContextAddress = elyOps.getElytronAddress(CLIENT_SSL_CONTEXT, clientSSLContextName),
                trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password).build(),
                protocolList = new ModelNodeListBuilder(new ModelNode(TLS_V11)).addNode(new ModelNode(TLS_V12))
                        .build(),
                newProtocolList = new ModelNodeListBuilder(new ModelNode(TLS_V12)).build();

        try {
            ops.add(keyManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(clientSSLContextAddress, Values.of(KEY_MANAGER, keyManagerName).and(PROTOCOLS, protocolList))
                    .assertSuccess();
            ops.add(trustManagerAddress, Values.of(ALGORITHM, ALGORITH_VALUE_1)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue())).assertSuccess();

            page.navigateToApplication().selectResource(CLIENT_SSL_CONTEXT_LABEL).getResourceManager()
                    .selectByName(clientSSLContextName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, clientSSLContextAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, PROTOCOLS, TLS_V12)
                    .edit(TEXT, TRUST_MANAGER, trustManagerName)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(PROTOCOLS, newProtocolList)
                    .verifyAttribute(TRUST_MANAGER, trustManagerName);
        } finally {
            ops.removeIfExists(clientSSLContextAddress);
            ops.removeIfExists(trustManagerAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.remove(keyStoreAddress);
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
