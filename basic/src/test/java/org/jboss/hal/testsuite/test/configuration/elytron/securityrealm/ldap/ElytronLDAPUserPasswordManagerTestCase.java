package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronLDAPUserPasswordManagerTestCase extends ElytronLDAPSecurityRealmTestCaseAbstract {

    private static final String USER_PASSWORD_MAPPER = "user-password-mapper";
    private static final String USER_PASSWORD_MAPPER_LABEL = "User Password Mapper";
    private static final String VERIFIABLE = "verifiable";
    private static final String WRITABLE = "writable";


    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit user-password-mapper object
     * in its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editUserPasswordMapper() throws Exception {
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String fromValue = RandomStringUtils.randomAlphanumeric(7);
        final boolean verifiableValue = true;
        final boolean writableValue = true;
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

        final ModelNode expectedValue = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(FROM, fromValue)
                .addProperty(VERIFIABLE, new ModelNode(verifiableValue))
                .addProperty(WRITABLE, new ModelNode(writableValue))
                .build();

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(USER_PASSWORD_MAPPER_LABEL);

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, FROM, fromValue)
                    .edit(ConfigChecker.InputType.CHECKBOX, VERIFIABLE, verifiableValue)
                    .edit(ConfigChecker.InputType.CHECKBOX, WRITABLE, writableValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY_MAPPING + "." + USER_PASSWORD_MAPPER, expectedValue);

        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }

}
