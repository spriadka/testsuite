package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddRealmWizard;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddSecurityDomainWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class SecurityDomainTestCase extends AbstractElytronTestCase {

    private static final String SECURITY_DOMAIN = "security-domain";
    private static final String SECURITY_DOMAIN_LABEL = "Security Domain";
    private static final String REALM = "realm";
    private static final String REALMS = "realms";
    private static final String DEFAULT_REALM = "default-realm";
    private static final String LOCAL_REALM_NAME = "local";
    private static final String APPLICATION_REALM_NAME = "ApplicationRealm";
    private static final String APPLICATION_DOMAIN_NAME = "ApplicationDomain";
    private static final String PERMISSION_MAPPER = "permission-mapper";
    private static final String DEFAULT_PERMISSION_MAPPER_NAME = "default-permission-mapper";
    private static final String ROLE_MAPPER = "role-mapper";
    private static final String SUPER_USER_MAPPER_NAME = "super-user-mapper";
    private static final String ROLE_DECODER = "role-decoder";
    private static final String GROUPS_TO_ROLES_ROLE_DECODER_NAME = "groups-to-roles";
    private static final String OUTFLOW_ANONYMOUS = "outflow-anonymous";
    private static final String OUTFLOW_SECURITY_DOMAINS = "outflow-security-domains";
    private static final String REALM_MAPPER = "realm-mapper";
    private static final String CONSTANT_REALM_MAPPER_NAME = "local";
    private static final String CONSTANT_ROLE_MAPPER_NAME = "super-user-mapper";
    private static final String REALMS_LABEL = "Realms";
    private static final String PRINCIPAL_TRANSFORMER = "principal-transformer";

    @Page
    private SSLPage page;

    /**
     * @tpTestDetails Try to create Elytron Security Domain instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Security Domain table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addSecurityDomainTest() throws Exception {
        final String securityDomainName = "security_domain_"  + randomAlphanumeric(5);
        final Address securityDomainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, securityDomainName);
        try {
            page.navigateToApplication()
                    .selectResource(SECURITY_DOMAIN_LABEL)
                    .getResourceManager()
                    .addResource(AddSecurityDomainWizard.class)
                    .name(securityDomainName)
                    .realmName(LOCAL_REALM_NAME)
                    .permissionMapper(DEFAULT_PERMISSION_MAPPER_NAME)
                    .realmRoleMapper(SUPER_USER_MAPPER_NAME)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table! See https://issues.jboss.org/browse/HAL-1326",
                    page.resourceIsPresentInMainTable(securityDomainName));
            new ResourceVerifier(securityDomainAddress, client).verifyExists()
                    .verifyAttribute(DEFAULT_REALM, LOCAL_REALM_NAME)
                    .verifyAttribute(PERMISSION_MAPPER, DEFAULT_PERMISSION_MAPPER_NAME)
                    .verifyAttribute(REALMS, new ModelNodeListBuilder().addNode(new ModelNodePropertiesBuilder()
                                    .addProperty(REALM, LOCAL_REALM_NAME)
                                    .addUndefinedProperty(PRINCIPAL_TRANSFORMER)
                                    .addUndefinedProperty(ROLE_DECODER)
                                    .addProperty(ROLE_MAPPER, SUPER_USER_MAPPER_NAME)
                                    .build()).build());
        } finally {
            ops.removeIfExists(securityDomainAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Security Domain instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Security Domain table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeSecurityDomainTest() throws Exception {
        final String securityDomainName = "security_domain_" + RandomStringUtils.randomAlphanumeric(7);
        final Address securityDomainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, securityDomainName);
        try {
            createSecurityDomain(securityDomainAddress);
            page.navigateToApplication()
                    .selectResource(SECURITY_DOMAIN_LABEL)
                    .getResourceManager()
                    .removeResource(securityDomainName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(securityDomainName));
            new ResourceVerifier(securityDomainAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(securityDomainAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Security Domain instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editSecurityDomainAttributesTest() throws Exception {
        final String securityDomainName = "security_domain_" + RandomStringUtils.randomAlphanumeric(7);
        final Address securityDomainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, securityDomainName);
        try {
            createSecurityDomain(securityDomainAddress);
            page.navigateToApplication().selectResource(SECURITY_DOMAIN_LABEL)
                    .getResourceManager()
                    .selectByName(securityDomainName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, securityDomainAddress).configFragment(page.getConfigFragment())
                    .edit(CHECKBOX, OUTFLOW_ANONYMOUS, true)
                    .edit(TEXT, OUTFLOW_SECURITY_DOMAINS, APPLICATION_DOMAIN_NAME)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(OUTFLOW_ANONYMOUS, true)
                    .verifyAttribute(OUTFLOW_SECURITY_DOMAINS,
                            new ModelNodeListBuilder(new ModelNode(APPLICATION_DOMAIN_NAME)).build());
            new ConfigChecker.Builder(client, securityDomainAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, DEFAULT_REALM, APPLICATION_REALM_NAME)
                    .edit(TEXT, REALM_MAPPER, CONSTANT_REALM_MAPPER_NAME)
                    .edit(TEXT, ROLE_MAPPER, CONSTANT_ROLE_MAPPER_NAME)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(DEFAULT_REALM, APPLICATION_REALM_NAME)
                    .verifyAttribute(REALM_MAPPER, CONSTANT_REALM_MAPPER_NAME)
                    .verifyAttribute(ROLE_MAPPER, CONSTANT_ROLE_MAPPER_NAME);
        } finally {
            ops.removeIfExists(securityDomainAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Security Domain instance in model
     * and try to add item to it's realms list in Web Console's Elytron subsystem configuration.
     * Validate created item is visible in Realms table.
     * Validate added item is present in the model.
     */
    @Test
    public void securityDomainAddRealmsTest() throws Exception {
        final String securityDomainName = "security_domain_" + RandomStringUtils.randomAlphanumeric(7);
        final String newRealmName = "security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address securityDomainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, securityDomainName);
        final Address newRealmAddress = new UndertowElytronOperations(client).createSecurityRealm(newRealmName);
        try {
            createSecurityDomain(securityDomainAddress);
            page.navigateToApplication().selectResource(SECURITY_DOMAIN_LABEL)
                    .getResourceManager()
                    .selectByName(securityDomainName);
            page.switchToConfigAreaTab(REALMS_LABEL);
            page.getConfigAreaResourceManager()
                    .addResource(AddRealmWizard.class)
                    .realm(newRealmName)
                    .roleDecoder(GROUPS_TO_ROLES_ROLE_DECODER_NAME)
                    .roleMapper(SUPER_USER_MAPPER_NAME)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table! See https://issues.jboss.org/browse/HAL-1326",
                    page.resourceIsPresentInConfigAreaTable(newRealmName));
            new ResourceVerifier(securityDomainAddress, client).verifyAttribute(REALMS, new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder()
                            .addProperty(REALM, LOCAL_REALM_NAME)
                            .addProperty(ROLE_MAPPER, SUPER_USER_MAPPER_NAME)
                    .build()).addNode(new ModelNodePropertiesBuilder()
                            .addProperty(REALM, APPLICATION_REALM_NAME)
                            .addProperty(ROLE_DECODER, GROUPS_TO_ROLES_ROLE_DECODER_NAME)
                    .build()).addNode(new ModelNodePropertiesBuilder()
                            .addProperty(REALM, newRealmName)
                            .addUndefinedProperty(PRINCIPAL_TRANSFORMER)
                            .addProperty(ROLE_DECODER, GROUPS_TO_ROLES_ROLE_DECODER_NAME)
                            .addProperty(ROLE_MAPPER, SUPER_USER_MAPPER_NAME)
                    .build()).build());
        } finally {
            ops.removeIfExists(securityDomainAddress);
            ops.removeIfExists(newRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Security Domain instance in model and try to remove default realm from it's realms
     * list in Web Console's Elytron subsystem configuration. Validate default realm was not removed from the Realms table.
     * Try to remove non-default realm and validate it is not any more present in Realms table.
     * Validate default realm is still present in the model and the removed non-default one is not any more present
     * in the model.
     */
    @Test
    public void securityDomainRemoveRealmsTest() throws Exception {
        final String securityDomainName = "security_domain_" + RandomStringUtils.randomAlphanumeric(7);
        final Address securityDomainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, securityDomainName);
        try {
            createSecurityDomain(securityDomainAddress);
            page.navigateToApplication()
                    .selectResource(SECURITY_DOMAIN_LABEL)
                    .getResourceManager()
                    .selectByName(securityDomainName);
            page.switchToConfigAreaTab(REALMS_LABEL);
            assertTrue("Default '" + LOCAL_REALM_NAME + "' realm should be still present in the table!",
                    page.resourceIsPresentInConfigAreaTable(LOCAL_REALM_NAME));
            assertTrue("'" + APPLICATION_REALM_NAME + "' should be still present in the table!",
                    page.resourceIsPresentInConfigAreaTable(LOCAL_REALM_NAME));
            page.getConfigAreaResourceManager().removeResource(LOCAL_REALM_NAME).confirm().assertClosed();
            assertTrue("Default '" + LOCAL_REALM_NAME + "' realm should not be able to be removed and thus should be "
                    + "still present in the table!", page.resourceIsPresentInConfigAreaTable(LOCAL_REALM_NAME));
            page.getConfigAreaResourceManager().removeResource(APPLICATION_REALM_NAME)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted '" + APPLICATION_REALM_NAME + "' should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(APPLICATION_REALM_NAME));

            new ResourceVerifier(securityDomainAddress, client).verifyAttribute(REALMS, new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder()
                            .addProperty(REALM, LOCAL_REALM_NAME)
                            .addProperty(ROLE_MAPPER, SUPER_USER_MAPPER_NAME)
                    .build()).build());
        } finally {
            ops.removeIfExists(securityDomainAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * Create security domain with random name
     */
    private void createSecurityDomain(Address securityDomainAddress) throws IOException, TimeoutException, InterruptedException {
        ModelNode realmsNode = new ModelNodeListBuilder()
                .addNode(new ModelNodePropertiesBuilder()
                        .addProperty(REALM, LOCAL_REALM_NAME)
                        .addProperty(ROLE_MAPPER, SUPER_USER_MAPPER_NAME)
                .build()).addNode(new ModelNodePropertiesBuilder()
                        .addProperty(REALM, APPLICATION_REALM_NAME)
                        .addProperty(ROLE_DECODER, GROUPS_TO_ROLES_ROLE_DECODER_NAME)
                .build()).build();
        ops.add(securityDomainAddress, Values
                .of(DEFAULT_REALM, LOCAL_REALM_NAME)
                .and(PERMISSION_MAPPER, DEFAULT_PERMISSION_MAPPER_NAME)
                .and(REALMS, realmsNode)).assertSuccess();
        adminOps.reloadIfRequired();
    }

}
