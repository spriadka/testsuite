package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;


import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddLDAPSecurityRealm;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

//TODO add tests for identity mappings, when https://issues.jboss.org/browse/JBEAP-10238 is fixed
@RunWith(Arquillian.class)
public class ElytronLDAPSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String
            URL = "url",
            LDAP_REALM = "ldap-realm",
            IDENTITY_MAPPING = "identity-mapping",
            DIR_CONTEXT = "dir-context";

    /**
     * @tpTestDetails Try to create Elytron LDAP security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in LDAP security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddLDAPSecurityRealm() throws Exception {
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, org.apache.commons.lang.RandomStringUtils.randomAlphabetic(7));

        final String dirContextValue = RandomStringUtils.randomAlphabetic(7);

        page.navigate();
        page.switchToLDAPRealms();

        try {
            page.getResourceManager().addResource(AddLDAPSecurityRealm.class)
                    .name(realmAddress.getLastPairValue())
                    .dirContext(dirContextValue)
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table! Probably fails because of https://issues.jboss.org/browse/HAL-1344",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(DIR_CONTEXT, dirContextValue);
        } finally {
            ops.removeIfExists(realmAddress);
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
                .addProperty("attribute-mapping",
                        new ModelNodeGenerator.ModelNodeListBuilder()
                                .addNode(
                                        new ModelNodeGenerator.ModelNodePropertiesBuilder()
                                                .addProperty("from", RandomStringUtils.randomAlphanumeric(7))
                                                .addProperty("to", RandomStringUtils.randomAlphanumeric(7))
                                                .build())
                                .build())
                .addProperty("rdn-identifier", RandomStringUtils.randomAlphanumeric(7))
                .build();
    }

}
