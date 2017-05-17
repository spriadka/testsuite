package org.jboss.hal.testsuite.test.configuration.elytron.principal.decoder;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
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
public class ElytronPrincipalDecoderTestCase extends AbstractElytronTestCase {

    private static final String
        CONSTANT_PRINCIPAL_DECODER = "constant-principal-decoder",
        CONSTANT_PRINCIPAL_DECODER_LABEL = "Constant Principal Decoder",
        CONSTANT = "constant",
        ARCHIVE_NAME = "elytron.custom.principal.decoder.jar",
        CUSTOM_PRINCIPAL_DECODER = "custom-principal-decoder",
        CUSTOM_PRINCIPAL_DECODER_LABEL = "Custom Principal Decoder",
        AGGREGATE_PRINCIPAL_DECODER = "aggregate-principal-decoder",
        AGGREGATE_PRINCIPAL_DECODER_LABEL = "Aggregate Principal Decoder",
        PRINCIPAL_DECODERS = "principal-decoders",
        CONCATENATING_PRINCIPAL_DECODER = "concatenating-principal-decoder",
        CONCATENATING_PRINCIPAL_DECODER_LABEL = "Concatenating Principal Decoder",
        JOINER = "joiner",
        X500_ATTRIBUTE_PRINCIPAL_DECODER = "x500-attribute-principal-decoder",
        X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL = "X500 Attribute Principal Decoder",
        ATTRIBUTE_NAME = "attribute-name",
        MAXIMUM_SEGMENTS = "maximum-segments",
        OID = "oid",
        REQUIRED_OIDS = "required-oids",
        REVERSE = "reverse";

    private static final Path CUSTOM_PRINCIPAL_DECODER_PATH = Paths.get("test", "elytron",
            "principal", "decoder" + randomAlphanumeric(5));

    private static String customPrincipalDecoderModuleName;

    private static ModuleUtils moduleUtils;

