package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronSecurityDomainTestCase extends AbstractElytronTestCase {

    private static final String
        SECURITY_DOMAIN = "security-domain",
        SECURITY_DOMAIN_LABEL = "Security Domain",
        REALM = "realm",
        REALMS = "realms",
        DEFAULT_REALM = "default-realm",
        REALM_NAME = "realm-name",
        LOCAL_REALM_NAME = "local",
        APPLICATION_REALM_NAME = "ApplicationRealm",
        APPLICATION_DOMAIN_NAME = "ApplicationDomain",
        PERMISSION_MAPPER = "permission-mapper",
        DEFAULT_PERMISSION_MAPPER_NAME = "default-permission-mapper",
        ROLE_MAPPER = "role-mapper",
        REALM_ROLE_MAPPER = "realm-role-mapper",
        SUPER_USER_MAPPER_NAME = "super-user-mapper",
        ROLE_DECODER = "role-decoder",
        GROUPS_TO_ROLES_ROLE_DECODER_NAME = "groups-to-roles",
        OUTFLOW_ANONYMOUS = "outflow-anonymous",
        OUTFLOW_SECURITY_DOMAINS = "outflow-security-domains",
        REALM_MAPPER = "realm-mapper",
        CONSTANT_REALM_MAPPER_NAME = "local",
        CONSTANT_ROLE_MAPPER_NAME = "super-user-mapper",
        REALMS_LABEL = "Realms",
        PRINCIPAL_TRANSFORMER = "principal-transformer";

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
        String securityDomainName = randomAlphanumeric(5);
        Address securityDomainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, securityDomainName);

        page.navigateToApplication().selectResource(SECURITY_DOMAIN_LABEL);

        try {
            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.text(NAME, securityDomainName);
            editor.text(REALM_NAME, LOCAL_REALM_NAME);
            wizard.openOptionalFieldsTab();
            wizard.maximizeWindow();
            editor.text(PERMISSION_MAPPER, DEFAULT_PERMISSION_MAPPER_NAME);
            editor.text(REALM_ROLE_MAPPER, SUPER_USER_MAPPER_NAME);

            assertTrue("Dialog should be closed!", wizard.finish());
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
        Address securityDomainAddress = createSecurityDomain();
        String securityDomainName = securityDomainAddress.getLastPairValue();

        try {
            page.navigateToApplication().selectResource(SECURITY_DOMAIN_LABEL).getResourceManager()
                    .removeResource(securityDomainName).confirmAndDismissReloadRequiredMessage().assertClosed();
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
        Address securityDomainAddress = createSecurityDomain();
        String securityDomainName = securityDomainAddress.getLastPairValue();

        try {
            page.navigateToApplication().selectResource(SECURITY_DOMAIN_LABEL).getResourceManager()
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
        Address
            securityDomainAddress = createSecurityDomain(),
            newRealmAddress = new UndertowElytronOperations(client).createSecurityRealm();
        String
            securityDomainName = securityDomainAddress.getLastPairValue(),
            newRealmName = newRealmAddress.getLastPairValue();

        try {
            page.navigateToApplication().selectResource(SECURITY_DOMAIN_LABEL).getResourceManager()
                    .selectByName(securityDomainName);
            page.switchToConfigAreaTab(REALMS_LABEL);

            WizardWindowWithOptionalFields wizard = page.getConfigAreaResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.text(REALM, newRealmName);
            wizard.openOptionalFieldsTab();
            wizard.maximizeWindow();
            editor.text(ROLE_DECODER, GROUPS_TO_ROLES_ROLE_DECODER_NAME);
            editor.text(ROLE_MAPPER, SUPER_USER_MAPPER_NAME);

            assertTrue("Dialog should be closed!", wizard.finishAndDismissReloadRequiredWindow());
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
        Address securityDomainAddress = createSecurityDomain();
        String securityDomainName = securityDomainAddress.getLastPairValue();

        try {
            page.navigateToApplication().selectResource(SECURITY_DOMAIN_LABEL).getResourceManager()
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
    private Address createSecurityDomain() throws IOException {
        Address securityDomainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, randomAlphanumeric(5));
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
        return securityDomainAddress;
    }

}
