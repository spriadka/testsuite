package org.jboss.hal.testsuite.test.configuration.elytron.principal.decoder;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class AggregatePrincipalDecoderTestCase extends AbstractElytronTestCase {

    private static final String AGGREGATE_PRINCIPAL_DECODER = "aggregate-principal-decoder";
    private static final String AGGREGATE_PRINCIPAL_DECODER_LABEL = "Aggregate Principal Decoder";
    private static final String PRINCIPAL_DECODERS = "principal-decoders";
    private static final String CONSTANT = "constant";
    private static final String CONSTANT_PRINCIPAL_DECODER = "constant-principal-decoder";

    @Page
    private MapperDecoderPage page;

    /**
     * @tpTestDetails Try to create Elytron Aggregate Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Aggregate Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addAggregatePrincipalDecoderTest() throws Exception {
        final String aggregatePrincipalDecoderName = randomAlphanumeric(5);
        final String constantPrincipalDecoder1name = randomAlphanumeric(5);
        final String constantPrincipalDecoder2name = randomAlphanumeric(5);
        final String constant1value = randomAlphanumeric(5);
        final String constant2value = randomAlphanumeric(5);
        final Address aggregatePrincipalDecoderAddress = elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_DECODER,
                aggregatePrincipalDecoderName);
        final Address constantPrincipalDecoder1address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder1name);
        final Address constantPrincipalDecoder2address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder2name);

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
            new ResourceVerifier(aggregatePrincipalDecoderAddress, client)
                    .verifyExists()
                    .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
        final String aggregatePrincipalDecoderName = randomAlphanumeric(5);
        final String constantPrincipalDecoder1name = randomAlphanumeric(5);
        final String constantPrincipalDecoder2name = randomAlphanumeric(5);
        final String constantPrincipalDecoder3name = randomAlphanumeric(5);
        final String constant1value = randomAlphanumeric(5);
        final String constant2value = randomAlphanumeric(5);
        final String constant3value = randomAlphanumeric(5);
        final Address aggregatePrincipalDecoderAddress = elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_DECODER,
                aggregatePrincipalDecoderName);
        final Address constantPrincipalDecoder1address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder1name);
        final Address constantPrincipalDecoder2address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder2name);
        final Address constantPrincipalDecoder3address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder3name);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            ops.add(constantPrincipalDecoder3address, Values.of(CONSTANT, constant3value)).assertSuccess();
            ops.add(aggregatePrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
                    .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
        final String aggregatePrincipalDecoderName = randomAlphanumeric(5);
        final String constantPrincipalDecoder1name = randomAlphanumeric(5);
        final String constantPrincipalDecoder2name = randomAlphanumeric(5);
        final String constant1value = randomAlphanumeric(5);
        final String constant2value = randomAlphanumeric(5);
        final Address aggregatePrincipalDecoderAddress = elyOps.getElytronAddress(AGGREGATE_PRINCIPAL_DECODER,
                aggregatePrincipalDecoderName);
        final Address constantPrincipalDecoder1address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder1name);
        final Address constantPrincipalDecoder2address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder2name);
        ResourceVerifier aggregatePrincipalDecoderVerifier = new ResourceVerifier(aggregatePrincipalDecoderAddress, client);

        try {
            ops.add(constantPrincipalDecoder1address, Values.of(CONSTANT, constant1value)).assertSuccess();
            ops.add(constantPrincipalDecoder2address, Values.of(CONSTANT, constant2value)).assertSuccess();
            ops.add(aggregatePrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
            ops.removeIfExists(constantPrincipalDecoder1address);
            ops.removeIfExists(constantPrincipalDecoder2address);
            adminOps.reloadIfRequired();
        }
    }
}
