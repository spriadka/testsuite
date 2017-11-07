package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddKeyStoreWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class KeyStoreTestCase extends AbstractElytronTestCase {

    private static final String KEY_STORE = "key-store";
    private static final String KEY_STORE_LABEL = "Key Store";
    private static final String TYPE = "type";
    private static final String JKS = "jks";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String ALIAS_FILTER = "alias-filter";
    private static final String CREDENTIAL_REFERENCE_LABEL = "Credential Reference";

    @Page
    private SSLPage page;

    @Test
    public void addKeyStoreTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode expectedCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password).build();

        try {
            page.navigateToApplication().selectResource(KEY_STORE_LABEL);
            page.getResourceManager()
                    .addResource(AddKeyStoreWizard.class)
                    .name(keyStoreName)
                    .type(JKS)
                    .credentialReferenceClearText(password)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(keyStoreName));
            new ResourceVerifier(keyStoreAddress, client).verifyExists()
                    .verifyAttribute(TYPE, JKS)
                    .verifyAttribute(CREDENTIAL_REFERENCE, expectedCredentialReferenceNode);

        } finally {
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeKeyStoreTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        final ResourceVerifier keyStoreVerifier = new ResourceVerifier(keyStoreAddress, client);

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            keyStoreVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(KEY_STORE_LABEL)
                    .getResourceManager()
                    .removeResource(keyStoreName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(keyStoreName));
            keyStoreVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editKeyStoreAttributesTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5),
                aliasFilterValue = RandomStringUtils.randomAlphanumeric(5),
                providerNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode initialCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
                    .assertSuccess();

            page.navigateToApplication()
                    .selectResource(KEY_STORE_LABEL)
                    .getResourceManager()
                    .selectByName(keyStoreName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, keyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, ALIAS_FILTER, aliasFilterValue).verifyFormSaved()
                    .verifyAttribute(ALIAS_FILTER, aliasFilterValue);
            new ConfigChecker.Builder(client, keyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PROVIDER_NAME, providerNameValue).verifyFormSaved()
                    .verifyAttribute(PROVIDER_NAME, providerNameValue);
        } finally {
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editKeyStoreCredentialReferenceTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                initialPassword = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode initialCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, initialPassword).build();

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
                    .assertSuccess();

            page.navigateToApplication().selectResource(KEY_STORE_LABEL).getResourceManager()
                    .selectByName(keyStoreName);
            page.switchToConfigAreaTab(CREDENTIAL_REFERENCE_LABEL);

            ElytronIntegrationChecker credentialReferenceChecker = new ElytronIntegrationChecker.Builder(client)
                    .address(keyStoreAddress).configFragment(page.getConfigFragment()).build();
            credentialReferenceChecker.setCredentialStoreCredentialReferenceAndVerify();
            credentialReferenceChecker.testIllegalCombinationCredentialReferenceAttributes();
            credentialReferenceChecker.setClearTextCredentialReferenceAndVerify();

        } finally {
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }
}
