package org.jboss.hal.testsuite.test.configuration.elytron.role.decoder;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
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

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ElytronRoleDecoderTestCase extends AbstractElytronTestCase {

    private static final String
        SIMPLE_ROLE_DECODER = "simple-role-decoder",
        SIMPLE_ROLE_DECODER_LABEL = "Simple Role Decoder",
        ATTRIBUTE = "attribute",
        ARCHIVE_NAME = "elytron.custom.role.decoder.jar",
        CUSTOM_ROLE_DECODER = "custom-role-decoder",
        CUSTOM_ROLE_DECODER_LABEL = "Custom Role Decoder";

    private static final Path CUSTOM_ROLE_DECODER_PATH = Paths.get("test", "elytron",
            "role", "decoder" + randomAlphanumeric(5));

    private static String customRoleDecoderModuleName;

    private static ModuleUtils moduleUtils;

    @Page
    private MapperDecoderPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(BugsBunnyCustomRoleDecoder.class, SuperMarioCustomRoleDecoder.class);
        customRoleDecoderModuleName = moduleUtils.createModule(CUSTOM_ROLE_DECODER_PATH, jar,
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_ROLE_DECODER_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Simple Role Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Simple Role Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addSimpleRoleDecoderTest() throws Exception {
        String
            simpleRoleDecoderName = randomAlphanumeric(5),
            attributeValue = randomAlphanumeric(5);
        Address simpleRoleDecoderAddress =
                elyOps.getElytronAddress(SIMPLE_ROLE_DECODER, simpleRoleDecoderName);

        try {
            page.navigateToDecoder()
                .selectResource(SIMPLE_ROLE_DECODER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(simpleRoleDecoderName)
                .text(ATTRIBUTE, attributeValue)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(simpleRoleDecoderName));
            new ResourceVerifier(simpleRoleDecoderAddress, client).verifyExists()
                    .verifyAttribute(ATTRIBUTE, attributeValue);
        } finally {
            ops.removeIfExists(simpleRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple Role Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editSimpleRoleDecoderAttributesTest() throws Exception {
        String
            simpleRoleDecoderName = randomAlphanumeric(5),
            originalAttributeValue = randomAlphanumeric(5),
            newAttributeValue = randomAlphanumeric(5);
        Address simpleRoleDecoderAddress =
                elyOps.getElytronAddress(SIMPLE_ROLE_DECODER, simpleRoleDecoderName);

        try {
            ops.add(simpleRoleDecoderAddress, Values.of(ATTRIBUTE, originalAttributeValue)).assertSuccess();

            page.navigateToDecoder().selectResource(SIMPLE_ROLE_DECODER_LABEL).getResourceManager()
                    .selectByName(simpleRoleDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, simpleRoleDecoderAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, ATTRIBUTE, newAttributeValue)
                .verifyFormSaved()
                .verifyAttribute(ATTRIBUTE, newAttributeValue);
        } finally {
            ops.removeIfExists(simpleRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple Role Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Simple Role Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeSimpleRoleDecoderTest() throws Exception {
        String
            simpleRoleDecoderName = randomAlphanumeric(5),
            attributeValue = randomAlphanumeric(5);
        Address simpleRoleDecoderAddress =
                elyOps.getElytronAddress(SIMPLE_ROLE_DECODER, simpleRoleDecoderName);
        ResourceVerifier simpleRoleDecoderVerifier = new ResourceVerifier(simpleRoleDecoderAddress, client);

        try {
            ops.add(simpleRoleDecoderAddress, Values.of(ATTRIBUTE, attributeValue)).assertSuccess();
            simpleRoleDecoderVerifier.verifyExists();

            page.navigateToDecoder().selectResource(SIMPLE_ROLE_DECODER_LABEL).getResourceManager()
                    .removeResource(simpleRoleDecoderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(simpleRoleDecoderName));
            simpleRoleDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(simpleRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Role Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom Role Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addCustomRoleDecoderTest() throws Exception {
        String customRoleDecoderName = randomAlphanumeric(5);
        Address customRoleDecoderAddress =
                elyOps.getElytronAddress(CUSTOM_ROLE_DECODER, customRoleDecoderName);

        try {
            page.navigateToDecoder()
                .selectResource(CUSTOM_ROLE_DECODER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(customRoleDecoderName)
                .text(CLASS_NAME, BugsBunnyCustomRoleDecoder.class.getName())
                .text(MODULE, customRoleDecoderModuleName)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(customRoleDecoderName));
            new ResourceVerifier(customRoleDecoderAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, BugsBunnyCustomRoleDecoder.class.getName())
                    .verifyAttribute(MODULE, customRoleDecoderModuleName);
        } finally {
            ops.removeIfExists(customRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Role Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editCustomRoleDecoderAttributesTest() throws Exception {
        String
            customRoleDecoderName = randomAlphanumeric(5),
            key1 = randomAlphanumeric(5),
            value1 = randomAlphanumeric(5),
            key2 = randomAlphanumeric(5),
            value2 = randomAlphanumeric(5);
        Address customRoleDecoderAddress =
                elyOps.getElytronAddress(CUSTOM_ROLE_DECODER, customRoleDecoderName);

        try {
            ops.add(customRoleDecoderAddress, Values.of(CLASS_NAME, BugsBunnyCustomRoleDecoder.class.getName())
                    .and(MODULE, customRoleDecoderModuleName)).assertSuccess();

            page.navigateToDecoder().selectResource(CUSTOM_ROLE_DECODER_LABEL).getResourceManager()
                    .selectByName(customRoleDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, customRoleDecoderAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, CLASS_NAME, SuperMarioCustomRoleDecoder.class.getName())
                .edit(TEXT, CONFIGURATION, key1 + "=" + value1 + "\n" + key2 + "=" + value2)
                .andSave().verifyFormSaved()
                .verifyAttribute(CLASS_NAME, SuperMarioCustomRoleDecoder.class.getName())
                .verifyAttribute(CONFIGURATION, new ModelNodePropertiesBuilder()
                        .addProperty(key1, value1)
                        .addProperty(key2, value2)
                        .build());
        } finally {
            ops.removeIfExists(customRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Role Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom Role Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeCustomRoleDecoderTest() throws Exception {
        String customRoleDecoderName = randomAlphanumeric(5);
        Address customRoleDecoderAddress =
                elyOps.getElytronAddress(CUSTOM_ROLE_DECODER, customRoleDecoderName);
        ResourceVerifier customRoleDecoderVerifier = new ResourceVerifier(customRoleDecoderAddress, client);

        try {
            ops.add(customRoleDecoderAddress, Values.of(CLASS_NAME, BugsBunnyCustomRoleDecoder.class.getName())
                    .and(MODULE, customRoleDecoderModuleName)).assertSuccess();
            customRoleDecoderVerifier.verifyExists();

            page.navigateToDecoder().selectResource(CUSTOM_ROLE_DECODER_LABEL).getResourceManager()
                    .removeResource(customRoleDecoderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customRoleDecoderName));
            customRoleDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }
}
