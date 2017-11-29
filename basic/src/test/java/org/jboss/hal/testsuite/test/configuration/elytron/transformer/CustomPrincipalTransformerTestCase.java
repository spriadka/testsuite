package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.transformer.AddCustomPrincipalTransformerWizard;
import org.jboss.hal.testsuite.page.config.elytron.TransformerPage;
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
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class CustomPrincipalTransformerTestCase extends AbstractElytronTestCase {

    private static final String CUSTOM_PRINCIPAL_TRANSFORMER = "custom-principal-transformer";
    private static final String CUSTOM_PRINCIPAL_TRANSFORMER_LABEL = "Custom";

    private static final String ARCHIVE_NAME = "elytron.customer.transformer.jar";
    private static String customTransformerModuleName;
    private static final Path CUSTOM_TRANSFORMER_MODULE_PATH = Paths.get("test", "elytron",
            "transformer" + randomAlphanumeric(5));
    private static final String[] ELYTRON_DEPENDENCY_MODULE_NAMES = {"org.wildfly.extension.elytron",
            "org.wildfly.security.elytron-private"};
    private static ModuleUtils moduleUtils;


    @Page
    private TransformerPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(IdentityCustomTransformer.class, NamePrincipalCustomTransformer.class);
        customTransformerModuleName = moduleUtils.createModule(CUSTOM_TRANSFORMER_MODULE_PATH, jar,
                ELYTRON_DEPENDENCY_MODULE_NAMES);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_TRANSFORMER_MODULE_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addCustomPrincipalTransformerTest() throws Exception {
        final String customPrincipalTransformerName = "custom_principal_transformer_" + randomAlphanumeric(5);
        final Address customPrincipalTransformerAddress = elyOps.getElytronAddress(CUSTOM_PRINCIPAL_TRANSFORMER,
                customPrincipalTransformerName);
        try {
            page.navigateToApplication()
                    .selectResource(CUSTOM_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .addResource(AddCustomPrincipalTransformerWizard.class)
                    .name(customPrincipalTransformerName)
                    .className(IdentityCustomTransformer.class.getName())
                    .module(customTransformerModuleName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table! See https://issues.jboss.org/browse/WFLY-8382",
                    page.resourceIsPresentInMainTable(customPrincipalTransformerName));
            new ResourceVerifier(customPrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, IdentityCustomTransformer.class.getName())
                    .verifyAttribute(MODULE, customTransformerModuleName);
        } finally {
            ops.removeIfExists(customPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeCustomPrincipalTransformerTest() throws Exception {
        final String customPrincipalTransformerName = "custom_principal_transformer_" + randomAlphanumeric(5);
        final Address customPrincipalTransformerAddress = elyOps.getElytronAddress(CUSTOM_PRINCIPAL_TRANSFORMER,
                customPrincipalTransformerName);
        final ResourceVerifier customPrincipalTransformerVerifier =
                new ResourceVerifier(customPrincipalTransformerAddress, client);
        try {
            createCustomPrincipalTransformerInModel(customPrincipalTransformerAddress);
            customPrincipalTransformerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(CUSTOM_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .removeResource(customPrincipalTransformerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customPrincipalTransformerName));
            customPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createCustomPrincipalTransformerInModel(Address customPrincipalTransformerAddress) throws IOException {
        ops.add(customPrincipalTransformerAddress, Values.of(CLASS_NAME, IdentityCustomTransformer.class.getName())
                .and(MODULE, customTransformerModuleName))
                .assertSuccess("See https://issues.jboss.org/browse/WFLY-8382");
    }

    /**
     * @tpTestDetails Create Elytron Custom Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editCustomPrincipalTransformerAttributesTest() throws Exception {
        final String customPrincipalTransformerName = "custom_principal_transformer_" + randomAlphanumeric(5);
        final Address customPrincipalTransformerAddress = elyOps.getElytronAddress(CUSTOM_PRINCIPAL_TRANSFORMER,
                customPrincipalTransformerName);
        try {
            createCustomPrincipalTransformerInModel(customPrincipalTransformerAddress);
            page.navigateToApplication()
                    .selectResource(CUSTOM_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .selectByName(customPrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, customPrincipalTransformerAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, CLASS_NAME, NamePrincipalCustomTransformer.class.getName()).verifyFormSaved()
                    .verifyAttribute(CLASS_NAME, NamePrincipalCustomTransformer.class.getName());
        } finally {
            ops.removeIfExists(customPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }
}
