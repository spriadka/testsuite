package org.jboss.hal.testsuite.test.configuration.elytron.other;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.category.KnownIssue;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.other.AddElytronPolicyWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronOtherOtherPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ModuleUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.*;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronPolicyTestCase extends AbstractElytronTestCase {

    private static final String JACC_POLICY = "jacc-policy";
    private static final String CUSTOM_POLICY = "custom-policy";
    private static final String CONFIGURATION_FACTORY = "configuration-factory";
    private static final String CLASS_NAME = "class-name";
    private static final String MODULE = "module";
    private static final String POLICY = "policy";
    private static final String POLICY_LABEL = "Policy";

    private static final SnapshotBackup snapshotBackup = new SnapshotBackup();
    private static ModuleUtils moduleUtils;
    private static final Path POLICY_MODULE_PATH = Paths.get("test", "configuration", "elytron"
            , "policy" + RandomStringUtils.randomAlphanumeric(7));
    private static final String POLICY_MODULE_ARCHIVE_NAME = "elytron.policy.custom.policy.jar";
    private static String policyModuleName;

    @Page
    private ElytronOtherOtherPage page;

    @BeforeClass
    public static void setUp() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        moduleUtils = new ModuleUtils(client);
        client.apply(snapshotBackup.backup());
        disableJACCInLegacySecurity();
        policyModuleName = moduleUtils.createModule(POLICY_MODULE_PATH, createJarModule());
        adminOps.reloadIfRequired();
    }

    private static void disableJACCInLegacySecurity() throws IOException {
        ops.writeAttribute(Address.subsystem("security"), "initialize-jacc", false);
    }

    private static JavaArchive createJarModule() {
        return ShrinkWrap.create(JavaArchive.class, POLICY_MODULE_ARCHIVE_NAME)
                .addClass(CustomPolicyProvider.class)
                .addClass(CustomPolicyConfigurationProvider.class);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, CommandFailedException {
        try {
            moduleUtils.removeModule(POLICY_MODULE_PATH);
            client.apply(snapshotBackup.restore());
        } finally {
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create custom Elytron Policy instance in Web Console.
     * Validate the resource is visible in Elytron Policy's policy table.
     * Validate created resource against model.
     */
    @Test
    public void testAddCustomPolicy() throws Exception {
        final String customPolicyName = randomAlphabetic(7);
        final String className = CustomPolicyProvider.class.getCanonicalName();
        final Address policyAddress = elyOps.getElytronAddress(POLICY, customPolicyName);
        try {
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            page.getResourceManager()
                    .addResource(AddElytronPolicyWizard.class)
                    .customPolicy()
                    .name(customPolicyName)
                    .className(className)
                    .module(policyModuleName)
                    .nextAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue("Created custom policy should be present in the policy table",
                    page.getResourceManager().isResourcePresent(customPolicyName));

            new ResourceVerifier(policyAddress, client)
                    .verifyExists()
                    .verifyAttribute(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(CLASS_NAME, className)
                            .addProperty(MODULE, policyModuleName)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();

        }
    }

    /**
     * @tpTestDetails Create custom Elytron Policy instance in model. Then try to remove it in Web Console's configuration.
     * Validate the resource is not visible in Elytron's policy table anymore.
     * Validate created resource is not present in the model.
     */
    @Test
    public void testRemoveCustomPolicy() throws Exception {
        final String customPolicyName = randomAlphabetic(7);
        final Address policyAddress = elyOps.getElytronAddress(POLICY, customPolicyName);
        try {
            createCustomPolicyInModel(policyAddress);
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            page.getResourceManager()
                    .removeResource(customPolicyName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Newly removed custom policy should not be present in the policy table anymore",
                    page.getResourceManager().isResourcePresent(customPolicyName));

            new ResourceVerifier(policyAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create custom Elytron Policy in model. Then try to edit it in Web Console's configuration
     * Validate edited attributes changed in model
     */
    @Test
    public void testEditCustomPolicy() throws Exception {
        final String policyName = RandomStringUtils.randomAlphabetic(7);
        final String className = AllPermissionPolicyProvider.class.getCanonicalName();
        final Path editingModulePath = Paths.get("test", "elytron",
                "other", "policy_to_be_edited_" + RandomStringUtils.randomAlphanumeric(7));
        String editingModuleName;
        String editingModuleArchive = String.format("edit_policy_module_%s.jar", RandomStringUtils.randomAlphabetic(7));
        final Address policyAddress = elyOps.getElytronAddress(POLICY, policyName);
        try {
            createCustomPolicyInModel(policyAddress);
            editingModuleName = moduleUtils.createModule(editingModulePath, ShrinkWrap.create(JavaArchive.class, editingModuleArchive)
                    .addClass(AllPermissionPolicyProvider.class));
            adminOps.reloadIfRequired();
            page.navigateToApplication().switchSubTab(POLICY_LABEL);
            page.getResourceManager().selectByName(policyName);
            ConfigFragment editFragment = page.getConfig().switchTo(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, policyAddress)
                    .configFragment(editFragment)
                    .edit(ConfigChecker.InputType.TEXT, MODULE, editingModuleName)
                    .edit(ConfigChecker.InputType.TEXT, CLASS_NAME, className)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(CLASS_NAME, className)
                            .addProperty(MODULE, editingModuleName)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create JACC Elytron Policy instance in Web Console's configuration, filling only required fields
     * Validate the resource is visible in Elytron's policy table.
     * Validate created resource is present in the model.
     */
    @Test
    public void testAddJACCPolicyRequiredFieldOnly() throws Exception {
        final String jaccPolicyName = randomAlphabetic(7);
        final Address policyAddress = elyOps.getElytronAddress(POLICY, jaccPolicyName);
        try {
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            page.getResourceManager()
                    .addResource(AddElytronPolicyWizard.class)
                    .jaccPolicy()
                    .name(jaccPolicyName)
                    .finishAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created JACC Policy should be present in the policy table", page.getResourceManager()
                    .isResourcePresent(jaccPolicyName));
            new ResourceVerifier(policyAddress, client)
                    .verifyExists()
                    .verifyAttribute(JACC_POLICY, new ModelNode().addEmptyObject());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create JACC Policy instance in model filling required and optional fields.
     * Validate the resource is visible in Elytron's policy table.
     * Validate created resource is present in the model.
     */
    @Test
    public void testAddJACCPolicyWithOptionalFields() throws Exception {
        final String name = randomAlphabetic(7);
        final String policy = CustomPolicyProvider.class.getCanonicalName();
        final String configurationFactory = CustomPolicyConfigurationProvider.class.getCanonicalName();
        final String module = policyModuleName;
        final Address policyAddress = elyOps.getElytronAddress(POLICY, name);
        try {
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            page.getResourceManager()
                    .addResource(AddElytronPolicyWizard.class)
                    .jaccPolicy()
                    .name(name)
                    .policy(policy)
                    .module(policyModuleName)
                    .configurationFactory(configurationFactory)
                    .finishAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created JACC policy should be present in the table",
                    page.getResourceManager().isResourcePresent(name));

            new ResourceVerifier(policyAddress, client)
                    .verifyAttribute(JACC_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(CONFIGURATION_FACTORY, configurationFactory)
                            .addProperty(POLICY, policy)
                            .addProperty(MODULE, module)
                            .build());
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create JACC Policy instance in model. Try to edit it's attributes in Web Console
     * Validate edited attributes change in model
     */
    @Test
    public void testEditJACCPolicy() throws Exception {
        final String jaccPolicyName = randomAlphabetic(7);
        final String policy = CustomPolicyProvider.class.getCanonicalName();
        final String configurationFactory = CustomPolicyConfigurationProvider.class.getCanonicalName();
        final Address policyAddress = elyOps.getElytronAddress(POLICY, jaccPolicyName);
        try {
            createJACCPolicyInModel(policyAddress);
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            page.getResourceManager().selectByName(jaccPolicyName);
            ConfigFragment fragment = page.getConfig().switchTo(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, policyAddress)
                    .configFragment(fragment)
                    .edit(ConfigChecker.InputType.TEXT, MODULE, policyModuleName)
                    .edit(ConfigChecker.InputType.TEXT, POLICY, policy)
                    .edit(ConfigChecker.InputType.TEXT, CONFIGURATION_FACTORY, configurationFactory)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JACC_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(CONFIGURATION_FACTORY, configurationFactory)
                            .addProperty(POLICY, policy)
                            .addProperty(MODULE, policyModuleName).build());

        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    @Category(KnownIssue.class)
    public void testEditJACCPolicySingleAttribute() throws Exception {
        final String jaccPolicyName = randomAlphabetic(7);
        final String defaultModuleName = policyModuleName;
        final String defaultPolicyName = CustomPolicyProvider.class.getCanonicalName();
        final String defaultConfigurationFactory = CustomPolicyConfigurationProvider.class.getCanonicalName();
        final String configurationFactory = "bububu";
        final Address policyAddress = elyOps.getElytronAddress(POLICY, jaccPolicyName);
        try {
            createJACCPolicyInModel(policyAddress);
            ops.writeAttribute(policyAddress, JACC_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(MODULE, defaultModuleName)
                    .addProperty(POLICY, defaultPolicyName)
                    .addProperty(CONFIGURATION_FACTORY, defaultConfigurationFactory)
            .build()).assertSuccess();
            adminOps.reloadIfRequired();
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            page.getResourceManager().selectByName(jaccPolicyName);
            ConfigFragment fragment = page.getConfig().switchTo(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, policyAddress)
                    .configFragment(fragment)
                    .edit(ConfigChecker.InputType.TEXT, CONFIGURATION_FACTORY, configurationFactory)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JACC_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(CONFIGURATION_FACTORY, configurationFactory)
                            .addProperty(POLICY, defaultPolicyName)
                            .addProperty(MODULE, defaultModuleName).build(),
                            "Fails because of: https://issues.jboss.org/browse/HAL-1385");

        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Policy instance in model.Then try to remove it in Web Console's configuration.
     * Validate the resource is not visible in Elytron's policy table anymore.
     * Validate created resource is not present in the model.
     */
    @Test
    public void testRemoveJACCPolicy() throws Exception {
        final String jaccPolicyName = randomAlphabetic(7);
        final Address policyAddress = elyOps.getElytronAddress(POLICY, jaccPolicyName);

        try {
            createJACCPolicyInModel(policyAddress);
            page.navigateToApplication()
                    .switchSubTab(POLICY_LABEL);
            page.getResourceManager()
                    .removeResource(jaccPolicyName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Newly removed JACC policy should not be present in policy table anymore"
                    , page.getResourceManager().isResourcePresent(jaccPolicyName));
            new ResourceVerifier(policyAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(policyAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createJACCPolicyInModel(Address policyAddress) throws IOException, TimeoutException, InterruptedException {
        ops.add(policyAddress, Values.of(JACC_POLICY, new ModelNode().addEmptyObject())).assertSuccess();
        adminOps.reloadIfRequired();
    }

    private void createCustomPolicyInModel(Address address) throws IOException, TimeoutException, InterruptedException {
        ops.add(address, Values.of(CUSTOM_POLICY, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLASS_NAME, CustomPolicyProvider.class.getCanonicalName())
                .addProperty(MODULE, policyModuleName)
                .build()))
                .assertSuccess();
        adminOps.reloadIfRequired();
    }

}
