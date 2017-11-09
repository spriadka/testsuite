package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddAggregateSecurityRealmWizard;
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

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronAggregateSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String
            AGGREGATE_REALM = "aggregate-realm",
            AUTHENTICATION_REALM = "authentication-realm",
            AUTHORIZATION_REALM = "authorization-realm",
            IDENTITY = "identity",
            IDENTITY_REALM = "identity-realm";

    /**
     * @tpTestDetails Try to create Elytron Aggregate security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddAggregateSecurityRealm() throws Exception {
        Address authenticationRealm = null,
                authorizationRealm = null,
                aggregateRealm = elyOps.getElytronAddress(AGGREGATE_REALM, RandomStringUtils.randomAlphanumeric(7));

        try {
            authenticationRealm = createIdentityRealm();
            authorizationRealm = createIdentityRealm();

            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .addResource(AddAggregateSecurityRealmWizard.class)
                    .name(aggregateRealm.getLastPairValue())
                    .authenticationRealm(authenticationRealm.getLastPairValue())
                    .authorizationRealm(authorizationRealm.getLastPairValue())
                    .saveWithState()
                    .assertWindowClosed();


            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(aggregateRealm.getLastPairValue()));

            new ResourceVerifier(aggregateRealm, client)
                    .verifyExists()
                    .verifyAttribute(AUTHENTICATION_REALM, authenticationRealm.getLastPairValue())
                    .verifyAttribute(AUTHORIZATION_REALM, authorizationRealm.getLastPairValue());
        } finally {
            ops.removeIfExists(aggregateRealm);
            if (authenticationRealm != null) {
                ops.removeIfExists(authenticationRealm);
            }
            if (authorizationRealm != null) {
                ops.removeIfExists(authorizationRealm);
            }
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate security realm instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Aggregate security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveAggregateRealm() throws Exception {
        Address authenticationRealm = null,
                authorizationRealm = null,
                aggregateRealm = null;

        try {
            authenticationRealm = createIdentityRealm();
            authorizationRealm = createIdentityRealm();
            aggregateRealm = createAggegateRealm(authorizationRealm, authenticationRealm);

            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .removeResource(aggregateRealm.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse("Resource should NOT be present in table!",
                    page.getResourceManager().isResourcePresent(aggregateRealm.getLastPairValue()));

            new ResourceVerifier(aggregateRealm, client).verifyDoesNotExist();
        } finally {
            if (aggregateRealm != null) {
                ops.removeIfExists(aggregateRealm);
            }
            if (authenticationRealm != null) {
                ops.removeIfExists(authenticationRealm);
            }
            if (authorizationRealm != null) {
                ops.removeIfExists(authorizationRealm);
            }
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate security realm instance in model and try to edit its authorization-realm
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAuthorizationRealm() throws Exception {
        Address authenticationRealm = null,
                authorizationRealm = null,
                authorizationRealm2 = null,
                aggregateRealm = null;

        try {
            authenticationRealm = createIdentityRealm();
            authorizationRealm = createIdentityRealm();
            authorizationRealm2 = createIdentityRealm();
            aggregateRealm = createAggegateRealm(authorizationRealm, authenticationRealm);

            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .selectByName(aggregateRealm.getLastPairValue());

            new ConfigChecker.Builder(client, aggregateRealm)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_REALM, authorizationRealm2.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_REALM, authorizationRealm2.getLastPairValue());

        } finally {
            if (aggregateRealm != null) {
                ops.removeIfExists(aggregateRealm);
            }
            if (authorizationRealm2 != null) {
                ops.removeIfExists(authorizationRealm2);
            }
            if (authenticationRealm != null) {
                ops.removeIfExists(authenticationRealm);
            }
            if (authorizationRealm != null) {
                ops.removeIfExists(authorizationRealm);
            }
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate security realm instance in model and try to edit its authentication-realm
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAuthenticationRealm() throws Exception {
        Address authenticationRealm = null,
                authenticationRealm2 = null,
                authorizationRealm = null,
                aggregateRealm = null;

        try {
            authenticationRealm = createIdentityRealm();
            authenticationRealm2 = createIdentityRealm();
            authorizationRealm = createIdentityRealm();
            aggregateRealm = createAggegateRealm(authorizationRealm, authenticationRealm);

            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .selectByName(aggregateRealm.getLastPairValue());

            new ConfigChecker.Builder(client, aggregateRealm)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_REALM, authenticationRealm2.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_REALM, authenticationRealm2.getLastPairValue());

        } finally {
            if (aggregateRealm != null) {
                ops.removeIfExists(aggregateRealm);
            }
            if (authenticationRealm2 != null) {
                ops.removeIfExists(authenticationRealm2);
            }
            if (authenticationRealm != null) {
                ops.removeIfExists(authenticationRealm);
            }
            if (authorizationRealm != null) {
                ops.removeIfExists(authorizationRealm);
            }
        }
    }


    private Address createAggegateRealm(Address authorizationRealm, Address authenticationRealm) throws IOException {
        final Address realmAddress = elyOps.getElytronAddress(AGGREGATE_REALM, RandomStringUtils.randomAlphabetic(7));
        ops.add(realmAddress, Values.of(AUTHENTICATION_REALM, authenticationRealm.getLastPairValue())
                .and(AUTHORIZATION_REALM, authorizationRealm.getLastPairValue())).assertSuccess();
        return realmAddress;
    }

    private Address createIdentityRealm() throws IOException {
        final Address realmAddress = elyOps.getElytronAddress(IDENTITY_REALM, RandomStringUtils.randomAlphabetic(7));
        ops.add(realmAddress, Values.of(IDENTITY, RandomStringUtils.randomAlphanumeric(7)));
        return realmAddress;
    }
}
