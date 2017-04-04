package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.elytron.TransformerPage;
import org.jboss.hal.testsuite.test.configuration.elytron.transformer.IdentityCustomTransformer;
import org.jboss.hal.testsuite.test.configuration.elytron.transformer.NamePrincipalCustomTransformer;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ModuleUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class TransformerTestCase extends AbstractElytronTestCase {

    private static final String
        CONSTANT_PRINCIPAL_TRANSFORMER = "constant-principal-transformer",
        CONSTANT_PRINCIPAL_TRANSFORMER_LABEL = "Constant",
        CONSTANT = "constant",
        AGGREGATE_PRINCIPAL_TRANSFORMER = "aggregate-principal-transformer",
        AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL = "Aggregate",
        PRINCIPAL_TRANSFORMERS = "principal-transformers",
        CHAINED_PRINCIPAL_TRANSFORMER = "chained-principal-transformer",
        CHAINED_PRINCIPAL_TRANSFORMER_LABEL = "Chained",
        ARCHIVE_NAME = "elytron.customer.transformer.jar",
        CUSTOM_PRINCIPAL_TRANSFORMER = "custom-principal-transformer",
        CUSTOM_PRINCIPAL_TRANSFORMER_LABEL = "Custom",
        CLASS_NAME = "class-name",
        MODULE = "module",
        REGEX_VALIDATING_PRINCIPAL_TRANSFORMER = "regex-validating-principal-transformer",
        REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_LABEL = "Regex Validating",
        REGEX_PRINCIPAL_TRANSFORMER = "regex-principal-transformer",
        REGEX_PRINCIPAL_TRANSFORMER_LABEL = "Regex",
        PATTERN = "pattern",
        REPLACEMENT = "replacement";
    private static String customTransformerModuleName;
    private static final Path CUSTOM_TRANSFORMER_MODULE_PATH = Paths.get("test", "elytron",
            "transformer" + randomAlphanumeric(5));
    private static ModuleUtils moduleUtils;

    @BeforeClass
    public static void beforeClass() throws Exception {
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(IdentityCustomTransformer.class, NamePrincipalCustomTransformer.class);
        customTransformerModuleName = moduleUtils.createModule(CUSTOM_TRANSFORMER_MODULE_PATH, jar,
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        moduleUtils.removeModule(CUSTOM_TRANSFORMER_MODULE_PATH);
    }

    @Page
    private TransformerPage page;

    /**
     * @tpTestDetails Try to create Elytron Constant Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Constant Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addConstantPrincipalTransformerTest() throws Exception {
        String
            constantPrincipalTransformerName = randomAlphanumeric(5),
            constantPrincipalTransformerValue = randomAlphanumeric(5);
        Address constantPrincipalTransformerAddress =
            elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, constantPrincipalTransformerName);

        page.navigateToApplication().selectResource(CONSTANT_PRINCIPAL_TRANSFORMER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, constantPrincipalTransformerName);
            editor.text(CONSTANT, constantPrincipalTransformerValue);

            assertTrue("Dialog should be closed!", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(constantPrincipalTransformerName));
            new ResourceVerifier(constantPrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(CONSTANT, constantPrincipalTransformerValue);
        } finally {
            ops.removeIfExists(constantPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Constant Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeConstantPrincipalTransformerTest() throws Exception {
        String
            constantPrincipalTransformerName = randomAlphanumeric(5),
            constantPrincipalTransformerValue = randomAlphanumeric(5);
        Address constantPrincipalTransformerAddress =
            elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, constantPrincipalTransformerName);
        ResourceVerifier constantPrincipalTransformerVerifier =
            new ResourceVerifier(constantPrincipalTransformerAddress, client);

        try {
            ops.add(constantPrincipalTransformerAddress, Values.of(CONSTANT, constantPrincipalTransformerValue))
                    .assertSuccess();
            constantPrincipalTransformerVerifier.verifyExists();

            page.navigateToApplication().selectResource(CONSTANT_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .removeResource(constantPrincipalTransformerName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(constantPrincipalTransformerName));
            constantPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(constantPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Constant Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editConstantPrincipalTransformerAttributesTest() throws Exception {
        String
            constantPrincipalTransformerName = randomAlphanumeric(5),
            constantPrincipalTransformerOriginalValue = randomAlphanumeric(5),
            constantPrincipalTransformerNewValue = randomAlphanumeric(5);
        Address constantPrincipalTransformerAddress =
            elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, constantPrincipalTransformerName);

        try {
            ops.add(constantPrincipalTransformerAddress,
                    Values.of(CONSTANT, constantPrincipalTransformerOriginalValue)).assertSuccess();

            page.navigateToApplication().selectResource(CONSTANT_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .selectByName(constantPrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, constantPrincipalTransformerAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, CONSTANT, constantPrincipalTransformerNewValue).verifyFormSaved()
                .verifyAttribute(CONSTANT, constantPrincipalTransformerNewValue);
        } finally {
            ops.removeIfExists(constantPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Aggregate Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addAggregatePrincipalTransformerTest() throws Exception {
        String aggregatePrincipalTransformerName = randomAlphanumeric(5);
        Address
            transformerAddress1 = createRandomConstantTransformer(),
            transformerAddress2 = createRandomConstantTransformer(),
            aggregatePrincipalTransformerAddress =
                elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_TRANSFORMER, aggregatePrincipalTransformerName);
        String
            transformerName1 = transformerAddress1.getLastPairValue(),
            transformerName2 = transformerAddress2.getLastPairValue(),
            transformerNamesSeparatedByNewLine = transformerName1 + "\n" + transformerName2;

        page.navigateToApplication().selectResource(AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, aggregatePrincipalTransformerName);
            editor.text(PRINCIPAL_TRANSFORMERS, transformerNamesSeparatedByNewLine);

            assertTrue("Dialog should be closed! See https://issues.jboss.org/browse/HAL-1337", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregatePrincipalTransformerName));
            new ResourceVerifier(aggregatePrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeListBuilder()
                            .addNode(new ModelNode(transformerName1))
                            .addNode(new ModelNode(transformerName2)).build());
        } finally {
            ops.removeIfExists(aggregatePrincipalTransformerAddress);
            ops.removeIfExists(transformerAddress1);
            ops.removeIfExists(transformerAddress2);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Aggregate Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeAggregatePrincipalTransformerTest() throws Exception {
        String aggregatePrincipalTransformerName = randomAlphanumeric(5);
        Address
            transformerAddress1 = createRandomConstantTransformer(),
            transformerAddress2 = createRandomConstantTransformer(),
            aggregatePrincipalTransformerAddress =
                elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_TRANSFORMER, aggregatePrincipalTransformerName);
        String
            transformerName1 = transformerAddress1.getLastPairValue(),
            transformerName2 = transformerAddress2.getLastPairValue();
        ResourceVerifier aggregatePrincipalTransformerVerifier =
            new ResourceVerifier(aggregatePrincipalTransformerAddress, client);

        try {
            ops.add(aggregatePrincipalTransformerAddress, Values.of(PRINCIPAL_TRANSFORMERS,
                new ModelNodeListBuilder()
                    .addNode(new ModelNode(transformerName1))
                    .addNode(new ModelNode(transformerName2)).build())).assertSuccess();
            aggregatePrincipalTransformerVerifier.verifyExists();

            page.navigateToApplication().selectResource(AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .removeResource(aggregatePrincipalTransformerName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(aggregatePrincipalTransformerName));
            aggregatePrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregatePrincipalTransformerAddress);
            ops.removeIfExists(transformerAddress1);
            ops.removeIfExists(transformerAddress2);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAggregatePrincipalTransformerAttributesTest() throws Exception {
        String aggregatePrincipalTransformerName = randomAlphanumeric(5);
        Address
            transformerAddress1 = createRandomConstantTransformer(),
            transformerAddress2 = createRandomConstantTransformer(),
            transformerAddress3 = createRandomConstantTransformer(),
            aggregatePrincipalTransformerAddress =
                elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_TRANSFORMER, aggregatePrincipalTransformerName);
        String
            transformerName1 = transformerAddress1.getLastPairValue(),
            transformerName2 = transformerAddress2.getLastPairValue(),
            transformerName3 = transformerAddress3.getLastPairValue(),
            newTransformerNamesSeparatedByNewLine = transformerName2 + "\n" + transformerName3;

        try {
            ops.add(aggregatePrincipalTransformerAddress, Values.of(PRINCIPAL_TRANSFORMERS,
                    new ModelNodeListBuilder()
                        .addNode(new ModelNode(transformerName1))
                        .addNode(new ModelNode(transformerName2)).build())).assertSuccess();

            page.navigateToApplication().selectResource(AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .selectByName(aggregatePrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, aggregatePrincipalTransformerAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, PRINCIPAL_TRANSFORMERS, newTransformerNamesSeparatedByNewLine)
                .verifyFormSaved("See https://issues.jboss.org/browse/HAL-1337")
                .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeListBuilder()
                        .addNode(new ModelNode(transformerName2))
                        .addNode(new ModelNode(transformerName3)).build());
        } finally {
            ops.removeIfExists(aggregatePrincipalTransformerAddress);
            ops.removeIfExists(transformerAddress1);
            ops.removeIfExists(transformerAddress2);
            ops.removeIfExists(transformerAddress3);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Chained Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Chained Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addChainedPrincipalTransformerTest() throws Exception {
        String chainedPrincipalTransformerName = randomAlphanumeric(5);
        Address
            transformerAddress1 = createRandomConstantTransformer(),
            transformerAddress2 = createRandomConstantTransformer(),
            chainedPrincipalTransformerAddress =
                elyOps.getElytronAddress(CHAINED_PRINCIPAL_TRANSFORMER, chainedPrincipalTransformerName);
        String
            transformerName1 = transformerAddress1.getLastPairValue(),
            transformerName2 = transformerAddress2.getLastPairValue(),
            transformerNamesSeparatedByNewLine = transformerName1 + "\n" + transformerName2;

        page.navigateToApplication().selectResource(CHAINED_PRINCIPAL_TRANSFORMER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, chainedPrincipalTransformerName);
            editor.text(PRINCIPAL_TRANSFORMERS, transformerNamesSeparatedByNewLine);

            assertTrue("Dialog should be closed! See https://issues.jboss.org/browse/HAL-1337", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(chainedPrincipalTransformerName));
            new ResourceVerifier(chainedPrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeListBuilder()
                            .addNode(new ModelNode(transformerName1))
                            .addNode(new ModelNode(transformerName2)).build());
        } finally {
            ops.removeIfExists(chainedPrincipalTransformerAddress);
            ops.removeIfExists(transformerAddress1);
            ops.removeIfExists(transformerAddress2);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Chained Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Chained Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeChainedPrincipalTransformerTest() throws Exception {
        String chainedPrincipalTransformerName = randomAlphanumeric(5);
        Address
            transformerAddress1 = createRandomConstantTransformer(),
            transformerAddress2 = createRandomConstantTransformer(),
            chainedPrincipalTransformerAddress =
                elyOps.getElytronAddress(CHAINED_PRINCIPAL_TRANSFORMER, chainedPrincipalTransformerName);
        String
            transformerName1 = transformerAddress1.getLastPairValue(),
            transformerName2 = transformerAddress2.getLastPairValue();
        ResourceVerifier chainedPrincipalTransformerVerifier =
            new ResourceVerifier(chainedPrincipalTransformerAddress, client);

        try {
            ops.add(chainedPrincipalTransformerAddress, Values.of(PRINCIPAL_TRANSFORMERS,
                new ModelNodeListBuilder()
                    .addNode(new ModelNode(transformerName1))
                    .addNode(new ModelNode(transformerName2)).build())).assertSuccess();
            chainedPrincipalTransformerVerifier.verifyExists();

            page.navigateToApplication().selectResource(CHAINED_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .removeResource(chainedPrincipalTransformerName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(chainedPrincipalTransformerName));
            chainedPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(chainedPrincipalTransformerAddress);
            ops.removeIfExists(transformerAddress1);
            ops.removeIfExists(transformerAddress2);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Chained Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editChainedPrincipalTransformerAttributesTest() throws Exception {
        String chainedPrincipalTransformerName = randomAlphanumeric(5);
        Address
            transformerAddress1 = createRandomConstantTransformer(),
            transformerAddress2 = createRandomConstantTransformer(),
            transformerAddress3 = createRandomConstantTransformer(),
            chainedPrincipalTransformerAddress =
                elyOps.getElytronAddress(CHAINED_PRINCIPAL_TRANSFORMER, chainedPrincipalTransformerName);
        String
            transformerName1 = transformerAddress1.getLastPairValue(),
            transformerName2 = transformerAddress2.getLastPairValue(),
            transformerName3 = transformerAddress3.getLastPairValue(),
            newTransformerNamesSeparatedByNewLine = transformerName2 + "\n" + transformerName3;

        try {
            ops.add(chainedPrincipalTransformerAddress, Values.of(PRINCIPAL_TRANSFORMERS,
                    new ModelNodeListBuilder()
                        .addNode(new ModelNode(transformerName1))
                        .addNode(new ModelNode(transformerName2)).build())).assertSuccess();

            page.navigateToApplication().selectResource(CHAINED_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .selectByName(chainedPrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, chainedPrincipalTransformerAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, PRINCIPAL_TRANSFORMERS, newTransformerNamesSeparatedByNewLine)
                .verifyFormSaved("See https://issues.jboss.org/browse/HAL-1337")
                .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeListBuilder()
                        .addNode(new ModelNode(transformerName2))
                        .addNode(new ModelNode(transformerName3)).build());
        } finally {
            ops.removeIfExists(chainedPrincipalTransformerAddress);
            ops.removeIfExists(transformerAddress1);
            ops.removeIfExists(transformerAddress2);
            ops.removeIfExists(transformerAddress3);
            adminOps.reloadIfRequired();
        }
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
        String customPrincipalTransformerName = randomAlphanumeric(5);
        Address customPrincipalTransformerAddress =
            elyOps.getElytronAddress(CUSTOM_PRINCIPAL_TRANSFORMER, customPrincipalTransformerName);

        page.navigateToApplication().selectResource(CUSTOM_PRINCIPAL_TRANSFORMER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, customPrincipalTransformerName);
            editor.text(CLASS_NAME, IdentityCustomTransformer.class.getName());
            editor.text(MODULE, customTransformerModuleName);

            assertTrue("Dialog should be closed!", wizard.finish());
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
        String customPrincipalTransformerName = randomAlphanumeric(5);
        Address customPrincipalTransformerAddress =
            elyOps.getElytronAddress(CUSTOM_PRINCIPAL_TRANSFORMER, customPrincipalTransformerName);
        ResourceVerifier customPrincipalTransformerVerifier =
            new ResourceVerifier(customPrincipalTransformerAddress, client);

        try {
            ops.add(customPrincipalTransformerAddress, Values.of(CLASS_NAME, IdentityCustomTransformer.class.getName())
                    .and(MODULE, customTransformerModuleName))
                    .assertSuccess("See https://issues.jboss.org/browse/WFLY-8382");
            customPrincipalTransformerVerifier.verifyExists();

            page.navigateToApplication().selectResource(CUSTOM_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .removeResource(customPrincipalTransformerName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customPrincipalTransformerName));
            customPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editCustomPrincipalTransformerAttributesTest() throws Exception {
        String customPrincipalTransformerName = randomAlphanumeric(5);
        Address customPrincipalTransformerAddress =
            elyOps.getElytronAddress(CUSTOM_PRINCIPAL_TRANSFORMER, customPrincipalTransformerName);

        try {
            ops.add(customPrincipalTransformerAddress, Values.of(CLASS_NAME, IdentityCustomTransformer.class.getName())
                    .and(MODULE, customTransformerModuleName))
                    .assertSuccess("See https://issues.jboss.org/browse/WFLY-8382");

            page.navigateToApplication().selectResource(CUSTOM_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
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

    /**
     * @tpTestDetails Try to create Elytron Regex Validating Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Regex Validating Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addRegexValidatingPrincipalTransformerTest() throws Exception {
        Assert.fail("Add test implementation as soon as https://issues.jboss.org/browse/HAL-1341 is fixed.");
    }

    /**
     * @tpTestDetails Create Elytron Regex Validating Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Regex Validating Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeRegexValidatingPrincipalTransformerTest() throws Exception {
        String
            regexValidatingPrincipalTransformerName = randomAlphanumeric(5),
            patternValue = randomAlphanumeric(5);
        Address regexValidatingPrincipalTransformerAddress =
            elyOps.getElytronAddress(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER, regexValidatingPrincipalTransformerName);
        ResourceVerifier regexValidatingPrincipalTransformerVerifier =
            new ResourceVerifier(regexValidatingPrincipalTransformerAddress, client);

        try {
            ops.add(regexValidatingPrincipalTransformerAddress, Values.of(PATTERN, patternValue)).assertSuccess();
            regexValidatingPrincipalTransformerVerifier.verifyExists();

            page.navigateToApplication().selectResource(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_LABEL).getResourceManager()
                    .removeResource(regexValidatingPrincipalTransformerName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(regexValidatingPrincipalTransformerName));
            regexValidatingPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(regexValidatingPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Regex Validating Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editRegexValidatingPrincipalTransformerAttributesTest() throws Exception {
        Assert.fail("Add test implementation as soon as https://issues.jboss.org/browse/HAL-1341 is fixed.");
    }

    /**
     * @tpTestDetails Try to create Elytron Regex Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Regex Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addRegexPrincipalTransformerTest() throws Exception {
        Assert.fail("Add test implementation as soon as https://issues.jboss.org/browse/HAL-1342 is fixed.");
    }

    /**
     * @tpTestDetails Create Elytron Regex Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Regex Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeRegexPrincipalTransformerTest() throws Exception {
        String
            regexPrincipalTransformerName = randomAlphanumeric(5),
            patternValue = randomAlphanumeric(5),
            replacementValue = randomAlphanumeric(5);
        Address regexPrincipalTransformerAddress =
            elyOps.getElytronAddress(REGEX_PRINCIPAL_TRANSFORMER, regexPrincipalTransformerName);
        ResourceVerifier regexPrincipalTransformerVerifier =
            new ResourceVerifier(regexPrincipalTransformerAddress, client);

        try {
            ops.add(regexPrincipalTransformerAddress, Values.of(PATTERN, patternValue)
                    .and(REPLACEMENT, replacementValue)).assertSuccess();
            regexPrincipalTransformerVerifier.verifyExists();

            page.navigateToApplication().selectResourceWithExactLabelMatch(REGEX_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager().removeResource(regexPrincipalTransformerName)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(regexPrincipalTransformerName));
            regexPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(regexPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Regex Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editRegexPrincipalTransformerAttributesTest() throws Exception {
        Assert.fail("Add test implementation as soon as https://issues.jboss.org/browse/HAL-1342 is fixed.");
    }

    private Address createRandomConstantTransformer() throws IOException {
        String
            transformerName = randomAlphanumeric(5),
            transformerValue = randomAlphanumeric(5);
        Address transformerAddress = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName);
        ops.add(transformerAddress, Values.of(CONSTANT, transformerValue)).assertSuccess();
        return transformerAddress;
    }

}
