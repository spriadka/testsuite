package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ElytronStoreTestCase extends AbstractElytronTestCase {

    private static final String KEY_STORE = "key-store", KEY_STORE_LABEL = "Key Store", TYPE = "type", JKS = "jks",
            CREDENTIAL_REFERENCE = "credential-reference", CLEAR_TEXT = "clear-text",
            CREDENTIAL_REFERENCE_CLEAR_TEXT_IDENTIFIER = "credential-reference-clear-text",
            CREDENTIAL_REFERENCE_LABEL = "Credential Reference", ALIAS_FILTER = "alias-filter", ALIAS = "alias",
            STORE = "store", CREDENTIAL_STORE = "credential-store", CREDENTIAL_STORE_LABEL = "Credential Store",
            URI = "uri", RELATIVE_TO = "relative-to", FILTERING_KEY_STORE = "filtering-key-store",
            FILTERING_KEY_STORE_LABEL = "Filtering Key Store", LDAP_KEY_STORE = "ldap-key-store",
            LDAP_KEY_STORE_LABEL = "Ldap Key Store", DIR_CONTEXT = "dir-context", SEARCH_PATH = "search-path",
            SEARCH_RECURSIVE = "search-recursive", SEARCH_TIME_LIMIT = "search-time-limit",
            CERTIFICATE_TYPE = "certificate-type";

    @Page
    private SSLPage page;

    @Test
    public void addKeyStoreTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode expectedCredentialReferenceNode = new ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password).build();

        page.navigateToApplication().selectResource(KEY_STORE_LABEL);

        try {
            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            wizard.openOptionalFieldsTab();
            Editor editor = wizard.getEditor();
            editor.text(NAME, keyStoreName);
            editor.text(TYPE, JKS);
            editor.text(CREDENTIAL_REFERENCE_CLEAR_TEXT_IDENTIFIER, password);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(keyStoreName));
            new ResourceVerifier(keyStoreAddress, client).verifyExists().verifyAttribute(TYPE, JKS)
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
        final ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        final ResourceVerifier keyStoreVerifier = new ResourceVerifier(keyStoreAddress, client);

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            keyStoreVerifier.verifyExists();

            page.navigateToApplication().selectResource(KEY_STORE_LABEL).getResourceManager()
                    .removeResource(keyStoreName).confirmAndDismissReloadRequiredMessage().assertClosed();
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
        final ModelNode initialCredentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
                    .assertSuccess();

            page.navigateToApplication().selectResource(KEY_STORE_LABEL).getResourceManager()
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
                initialPassword = RandomStringUtils.randomAlphanumeric(5),
                newPassword = RandomStringUtils.randomAlphanumeric(5),
                aliasValue = RandomStringUtils.randomAlphanumeric(5),
                storeValue = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode initialCredentialReferenceNode = new ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, initialPassword).build();

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
                    .assertSuccess();

            page.navigateToApplication().selectResource(KEY_STORE_LABEL).getResourceManager()
                    .selectByName(keyStoreName);
            page.switchToConfigAreaTab(CREDENTIAL_REFERENCE_LABEL);

            new ConfigChecker.Builder(client, keyStoreAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, CLEAR_TEXT, "").edit(TEXT, STORE, storeValue).edit(TEXT, ALIAS, aliasValue).andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CREDENTIAL_REFERENCE, new ModelNodePropertiesBuilder()
                            .addUndefinedProperty(CLEAR_TEXT).addProperty(STORE, storeValue)
                            .addProperty(ALIAS, aliasValue).addUndefinedProperty(TYPE).build());

            new ConfigChecker.Builder(client, keyStoreAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, CLEAR_TEXT, newPassword).edit(TEXT, STORE, "").edit(TEXT, ALIAS, "").andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CREDENTIAL_REFERENCE, new ModelNodePropertiesBuilder()
                            .addProperty(CLEAR_TEXT, newPassword).addUndefinedProperty(STORE)
                            .addUndefinedProperty(ALIAS).addUndefinedProperty(TYPE).build());

        } finally {
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addCredentialStoreTest() throws Exception {
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5),
                uriValue = "cr-store://test/" + RandomStringUtils.randomAlphanumeric(5) + ".jceks?create.storage=true",
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode expectedCredentialReferenceNode = new ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password).build();

        page.navigateToApplication().selectResource(CREDENTIAL_STORE_LABEL);

        try {
            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            wizard.openOptionalFieldsTab();
            Editor editor = wizard.getEditor();
            editor.text(NAME, credentialStoreName);
            editor.text(URI, uriValue);
            editor.text(CREDENTIAL_REFERENCE_CLEAR_TEXT_IDENTIFIER, password);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(credentialStoreName));
            new ResourceVerifier(credentialStoreAddress, client).verifyExists().verifyAttribute(URI, uriValue)
                    .verifyAttribute(CREDENTIAL_REFERENCE, expectedCredentialReferenceNode);

        } finally {
            ops.removeIfExists(credentialStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeCredentialStoreTest() throws Exception {
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5),
                uriValue = "cr-store://test/" + RandomStringUtils.randomAlphanumeric(5) + ".jceks?create.storage=true",
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        final ResourceVerifier credentialStoreVerifier = new ResourceVerifier(credentialStoreAddress, client);

        try {
            ops.add(credentialStoreAddress, Values.of(URI, uriValue).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            credentialStoreVerifier.verifyExists();

            page.navigateToApplication().selectResource(CREDENTIAL_STORE_LABEL).getResourceManager()
                    .removeResource(credentialStoreName).confirmAndDismissReloadRequiredMessage().assertClosed();
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
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5),
                uriValue = "cr-store://test/" + RandomStringUtils.randomAlphanumeric(5) + ".jceks?create.storage=true",
                password = RandomStringUtils.randomAlphanumeric(5),
                jbossHomeDir = "jboss.home.dir",
                providerNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode initialCredentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(credentialStoreAddress, Values.of(URI, uriValue).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
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
        final String credentialStoreName = RandomStringUtils.randomAlphanumeric(5),
                uriValue = "cr-store://test/" + RandomStringUtils.randomAlphanumeric(5) + ".jceks?create.storage=true",
                initialPassword = RandomStringUtils.randomAlphanumeric(5),
                newPassword = RandomStringUtils.randomAlphanumeric(5),
                aliasValue = RandomStringUtils.randomAlphanumeric(5),
                storeValue = RandomStringUtils.randomAlphanumeric(5);
        final Address credentialStoreAddress = elyOps.getElytronAddress(CREDENTIAL_STORE, credentialStoreName);
        final ModelNode initialCredentialReferenceNode = new ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, initialPassword).build();

        try {
            ops.add(credentialStoreAddress, Values.of(URI, uriValue).and(CREDENTIAL_REFERENCE, initialCredentialReferenceNode))
                    .assertSuccess();

            page.navigateToApplication().selectResource(CREDENTIAL_STORE_LABEL).getResourceManager()
                    .selectByName(credentialStoreName);
            page.switchToConfigAreaTab(CREDENTIAL_REFERENCE_LABEL);

            new ConfigChecker.Builder(client, credentialStoreAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, CLEAR_TEXT, "").edit(TEXT, STORE, storeValue).edit(TEXT, ALIAS, aliasValue).andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CREDENTIAL_REFERENCE, new ModelNodePropertiesBuilder()
                            .addUndefinedProperty(CLEAR_TEXT).addProperty(STORE, storeValue)
                            .addProperty(ALIAS, aliasValue).addUndefinedProperty(TYPE).build());

            new ConfigChecker.Builder(client, credentialStoreAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, CLEAR_TEXT, newPassword).edit(TEXT, STORE, "").edit(TEXT, ALIAS, "").andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CREDENTIAL_REFERENCE, new ModelNodePropertiesBuilder()
                            .addProperty(CLEAR_TEXT, newPassword).addUndefinedProperty(STORE)
                            .addUndefinedProperty(ALIAS).addUndefinedProperty(TYPE).build());

        } finally {
            ops.removeIfExists(credentialStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFilteringKeyStoreTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                filteringKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                aliasFilterValue = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName),
                filteringKeyStoreAddress = elyOps.getElytronAddress(FILTERING_KEY_STORE, filteringKeyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        page.navigateToApplication().selectResource(FILTERING_KEY_STORE_LABEL);

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, filteringKeyStoreName);
            editor.text(ALIAS_FILTER, aliasFilterValue);
            editor.text(KEY_STORE, keyStoreName);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(filteringKeyStoreName));
            new ResourceVerifier(filteringKeyStoreAddress, client).verifyExists()
                    .verifyAttribute(ALIAS_FILTER, aliasFilterValue)
                    .verifyAttribute(KEY_STORE, keyStoreName);

        } finally {
            ops.removeIfExists(filteringKeyStoreAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeFilteringKeyStoreTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                filteringKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                aliasFilterValue = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName),
                filteringKeyStoreAddress = elyOps.getElytronAddress(FILTERING_KEY_STORE, filteringKeyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        final ResourceVerifier filteringKeyStoreVerifier = new ResourceVerifier(filteringKeyStoreAddress, client);

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(filteringKeyStoreAddress, Values.of(ALIAS_FILTER, aliasFilterValue).and(KEY_STORE, keyStoreName))
                    .assertSuccess();
            filteringKeyStoreVerifier.verifyExists();

            page.navigateToApplication().selectResource(FILTERING_KEY_STORE_LABEL).getResourceManager()
                    .removeResource(filteringKeyStoreName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(filteringKeyStoreName));
            filteringKeyStoreVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(filteringKeyStoreAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editFilteringKeyStoreAttributesTest() throws Exception {
        final String originalKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                newKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                filteringKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5),
                originalAliasFilterValue = RandomStringUtils.randomAlphanumeric(5),
                newAliasFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final Address originalKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, originalKeyStoreName),
                newKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, newKeyStoreName),
                filteringKeyStoreAddress = elyOps.getElytronAddress(FILTERING_KEY_STORE, filteringKeyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(originalKeyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(newKeyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(filteringKeyStoreAddress, Values.of(ALIAS_FILTER, originalAliasFilterValue)
                    .and(KEY_STORE, originalKeyStoreName)).assertSuccess();

            page.navigateToApplication().selectResource(FILTERING_KEY_STORE_LABEL).getResourceManager()
                    .selectByName(filteringKeyStoreName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, filteringKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, ALIAS_FILTER, newAliasFilterValue).verifyFormSaved()
                    .verifyAttribute(ALIAS_FILTER, newAliasFilterValue);

            new ConfigChecker.Builder(client, filteringKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, KEY_STORE, newKeyStoreName).verifyFormSaved()
                    .verifyAttribute(KEY_STORE, newKeyStoreName);

        } finally {
            ops.removeIfExists(filteringKeyStoreAddress);
            ops.removeIfExists(originalKeyStoreAddress);
            ops.removeIfExists(newKeyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addLDAPKeyStoreTest() throws Exception {
        final DirContext dirContext = createDirContext();
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                searchPathValue = "dc=" + RandomStringUtils.randomAlphanumeric(5),
                searchTimeAlphabetic = RandomStringUtils.randomAlphabetic(5),
                searchTimeNumeric = "1" + RandomStringUtils.randomNumeric(4);
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);

        page.navigateToApplication().selectResource(LDAP_KEY_STORE_LABEL);

        try {
            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.text(NAME, ldapKeyStoreName);
            editor.text(DIR_CONTEXT, dirContext.name);
            editor.text(SEARCH_PATH, searchPathValue);
            wizard.openOptionalFieldsTab();
            editor.checkbox(SEARCH_RECURSIVE, true);
            editor.text(SEARCH_TIME_LIMIT, searchTimeAlphabetic);
            boolean closed = wizard.finish();

            assertFalse("Dialog should not be closed! Non-numeric " + SEARCH_TIME_LIMIT
                    + " should not pass client side validation!", closed);

            editor.text(SEARCH_TIME_LIMIT, searchTimeNumeric);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(ldapKeyStoreName));
            new ResourceVerifier(ldapKeyStoreAddress, client).verifyExists()
                    .verifyAttribute(DIR_CONTEXT, dirContext.name)
                    .verifyAttribute(SEARCH_PATH, searchPathValue)
                    .verifyAttribute(SEARCH_RECURSIVE, true)
                    .verifyAttribute(SEARCH_TIME_LIMIT, Integer.valueOf(searchTimeNumeric));

        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(dirContext.address);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeLDAPKeyStoreTest() throws Exception {
        final DirContext dirContext = createDirContext();
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                searchPathValue = "dc=" + RandomStringUtils.randomAlphanumeric(5);
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);
        ResourceVerifier ldapKeyStoreVerifier = new ResourceVerifier(ldapKeyStoreAddress, client);

        try {
            ops.add(ldapKeyStoreAddress, Values.of(DIR_CONTEXT, dirContext.name).and(SEARCH_PATH, searchPathValue))
                    .assertSuccess();
            ldapKeyStoreVerifier.verifyExists();

            page.navigateToApplication().selectResource(LDAP_KEY_STORE_LABEL).getResourceManager()
                    .removeResource(ldapKeyStoreName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(ldapKeyStoreName));
            ldapKeyStoreVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(dirContext.address);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editLDAPKeyStoreAttributesTest() throws Exception {
        final DirContext originalDirContext = createDirContext(), newDirContext = createDirContext();
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                searchPathValue = "dc=" + RandomStringUtils.randomAlphanumeric(5),
                certificateTypeValue = RandomStringUtils.randomAlphanumeric(5);
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);

        try {
            ops.add(ldapKeyStoreAddress, Values.of(DIR_CONTEXT, originalDirContext.name)
                    .and(SEARCH_PATH, searchPathValue)).assertSuccess();

            page.navigateToApplication().selectResource(LDAP_KEY_STORE_LABEL).getResourceManager()
                    .selectByName(ldapKeyStoreName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, ldapKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, DIR_CONTEXT, newDirContext.name).verifyFormSaved()
                    .verifyAttribute(DIR_CONTEXT, newDirContext.name);

            new ConfigChecker.Builder(client, ldapKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, CERTIFICATE_TYPE, certificateTypeValue).verifyFormSaved()
                    .verifyAttribute(CERTIFICATE_TYPE, certificateTypeValue);

        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(originalDirContext.address);
            ops.removeIfExists(newDirContext.address);
            adminOps.reloadIfRequired();
        }
    }

    // TODO add test for new-item-template attribute of ldap-key-store as soon as it is added

    private DirContext createDirContext() throws IOException {
        final String dirContextName = RandomStringUtils.randomAlphanumeric(5),
                urlValue = "ldap://127.0.0.1:" + RandomStringUtils.randomNumeric(5),
                principalValue = "uid=admin,ou=" + RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        ops.add(dirContextAddress, Values.of("url", urlValue).and("principal", principalValue)
                .and(CREDENTIAL_REFERENCE, credentialReferenceNode)).assertSuccess();
        return new DirContext(dirContextName, dirContextAddress);
    }

    private static class DirContext {
        private String name;
        private Address address;
        private DirContext(String dirContextName, Address dirContextAddress) {
            this.name = dirContextName;
            this.address = dirContextAddress;
        }
    }

}
