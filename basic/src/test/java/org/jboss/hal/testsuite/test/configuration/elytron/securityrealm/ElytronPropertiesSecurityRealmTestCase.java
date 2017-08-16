package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddPropertiesSecurityRealmWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

@Category(Elytron.class)
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
            RELATIVE_TO = "relative-to",
            CONFIG_DIR_PATH_NAME = ConfigUtils.getConfigDirPathName();

    @Page
    private SecurityRealmPage page;

    /**
     * @tpTestDetails Try to create Elytron Properties security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Properties security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddPropertiesSecurityRealm() throws Exception {
        final Address realmAddress = elyOps.getElytronAddress(PROPERTIES_REALM, RandomStringUtils.randomAlphabetic(7));

        final String usersPropertiesPathValue = "mgmt-users.properties";

        page.navigate();

        try {
            page.getResourceManager().addResource(AddPropertiesSecurityRealmWizard.class)
                    .name(realmAddress.getLastPairValue())
                    .usersPropertiesPath(usersPropertiesPathValue)
                    .relativeTo(CONFIG_DIR_PATH_NAME)
                    .saveWithState()
                    .assertWindowClosed("Failed probably because of https://issues.jboss.org/browse/HAL-1347");

            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client).verifyExists()
                    .verifyAttribute(USERS_PROPERTIES + "." + RELATIVE_TO, CONFIG_DIR_PATH_NAME)
                    .verifyAttribute(USERS_PROPERTIES + "." + PATH, usersPropertiesPathValue);
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Properties security realm instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Properties security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemovePropertiesSecurityRealm() throws Exception {
        final Address securityRealmAddress = elyOps.getElytronAddress(PROPERTIES_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            createPropertiesSecurityRealm(securityRealmAddress);

            page.navigate();
            page.getResourceManager()
                    .removeResource(securityRealmAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(securityRealmAddress.getLastPairValue()));

            new ResourceVerifier(securityRealmAddress, client).verifyDoesNotExist();

        } finally {
            ops.removeIfExists(securityRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Properties security realm instance in model and try to edit its users-properties
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
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

    /**
     * @tpTestDetails Create Elytron Properties security realm instance in model and try to edit its groups-properties
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
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

    /**
     * @tpTestDetails Create Elytron Properties security realm instance in model and try to edit its groups-attribute
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
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
                .addProperty(RELATIVE_TO, CONFIG_DIR_PATH_NAME)
                .build()))
                .assertSuccess();

        return securityRealmAddress;
    }

}