    @Page
    private MapperDecoderPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(LowercaseCustomPrincipalDecoder.class, UppercaseCustomPrincipalDecoder.class);
        customPrincipalDecoderModuleName = moduleUtils.createModule(CUSTOM_PRINCIPAL_DECODER_PATH, jar,
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_PRINCIPAL_DECODER_PATH);
    }

    /**
     * @tpTestDetails Try to create Elytron Constant Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Constant Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addConstantPrincipalDecoderTest() throws Exception {
        String
            constantPrincipalDecoderName = randomAlphanumeric(5),
            constantValue = randomAlphanumeric(5);
        Address constantPrincipalDecoderAddress =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoderName);

        try {
            page.navigateToDecoder()
                .selectResource(CONSTANT_PRINCIPAL_DECODER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(constantPrincipalDecoderName)
                .text(CONSTANT, constantValue)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(constantPrincipalDecoderName));
            new ResourceVerifier(constantPrincipalDecoderAddress, client).verifyExists()
                    .verifyAttribute(CONSTANT, constantValue);
        } finally {
            ops.removeIfExists(constantPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Principal Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editConstantPrincipalDecoderAttributesTest() throws Exception {
        String
            constantPrincipalDecoderName = randomAlphanumeric(5),
            originalAttributeValue = randomAlphanumeric(5),
            newAttributeValue = randomAlphanumeric(5);
        Address constantPrincipalDecoderAddress =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoderName);

        try {
            ops.add(constantPrincipalDecoderAddress, Values.of(CONSTANT, originalAttributeValue)).assertSuccess();

            page.navigateToDecoder().selectResource(CONSTANT_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .selectByName(constantPrincipalDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, constantPrincipalDecoderAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, CONSTANT, newAttributeValue)
                .verifyFormSaved()
                .verifyAttribute(CONSTANT, newAttributeValue);
        } finally {
            ops.removeIfExists(constantPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Principal Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Constant Principal Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeConstantPrincipalDecoderTest() throws Exception {
        String
            constantPrincipalDecoderName = randomAlphanumeric(5),
            constantValue = randomAlphanumeric(5);
        Address constantPrincipalDecoderAddress =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoderName);
        ResourceVerifier constantPrincipalDecoderVerifier = new ResourceVerifier(constantPrincipalDecoderAddress, client);

        try {
            ops.add(constantPrincipalDecoderAddress, Values.of(CONSTANT, constantValue)).assertSuccess();
            constantPrincipalDecoderVerifier.verifyExists();

            page.navigateToDecoder().selectResource(CONSTANT_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .removeResource(constantPrincipalDecoderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(constantPrincipalDecoderName));
            constantPrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(constantPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
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
        String customPrincipalDecoderName = randomAlphanumeric(5);
        Address customPrincipalDecoderAddress =
                elyOps.getElytronAddress(CUSTOM_PRINCIPAL_DECODER, customPrincipalDecoderName);

        try {
            page.navigateToDecoder()
                .selectResource(CUSTOM_PRINCIPAL_DECODER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(customPrincipalDecoderName)
                .text(CLASS_NAME, LowercaseCustomPrincipalDecoder.class.getName())
                .text(MODULE, customPrincipalDecoderModuleName)
                .saveWithState().assertWindowClosed();

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
        String
            customPrincipalDecoderName = randomAlphanumeric(5),
            key1 = randomAlphanumeric(5),
            value1 = randomAlphanumeric(5),
            key2 = randomAlphanumeric(5),
            value2 = randomAlphanumeric(5);
        Address customPrincipalDecoderAddress =
                elyOps.getElytronAddress(CUSTOM_PRINCIPAL_DECODER, customPrincipalDecoderName);

        try {
            ops.add(customPrincipalDecoderAddress, Values.of(CLASS_NAME, LowercaseCustomPrincipalDecoder.class.getName())
                    .and(MODULE, customPrincipalDecoderModuleName)).assertSuccess();

            page.navigateToDecoder().selectResource(CUSTOM_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .selectByName(customPrincipalDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, customPrincipalDecoderAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, CLASS_NAME, UppercaseCustomPrincipalDecoder.class.getName())
                .edit(TEXT, CONFIGURATION, key1 + "=" + value1 + "\n" + key2 + "=" + value2)
                .andSave().verifyFormSaved()
                .verifyAttribute(CLASS_NAME, UppercaseCustomPrincipalDecoder.class.getName())
                .verifyAttribute(CONFIGURATION, new ModelNodePropertiesBuilder()
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
        String customPrincipalDecoderName = randomAlphanumeric(5);
        Address customPrincipalDecoderAddress =
                elyOps.getElytronAddress(CUSTOM_PRINCIPAL_DECODER, customPrincipalDecoderName);
        ResourceVerifier customPrincipalDecoderVerifier = new ResourceVerifier(customPrincipalDecoderAddress, client);

        try {
            ops.add(customPrincipalDecoderAddress, Values.of(CLASS_NAME, LowercaseCustomPrincipalDecoder.class.getName())
                    .and(MODULE, customPrincipalDecoderModuleName)).assertSuccess();
            customPrincipalDecoderVerifier.verifyExists();

            page.navigateToDecoder().selectResource(CUSTOM_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .removeResource(customPrincipalDecoderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customPrincipalDecoderName));
            customPrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Aggregate Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addAggregatePrincipalDecoderTest() throws Exception {
        String
            aggregatePrincipalDecoderName = randomAlphanumeric(5),
            constantPrincipalDecoder1name = randomAlphanumeric(5),
            constantPrincipalDecoder2name = randomAlphanumeric(5),
            constant1value = randomAlphanumeric(5),
            constant2value = randomAlphanumeric(5);
        Address
            aggregatePrincipalDecoderAddress =
                elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_DECODER, aggregatePrincipalDecoderName),
            constantPrincipalDecoder1address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder1name),
            constantPrincipalDecoder2address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder2name);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            page.navigateToDecoder()
                .selectResource(AGGREGATE_PRINCIPAL_DECODER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(aggregatePrincipalDecoderName)
                .text(PRINCIPAL_DECODERS, constantPrincipalDecoder1name + "\n" + constantPrincipalDecoder2name)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregatePrincipalDecoderName));
            new ResourceVerifier(aggregatePrincipalDecoderAddress, client).verifyExists()
                    .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                            .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                            .build());
        } finally {
            ops.removeIfExists(aggregatePrincipalDecoderAddress);
            ops.removeIfExists(constantPrincipalDecoder1address);
            ops.removeIfExists(constantPrincipalDecoder2address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Principal Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAggregatePrincipalDecoderAttributesTest() throws Exception {
        String
            aggregatePrincipalDecoderName = randomAlphanumeric(5),
            constantPrincipalDecoder1name = randomAlphanumeric(5),
            constantPrincipalDecoder2name = randomAlphanumeric(5),
            constantPrincipalDecoder3name = randomAlphanumeric(5),
            constant1value = randomAlphanumeric(5),
            constant2value = randomAlphanumeric(5),
            constant3value = randomAlphanumeric(5);
        Address
            aggregatePrincipalDecoderAddress =
                elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_DECODER, aggregatePrincipalDecoderName),
            constantPrincipalDecoder1address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder1name),
            constantPrincipalDecoder2address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder2name),
            constantPrincipalDecoder3address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder3name);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            ops.add(constantPrincipalDecoder3address, Values.of(CONSTANT, constant3value)).assertSuccess();
            ops.add(aggregatePrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                    .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                    .build())).assertSuccess();

            page.navigateToDecoder().selectResource(AGGREGATE_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .selectByName(aggregatePrincipalDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, aggregatePrincipalDecoderAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, PRINCIPAL_DECODERS, constantPrincipalDecoder3name + "\n"
                        + constantPrincipalDecoder1name)
                .verifyFormSaved()
                .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                        .addAll(constantPrincipalDecoder3name, constantPrincipalDecoder1name)
                        .build());
        } finally {
            ops.removeIfExists(aggregatePrincipalDecoderAddress);
            ops.removeIfExists(constantPrincipalDecoder1address);
            ops.removeIfExists(constantPrincipalDecoder2address);
            ops.removeIfExists(constantPrincipalDecoder3address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Principal Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Aggregate Principal Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeAggregatePrincipalDecoderTest() throws Exception {
        String
            aggregatePrincipalDecoderName = randomAlphanumeric(5),
            constantPrincipalDecoder1name = randomAlphanumeric(5),
            constantPrincipalDecoder2name = randomAlphanumeric(5),
            constant1value = randomAlphanumeric(5),
            constant2value = randomAlphanumeric(5);
        Address
            aggregatePrincipalDecoderAddress =
                elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_DECODER, aggregatePrincipalDecoderName),
            constantPrincipalDecoder1address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder1name),
            constantPrincipalDecoder2address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder2name);
        ResourceVerifier aggregatePrincipalDecoderVerifier = new ResourceVerifier(aggregatePrincipalDecoderAddress, client);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            ops.add(aggregatePrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                    .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                    .build())).assertSuccess();
            aggregatePrincipalDecoderVerifier.verifyExists();

            page.navigateToDecoder().selectResource(AGGREGATE_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .removeResource(aggregatePrincipalDecoderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(aggregatePrincipalDecoderName));
            aggregatePrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregatePrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Concatenating Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Concatenating Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addConcatenatingPrincipalDecoderTest() throws Exception {
        String
            concatenatingPrincipalDecoderName = randomAlphanumeric(5),
            constantPrincipalDecoder1name = randomAlphanumeric(5),
            constantPrincipalDecoder2name = randomAlphanumeric(5),
            constant1value = randomAlphanumeric(5),
            constant2value = randomAlphanumeric(5);
        Address
            concatenatingPrincipalDecoderAddress =
                elyOps.getElytronAddress(CONCATENATING_PRINCIPAL_DECODER, concatenatingPrincipalDecoderName),
            constantPrincipalDecoder1address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder1name),
            constantPrincipalDecoder2address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder2name);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            page.navigateToDecoder()
                .selectResource(CONCATENATING_PRINCIPAL_DECODER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(concatenatingPrincipalDecoderName)
                .text(PRINCIPAL_DECODERS, constantPrincipalDecoder1name + "\n" + constantPrincipalDecoder2name)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(concatenatingPrincipalDecoderName));
            new ResourceVerifier(concatenatingPrincipalDecoderAddress, client).verifyExists()
                    .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                            .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                            .build());
        } finally {
            ops.removeIfExists(concatenatingPrincipalDecoderAddress);
            ops.removeIfExists(constantPrincipalDecoder1address);
            ops.removeIfExists(constantPrincipalDecoder2address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Concatenating Principal Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editConcatenatingPrincipalDecoderAttributesTest() throws Exception {
        String
            concatenatingPrincipalDecoderName = randomAlphanumeric(5),
            constantPrincipalDecoder1name = randomAlphanumeric(5),
            constantPrincipalDecoder2name = randomAlphanumeric(5),
            constantPrincipalDecoder3name = randomAlphanumeric(5),
            constant1value = randomAlphanumeric(5),
            constant2value = randomAlphanumeric(5),
            constant3value = randomAlphanumeric(5),
            joinerValue = randomAlphanumeric(5);
        Address
            concatenatingPrincipalDecoderAddress =
                elyOps.getElytronAddress(CONCATENATING_PRINCIPAL_DECODER, concatenatingPrincipalDecoderName),
            constantPrincipalDecoder1address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder1name),
            constantPrincipalDecoder2address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder2name),
            constantPrincipalDecoder3address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder3name);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            ops.add(constantPrincipalDecoder3address, Values.of(CONSTANT, constant3value)).assertSuccess();
            ops.add(concatenatingPrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                    .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                    .build())).assertSuccess();

            page.navigateToDecoder().selectResource(CONCATENATING_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .selectByName(concatenatingPrincipalDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, concatenatingPrincipalDecoderAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, JOINER, joinerValue)
                .edit(TEXT, PRINCIPAL_DECODERS, constantPrincipalDecoder3name + "\n"
                        + constantPrincipalDecoder1name)
                .andSave().verifyFormSaved()
                .verifyAttribute(JOINER, joinerValue)
                .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                        .addAll(constantPrincipalDecoder3name, constantPrincipalDecoder1name)
                        .build());
        } finally {
            ops.removeIfExists(concatenatingPrincipalDecoderAddress);
            ops.removeIfExists(constantPrincipalDecoder1address);
            ops.removeIfExists(constantPrincipalDecoder2address);
            ops.removeIfExists(constantPrincipalDecoder3address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Concatenating Principal Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Concatenating Principal Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeConcatenatingPrincipalDecoderTest() throws Exception {
        String
            concatenatingPrincipalDecoderName = randomAlphanumeric(5),
            constantPrincipalDecoder1name = randomAlphanumeric(5),
            constantPrincipalDecoder2name = randomAlphanumeric(5),
            constant1value = randomAlphanumeric(5),
            constant2value = randomAlphanumeric(5);
        Address
            concatenatingPrincipalDecoderAddress =
                elyOps.getElytronAddress(CONCATENATING_PRINCIPAL_DECODER, concatenatingPrincipalDecoderName),
            constantPrincipalDecoder1address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder1name),
            constantPrincipalDecoder2address =
                elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder2name);
        ResourceVerifier concatenatingPrincipalDecoderVerifier = new ResourceVerifier(concatenatingPrincipalDecoderAddress, client);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            ops.add(concatenatingPrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeListBuilder()
                    .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                    .build())).assertSuccess();
            concatenatingPrincipalDecoderVerifier.verifyExists();

            page.navigateToDecoder().selectResource(CONCATENATING_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .removeResource(concatenatingPrincipalDecoderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(concatenatingPrincipalDecoderName));
            concatenatingPrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(concatenatingPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron X500 Attribute Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in X500 Attribute Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addX500AttributePrincipalDecoderTest() throws Exception {
        String
            x500AttributePrincipalDecoderName = randomAlphanumeric(5),
            attributeNameValue = randomAlphanumeric(5),
            oidValue = randomAlphanumeric(5),
            requiredAttr1value = randomAlphanumeric(5),
            requiredAttr2value = randomAlphanumeric(5);
        Address x500AttributePrincipalDecoderAddress =
                elyOps.getElytronAddress(X500_ATTRIBUTE_PRINCIPAL_DECODER, x500AttributePrincipalDecoderName);

        try {
            AddResourceWizard wizard = page.navigateToDecoder()
                .selectResource(X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(x500AttributePrincipalDecoderName)
                .text(ATTRIBUTE_NAME, attributeNameValue)
                .text(OID, oidValue);
                wizard.saveWithState().assertWindowOpen(); // ATTRIBUTE_NAME and OID cannot be set at the same time
                wizard.text(ATTRIBUTE_NAME, "")
                .text(REQUIRED_OIDS, requiredAttr1value + "\n" + requiredAttr2value)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(x500AttributePrincipalDecoderName));
            new ResourceVerifier(x500AttributePrincipalDecoderAddress, client).verifyExists()
                    .verifyAttribute(OID, oidValue)
                    .verifyAttribute(REQUIRED_OIDS, new ModelNodeListBuilder()
                            .addAll(requiredAttr1value, requiredAttr2value)
                            .build());
        } finally {
            ops.removeIfExists(x500AttributePrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron X500 Attribute Principal Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editX500AttributePrincipalDecoderAttributesTest() throws Exception {
        String
            x500AttributePrincipalDecoderName = randomAlphanumeric(5),
            originalOidValue = randomAlphanumeric(5),
            newOidValue = randomAlphanumeric(5),
            newJoinerValue = randomAlphanumeric(5);
        int newMaximumSegmentsValue = 17;
        Address x500AttributePrincipalDecoderAddress =
                elyOps.getElytronAddress(X500_ATTRIBUTE_PRINCIPAL_DECODER, x500AttributePrincipalDecoderName);

        try {
            ops.add(x500AttributePrincipalDecoderAddress, Values.of(OID, originalOidValue)).assertSuccess();

            page.navigateToDecoder().selectResource(X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .selectByName(x500AttributePrincipalDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, x500AttributePrincipalDecoderAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, JOINER, newJoinerValue)
                .edit(TEXT, MAXIMUM_SEGMENTS, newMaximumSegmentsValue)
                .edit(TEXT, OID, newOidValue)
                .edit(CHECKBOX, REVERSE, true)
                .andSave().verifyFormSaved()
                .verifyAttribute(JOINER, newJoinerValue)
                .verifyAttribute(MAXIMUM_SEGMENTS, newMaximumSegmentsValue)
                .verifyAttribute(OID, newOidValue)
                .verifyAttribute(REVERSE, true);
        } finally {
            ops.removeIfExists(x500AttributePrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron X500 Attribute Principal Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in X500 Attribute Principal Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeX500AttributePrincipalDecoderTest() throws Exception {
        String
            x500AttributePrincipalDecoderName = randomAlphanumeric(5),
            oidValue = randomAlphanumeric(5);
        Address x500AttributePrincipalDecoderAddress =
                elyOps.getElytronAddress(X500_ATTRIBUTE_PRINCIPAL_DECODER, x500AttributePrincipalDecoderName);
        ResourceVerifier x500AttributePrincipalDecoderVerifier =
                new ResourceVerifier(x500AttributePrincipalDecoderAddress, client);

        try {
            ops.add(x500AttributePrincipalDecoderAddress, Values.of(OID, oidValue)).assertSuccess();
            x500AttributePrincipalDecoderVerifier.verifyExists();

            page.navigateToDecoder().selectResource(X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL).getResourceManager()
                    .removeResource(x500AttributePrincipalDecoderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(x500AttributePrincipalDecoderName));
            x500AttributePrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(x500AttributePrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }
}
