package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.dmr.ModelNodeUtils;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
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

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class ElytronLDAPIdentityAttributeMappingTestCase extends ElytronLDAPSecurityRealmTestCaseAbstract {

    private static final String IDENTITY_ATTRIBUTE_MAPPING_LABEL = "Identity Attribute Mapping";
    private static final String EXTRACT_RDN = "extract-rdn";
    private static final String FILTER = "filter";
    private static final String FILTER_BASE_DN = "filter-base-dn";
    private static final String REFERENCE = "reference";
    private static final String ROLE_RECURSION = "role-recursion";
    private static final String ROLE_RECURSION_NAME = "role-recursion-name";
    private static final String SEARCH_RECURSIVE = "search-recursive";
    private static final String VALUE = "value";


    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to add new attribute-mapping list
     * member inside its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testAddAttributeMapping() throws Exception {
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);
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
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
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
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
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
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

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
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
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
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }
}
