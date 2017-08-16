package org.jboss.hal.testsuite.test.configuration.elytron;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronOtherOtherPage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronPolicyTestCase extends AbstractElytronTestCase {

    private static final String
            JACC_POLICY = "jacc-policy",
            CUSTOM_POLICY = "custom-policy",
            CUSTOM_POLICY_LABEL = "Custom Policy",
            JACC_POLICY_LABEL = "JACC Policy",
            CONFIGURATION_FACTORY = "configuration-factory",
            CLASS_NAME = "class-name",
            MODULE = "module",
            NAME = "name",
            DEFAULT_POLICY = "default-policy",
            JACC = "jacc",
            POLICY = "policy",
            POLICY_LABEL = "Policy",
            LIST_ADD = "list-add";

    private static final SnapshotBackup snapshotBackup = new SnapshotBackup();

    @Page
    private ElytronOtherOtherPage page;

    @BeforeClass
    public static void setUp() throws CommandFailedException {
        client.apply(snapshotBackup.backup());
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, CommandFailedException {
        try {
            client.apply(snapshotBackup.restore());
        } finally {
            adminOps.reloadIfRequired();
        }
    }


    /**
     * @tpTestDetails Try to create Policy instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Policy table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddPolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        try {
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(policyAddress.getLastPairValue())
                    .text(DEFAULT_POLICY, JACC)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue("Policy should be present in the table",
                    page.getResourceManager().isResourcePresent(policyAddress.getLastPairValue()));

            new ResourceVerifier(policyAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Policy instance in Web Console's configuration without required name field
     * Validate that a validation message is shown in form
     */
    @Test
    public void testAddPolicyWithoutNameShowsError() throws Exception {
        page.navigateToApplication().switchSubTab(POLICY_LABEL);
        page.getResourceManager()
                .addResource(AddResourceWizard.class)
                .saveAndDismissReloadRequiredWindowWithState().assertWindowOpen();
        assertTrue("An validation error regarding missing policy name should be visible"
                , page.getWindowFragment().isErrorShownInForm());
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to create Elytron Policy instance with same name in Web Console's configuration
     * Validate that a validation message in alert area is shown
     */
    @Test
    public void testAddPolicyDuplicateShowsError() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        try {
            createPolicy(policyAddress);
            page.navigateToApplication().switchSubTab(POLICY_LABEL);
            page.getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(JACC)
                    .saveAndDismissReloadRequiredWindowWithState().assertWindowClosed();
            assertNotNull("There should be warning regarding duplicate resource", page.getAlertArea());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }


    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to remove it in Web Console's Elytron subsystem
     * configuration.
     * Validate the resource is not any more visible in Elytron Policy table.
     * Validate removed resource is not any more present in the model.
     */
    @Test
    public void testRemovePolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        try {
            createPolicy(policyAddress);

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getResourceManager()
                    .removeResource(policyAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            assertFalse(page.getResourceManager().isResourcePresent(policyAddress.getLastPairValue()));

            new ResourceVerifier(policyAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to add its custom-policy child in Web Console's
     * configuration, filling only required fields
     * Validate the resource is visible in Elytron Policy's custom-policy table.
     * Validate created resource is present in the model.
     */
    @Test
    public void testAddCustomPolicyRequiredFieldOnly() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String customPolicyName = randomAlphabetic(7),
                classNameValue = randomAlphabetic(7);
        try {
            createPolicy(policyAddress);

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(customPolicyName)
                    .text(CLASS_NAME, classNameValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue(page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(customPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeContainsValue(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(NAME, customPolicyName)
                            .addProperty(CLASS_NAME, classNameValue)
                            .addUndefinedProperty(MODULE)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();

        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to add its custom-policy child in Web Console's
     * configuration, filling required and optional fields.
     * Validate the resource is visible in Elytron Policy's custom-policy table.
     * Validate created resource is present in the model.
     */
    @Test
    public void testAddCustomPolicyWithOptionalFields() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String customPolicyName = randomAlphabetic(7),
                classNameValue = randomAlphabetic(7),
                moduleValue = "my_module_" + RandomStringUtils.randomAlphanumeric(7);
        try {
            createPolicy(policyAddress);
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            AddResourceWizard wizard = page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .addResource(AddResourceWizard.class);
            wizard
                    .name(customPolicyName)
                    .text(CLASS_NAME, classNameValue);
            wizard.openOptionalFieldsTab();
            wizard.text(MODULE, moduleValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue(page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(customPolicyName));
            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeContainsValue(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(NAME, customPolicyName)
                            .addProperty(CLASS_NAME, classNameValue)
                            .addProperty(MODULE, moduleValue)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();

        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to add its custom-policy child in Web Console without filling required fields
     * Validate that a validation message is shown in form
     */
    @Test
    public void testAddCustomPolicyWithoutRequiredFieldsShowsError() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String customPolicyName = randomAlphabetic(7),
                classNameValue = randomAlphabetic(7);
        try {
            createPolicy(policyAddress);
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            AddResourceWizard wizard = page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .addResource(AddResourceWizard.class);
            wizard.saveAndDismissReloadRequiredWindowWithState().assertWindowOpen();
            assertTrue("Validation errors regarding missing policy name and class should be visible",
                    page.getWindowFragment().isErrorShownInForm());
            wizard
                    .name(customPolicyName)
                    .saveAndDismissReloadRequiredWindowWithState().assertWindowOpen();
            assertTrue("Validation error regarding missing policy class should be visible", page.getWindowFragment().isErrorShownInForm());
            wizard.getEditor().getText(NAME).clear();
            wizard
                    .text(CLASS_NAME, classNameValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowOpen();
            assertTrue("Validation error regarding missing policy name should be visible", page.getWindowFragment().isErrorShownInForm());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and then add its custom-policy child in model. Then try to
     * remove it in Web Console's configuration.
     * Validate the resource is not visible in Elytron Policy's custom-policy table anymore.
     * Validate created resource is not present in the model.
     */
    @Test
    public void testRemoveCustomPolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String customPolicyName = randomAlphabetic(7),
                classNameValue = randomAlphabetic(7);

        final ModelNode customPolicyModelNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, customPolicyName)
                .addProperty(CLASS_NAME, classNameValue)
                .addUndefinedProperty(MODULE)
                .build();
        try {
            createPolicy(policyAddress);
            ops.invoke(LIST_ADD, policyAddress, Values.empty()
                    .and(NAME, CUSTOM_POLICY)
                    .and("value", customPolicyModelNode)).assertSuccess();
            adminOps.reloadIfRequired();

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .removeResource(customPolicyName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            assertFalse(page.getConfig()
                    .switchTo(CUSTOM_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(customPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeDoesNotContainValue(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(NAME, customPolicyName)
                            .addProperty(CLASS_NAME, classNameValue)
                            .addUndefinedProperty(MODULE)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to add its JACC-policy child in Web Console's
     * configuration, filling only required fields
     * Validate the resource is visible in Elytron Policy's JACC-policy table.
     * Validate created resource is present in the model.
     */
    @Test
    public void testAddJACCPolicyRequiredFieldOnly() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String jaccPolicyName = randomAlphabetic(7);
        try {
            createPolicy(policyAddress);

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getConfig()
                    .switchTo(JACC_POLICY_LABEL)
                    .getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(jaccPolicyName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue(page.getConfig()
                    .switchTo(JACC_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(jaccPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeContainsValue(JACC_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(NAME, jaccPolicyName)
                            .addUndefinedProperty(POLICY)
                            .addUndefinedProperty(CONFIGURATION_FACTORY)
                            .addUndefinedProperty(MODULE)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and try to add its JACC-policy child in Web Console's
     * configuration, filling required and optional fields.
     * Validate the resource is visible in Elytron Policy's JACC-policy table.
     * Validate created resource is present in the model.
     */
    @Test
    public void testAddJACCPolicyWithOptionalFields() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String jaccPolicyName = randomAlphabetic(7);
        final String jaccPolicyPolicy = "policy_" + RandomStringUtils.randomAlphanumeric(7);
        final String jaccPolicyConfigurationFactory = "configuration_factory_" + RandomStringUtils.randomAlphanumeric(7);
        final String jaccPolicyModule = "module_" + RandomStringUtils.randomAlphanumeric(7);
        try {
            createPolicy(policyAddress);

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getConfig()
                    .switchTo(JACC_POLICY_LABEL)
                    .getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(jaccPolicyName)
                    .text(POLICY, jaccPolicyPolicy)
                    .text(CONFIGURATION_FACTORY, jaccPolicyConfigurationFactory)
                    .text(MODULE, jaccPolicyModule)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue(page.getConfig()
                    .switchTo(JACC_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(jaccPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeContainsValue(JACC_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(NAME, jaccPolicyName)
                            .addProperty(POLICY, jaccPolicyPolicy)
                            .addProperty(CONFIGURATION_FACTORY, jaccPolicyConfigurationFactory)
                            .addProperty(MODULE, jaccPolicyModule)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model and then add its JACC-policy child in model. Then try to
     * remove it in Web Console's configuration.
     * Validate the resource is not visible in Elytron Policy's JACC-policy table anymore.
     * Validate created resource is not present in the model.
     */
    @Test
    public void testRemoveJACCPolicy() throws Exception {
        final Address policyAddress = elyOps.getElytronAddress(POLICY, JACC);
        final String jaccPolicyName = randomAlphabetic(7);

        final ModelNode customPolicyModelNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, jaccPolicyName)
                .addUndefinedProperty(POLICY)
                .addUndefinedProperty(CONFIGURATION_FACTORY)
                .addUndefinedProperty(MODULE)
                .build();
        try {
            createPolicy(policyAddress);
            ops.invoke(LIST_ADD, policyAddress, Values.empty()
                    .and(NAME, JACC_POLICY)
                    .and("value", customPolicyModelNode)).assertSuccess();
            adminOps.reloadIfRequired();

            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);

            page.getConfig()
                    .switchTo(JACC_POLICY_LABEL)
                    .getResourceManager()
                    .removeResource(jaccPolicyName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            assertFalse(page.getConfig()
                    .switchTo(JACC_POLICY_LABEL)
                    .getResourceManager()
                    .isResourcePresent(jaccPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyListAttributeDoesNotContainValue(JACC_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(NAME, jaccPolicyName)
                            .addUndefinedProperty(MODULE)
                            .addUndefinedProperty(POLICY)
                            .addUndefinedProperty(CONFIGURATION_FACTORY)
                            .addUndefinedProperty(MODULE)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createPolicy(Address address) throws IOException, TimeoutException, InterruptedException {
        ops.add(address, Values.of(JACC_POLICY, new ModelNodeGenerator.ModelNodeListBuilder(
                new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(NAME, JACC)
                        .build()
        ).build())).assertSuccess();
        adminOps.reloadIfRequired();
        //TODO: remove when JBEAP-12434 is fixed
        adminOps.restart();
    }

}
