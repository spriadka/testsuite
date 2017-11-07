package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddCredentialStoreWizard;
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
public class CredentialStoreTestCase extends AbstractElytronTestCase {

    private static final String CREDENTIAL_REFERENCE_LABEL = "Credential Reference";
    private static final String CREDENTIAL_STORE = "credential-store";
    private static final String CREDENTIAL_STORE_LABEL = "Credential Store";
    private static final String CREATE = "create";
    private static final String RELATIVE_TO = "relative-to";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String MODIFIABLE = "modifiable";

    @Page
    private SSLPage page;

    @Test
    public void addCredentialStoreTest() throws Exception {
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode expectedCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password).build();

        try {
            page.navigateToApplication()
                    .selectResource(CREDENTIAL_STORE_LABEL)
                    .getResourceManager()
                    .addResource(AddCredentialStoreWizard.class)
                    .name(credentialStoreName)
                    .create(true)
                    .modifiable(true)
                    .credentialReferenceClearText(password)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(credentialStoreName));
            new ResourceVerifier(credentialStoreAddress, client).verifyExists()
                    .verifyAttribute(CREATE, true)
                    .verifyAttribute(MODIFIABLE, true)
                    .verifyAttribute(CREDENTIAL_REFERENCE, expectedCredentialReferenceNode);

        } finally {
            ops.removeIfExists(credentialStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeCredentialStoreTest() throws Exception {
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        final ResourceVerifier credentialStoreVerifier = new ResourceVerifier(credentialStoreAddress, client);
        try {
            ops.add(credentialStoreAddress, Values.of(CREATE, true).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            credentialStoreVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(CREDENTIAL_STORE_LABEL)
                    .getResourceManager()
                    .removeResource(credentialStoreName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(credentialStoreName));
            credentialStoreVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(credentialStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editCredentialStoreAttributesTest() throws Exception {
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final String jbossHomeDir = "jboss.home.dir";
        final String providerNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode initialCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(credentialStoreAddress, Values.of(CREATE, true).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
                    .assertSuccess();

            page.navigateToApplication().selectResource(CREDENTIAL_STORE_LABEL).getResourceManager()
                    .selectByName(credentialStoreName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, credentialStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, RELATIVE_TO, jbossHomeDir).verifyFormSaved()
                    .verifyAttribute(RELATIVE_TO, jbossHomeDir);

            new ConfigChecker.Builder(client, credentialStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PROVIDER_NAME, providerNameValue).verifyFormSaved()
                    .verifyAttribute(PROVIDER_NAME, providerNameValue);

        } finally {
            ops.removeIfExists(credentialStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editCredentialStoreCredentialReferenceTest() throws Exception {
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String initialPassword = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode initialCredentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, initialPassword).build();

        try {
            ops.add(credentialStoreAddress, Values.of(CREATE, true).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
                    .assertSuccess();

            page.navigateToApplication().selectResource(CREDENTIAL_STORE_LABEL).getResourceManager()
                    .selectByName(credentialStoreName);
            page.switchToConfigAreaTab(CREDENTIAL_REFERENCE_LABEL);

            ElytronIntegrationChecker credentialReferenceChecker = new ElytronIntegrationChecker.Builder(client)
                    .address(credentialStoreAddress).configFragment(page.getConfigFragment()).build();
            credentialReferenceChecker.setCredentialStoreCredentialReferenceAndVerify();
            credentialReferenceChecker.testIllegalCombinationCredentialReferenceAttributes();
            credentialReferenceChecker.setClearTextCredentialReferenceAndVerify();

        } finally {
            ops.removeIfExists(credentialStoreAddress);
            adminOps.reloadIfRequired();
        }
    }
}
