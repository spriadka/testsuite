package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddPropertiesSecurityRealmWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

@RunWith(Arquillian.class)
public class ElytronPropertiesSecurityRealmTestCase extends AbstractElytronTestCase {

    private static final String
            USERS_PROPERTIES = "users-properties",
            GROUPS_PROPERTIES = "groups-properties",
            GROUPS_ATTRIBUTE = "groups-attribute",
            PROPERTIES_REALM = "properties-realm",
            DIGEST_REALM_NAME = "digest-realm-name",
            PATH = "path",
            PLAIN_TEXT = "plain-text",
            RELATIVE_TO = "relative-to";

    @Page
    private SecurityRealmPage page;

    @Test
    public void testAddPropertiesSecurityRealm() throws Exception {
        Address realmAddress = elyOps.getElytronAddress(PROPERTIES_REALM, RandomStringUtils.randomAlphabetic(7));

        page.navigate();

        try {
            page.getResourceManager().addResource(AddPropertiesSecurityRealmWizard.class)
                    .name(realmAddress.getLastPairValue())
                    .usersPropertiesPath("mgmt-users.properties")
                    .relativeTo("jboss.server.config.dir")
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editUsersProperties() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(PROPERTIES_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            createPropertiesSecurityRealm(securityRealmAddress);

            page.navigate();
            page.getResourceManager().selectByName(securityRealmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo("Users Properties");

            final String
                    digestRealmNameValue = RandomStringUtils.randomAlphabetic(7),
                    pathValue = RandomStringUtils.randomAlphanumeric(7),
                    relativeToValue = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(7);
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, DIGEST_REALM_NAME, digestRealmNameValue)
                    .edit(ConfigChecker.InputType.TEXT, PATH, pathValue)
                    .edit(ConfigChecker.InputType.CHECKBOX, PLAIN_TEXT, true)
                    .edit(ConfigChecker.InputType.TEXT, RELATIVE_TO, relativeToValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(USERS_PROPERTIES, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(DIGEST_REALM_NAME, digestRealmNameValue)
                            .addProperty(PATH, pathValue)
                            .addProperty(PLAIN_TEXT, new ModelNode(true))
                            .addProperty(RELATIVE_TO, relativeToValue)
                            .build());

        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editGroupsProperties() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(PROPERTIES_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            createPropertiesSecurityRealm(securityRealmAddress);

            page.navigate();
            page.getResourceManager().selectByName(securityRealmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo("Groups Properties");

            final String
                    pathValue = RandomStringUtils.randomAlphanumeric(7),
                    relativeToValue = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(7);
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, PATH, pathValue)
                    .edit(ConfigChecker.InputType.TEXT, RELATIVE_TO, relativeToValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(GROUPS_PROPERTIES, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(PATH, pathValue)
                            .addProperty(RELATIVE_TO, relativeToValue)
                            .build());

        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editGroupsAttribute() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(PROPERTIES_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            createPropertiesSecurityRealm(securityRealmAddress);

            page.navigate();
            page.getResourceManager().selectByName(securityRealmAddress.getLastPairValue());

            final String value = RandomStringUtils.randomAlphanumeric(7);
            new ConfigChecker.Builder(client, securityRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, GROUPS_ATTRIBUTE, value)
                    .verifyFormSaved()
                    .verifyAttribute(GROUPS_ATTRIBUTE, value);
        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    private Address createPropertiesSecurityRealm(Address securityRealmAddress) throws IOException {
        if (securityRealmAddress == null) {
            securityRealmAddress = elyOps.getElytronAddress(PROPERTIES_REALM, RandomStringUtils.randomAlphanumeric(7));
        }

        ops.add(securityRealmAddress, Values.of(USERS_PROPERTIES, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(PATH, "mgmt-users.properties")
                        .addProperty(RELATIVE_TO, "jboss.server.config.dir")
                        .build()))
                .assertSuccess();

        return securityRealmAddress;
    }

}
