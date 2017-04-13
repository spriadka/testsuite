package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;


import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddIdentitySecurityRealm;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.Arrays;

@RunWith(Arquillian.class)
public class ElytronIdentitySecurityRealmTestCase extends AbstractElytronTestCase {

    private static final String
            ATTRIBUTE_NAME = "attribute-name",
            ATTRIBUTE_VALUES = "attribute-values",
            IDENTITY_REALM = "identity-realm",
            IDENTITY = "identity";

    @Page
    private SecurityRealmPage page;

    @Test
    public void testAddIdentitySecurityRealm() throws Exception {
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, RandomStringUtils.randomAlphabetic(7));
        final String identityValue = RandomStringUtils.randomAlphanumeric(7);

        page.navigate();
        page.switchToIdentityRealms();

        try {
            page.getResourceManager().addResource(AddIdentitySecurityRealm.class)
                    .name(realmAddress.getLastPairValue())
                    .identity(identityValue)
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(IDENTITY, identityValue);
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void testRemoveIdentitySecurityRealm() throws Exception {
        Address realmAddress = null;

        try {
            realmAddress = createIdentityRealm();
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .removeResource(realmAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client).verifyDoesNotExist();
        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editAttributeName() throws Exception {
        Address realmAddress = null;

        try {
            realmAddress = createIdentityRealm();
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final String value = RandomStringUtils.randomAlphanumeric(7);
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, ATTRIBUTE_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(ATTRIBUTE_NAME, value);

        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            adminOps.reloadIfRequired();
        }

    }

    @Test
    public void editIdentity() throws Exception {
        Address realmAddress = null;

        try {
            realmAddress = createIdentityRealm();
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final String value = RandomStringUtils.randomAlphanumeric(7);
            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, IDENTITY, value)
                    .verifyFormSaved()
                    .verifyAttribute(IDENTITY, value);

        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            adminOps.reloadIfRequired();
        }

    }

    @Test
    public void editAttributeValues() throws Exception {
        Address realmAddress = null;

        try {
            realmAddress = createIdentityRealm();
            page.navigate();
            page.switchToIdentityRealms()
                    .getResourceManager()
                    .selectByName(realmAddress.getLastPairValue());

            final String[] value = new String[] {
                    RandomStringUtils.randomAlphanumeric(7),
                    RandomStringUtils.randomAlphanumeric(7)
            };
            final ModelNodeGenerator.ModelNodeListBuilder nodeBuilder = new ModelNodeGenerator.ModelNodeListBuilder();
            Arrays.stream(value).forEach(item -> nodeBuilder.addNode(new ModelNode(item)));

            new ConfigChecker.Builder(client, realmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, ATTRIBUTE_VALUES, String.join("\n", value))
                    .verifyFormSaved()
                    .verifyAttribute(ATTRIBUTE_VALUES, nodeBuilder.build());

        } finally {
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            adminOps.reloadIfRequired();
        }

    }

    private Address createIdentityRealm() throws IOException {
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, RandomStringUtils.randomAlphabetic(7));
        ops.add(realmAddress, Values.of(IDENTITY, RandomStringUtils.randomAlphanumeric(7)));
        return realmAddress;
    }
}
