package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.ldap;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Arquillian.class)
public class ElytronLDAPOTPCredentialMapperTestCase extends ElytronLDAPSecurityRealmTestCaseAbstract {

    private static final String ALGORITHM_FROM = "algorithm-from";
    private static final String HASH_FROM = "hash-from";
    private static final String OTP_CREDENTIAL_MAPPER = "otp-credential-mapper";
    private static final String OTP_CREDENTIAL_MAPPER_LABEL = "OTP Credential Mapper";
    private static final String SEED_FROM = "seed-from";
    private static final String SEQUENCE_FROM = "sequence-from";


    /**
     * @tpTestDetails Create Elytron LDAP security realm instance in model and try to edit otp-credential-mapper object
     * in its identity-mapping attribute in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editOTPCredentialMapper() throws Exception {
        final String dirContextName = "dir_context_" + RandomStringUtils.randomAlphanumeric(7);
        final String realmName = "ldap_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String algorithmFrom = RandomStringUtils.randomAlphanumeric(7);
        final String hashFrom = RandomStringUtils.randomAlphanumeric(7);
        final String seedFrom = RandomStringUtils.randomAlphanumeric(7);
        final String sequenceFrom = RandomStringUtils.randomAlphanumeric(7);
        final Address dirContextAddress = elyOps.getElytronAddress(DIR_CONTEXT, dirContextName);
        final Address realmAddress = elyOps.getElytronAddress(LDAP_REALM, realmName);

        final ModelNode expectedValue = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(ALGORITHM_FROM, algorithmFrom)
                .addProperty(HASH_FROM, hashFrom)
                .addProperty(SEED_FROM, seedFrom)
                .addProperty(SEQUENCE_FROM, sequenceFrom)
                .build();

        try {
            createDirContext(dirContextAddress);
            createLDAPSecurityRealm(realmAddress, dirContextName);
            page.navigate();
            page.switchToLDAPRealms()
                    .getResourceManager()
                    .selectByName(realmName);
            final ConfigFragment configFragment = page.getConfig().switchTo(OTP_CREDENTIAL_MAPPER_LABEL);
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, ALGORITHM_FROM, algorithmFrom)
                    .edit(ConfigChecker.InputType.TEXT, HASH_FROM, hashFrom)
                    .edit(ConfigChecker.InputType.TEXT, SEED_FROM, seedFrom)
                    .edit(ConfigChecker.InputType.TEXT, SEQUENCE_FROM, sequenceFrom)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY_MAPPING + "." + OTP_CREDENTIAL_MAPPER, expectedValue);

        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(dirContextAddress);
            adminOps.reloadIfRequired();
        }
    }
}
