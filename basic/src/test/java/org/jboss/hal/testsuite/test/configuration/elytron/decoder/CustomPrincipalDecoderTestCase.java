package org.jboss.hal.testsuite.test.configuration.elytron.decoder;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.decoder.AddCustomPrincipalDecoderWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
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

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class CustomPrincipalDecoderTestCase extends AbstractElytronTestCase {

    private static final String CUSTOM_PRINCIPAL_DECODER = "custom-principal-decoder";
    private static final String CUSTOM_PRINCIPAL_DECODER_LABEL = "Custom Principal Decoder";
    private static final String ARCHIVE_NAME = "elytron.custom.principal.decoder.jar";
    private static final Path CUSTOM_PRINCIPAL_DECODER_PATH = Paths.get("test", "elytron",
            "principal", "decoder" + randomAlphanumeric(5));
    private static String customPrincipalDecoderModuleName;
    private static final String[] ELYTRON_DEPENDENCY_MODULE_NAMES = {"org.wildfly.extension.elytron", "org.wildfly.security.elytron-private"};

    private static ModuleUtils moduleUtils;

    @Page
    private MapperDecoderPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(LowercaseCustomPrincipalDecoder.class, UppercaseCustomPrincipalDecoder.class);
        customPrincipalDecoderModuleName = moduleUtils.createModule(CUSTOM_PRINCIPAL_DECODER_PATH, jar,
                ELYTRON_DEPENDENCY_MODULE_NAMES);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_PRINCIPAL_DECODER_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addCustomPrincipalDecoderTest() throws Exception {
        final String customPrincipalDecoderName = randomAlphanumeric(5);
        final Address customPrincipalDecoderAddress = elyOps.getElytronAddress(CUSTOM_PRINCIPAL_DECODER,
                customPrincipalDecoderName);

        try {
            page.navigateToDecoder()
                    .selectResource(CUSTOM_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .addResource(AddCustomPrincipalDecoderWizard.class)
                    .name(customPrincipalDecoderName)
                    .className(LowercaseCustomPrincipalDecoder.class.getName())
                    .module(customPrincipalDecoderModuleName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(customPrincipalDecoderName));
            new ResourceVerifier(customPrincipalDecoderAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, LowercaseCustomPrincipalDecoder.class.getName())
                    .verifyAttribute(MODULE, customPrincipalDecoderModuleName);
        } finally {
            ops.removeIfExists(customPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Principal Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editCustomPrincipalDecoderAttributesTest() throws Exception {
        final String customPrincipalDecoderName = randomAlphanumeric(5);
        final String key1 = randomAlphanumeric(5);
        final String value1 = randomAlphanumeric(5);
        final String key2 = randomAlphanumeric(5);
        final String value2 = randomAlphanumeric(5);
        final Address customPrincipalDecoderAddress = elyOps.getElytronAddress(CUSTOM_PRINCIPAL_DECODER,
                customPrincipalDecoderName);
        try {
            ops.add(customPrincipalDecoderAddress, Values.of(CLASS_NAME, LowercaseCustomPrincipalDecoder.class.getName())
                    .and(MODULE, customPrincipalDecoderModuleName)).assertSuccess();

            page.navigateToDecoder()
                    .selectResource(CUSTOM_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .selectByName(customPrincipalDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, customPrincipalDecoderAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, CLASS_NAME, UppercaseCustomPrincipalDecoder.class.getName())
                    .edit(TEXT, CONFIGURATION, key1 + "=" + value1 + "\n" + key2 + "=" + value2)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(CLASS_NAME, UppercaseCustomPrincipalDecoder.class.getName())
                    .verifyAttribute(CONFIGURATION, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(key1, value1)
                            .addProperty(key2, value2)
                            .build());
        } finally {
            ops.removeIfExists(customPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Principal Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom Principal Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeCustomPrincipalDecoderTest() throws Exception {
        final String customPrincipalDecoderName = randomAlphanumeric(5);
        final Address customPrincipalDecoderAddress = elyOps.getElytronAddress(CUSTOM_PRINCIPAL_DECODER,
                customPrincipalDecoderName);
        ResourceVerifier customPrincipalDecoderVerifier = new ResourceVerifier(customPrincipalDecoderAddress, client);

        try {
            ops.add(customPrincipalDecoderAddress, Values.of(CLASS_NAME, LowercaseCustomPrincipalDecoder.class.getName())
                    .and(MODULE, customPrincipalDecoderModuleName)).assertSuccess();
            customPrincipalDecoderVerifier.verifyExists();
            page.navigateToDecoder()
                    .selectResource(CUSTOM_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .removeResource(customPrincipalDecoderName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customPrincipalDecoderName));
            customPrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }
}
