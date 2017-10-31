package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddLDAPNewIdentityAttributeWizard;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronLDAPNewIdentityAttributesTestCase extends ElytronLDAPSecurityRealmTestCaseAbstract {

    private static final String LIST_ADD = "list-add";
    private static final String NAME = "name";
    private static final String NEW_IDENTITY_ATTRIBUTES = "new-identity-attributes";
    private static final String NEW_IDENTITY_ATTRIBUTES_LABEL = "New Identity Attributes";
    private static final String VALUE = "value";

    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to add new new-identity-attributes
     * list member inside its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testAddNewIdentityAttribute() throws Exception {
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String identityName = RandomStringUtils.randomAlphanumeric(7);
        final String identityValue = RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);
        final ModelNode expectedValue = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, identityName)
                .addProperty(VALUE, new ModelNodeGenerator.ModelNodeListBuilder(
                        new ModelNode(identityValue))
                        .build())
                .build();

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());
            page.getConfig().switchTo(NEW_IDENTITY_ATTRIBUTES_LABEL);
            page.getConfig()
                    .getResourceManager()
                    .addResource(AddLDAPNewIdentityAttributeWizard.class)
                    .name(identityName)
                    .value(identityValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Resource should be present in resource table after adding!",
                    page.getConfig().getResourceManager().isResourcePresent(expectedValue.get(NAME).asString()));
            new ResourceVerifier(realmAddress, client).verifyListAttributeContainsValue(
                    IDENTITY_MAPPING + "." + NEW_IDENTITY_ATTRIBUTES,
                    expectedValue
            );
        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
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
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String identityName = RandomStringUtils.randomAlphanumeric(7);
        final String identityValue = RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);
        final ModelNode identityToRemove = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, identityName)
                .addProperty(VALUE, new ModelNodeGenerator.ModelNodeListBuilder(
                        new ModelNode(identityValue))
                        .build())
                .build();

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            ops.invoke(LIST_ADD, realmAddress, Values.empty()
                    .and(NAME, IDENTITY_MAPPING + "." + NEW_IDENTITY_ATTRIBUTES)
                    .and(VALUE, identityToRemove));
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmName);
            page.getConfig().switchTo(NEW_IDENTITY_ATTRIBUTES_LABEL);
            page.getConfig()
                    .getResourceManager()
                    .removeResource(identityToRemove.get(NAME).asString())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse("Resource should NOT be present in resource table after removing!",
                    page.getConfig().getResourceManager().isResourcePresent(identityName));

            new ResourceVerifier(realmAddress, client).verifyListAttributeDoesNotContainValue(
                    IDENTITY_MAPPING + "." + NEW_IDENTITY_ATTRIBUTES,
                    identityToRemove
            );
        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }
}
