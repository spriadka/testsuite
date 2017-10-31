package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronLDAPAttributesSecurityRealmTestCase extends ElytronLDAPSecurityRealmTestCaseAbstract {

    private static final String ALLOW_BLANK_PASSWORD = "allow-blank-password";
    private static final String DIRECT_VERIFICATION = "direct-verification";

    @Test
    public void toggleAllowBlankPasswordOnlyShowsError() throws Exception {
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            final ModelNodeResult initialAllowBlankPassword = ops.readAttribute(realmAddress, ALLOW_BLANK_PASSWORD);
            initialAllowBlankPassword.assertSuccess();
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());
            final ConfigFragment configFragment = page.getConfigFragment();
            configFragment.editCheckboxAndSave(ALLOW_BLANK_PASSWORD, !initialAllowBlankPassword.booleanValue());
            Assert.assertTrue("Page should be showing validation error regarding toggling \"direct verification\" with " +
                    "\"allow blank password\"", configFragment.isErrorShownInForm());

        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
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
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            final ModelNodeResult initialAllowBlankPassword = ops.readAttribute(realmAddress, ALLOW_BLANK_PASSWORD);
            final ModelNodeResult initialDirectVerification = ops.readAttribute(realmAddress, DIRECT_VERIFICATION);
            initialAllowBlankPassword.assertSuccess();
            initialDirectVerification.assertSuccess();
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.CHECKBOX, ALLOW_BLANK_PASSWORD, !initialAllowBlankPassword.booleanValue())
                    .edit(ConfigChecker.InputType.CHECKBOX, DIRECT_VERIFICATION, !initialDirectVerification.booleanValue())
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(ALLOW_BLANK_PASSWORD, !initialAllowBlankPassword.booleanValue(),
                            "Probably caused by https://issues.jboss.org/browse/HAL-1346");

        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }
    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit its allow-blank-password and
     * direct-verification attributes value in Web Console's Elytron subsystem configuration.
     * Validate edited attributes value in the model.
     */
    @Test
    public void toggleDirectVerification() throws Exception {
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            final ModelNodeResult initialDirectVerification = ops.readAttribute(realmAddress, DIRECT_VERIFICATION);
            initialDirectVerification.assertSuccess();
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.CHECKBOX, DIRECT_VERIFICATION, !initialDirectVerification.booleanValue())
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(DIRECT_VERIFICATION, !initialDirectVerification.booleanValue(),
                            "Probably caused by https://issues.jboss.org/browse/HAL-1346");

        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
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
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String initialDirContextName = "initial_dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, initialDirContextName);
        final Address newDirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

        try {
            createDirContext(dirContextAddress);
            createDirContext(newDirContextAddress);
            createLDAPSecurityRealm(realmAddress, initialDirContextName);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, DIR_CONTEXT, dirContextName)
                    .verifyFormSaved()
                    .verifyAttribute(DIR_CONTEXT, dirContextName,
                            "Probably caused by https://issues.jboss.org/browse/HAL-1346");

        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            ops.removeIfExists(newDirContextAddress);
            adminOps.reloadIfRequired();
        }
    }
}
