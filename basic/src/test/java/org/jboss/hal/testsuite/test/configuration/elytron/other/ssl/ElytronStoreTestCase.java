package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@Category(Elytron.class)
public class ElytronStoreTestCase extends AbstractElytronTestCase {

    private static final String
            CREDENTIAL_REFERENCE = "credential-reference",
            CLEAR_TEXT = "clear-text",
            LDAP_KEY_STORE = "ldap-key-store",
            LDAP_KEY_STORE_LABEL = "Ldap Key Store",
            DIR_CONTEXT = "dir-context",
            SEARCH_PATH = "search-path",
            SEARCH_RECURSIVE = "search-recursive",
            SEARCH_TIME_LIMIT = "search-time-limit",
            CERTIFICATE_TYPE = "certificate-type",
            VALUE = "value",
            NEW_ITEM_TEMPLATE = "new-item-template",
            NEW_ITEM_TEMPLATE_LABEL = "New Item Template",
            NEW_ITEM_PATH = "new-item-path",
            NEW_ITEM_RDN = "new-item-rdn",
            NEW_ITEM_ATTRIBUTES = "new-item-attributes";

    @Page
    private SSLPage page;

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

    /**
     * @tpTestDetails Create Elytron LDAP key store instance in model
     * and try to edit it's new-item-template attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */

    @Test
    public void editLDAPKeyStoreNewItemTemplateTest() throws Exception {
        final DirContext dirContext = createDirContext();
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5),
                searchPathValue = "dc=" + RandomStringUtils.randomAlphanumeric(5),
                newItemPathValue = RandomStringUtils.randomAlphanumeric(5),
                newItemRdnValue = RandomStringUtils.randomAlphanumeric(5),
                newItemKey1 = RandomStringUtils.randomAlphanumeric(5),
                newItemValue11 = RandomStringUtils.randomAlphanumeric(5),
                newItemValue12 = RandomStringUtils.randomAlphanumeric(5),
                newItemKey2 = RandomStringUtils.randomAlphanumeric(5),
                newItemValue21 = RandomStringUtils.randomAlphanumeric(5),
                newItemValue22 = RandomStringUtils.randomAlphanumeric(5),
                newItemValue23 = RandomStringUtils.randomAlphanumeric(5),
                newItemAttributesString =
                    newItemKey1 + "=" + newItemValue11 + "," + newItemValue12 + "\n" +
                    newItemKey2 + "=" + newItemValue21 + "," + newItemValue22 + "," + newItemValue23,
                newItemAttributesStringEdited =
                    newItemKey2 + "=" + newItemValue23 + "," + newItemValue12 + "," + newItemValue21;
        final ModelNode
                expectedNewItemTemplateNode = new ModelNodePropertiesBuilder()
                        .addProperty(NEW_ITEM_PATH, newItemPathValue)
                        .addProperty(NEW_ITEM_RDN, newItemRdnValue)
                        .addProperty(NEW_ITEM_ATTRIBUTES, new ModelNodeListBuilder()
                                .addNode(new ModelNodePropertiesBuilder()
                                        .addProperty(NAME, newItemKey1)
                                        .addProperty(VALUE, new ModelNodeListBuilder()
                                                .addNode(new ModelNode(newItemValue11))
                                                .addNode(new ModelNode(newItemValue12))
                                                .build())
                                        .build())
                                .addNode(new ModelNodePropertiesBuilder()
                                        .addProperty(NAME, newItemKey2)
                                        .addProperty(VALUE, new ModelNodeListBuilder()
                                                .addNode(new ModelNode(newItemValue21))
                                                .addNode(new ModelNode(newItemValue22))
                                                .addNode(new ModelNode(newItemValue23))
                                                .build())
                                        .build())
                                .build())
                        .build(),
                expectedNewItemTemplateNodeEdited = new ModelNodePropertiesBuilder()
                        .addProperty(NEW_ITEM_PATH, newItemPathValue)
                        .addProperty(NEW_ITEM_RDN, newItemRdnValue)
                        .addProperty(NEW_ITEM_ATTRIBUTES, new ModelNodeListBuilder()
                                .addNode(new ModelNodePropertiesBuilder()
                                        .addProperty(NAME, newItemKey2)
                                        .addProperty(VALUE, new ModelNodeListBuilder()
                                                .addNode(new ModelNode(newItemValue23))
                                                .addNode(new ModelNode(newItemValue12))
                                                .addNode(new ModelNode(newItemValue21))
                                                .build())
                                        .build())
                                .build())
                        .build();
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);

        try {
            ops.add(ldapKeyStoreAddress, Values.of(DIR_CONTEXT, dirContext.name)
                    .and(SEARCH_PATH, searchPathValue)).assertSuccess();

            page.navigateToApplication().selectResource(LDAP_KEY_STORE_LABEL).getResourceManager()
                    .selectByName(ldapKeyStoreName);
            page.switchToConfigAreaTab(NEW_ITEM_TEMPLATE_LABEL);

            new ConfigChecker.Builder(client, ldapKeyStoreAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, NEW_ITEM_ATTRIBUTES, newItemAttributesString)
                    .edit(TEXT, NEW_ITEM_PATH, newItemPathValue)
                    .edit(TEXT, NEW_ITEM_RDN, newItemRdnValue)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(NEW_ITEM_TEMPLATE, expectedNewItemTemplateNode);

            new ConfigChecker.Builder(client, ldapKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, NEW_ITEM_ATTRIBUTES, newItemAttributesStringEdited)
                    .verifyFormSaved()
                    .verifyAttribute(NEW_ITEM_TEMPLATE, expectedNewItemTemplateNodeEdited);

        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(dirContext.address);
            adminOps.reloadIfRequired();
        }
    }

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
