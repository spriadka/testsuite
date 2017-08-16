package org.jboss.hal.testsuite.test.configuration.elytron.securityrealmmapper;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealmmapper.AddConstantRealmMapperWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmMapperPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronConstantRealmMapperTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmMapperPage page;

    private static final String
            CONSTANT_REALM_MAPPER = "constant-realm-mapper",
            REALM_NAME = "realm-name";

    private static final RealmMapperOperations realmMapperOps = new RealmMapperOperations(client);

    /**
     * @tpTestDetails Try to create Elytron Constant realm mapper in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Constant realm mapper table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddConstantRealmMapper() throws Exception {
        final Address realmMapperAddress = elyOps.getElytronAddress(CONSTANT_REALM_MAPPER, RandomStringUtils.randomAlphanumeric(7)),
                realmAddress = realmMapperOps.createIdentityRealm();

        try {
            page.navigate();
            page.switchToConstantRealmMappers()
                    .getResourceManager()
                    .addResource(AddConstantRealmMapperWizard.class)
                    .name(realmMapperAddress.getLastPairValue())
                    .realm(realmAddress.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(realmMapperAddress.getLastPairValue()));

            new ResourceVerifier(realmMapperAddress, client).verifyExists()
                    .verifyAttribute(REALM_NAME, realmAddress.getLastPairValue());
        } finally {
            ops.removeIfExists(realmMapperAddress);
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant realm mapper instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Constant realm mapper table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveConstantRealmMapper() throws Exception {
        Address realmMapperAddress = null,
                realmAddress = null;

        try {
            realmAddress = realmMapperOps.createIdentityRealm();
            realmMapperAddress = realmMapperOps.createConstantRealmMapper(realmAddress);

            page.navigate();
            page.switchToConstantRealmMappers()
                    .getResourceManager()
                    .removeResource(realmMapperAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(realmMapperAddress.getLastPairValue()));

            new ResourceVerifier(realmMapperAddress, client).verifyDoesNotExist();
        } finally {
            if (realmMapperAddress != null) {
                ops.removeIfExists(realmMapperAddress);
            }
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant realm mapper instance in model and try to edit its realm-name attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editRealmName() throws Exception {
        Address realmMapperAddress = null,
                newRealmAddress = null,
                realmAddress = null;

        try {
            realmAddress = realmMapperOps.createIdentityRealm();
            realmMapperAddress = realmMapperOps.createConstantRealmMapper(realmAddress);
            newRealmAddress = realmMapperOps.createIdentityRealm();

            page.navigate();
            page.switchToConstantRealmMappers()
                    .getResourceManager()
                    .selectByName(realmMapperAddress.getLastPairValue());

            new ConfigChecker.Builder(client, realmMapperAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, REALM_NAME, newRealmAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(REALM_NAME, newRealmAddress.getLastPairValue());

        } finally {
            if (realmMapperAddress != null) {
                ops.removeIfExists(realmMapperAddress);
            }
            if (realmAddress != null) {
                ops.removeIfExists(realmAddress);
            }
            if (newRealmAddress != null) {
                ops.removeIfExists(newRealmAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

}
