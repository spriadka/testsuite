package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;


import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.dmr.ModelNodeUtils;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddLDAPSecurityRealm;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronLDAPSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String
            ALGORITHM_FROM = "algorithm-from",
            ATTRIBUTE_MAPPING = "attribute-mapping",
            DIR_CONTEXT = "dir-context",
            EXTRACT_RDN = "extract-rdn",
            FILTER = "filter",
            FILTER_BASE_DN = "filter-base-dn",
            FILTER_NAME = "filter-name",
            FROM = "from",
            HASH_FROM = "hash-from",
            IDENTITY_ATTRIBUTE_MAPPING_LABEL = "Identity Attribute Mapping",
            IDENTITY_MAPPING = "identity-mapping",
            IDENTITY_MAPPING_LABEL = "Identity Mapping",
            ITERATOR_FILTER = "iterator-filter",
            LDAP_REALM = "ldap-realm",
            LIST_ADD = "list-add",
            NAME = "name",
            NEW_IDENTITY_ATTRIBUTES = "new-identity-attributes",
            NEW_IDENTITY_ATTRIBUTES_LABEL = "New Identity Attributes",
            NEW_IDENTITY_PARENT_DN = "new-identity-parent-dn",
            OTP_CREDENTIAL_MAPPER = "otp-credential-mapper",
            OTP_CREDENTIAL_MAPPER_LABEL = "OTP Credential Mapper",
            RDN_IDENTIFIER = "rdn-identifier",
            REFERENCE = "reference",
            ROLE_RECURSION = "role-recursion",
            ROLE_RECURSION_NAME = "role-recursion-name",
            SEARCH_BASE_DN = "search-base-dn",
            SEARCH_RECURSIVE = "search-recursive",
            SEED_FROM = "seed-from",
            SEQUENCE_FROM = "sequence-from",
            TO = "to",
            URL = "url",
            USER_PASSWORD_MAPPER = "user-password-mapper",
            USER_PASSWORD_MAPPER_LABEL = "User Password Mapper",
            USE_RECURSIVE_SEARCH = "use-recursive-search",
            VALUE = "value",
            VERIFIABLE = "verifiable",
            WRITABLE = "writable";

    /**
     * @tpTestDetails Try to create Elytron LDAP security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in LDAP security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddLDAPSecurityRealm() throws Exception {
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, org.apache.commons.lang.RandomStringUtils.randomAlphabetic(7)),
                dirContextAddress = createDirContext();

        final String rdnIdentifierValue = RandomStringUtils.randomAlphabetic(7);

        try {
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .addResource(AddLDAPSecurityRealm.class)
                    .name(realmAddress.getLastPairValue())
                    .dirContext(dirContextAddress.getLastPairValue())
                    .identityMappingRdnIdentifier(rdnIdentifierValue)
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table! Probably fails because of https://issues.jboss.org/browse/HAL-1344",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(IDENTITY_MAPPING + "." + RDN_IDENTIFIER, rdnIdentifierValue)
                    .verifyAttribute(DIR_CONTEXT, dirContextAddress.getLastPairValue());
        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in LDAP security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveLDAPSecurityRealm() throws Exception {
        Address dirContextAddress = null;
        Address realmAddress = null;

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .removeResource(realmAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse("Resource should not be present! Probably caused by https://issues.jboss.org/browse/HAL-1345",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client).verifyDoesNotExist();
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit its allow-blank-password and
     * direct-verification attributes value in Web Console's Elytron subsystem configuration.
     * Validate edited attributes value in the model.
     */
    @Test
    public void toggleAllowBlankPasswordAndDirectVerification() throws Exception {
        Address dirContextAddress = null;
        Address realmAddress = null;

        final String allowBlankPassword = "allow-blank-password", directVerification = "direct-verification";

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            final ModelNodeResult originalModelNodeResult = ops.readAttribute(realmAddress, allowBlankPassword);
            originalModelNodeResult.assertSuccess();
            final boolean originalBoolValue = originalModelNodeResult.booleanValue();

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.CHECKBOX, allowBlankPassword, !originalBoolValue)
                    .edit(ConfigChecker.InputType.CHECKBOX, directVerification, !originalBoolValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(allowBlankPassword, !originalBoolValue,
                            "Probably caused by https://issues.jboss.org/browse/HAL-1346");

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.CHECKBOX, allowBlankPassword, originalBoolValue)
                    .edit(ConfigChecker.InputType.CHECKBOX, directVerification, originalBoolValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(allowBlankPassword, originalBoolValue);
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit its dir-context attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editDirContext() throws Exception {
        Address dirContextAddress = null,
                realmAddress = null,
                newDirContextAddress = null;

        try {
            dirContextAddress = createDirContext();
            newDirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, DIR_CONTEXT, newDirContextAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(DIR_CONTEXT, newDirContextAddress.getLastPairValue(),
                            "Probably caused by https://issues.jboss.org/browse/HAL-1346");

        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            if (newDirContextAddress != null) {
                ops.removeIfExists(newDirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit its identity-mapping
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editIdentityMapping() throws Exception {
        Address dirContextAddress = null,
                realmAddress = null;

        final String filterNameValue = RandomStringUtils.randomAlphanumeric(7),
                iteratorFilterValue = RandomStringUtils.randomAlphanumeric(7),
                newIdentityParentDnValue = RandomStringUtils.randomAlphanumeric(7),
                rdnIdentifierValue = RandomStringUtils.randomAlphanumeric(7),
                searchBaseDnValue = RandomStringUtils.randomAlphanumeric(7);

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            ModelNodeResult originalValueResult = ops.readAttribute(realmAddress, IDENTITY_MAPPING);
            originalValueResult.assertSuccess();
            ModelNode originalValue = originalValueResult.value();

            ConfigFragment configFragment = page.getConfig().switchTo(IDENTITY_MAPPING_LABEL);
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, FILTER_NAME, filterNameValue)
                    .edit(ConfigChecker.InputType.TEXT, ITERATOR_FILTER, iteratorFilterValue)
                    .edit(ConfigChecker.InputType.TEXT, NEW_IDENTITY_PARENT_DN, newIdentityParentDnValue)
                    .edit(ConfigChecker.InputType.TEXT, RDN_IDENTIFIER, rdnIdentifierValue)
                    .edit(ConfigChecker.InputType.TEXT, SEARCH_BASE_DN, searchBaseDnValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY_MAPPING, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(ATTRIBUTE_MAPPING, originalValue.get(ATTRIBUTE_MAPPING))
                            .addProperty(RDN_IDENTIFIER, rdnIdentifierValue)
                            .addProperty(FILTER_NAME, filterNameValue)
                            .addProperty(ITERATOR_FILTER, iteratorFilterValue)
                            .addProperty(NEW_IDENTITY_PARENT_DN, newIdentityParentDnValue)
                            .addProperty(SEARCH_BASE_DN, searchBaseDnValue)
                            .addProperty(USE_RECURSIVE_SEARCH, new ModelNode(false))
                            .build()
                    );
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to add new attribute-mapping list
     * member inside its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testAddAttributeMapping() throws Exception {
        Address dirContextAddress = null,
                realmAddress = null;

        final long roleRecursionValue = 45678;

        final ModelNode expectedValue = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(FROM, RandomStringUtils.randomAlphabetic(7))
                .addProperty(TO, RandomStringUtils.randomAlphabetic(7))
                .addUndefinedProperty(REFERENCE)
                .addProperty(FILTER, RandomStringUtils.randomAlphabetic(7))
                .addProperty(FILTER_BASE_DN, RandomStringUtils.randomAlphabetic(7))
                .addProperty(ROLE_RECURSION, new ModelNode(roleRecursionValue))
                .addProperty(ROLE_RECURSION_NAME, RandomStringUtils.randomAlphabetic(7))
                .addProperty(EXTRACT_RDN, RandomStringUtils.randomAlphabetic(7))
                .build();

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(IDENTITY_ATTRIBUTE_MAPPING_LABEL);

            final WizardWindow wizardWindow = configFragment.getResourceManager().addResource();
            final Editor editor = wizardWindow.getEditor();

            editor.text(FROM, expectedValue.get(FROM).asString());
            editor.text(TO, expectedValue.get(TO).asString());
            editor.text(FILTER, expectedValue.get(FILTER).asString());
            editor.text(FILTER_BASE_DN, expectedValue.get(FILTER_BASE_DN).asString());
            editor.text(ROLE_RECURSION, String.valueOf(roleRecursionValue));
            editor.text(ROLE_RECURSION_NAME, expectedValue.get(ROLE_RECURSION_NAME).asString());
            editor.text(EXTRACT_RDN, expectedValue.get(EXTRACT_RDN).asString());

            wizardWindow.saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();

            Assert.assertTrue("Resource is not present in resource table after adding!",
                    configFragment.getResourceManager().isResourcePresent(expectedValue.get(FROM).asString()));

            new ResourceVerifier(realmAddress, client).verifyListAttributeContainsValue(
                    IDENTITY_MAPPING + "." + ATTRIBUTE_MAPPING,
                    expectedValue
            );
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model, add new value to attribute-mapping list
     * inside its identity-mapping attribute and then try to remove this value in Web Console's Elytron subsystem
     * configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testRemoveIdentityAttributeMapping() throws IOException, OperationException, TimeoutException, InterruptedException {
        Address dirContextAddress = null,
                realmAddress = null;

        final long roleRecursionValue = 45678;

        final ModelNode mappingToRemove = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(FROM, RandomStringUtils.randomAlphanumeric(7))
                .addProperty(TO, RandomStringUtils.randomAlphanumeric(7))
                .addUndefinedProperty(REFERENCE)
                .addProperty(FILTER, RandomStringUtils.randomAlphanumeric(7))
                .addProperty(FILTER_BASE_DN, RandomStringUtils.randomAlphanumeric(7))
                .addProperty(SEARCH_RECURSIVE, new ModelNode(false))
                .addProperty(ROLE_RECURSION, new ModelNode(roleRecursionValue))
                .addProperty(ROLE_RECURSION_NAME, RandomStringUtils.randomAlphanumeric(7))
                .addProperty(EXTRACT_RDN, RandomStringUtils.randomAlphanumeric(7))
                .build();

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            ops.invoke(LIST_ADD, realmAddress, Values.empty()
                    .and(NAME, IDENTITY_MAPPING + "." + ATTRIBUTE_MAPPING)
                    .and(VALUE, mappingToRemove));

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(IDENTITY_ATTRIBUTE_MAPPING_LABEL);
            configFragment.getResourceManager()
                    .removeResource(mappingToRemove.get(FROM).asString())
                    .confirmAndDismissReloadRequiredMessage();

            Assert.assertFalse("Resource should not present in resource table after removing!",
                    configFragment.getResourceManager().isResourcePresent(mappingToRemove.get(FROM).asString()));

            ModelNodeResult editedValueResult = ops.readAttribute(realmAddress, IDENTITY_MAPPING);
            editedValueResult.assertSuccess();
            ModelNode editedIdentityMappingList = editedValueResult.value();

            Assert.assertFalse("Expected value should not present in the list!",
                    ModelNodeUtils.isValuePresentInModelNodeList(editedIdentityMappingList.get(ATTRIBUTE_MAPPING), mappingToRemove));
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to add new new-identity-attributes
     * list member inside its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testAddNewIdentityAttribute() throws Exception {
        Address dirContextAddress = null,
                realmAddress = null;

        final ModelNode expectedValue = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, RandomStringUtils.randomAlphabetic(7))
                .addProperty(VALUE, new ModelNodeGenerator.ModelNodeListBuilder(
                        new ModelNode(RandomStringUtils.randomAlphabetic(7)))
                        .build())
                .build();

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(NEW_IDENTITY_ATTRIBUTES_LABEL);

            final WizardWindow wizardWindow = configFragment.getResourceManager().addResource();
            final Editor editor = wizardWindow.getEditor();

            editor.text(NAME, expectedValue.get(NAME).asString());
            editor.text(VALUE, expectedValue.get(VALUE).asList().get(0).asString());

            wizardWindow.saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();

            Assert.assertTrue("Resource should be present in resource table after adding!",
                    configFragment.getResourceManager().isResourcePresent(expectedValue.get(NAME).asString()));

            new ResourceVerifier(realmAddress, client).verifyListAttributeContainsValue(
                    IDENTITY_MAPPING + "." + NEW_IDENTITY_ATTRIBUTES,
                    expectedValue
            );
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model, add new value to new-identity-attributes
     * list inside its identity-mapping attribute and then try to remove this value in Web Console's Elytron subsystem
     * configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testRemoveNewIdentityAttribute() throws Exception {
        Address dirContextAddress = null,
                realmAddress = null;

        final ModelNode identityToRemove = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, RandomStringUtils.randomAlphabetic(7))
                .addProperty(VALUE, new ModelNodeGenerator.ModelNodeListBuilder(
                        new ModelNode(RandomStringUtils.randomAlphabetic(7)))
                        .build())
                .build();

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            ops.invoke(LIST_ADD, realmAddress, Values.empty()
                            .and(NAME, IDENTITY_MAPPING + "." + NEW_IDENTITY_ATTRIBUTES)
                            .and(VALUE, identityToRemove));

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(NEW_IDENTITY_ATTRIBUTES_LABEL);

            configFragment
                    .getResourceManager()
                    .removeResource(identityToRemove.get(NAME).asString())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse("Resource should NOT be present in resource table after removing!",
                    configFragment.getResourceManager().isResourcePresent(identityToRemove.get(NAME).asString()));

            new ResourceVerifier(realmAddress, client).verifyListAttributeDoesNotContainValue(
                    IDENTITY_MAPPING + "." + NEW_IDENTITY_ATTRIBUTES,
                    identityToRemove
            );
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit user-password-mapper object
     * in its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editUserPasswordMapper() throws Exception {
        Address dirContextAddress = null,
                realmAddress = null;

        final ModelNode expectedValue = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(FROM, RandomStringUtils.randomAlphabetic(7))
                .addProperty(VERIFIABLE, new ModelNode(true))
                .addProperty(WRITABLE, new ModelNode(true))
                .build();

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(USER_PASSWORD_MAPPER_LABEL);

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, FROM, expectedValue.get(FROM).asString())
                    .edit(ConfigChecker.InputType.CHECKBOX, VERIFIABLE, expectedValue.get(VERIFIABLE).asBoolean())
                    .edit(ConfigChecker.InputType.CHECKBOX, WRITABLE, expectedValue.get(WRITABLE).asBoolean())
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY_MAPPING + "." + USER_PASSWORD_MAPPER, expectedValue);

        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit otp-credential-mapper object
     * in its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editOTPCredentialMapper() throws Exception {
        Address dirContextAddress = null,
                realmAddress = null;

        final ModelNode expectedValue = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(ALGORITHM_FROM, RandomStringUtils.randomAlphabetic(7))
                .addProperty(HASH_FROM, RandomStringUtils.randomAlphabetic(7))
                .addProperty(SEED_FROM, RandomStringUtils.randomAlphabetic(7))
                .addProperty(SEQUENCE_FROM, RandomStringUtils.randomAlphabetic(7))
                .build();

        try {
            dirContextAddress = createDirContext();
            realmAddress = createLDAPSecurityRealm(dirContextAddress);

            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(OTP_CREDENTIAL_MAPPER_LABEL);

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, ALGORITHM_FROM, expectedValue.get(ALGORITHM_FROM).asString())
                    .edit(ConfigChecker.InputType.TEXT, HASH_FROM, expectedValue.get(HASH_FROM).asString())
                    .edit(ConfigChecker.InputType.TEXT, SEED_FROM, expectedValue.get(SEED_FROM).asString())
                    .edit(ConfigChecker.InputType.TEXT, SEQUENCE_FROM, expectedValue.get(SEQUENCE_FROM).asString())
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY_MAPPING + "." + OTP_CREDENTIAL_MAPPER, expectedValue);

        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (dirContextAddress != null) {
                ops.removeIfExists(dirContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    private Address createDirContext() throws IOException {
        final Address address = elyOps.getElytronAddress(DIR_CONTEXT, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address, Values.of(URL, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        return address;
    }

    private Address createLDAPSecurityRealm(Address dirContextAddress) throws IOException {
        final Address address = elyOps.getElytronAddress(LDAP_REALM, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address, Values.of(IDENTITY_MAPPING, composeIdentityMappingAttribute())
                .and(DIR_CONTEXT, dirContextAddress.getLastPairValue())).assertSuccess();
        return address;
    }

    private ModelNode composeIdentityMappingAttribute() {
        return new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(ATTRIBUTE_MAPPING,
                        new ModelNodeGenerator.ModelNodeListBuilder()
                                .addNode(
                                        new ModelNodeGenerator.ModelNodePropertiesBuilder()
                                                .addProperty(FROM, RandomStringUtils.randomAlphanumeric(7))
                                                .addProperty(TO, RandomStringUtils.randomAlphanumeric(7))
                                                .build())
                                .build())
                .addProperty(RDN_IDENTIFIER, RandomStringUtils.randomAlphanumeric(7))
                .build();
    }

}
