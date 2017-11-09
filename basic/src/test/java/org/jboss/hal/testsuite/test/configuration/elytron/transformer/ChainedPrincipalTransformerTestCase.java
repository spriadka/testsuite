package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.transformer.AddChainedPrincipalTransformerWizard;
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
public class ChainedPrincipalTransformerTestCase extends AbstractElytronTestCase {

    private static final String PRINCIPAL_TRANSFORMERS = "principal-transformers";
    private static final String CHAINED_PRINCIPAL_TRANSFORMER = "chained-principal-transformer";
    private static final String CHAINED_PRINCIPAL_TRANSFORMER_LABEL = "Chained";
    private static final String CONSTANT_PRINCIPAL_TRANSFORMER = "constant-principal-transformer";

    private static final PrincipalTransformerUtils transformerUtils = new PrincipalTransformerUtils(client);

    @Page
    private TransformerPage page;

    /**
     * @tpTestDetails Try to create Elytron Chained Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Chained Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addChainedPrincipalTransformerTest() throws Exception {
        final String chainedPrincipalTransformerName = "chained_principal_transformer_" + randomAlphanumeric(5);
        final String transformerName1 = randomAlphanumeric(7);
        final String transformerName2 = randomAlphanumeric(7);
        final String transformerNamesSeparatedByNewLine = String.join("\n", transformerName1, transformerName2);
        final Address transformerAddress1 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName1);
        final Address transformerAddress2 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName2);
        final Address chainedPrincipalTransformerAddress = elyOps.getElytronAddress(CHAINED_PRINCIPAL_TRANSFORMER,
                chainedPrincipalTransformerName);
        try {
            transformerUtils.createConstantTransformer(transformerAddress1);
            transformerUtils.createConstantTransformer(transformerAddress2);
            page.navigateToApplication()
                    .selectResource(CHAINED_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .addResource(AddChainedPrincipalTransformerWizard.class)
                    .name(chainedPrincipalTransformerName)
                    .principalTransformers(transformerNamesSeparatedByNewLine)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(chainedPrincipalTransformerName));
            new ResourceVerifier(chainedPrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
        final String chainedPrincipalTransformerName = "chained_principal_transformer_" + randomAlphanumeric(5);
        final String transformerName1 = randomAlphanumeric(7);
        final String transformerName2 = randomAlphanumeric(7);
        final Address transformerAddress1 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName1);
        final Address transformerAddress2 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName2);
        final Address chainedPrincipalTransformerAddress = elyOps.getElytronAddress(CHAINED_PRINCIPAL_TRANSFORMER,
                chainedPrincipalTransformerName);
        ResourceVerifier chainedPrincipalTransformerVerifier = new ResourceVerifier(chainedPrincipalTransformerAddress, client);
        try {
            transformerUtils.createConstantTransformer(transformerAddress1);
            transformerUtils.createConstantTransformer(transformerAddress2);
            createChainedPrincipalTransformer(chainedPrincipalTransformerAddress, transformerName1, transformerName2);
            chainedPrincipalTransformerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(CHAINED_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .removeResource(chainedPrincipalTransformerName)
                    .confirmAndDismissReloadRequiredMessage()
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

    private void createChainedPrincipalTransformer(Address chainedPrincipalTransformerAddress,
                                                   String... principalTransformers) throws IOException {
        ops.add(chainedPrincipalTransformerAddress, Values.of(PRINCIPAL_TRANSFORMERS,
                new ModelNodeGenerator.ModelNodeListBuilder().addAll(principalTransformers).build()))
                .assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Chained Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editChainedPrincipalTransformerAttributesTest() throws Exception {
        final String chainedPrincipalTransformerName = "chained_principal_transformer_" + RandomStringUtils.randomAlphanumeric(7);
        final String transformerName1 = RandomStringUtils.randomAlphanumeric(7);
        final String transformerName2 = RandomStringUtils.randomAlphanumeric(7);
        final String transformerName3 = RandomStringUtils.randomAlphanumeric(7);
        final Address transformerAddress1 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName1);
        final Address transformerAddress2 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName2);
        final Address transformerAddress3 = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER, transformerName3);
        final Address chainedPrincipalTransformerAddress = elyOps.getElytronAddress(CHAINED_PRINCIPAL_TRANSFORMER,
                chainedPrincipalTransformerName);
        final String newTransformerNamesSeparatedByNewLine = String.join("\n", transformerName2, transformerName3);
        try {
            transformerUtils.createConstantTransformer(transformerAddress1);
            transformerUtils.createConstantTransformer(transformerAddress2);
            transformerUtils.createConstantTransformer(transformerAddress3);
            createChainedPrincipalTransformer(chainedPrincipalTransformerAddress, transformerName1, transformerName2);
            page.navigateToApplication()
                    .selectResource(CHAINED_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .selectByName(chainedPrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, chainedPrincipalTransformerAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PRINCIPAL_TRANSFORMERS, newTransformerNamesSeparatedByNewLine)
                    .verifyFormSaved("See https://issues.jboss.org/browse/HAL-1337")
                    .verifyAttribute(PRINCIPAL_TRANSFORMERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
}
