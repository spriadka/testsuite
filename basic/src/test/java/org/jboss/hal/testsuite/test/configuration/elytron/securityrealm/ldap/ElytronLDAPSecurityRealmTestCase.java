package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;


import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddLDAPSecurityRealm;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;


@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronLDAPSecurityRealmTestCase extends ElytronLDAPSecurityRealmTestCaseAbstract {


    /**
     * @tpTestDetails Try to create Elytron LDAP security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in LDAP security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddLDAPSecurityRealm() throws Exception {
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String rdnIdentifierValue = RandomStringUtils.randomAlphabetic(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);
        try {
            createDirContext(dirContextAddress);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .addResource(AddLDAPSecurityRealm.class)
                    .name(realmName)
                    .dirContext(dirContextName)
                    .identityMappingRdnIdentifier(rdnIdentifierValue)
                    .saveWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Resource should be present in table! Probably fails because of https://issues.jboss.org/browse/HAL-1344",
                    page.getResourceManager().isResourcePresent(realmName));
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
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);
        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .removeResource(realmAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse("Resource should not be present! Probably caused by https://issues.jboss.org/browse/HAL-1345",
                    page.getResourceManager().isResourcePresent(realmName));

            new ResourceVerifier(realmAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

}
