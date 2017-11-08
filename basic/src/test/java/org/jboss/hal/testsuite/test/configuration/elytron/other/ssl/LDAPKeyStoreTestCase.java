package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddLDAPKeyStoreWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class LDAPKeyStoreTestCase extends AbstractElytronTestCase {

    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String LDAP_KEY_STORE = "ldap-key-store";
    private static final String LDAP_KEY_STORE_LABEL = "Ldap Key Store";
    private static final String DIR_CONTEXT = "dir-context";
    private static final String SEARCH_PATH = "search-path";
    private static final String SEARCH_RECURSIVE = "search-recursive";
    private static final String SEARCH_TIME_LIMIT = "search-time-limit";
    private static final String CERTIFICATE_TYPE = "certificate-type";
    private static final String NEW_ITEM_TEMPLATE = "new-item-template";
    private static final String NEW_ITEM_TEMPLATE_LABEL = "New Item Template";
    private static final String NEW_ITEM_PATH = "new-item-path";
    private static final String NEW_ITEM_RDN = "new-item-rdn";
    private static final String NEW_ITEM_ATTRIBUTES = "new-item-attributes";

    @Page
    private SSLPage page;

    @Test
    public void addLDAPKeyStoreTest() throws Exception {
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String searchPathValue = "dc=" + RandomStringUtils.randomAlphanumeric(5);
        final String searchTimeNumeric = "1" + RandomStringUtils.randomNumeric(4);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);

        try {
            createDirContext(dirContextAddress);
            page.navigateToApplication()
                    .selectResource(LDAP_KEY_STORE_LABEL)
                    .getResourceManager()
                    .addResource(AddLDAPKeyStoreWizard.class)
                    .name(ldapKeyStoreName)
                    .dirContext(dirContextName)
                    .searchPath(searchPathValue)
                    .searchRecursive(true)
                    .searchTimeLimit(searchTimeNumeric)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(ldapKeyStoreName));
            new ResourceVerifier(ldapKeyStoreAddress, client).verifyExists()
                    .verifyAttribute(DIR_CONTEXT, dirContextName)
                    .verifyAttribute(SEARCH_PATH, searchPathValue)
                    .verifyAttribute(SEARCH_RECURSIVE, true)
                    .verifyAttribute(SEARCH_TIME_LIMIT, Integer.valueOf(searchTimeNumeric));
        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeLDAPKeyStoreTest() throws Exception {
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String searchPathValue = "dc=" + RandomStringUtils.randomAlphanumeric(5);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);
        ResourceVerifier ldapKeyStoreVerifier = new ResourceVerifier(ldapKeyStoreAddress, client);
        try {
            createDirContext(dirContextAddress);
            ops.add(ldapKeyStoreAddress, Values.of(DIR_CONTEXT, dirContextName).and(SEARCH_PATH, searchPathValue))
                    .assertSuccess();
            ldapKeyStoreVerifier.verifyExists();
            page.navigateToApplication().selectResource(LDAP_KEY_STORE_LABEL).getResourceManager()
                    .removeResource(ldapKeyStoreName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(ldapKeyStoreName));
            ldapKeyStoreVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editAttributesTest() throws Exception {
        final String oldDirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String newDirContextName = "dir_context_new_" + RandomStringUtils.randomAlphanumeric(7);
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String searchPathValue = "dc=" + RandomStringUtils.randomAlphanumeric(5);
        final String certificateTypeValue = RandomStringUtils.randomAlphanumeric(5);
        final Address oldDirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, oldDirContextName);
        final Address newDirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, newDirContextName);
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);

        try {
            createDirContext(oldDirContextAddress);
            createDirContext(newDirContextAddress);
            ops.add(ldapKeyStoreAddress, Values.of(DIR_CONTEXT, oldDirContextName)
                    .and(SEARCH_PATH, searchPathValue)).assertSuccess();

            page.navigateToApplication().selectResource(LDAP_KEY_STORE_LABEL).getResourceManager()
                    .selectByName(ldapKeyStoreName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, ldapKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, DIR_CONTEXT, newDirContextName)
                    .verifyFormSaved()
                    .verifyAttribute(DIR_CONTEXT, newDirContextName);
            new ConfigChecker.Builder(client, ldapKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, CERTIFICATE_TYPE, certificateTypeValue).verifyFormSaved()
                    .verifyAttribute(CERTIFICATE_TYPE, certificateTypeValue);

        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(oldDirContextAddress);
            ops.removeIfExists(newDirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP key store instance in model
     * and try to edit it's new-item-template addAttribute in Web Console's Elytron subsystem configuration.
     * Validate edited addAttribute value in the model.
     */

    @Test
    public void editNewItemTemplateTest() throws Exception {
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String ldapKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String searchPath = "dc=" + RandomStringUtils.randomAlphanumeric(5);
        final String path = RandomStringUtils.randomAlphanumeric(5);
        final String rdn = RandomStringUtils.randomAlphanumeric(5);
        final NewItemAttribute[] newItemAttributes = {
                new NewItemAttribute(RandomStringUtils.randomAlphanumeric(7),
                        Arrays.asList(RandomStringUtils.randomAlphanumeric(7),
                                RandomStringUtils.randomAlphanumeric(7))),
                new NewItemAttribute(RandomStringUtils.randomAlphanumeric(7),
                        Arrays.asList(RandomStringUtils.randomAlphanumeric(7),
                                RandomStringUtils.randomAlphanumeric(7),
                                RandomStringUtils.randomAlphanumeric(7)))
        };
        final NewItem expectedNewItem = new NewItem.Builder()
                .path(path)
                .rdn(rdn)
                .addAttributes(Arrays.asList(newItemAttributes))
                .build();
        final String newItemAttributesString = Arrays.stream(newItemAttributes)
                .map(NewItemAttribute::toString)
                .collect(Collectors.joining("\n"));
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address ldapKeyStoreAddress = elyOps.getElytronAddress(LDAP_KEY_STORE, ldapKeyStoreName);

        try {
            createDirContext(dirContextAddress);
            ops.add(ldapKeyStoreAddress, Values.of(DIR_CONTEXT, dirContextName)
                    .and(SEARCH_PATH, searchPath))
                    .assertSuccess();
            page.navigateToApplication()
                    .selectResource(LDAP_KEY_STORE_LABEL)
                    .getResourceManager()
                    .selectByName(ldapKeyStoreName);
            page.switchToConfigAreaTab(NEW_ITEM_TEMPLATE_LABEL);
            new ConfigChecker.Builder(client, ldapKeyStoreAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, NEW_ITEM_ATTRIBUTES, newItemAttributesString)
                    .edit(TEXT, NEW_ITEM_PATH, path)
                    .edit(TEXT, NEW_ITEM_RDN, rdn)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(NEW_ITEM_TEMPLATE, expectedNewItem.toModelNode());

        } finally {
            ops.removeIfExists(ldapKeyStoreAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createDirContext(Address dirContextAddress) throws IOException, TimeoutException, InterruptedException {
        final String urlValue = "ldap://127.0.0.1:" + RandomStringUtils.randomNumeric(5);
        final String principalValue = "uid=admin,ou=" + RandomStringUtils.randomAlphanumeric(5);
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final ModelNode credentialReferenceNode = new ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        ops.add(dirContextAddress, Values.of("url", urlValue).and("principal", principalValue)
                .and(CREDENTIAL_REFERENCE, credentialReferenceNode)).assertSuccess();
        adminOps.reloadIfRequired();
    }

}
