package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddCustomCredentialSecurityWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddCustomCredentialSecurityValidator;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ModuleUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class CustomCredentialSecurityTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL = "Custom Credential Security";
    private static final String CUSTOM_CREDENTIAL_SECURITY_FACTORY = "custom-credential-security-factory";

    private static final Path CUSTOM_CREDENTIAL_SECURITY_FACTORY_PATH = Paths.get("test", "elytron",
            "credential", "security", "factory_" + randomAlphanumeric(5));

    private static final String ARCHIVE_NAME = "elytron.customer.credential.security.factory.jar";

    private static String customCredentialSecurityFactoryModuleName;

    private static ModuleUtils moduleUtils;

    @BeforeClass
    public static void beforeClass() throws IOException {
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_2);
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(BubuCustomCredentialFactory.class, ChachaCustomCredentialFactory.class);
        customCredentialSecurityFactoryModuleName = moduleUtils.createModule(CUSTOM_CREDENTIAL_SECURITY_FACTORY_PATH,
                jar, "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_2);
        moduleUtils.removeModule(CUSTOM_CREDENTIAL_SECURITY_FACTORY_PATH);
        adminOps.reloadIfRequired();
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Credential Security Factory instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom Credential Security Factory table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addCustomCredentialSecurityTest() throws Exception {
        String customCredentialSecurityFactoryName = randomAlphanumeric(5);
        Address customCredentialSecurityFactoryAddress =
                elyOps.getElytronAddress(CUSTOM_CREDENTIAL_SECURITY_FACTORY, customCredentialSecurityFactoryName);

        try {
            page.navigateToApplication()
                    .selectResource(CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL)
                    .getResourceManager()
                    .addResource(AddCustomCredentialSecurityWizard.class)
                    .name(customCredentialSecurityFactoryName)
                    .className(BubuCustomCredentialFactory.class.getName())
                    .module(customCredentialSecurityFactoryModuleName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(customCredentialSecurityFactoryName));
            new ResourceVerifier(customCredentialSecurityFactoryAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, BubuCustomCredentialFactory.class.getName())
                    .verifyAttribute(MODULE, customCredentialSecurityFactoryModuleName);
        } finally {
            ops.removeIfExists(customCredentialSecurityFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Credential Security Factory instance in Web Console's Elytron subsystem
     * configuration without specifying required fields
     * Validate that an validation error is shown for each possible invalid combination
     */
    @Test
    public void addCustomCredentialSecurityMissingAttributesTest() {
        AddCustomCredentialSecurityWizard wizard = page.navigateToApplication()
                .selectResource(CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL)
                .getResourceManager()
                .addResource(AddCustomCredentialSecurityWizard.class);
        new AddCustomCredentialSecurityValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);
    }

    /**
     * @tpTestDetails Create Elytron Custom Credential Security Factory instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAttributesTest() throws Exception {
        String customCredentialSecurityFactoryName = randomAlphanumeric(5);
        String key1 = randomAlphanumeric(5);
        String value1 = randomAlphanumeric(5);
        String key2 = randomAlphanumeric(5);
        String value2 = randomAlphanumeric(5);
        Address customCredentialSecurityFactoryAddress = elyOps.getElytronAddress(CUSTOM_CREDENTIAL_SECURITY_FACTORY
                , customCredentialSecurityFactoryName);
        try {
            ops.add(customCredentialSecurityFactoryAddress, Values.of(CLASS_NAME, BubuCustomCredentialFactory.class.getName())
                    .and(MODULE, customCredentialSecurityFactoryModuleName)).assertSuccess();

            page.navigateToApplication().selectResource(CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL).getResourceManager()
                    .selectByName(customCredentialSecurityFactoryName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, customCredentialSecurityFactoryAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, CLASS_NAME, ChachaCustomCredentialFactory.class.getName())
                    .edit(TEXT, CONFIGURATION, key1 + "=" + value1 + "\n" + key2 + "=" + value2)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CLASS_NAME, ChachaCustomCredentialFactory.class.getName())
                    .verifyAttribute(CONFIGURATION, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(key1, value1)
                            .addProperty(key2, value2)
                            .build());
        } finally {
            ops.removeIfExists(customCredentialSecurityFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Credential Security Factory instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom Credential Security Factory table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeCustomCredentialSecurityTest() throws Exception {
        String customCredentialSecurityFactoryName = randomAlphanumeric(5);
        Address customCredentialSecurityFactoryAddress = elyOps.getElytronAddress(CUSTOM_CREDENTIAL_SECURITY_FACTORY,
                customCredentialSecurityFactoryName);
        ResourceVerifier customCredentialSecurityFactoryVerifier = new ResourceVerifier(customCredentialSecurityFactoryAddress, client);
        try {
            ops.add(customCredentialSecurityFactoryAddress, Values.of(CLASS_NAME, BubuCustomCredentialFactory.class.getName())
                    .and(MODULE, customCredentialSecurityFactoryModuleName)).assertSuccess();
            customCredentialSecurityFactoryVerifier.verifyExists();

            page.navigateToApplication().selectResource(CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL).getResourceManager()
                    .removeResource(customCredentialSecurityFactoryName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customCredentialSecurityFactoryName));
            customCredentialSecurityFactoryVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customCredentialSecurityFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }
}
