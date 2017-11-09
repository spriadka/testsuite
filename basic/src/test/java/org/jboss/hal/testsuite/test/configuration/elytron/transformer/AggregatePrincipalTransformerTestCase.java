package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.transformer.AddAggregatePrincipalTransformerWizard;
import org.jboss.hal.testsuite.page.config.elytron.TransformerPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class AggregatePrincipalTransformerTestCase extends AbstractElytronTestCase {

    private static final String CONSTANT_PRINCIPAL_TRANSFORMER = "constant-principal-transformer";
    private static final String AGGREGATE_PRINCIPAL_TRANSFORMER = "aggregate-principal-transformer";
    private static final String AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL = "Aggregate";
    private static final String PRINCIPAL_TRANSFORMERS = "principal-transformers";

    private static final PrincipalTransformerUtils transformerUtils = new PrincipalTransformerUtils(client);

    @Page
    private TransformerPage page;

    /**
     * @tpTestDetails Try to create Elytron Aggregate Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addAggregatePrincipalTransformerTest() throws Exception {
        final String aggregatePrincipalTransformerName = "aggregate_principal_transformer_" + randomAlphanumeric(5);
        final String transformerName1 = RandomStringUtils.randomAlphanumeric(7);
        final String transformerName2 = RandomStringUtils.randomAlphanumeric(7);
        final Address transformerAddress1 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName1);
        final Address transformerAddress2 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName2);
        final Address aggregatePrincipalTransformerAddress = elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_TRANSFORMER,
                aggregatePrincipalTransformerName);
        final String transformerNamesSeparatedByNewLine = String.join("\n", transformerName1, transformerName2);
        try {
            transformerUtils.createConstantTransformer(transformerAddress1);
            transformerUtils.createConstantTransformer(transformerAddress2);
            page.navigateToApplication()
                    .selectResource(AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .addResource(AddAggregatePrincipalTransformerWizard.class)
                    .name(aggregatePrincipalTransformerName)
                    .principalTransformers(transformerNamesSeparatedByNewLine)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregatePrincipalTransformerName));
            new ResourceVerifier(aggregatePrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
        final String aggregatePrincipalTransformerName = "aggregate_principal_transformer_" + randomAlphanumeric(5);
        final String transformerName1 = RandomStringUtils.randomAlphanumeric(7);
        final String transformerName2 = RandomStringUtils.randomAlphanumeric(7);
        final Address transformerAddress1 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName1);
        final Address transformerAddress2 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName2);
        final Address aggregatePrincipalTransformerAddress = elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_TRANSFORMER,
                aggregatePrincipalTransformerName);
        ResourceVerifier aggregatePrincipalTransformerVerifier = new ResourceVerifier(aggregatePrincipalTransformerAddress, client);
        try {
            transformerUtils.createConstantTransformer(transformerAddress1);
            transformerUtils.createConstantTransformer(transformerAddress2);
            createAggregatePrincipalTransformer(aggregatePrincipalTransformerAddress, transformerName1, transformerName2);
            aggregatePrincipalTransformerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .removeResource(aggregatePrincipalTransformerName)
                    .confirmAndDismissReloadRequiredMessage()
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

    private void createAggregatePrincipalTransformer(Address aggregatePrincipalAddress, String... transformers) throws IOException {
        ops.add(aggregatePrincipalAddress, Values.of(PRINCIPAL_TRANSFORMERS,
                new ModelNodeGenerator.ModelNodeListBuilder().addAll(transformers).build()))
                .assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Aggregate Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAggregatePrincipalTransformerAttributesTest() throws Exception {
        final String aggregatePrincipalTransformerName = "aggregate_principal_transformer_" + randomAlphanumeric(5);
        final String transformerName1 = RandomStringUtils.randomAlphanumeric(7);
        final String transformerName2 = RandomStringUtils.randomAlphanumeric(7);
        final String transformerName3 = RandomStringUtils.randomAlphanumeric(7);
        final Address transformerAddress1 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName1);
        final Address transformerAddress2 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName2);
        final Address transformerAddress3 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName3);
        final Address aggregatePrincipalTransformerAddress = elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_TRANSFORMER,
                aggregatePrincipalTransformerName);
        final String newTransformerNamesSeparatedByNewLine = String.join("\n", transformerName2, transformerName3);
        try {
            transformerUtils.createConstantTransformer(transformerAddress1);
            transformerUtils.createConstantTransformer(transformerAddress2);
            transformerUtils.createConstantTransformer(transformerAddress3);
            createAggregatePrincipalTransformer(aggregatePrincipalTransformerAddress, transformerName1, transformerName2);
            page.navigateToApplication()
                    .selectResource(AGGREGATE_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .selectByName(aggregatePrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, aggregatePrincipalTransformerAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PRINCIPAL_TRANSFORMERS, newTransformerNamesSeparatedByNewLine)
                    .verifyFormSaved("See https://issues.jboss.org/browse/HAL-1337")
                    .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeGenerator.ModelNodeListBuilder()
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

}
