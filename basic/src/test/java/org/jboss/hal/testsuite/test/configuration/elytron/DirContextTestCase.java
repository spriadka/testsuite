package org.jboss.hal.testsuite.test.configuration.elytron;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.elytron.DirContextPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class DirContextTestCase extends AbstractElytronTestCase {

    private static final String
        DIR_CONTEXT = "dir-context",
        DIR_CONTEXT_LABEL = "Dir Context",
        URL = "url",
        PRINCIPAL = "principal",
        CONNECTION_TIMEOUT = "connection-timeout",
        ENABLE_CONNECTION_POOLING = "enable-connection-pooling",
        CREDENTIAL_REFERENCE_LABEL = "Credential Reference",
        CREDENTIAL_REFERENCE = "credential-reference",
        CLEAR_TEXT = "clear-text",
        CREDENTIAL_REFERENCE_CLEAR_TEXT = "credential-reference-clear-text",
        LDAP_URL_BEGINNING = "ldap://127.0.0.1:3",
        PRINCIPAL_BEGINNING = "uid=admin,ou=";

    @Page
    private DirContextPage page;

    /**
     * @tpTestDetails Try to create Elytron Dir Context instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Dir Context table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addDirContextTest() throws Exception {
        String
            dirContextName = randomAlphanumeric(5),
            urlValue = LDAP_URL_BEGINNING + randomNumeric(3),
            principalValue = PRINCIPAL_BEGINNING + randomAlphanumeric(5),
            credentialStoreClearTextValue = RandomStringUtils.randomAlphanumeric(7);
        Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);

        page.navigateToApplication().selectResource(DIR_CONTEXT_LABEL);

        try {
            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.text(NAME, dirContextName);
            editor.text(URL, urlValue);
            wizard.openOptionalFieldsTab();
            editor.text(CREDENTIAL_REFERENCE_CLEAR_TEXT, credentialStoreClearTextValue);
            editor.text(PRINCIPAL, principalValue);

            wizard.saveWithState().assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(dirContextName));
            new ResourceVerifier(dirContextAddress, client).verifyExists()
                    .verifyAttribute(URL, urlValue)
                    .verifyAttribute(PRINCIPAL, principalValue)
                    .verifyAttribute(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(CLEAR_TEXT, credentialStoreClearTextValue)
                        .build());
        } finally {
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Dir Context instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Dir Context table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeDirContextTest() throws Exception {
        String
            dirContextName = randomAlphanumeric(5),
            urlValue = LDAP_URL_BEGINNING + randomNumeric(3);
        Address dirContextAddress =
                elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        ResourceVerifier dirContextVerifier = new ResourceVerifier(dirContextAddress, client);

        try {
            ops.add(dirContextAddress, Values.of(URL, urlValue)).assertSuccess();
            dirContextVerifier.verifyExists();

            page.navigateToApplication().selectResource(DIR_CONTEXT_LABEL).getResourceManager()
                    .removeResource(dirContextName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(dirContextName));
            dirContextVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Dir Context instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editDirContextAttributesTest() throws Exception {
        String
            dirContextName = randomAlphanumeric(5),
            originalUrlValue = LDAP_URL_BEGINNING + randomNumeric(3),
            newUrlValue = LDAP_URL_BEGINNING + randomNumeric(3),
            originalPrincipalValue = PRINCIPAL_BEGINNING + randomAlphanumeric(5),
            newPrincipalValue = PRINCIPAL_BEGINNING + randomAlphanumeric(5),
            connectionTimeoutValue = "1" + randomNumeric(3);
        Address dirContextAddress =
                elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);

        try {
            ops.add(dirContextAddress,
                    Values.of(URL, originalUrlValue).and(PRINCIPAL, originalPrincipalValue)).assertSuccess();

            page.navigateToApplication().selectResource(DIR_CONTEXT_LABEL).getResourceManager()
                    .selectByName(dirContextName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, dirContextAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, URL, newUrlValue)
                .edit(TEXT, PRINCIPAL, newPrincipalValue)
                .edit(TEXT, CONNECTION_TIMEOUT, connectionTimeoutValue)
                .edit(CHECKBOX, ENABLE_CONNECTION_POOLING, true)
                .andSave().verifyFormSaved()
                .verifyAttribute(URL, newUrlValue)
                .verifyAttribute(PRINCIPAL, newPrincipalValue)
                .verifyAttribute(CONNECTION_TIMEOUT, Integer.valueOf(connectionTimeoutValue))
                .verifyAttribute(ENABLE_CONNECTION_POOLING, true);
        } finally {
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Dir Context instance in model
     * and try to edit it's credential reference in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     * Test setting <ul>
     * <li>store + alias</li>
     * <li>clear text</li>
     * <li>illegal combination of both</li></ul>
     */
    @Test
    public void editDirContextCredentialReferenceTest() throws Exception {
        String
            dirContextName = randomAlphanumeric(5),
            urlValue = LDAP_URL_BEGINNING + randomNumeric(3);
        Address dirContextAddress =
                elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);

        try {
            ops.add(dirContextAddress,
                    Values.of(URL, urlValue)).assertSuccess();

            page.navigateToApplication().selectResource(DIR_CONTEXT_LABEL).getResourceManager()
                    .selectByName(dirContextName);
            page.switchToConfigAreaTab(CREDENTIAL_REFERENCE_LABEL);

            ElytronIntegrationChecker credentialReferenceChecker = new ElytronIntegrationChecker.Builder(client)
                    .address(dirContextAddress).configFragment(page.getConfigFragment()).build();
            credentialReferenceChecker.setCredentialStoreCredentialReferenceAndVerify();
            credentialReferenceChecker.setClearTextCredentialReferenceAndVerify();
            credentialReferenceChecker.testIllegalCombinationCredentialReferenceAttributes();
        } finally {
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

}
