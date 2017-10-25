package org.jboss.hal.testsuite.test.configuration.elytron.decoder;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.decoder.AddConcatenatingPrincipalDecoderWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class ConcatenatingPrincipalDecoderTestCase extends AbstractElytronTestCase {

    private static final String CONCATENATING_PRINCIPAL_DECODER = "concatenating-principal-decoder";
    private static final String CONCATENATING_PRINCIPAL_DECODER_LABEL = "Concatenating Principal Decoder";
    private static final String CONSTANT_PRINCIPAL_DECODER = "constant-principal-decoder";
    private static final String CONSTANT = "constant";
    private static final String PRINCIPAL_DECODERS = "principal-decoders";
    private static final String JOINER = "joiner";


    @Page
    private MapperDecoderPage page;

    /**
     * @tpTestDetails Try to create Elytron Concatenating Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Concatenating Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addConcatenatingPrincipalDecoderTest() throws Exception {
        final String concatenatingPrincipalDecoderName = randomAlphanumeric(5);
        final String constantPrincipalDecoder1name = randomAlphanumeric(5);
        final String constantPrincipalDecoder2name = randomAlphanumeric(5);
        final String constant1value = randomAlphanumeric(5);
        final String constant2value = randomAlphanumeric(5);
        final Address concatenatingPrincipalDecoderAddress = elyOps.getElytronAddress(CONCATENATING_PRINCIPAL_DECODER,
                concatenatingPrincipalDecoderName);
        final Address constantPrincipalDecoder1address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder1name);
        final Address constantPrincipalDecoder2address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoder2name);

        try {
            createConstantPrincipalDecoderInModel(constantPrincipalDecoder1address, constant1value);
            createConstantPrincipalDecoderInModel(constantPrincipalDecoder2address, constant2value);
            page.navigateToDecoder()
                    .selectResource(CONCATENATING_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .addResource(AddConcatenatingPrincipalDecoderWizard.class)
                    .name(concatenatingPrincipalDecoderName)
                    .principalDecoders(constantPrincipalDecoder1name + "\n" + constantPrincipalDecoder2name)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(concatenatingPrincipalDecoderName));
            new ResourceVerifier(concatenatingPrincipalDecoderAddress, client)
                    .verifyExists()
                    .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
                            .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                            .build());
        } finally {
            ops.removeIfExists(concatenatingPrincipalDecoderAddress);
            ops.removeIfExists(constantPrincipalDecoder1address);
            ops.removeIfExists(constantPrincipalDecoder2address);
            adminOps.reloadIfRequired();
        }
    }

    private void createConstantPrincipalDecoderInModel(Address constantPrincipalDecoderAddress, String value) throws IOException, TimeoutException, InterruptedException {
        ops.add(constantPrincipalDecoderAddress, Values.of(CONSTANT, value)).assertSuccess();
        adminOps.reloadIfRequired();
    }

    /**
     * @tpTestDetails Create Elytron Concatenating Principal Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editConcatenatingPrincipalDecoderAttributesTest() throws Exception {
        final String concatenatingPrincipalDecoderName = randomAlphanumeric(5);
        final String constantPrincipalDecoder1name = randomAlphanumeric(5);
        final String constantPrincipalDecoder2name = randomAlphanumeric(5);
        final String constantPrincipalDecoder3name = randomAlphanumeric(5);
        final String constant1value = randomAlphanumeric(5);
        final String constant2value = randomAlphanumeric(5);
        final String constant3value = randomAlphanumeric(5);
        final String joinerValue = randomAlphanumeric(5);
        final Address concatenatingPrincipalDecoderAddress = elyOps.getElytronAddress(CONCATENATING_PRINCIPAL_DECODER,
                concatenatingPrincipalDecoderName);
        final Address constantPrincipalDecoder1address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder1name);
        final Address constantPrincipalDecoder2address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder2name);
        final Address constantPrincipalDecoder3address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder3name);

        try {
            createConstantPrincipalDecoderInModel(constantPrincipalDecoder1address, constant1value);
            createConstantPrincipalDecoderInModel(constantPrincipalDecoder2address, constant2value);
            createConstantPrincipalDecoderInModel(constantPrincipalDecoder3address, constant3value);
            ops.add(concatenatingPrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
                    .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                    .build())).assertSuccess();

            page.navigateToDecoder()
                    .selectResource(CONCATENATING_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .selectByName(concatenatingPrincipalDecoderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, concatenatingPrincipalDecoderAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, JOINER, joinerValue)
                    .edit(TEXT, PRINCIPAL_DECODERS, constantPrincipalDecoder3name + "\n"
                            + constantPrincipalDecoder1name)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JOINER, joinerValue)
                    .verifyAttribute(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
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
        final String concatenatingPrincipalDecoderName = randomAlphanumeric(5);
        final String constantPrincipalDecoder1name = randomAlphanumeric(5);
        final String constantPrincipalDecoder2name = randomAlphanumeric(5);
        final String constant1value = randomAlphanumeric(5);
        final String constant2value = randomAlphanumeric(5);
        final Address concatenatingPrincipalDecoderAddress = elyOps.getElytronAddress(CONCATENATING_PRINCIPAL_DECODER,
                concatenatingPrincipalDecoderName);
        final Address constantPrincipalDecoder1address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder1name);
        final Address constantPrincipalDecoder2address = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoder2name);
        ResourceVerifier concatenatingPrincipalDecoderVerifier = new ResourceVerifier(concatenatingPrincipalDecoderAddress, client);

        try {
            createConstantPrincipalDecoderInModel(constantPrincipalDecoder1address, constant1value);
            createConstantPrincipalDecoderInModel(constantPrincipalDecoder2address, constant2value);
            ops.add(concatenatingPrincipalDecoderAddress, Values.of(PRINCIPAL_DECODERS, new ModelNodeGenerator.ModelNodeListBuilder()
                    .addAll(constantPrincipalDecoder1name, constantPrincipalDecoder2name)
                    .build()))
                    .assertSuccess();
            concatenatingPrincipalDecoderVerifier.verifyExists();

            page.navigateToDecoder()
                    .selectResource(CONCATENATING_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .removeResource(concatenatingPrincipalDecoderName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(concatenatingPrincipalDecoderName));
            concatenatingPrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(concatenatingPrincipalDecoderAddress);
            ops.removeIfExists(constantPrincipalDecoder1address);
            ops.removeIfExists(constantPrincipalDecoder2address);
            adminOps.reloadIfRequired();
        }
    }

}
