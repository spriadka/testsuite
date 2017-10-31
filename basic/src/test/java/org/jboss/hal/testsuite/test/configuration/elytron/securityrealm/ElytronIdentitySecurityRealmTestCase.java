package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;


import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddIdentitySecurityRealm;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronIdentitySecurityRealmTestCase extends AbstractElytronTestCase {

    private static final String ATTRIBUTE_NAME = "attribute-name";
    private static final String ATTRIBUTE_VALUES = "attribute-values";
    private static final String IDENTITY_REALM = "identity-realm";
    private static final String IDENTITY = "identity";

    @Page
    private SecurityRealmPage page;

    /**
     * @tpTestDetails Try to create Elytron Identity security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Identity security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddIdentitySecurityRealm() throws Exception {
        final String realmName = "identity_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String identityValue = RandomStringUtils.randomAlphanumeric(7);
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, realmName);
        try {
            page.navigate();
            page.switchToIdentityRealms();
            page.getResourceManager().addResource(AddIdentitySecurityRealm.class)
                    .name(realmName)
                    .identity(identityValue)
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmName));
            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(IDENTITY, identityValue);
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Identity security realm instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Identity security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveIdentitySecurityRealm() throws Exception {
        final String realmName = "identity_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, realmName);
        try {
            createIdentityRealm(realmAddress);
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .removeResource(realmName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse(page.getResourceManager().isResourcePresent(realmName));
            new ResourceVerifier(realmAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Identity security realm instance in model and try to edit its attribute-name
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editAttributeName() throws Exception {
        final String realmName = "identity_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String attributeNameValue = RandomStringUtils.randomAlphanumeric(7);
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, realmName);
        try {
            createIdentityRealm(realmAddress);
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .selectByName(realmName);
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, ATTRIBUTE_NAME, attributeNameValue)
                    .verifyFormSaved()
                    .verifyAttribute(ATTRIBUTE_NAME, attributeNameValue);

        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }

    }

    /**
     * @tpTestDetails Create Elytron Identity security realm instance in model and try to edit its identity attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editIdentity() throws Exception {
        final String realmName = "identity_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String identityValue = RandomStringUtils.randomAlphanumeric(7);
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, realmName);
        try {
            createIdentityRealm(realmAddress);
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .selectByName(realmName);

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, IDENTITY, identityValue)
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY, identityValue);

        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }

    }

    /**
     * @tpTestDetails Create Elytron Identity security realm instance in model and try to edit its attribute-values
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editAttributeValues() throws Exception {
        final String realmName = "identity_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String[] values = new String[] {
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7)
        };
        final ModelNode attributeValues = new ModelNodeGenerator.ModelNodeListBuilder()
                .addAll(values)
                .build();
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, realmName);
        try {
            createIdentityRealm(realmAddress);
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .selectByName(realmName);
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, ATTRIBUTE_VALUES, String.join("\n", values))
                    .verifyFormSaved()
                    .verifyAttribute(ATTRIBUTE_VALUES, attributeValues);

        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }

    }

    private void createIdentityRealm(Address realmAddress) throws IOException, TimeoutException, InterruptedException {
        ops.add(realmAddress, Values.of(IDENTITY, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        adminOps.reloadIfRequired();
    }
}
