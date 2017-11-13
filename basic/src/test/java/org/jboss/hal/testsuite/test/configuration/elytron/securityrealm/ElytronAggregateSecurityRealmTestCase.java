package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
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
import java.util.concurrent.TimeoutException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronAggregateSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String AGGREGATE_REALM = "aggregate-realm";
    private static final String AUTHENTICATION_REALM = "authentication-realm";
    private static final String AUTHORIZATION_REALM = "authorization-realm";
    private static final String IDENTITY = "identity";
    private static final String IDENTITY_REALM = "identity-realm";

    /**
     * @tpTestDetails Try to create Elytron Aggregate security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddAggregateSecurityRealm() throws Exception {
        final String authenticationRealmName = "authentication_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String authorizationRealmName = "authorization_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String aggregateRealmName = "aggregate_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address authenticationRealm = elyOps.getElytronAddress(IDENTITY_REALM, authenticationRealmName);
        final Address authorizationRealm = elyOps.getElytronAddress(IDENTITY_REALM, authorizationRealmName);
        final Address aggregateRealm = elyOps.getElytronAddress(AGGREGATE_REALM, aggregateRealmName);

        try {
            createIdentityRealmInModel(authenticationRealm);
            createIdentityRealmInModel(authorizationRealm);
            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .addResource(AddAggregateSecurityRealmWizard.class)
                    .name(aggregateRealmName)
                    .authenticationRealm(authenticationRealmName)
                    .authorizationRealm(authorizationRealmName)
                    .saveWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(aggregateRealmName));
            new ResourceVerifier(aggregateRealm, client)
                    .verifyExists()
                    .verifyAttribute(AUTHENTICATION_REALM, authenticationRealmName)
                    .verifyAttribute(AUTHORIZATION_REALM, authorizationRealmName);
        } finally {
            ops.removeIfExists(aggregateRealm);
            ops.removeIfExists(authenticationRealm);
            ops.removeIfExists(authorizationRealm);
            adminOps.reloadIfRequired();
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
        final String authenticationRealmName = "authentication_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String authorizationRealmName = "authorization_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String aggregateRealmName = "aggregate_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address authenticationRealm = elyOps.getElytronAddress(IDENTITY_REALM, authenticationRealmName);
        final Address authorizationRealm = elyOps.getElytronAddress(IDENTITY_REALM, authorizationRealmName);
        final Address aggregateRealm = elyOps.getElytronAddress(AGGREGATE_REALM, aggregateRealmName);

        try {
            createIdentityRealmInModel(authenticationRealm);
            createIdentityRealmInModel(authorizationRealm);
            createAggregateRealmInModel(aggregateRealm, authorizationRealmName, authenticationRealmName);

            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .removeResource(aggregateRealmName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse("Resource should NOT be present in table!",
                    page.getResourceManager().isResourcePresent(aggregateRealmName));

            new ResourceVerifier(aggregateRealm, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregateRealm);
            ops.removeIfExists(authenticationRealm);
            ops.removeIfExists(authorizationRealm);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate security realm instance in model and try to edit its authorization-realm
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAuthorizationRealm() throws Exception {
        final String authenticationRealmName = "authentication_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String authorizationRealmName2 = "authorization_realm_2_" + RandomStringUtils.randomAlphanumeric(7);
        final String authorizationRealmName = "authorization_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String aggregateRealmName = "aggregate_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address authenticationRealmAddress = elyOps.getElytronAddress(IDENTITY_REALM, authenticationRealmName);
        final Address authorizationRealmAddress = elyOps.getElytronAddress(IDENTITY_REALM, authorizationRealmName);
        final Address authorizationRealmAddress2 = elyOps.getElytronAddress(IDENTITY_REALM, authorizationRealmName2);
        final Address aggregateRealm = elyOps.getElytronAddress(AGGREGATE_REALM, aggregateRealmName);
        try {
            createIdentityRealmInModel(authenticationRealmAddress);
            createIdentityRealmInModel(authorizationRealmAddress);
            createIdentityRealmInModel(authorizationRealmAddress2);
            createAggregateRealmInModel(aggregateRealm, authorizationRealmName, authenticationRealmName);
            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .selectByName(aggregateRealmName);

            new ConfigChecker.Builder(client, aggregateRealm)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_REALM, authorizationRealmName2)
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_REALM, authorizationRealmName2);

        } finally {
            ops.removeIfExists(aggregateRealm);
            ops.removeIfExists(authorizationRealmAddress2);
            ops.removeIfExists(authenticationRealmAddress);
            ops.removeIfExists(authorizationRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate security realm instance in model and try to edit its authentication-realm
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAuthenticationRealm() throws Exception {
        final String authenticationRealmName = "authentication_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String authenticationRealmName2 = "authentication_realm_2_" + RandomStringUtils.randomAlphanumeric(7);
        final String authorizationRealmName = "authorization_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String aggregateRealmName = "aggregate_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address authenticationRealmAddress = elyOps.getElytronAddress(IDENTITY_REALM, authenticationRealmName);
        final Address authenticationRealmAddress2 = elyOps.getElytronAddress(IDENTITY_REALM, authenticationRealmName2);
        final Address authorizationRealmAddress = elyOps.getElytronAddress(IDENTITY_REALM, authorizationRealmName);
        final Address aggregateRealmAddress = elyOps.getElytronAddress(AGGREGATE_REALM, aggregateRealmName);

        try {
            createIdentityRealmInModel(authenticationRealmAddress);
            createIdentityRealmInModel(authenticationRealmAddress2);
            createIdentityRealmInModel(authorizationRealmAddress);
            createAggregateRealmInModel(aggregateRealmAddress, authorizationRealmName, authenticationRealmName);

            page.navigate();
            page.switchToAggregateRealms()
                    .getResourceManager()
                    .selectByName(aggregateRealmName);
            new ConfigChecker.Builder(client, aggregateRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_REALM, authenticationRealmName2)
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_REALM, authenticationRealmName2);

        } finally {
            ops.removeIfExists(aggregateRealmAddress);
            ops.removeIfExists(authenticationRealmAddress2);
            ops.removeIfExists(authenticationRealmAddress);
            ops.removeIfExists(authorizationRealmAddress);
            adminOps.reloadIfRequired();
        }
    }


    private void createAggregateRealmInModel(Address aggregateRealmAddress, String authorizationRealm, String authenticationRealm) throws IOException, TimeoutException, InterruptedException {
        ops.add(aggregateRealmAddress, Values.of(AUTHENTICATION_REALM, authenticationRealm)
                .and(AUTHORIZATION_REALM, authorizationRealm)).assertSuccess();
        adminOps.reloadIfRequired();
    }

    private void createIdentityRealmInModel(Address identityRealmAddress) throws IOException, TimeoutException, InterruptedException {
        ops.add(identityRealmAddress, Values.of(IDENTITY, RandomStringUtils.randomAlphanumeric(7)));
        adminOps.reloadIfRequired();
    }
}
