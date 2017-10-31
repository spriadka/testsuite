package org.jboss.hal.testsuite.test.configuration.elytron.decoder;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.decoder.AddConstantPrincipalDecoderWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
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
public class ConstantPrincipalDecoderTestCase extends AbstractElytronTestCase {

    private static final String CONSTANT_PRINCIPAL_DECODER = "constant-principal-decoder";
    private static final String CONSTANT_PRINCIPAL_DECODER_LABEL = "Constant Principal Decoder";
    private static final String CONSTANT = "constant";

    @Page
    private MapperDecoderPage page;

    /**
     * @tpTestDetails Try to create Elytron Constant Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Constant Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addConstantPrincipalDecoderTest() throws Exception {
        final String constantPrincipalDecoderName = randomAlphanumeric(5);
        final String constantValue = randomAlphanumeric(5);
        final Address constantPrincipalDecoderAddress = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER, constantPrincipalDecoderName);

        try {
            page.navigateToDecoder()
                    .selectResource(CONSTANT_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .addResource(AddConstantPrincipalDecoderWizard.class)
                    .name(constantPrincipalDecoderName)
                    .constant(constantValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

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
        final String constantPrincipalDecoderName = randomAlphanumeric(5);
        final String originalAttributeValue = randomAlphanumeric(5);
        final String newAttributeValue = randomAlphanumeric(5);
        final Address constantPrincipalDecoderAddress = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoderName);
        try {
            createConstantPrincipalDecoderInModel(constantPrincipalDecoderAddress, originalAttributeValue);

            page.navigateToDecoder()
                    .selectResource(CONSTANT_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
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

    private void createConstantPrincipalDecoderInModel(Address constantPrincipalDecoderAddress, String originalAttributeValue) throws IOException {
        ops.add(constantPrincipalDecoderAddress, Values.of(CONSTANT, originalAttributeValue)).assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Constant Principal Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Constant Principal Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeConstantPrincipalDecoderTest() throws Exception {
        final String constantPrincipalDecoderName = randomAlphanumeric(5);
        final String constantValue = randomAlphanumeric(5);
        final Address constantPrincipalDecoderAddress = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_DECODER,
                constantPrincipalDecoderName);
        ResourceVerifier constantPrincipalDecoderVerifier = new ResourceVerifier(constantPrincipalDecoderAddress, client);

        try {
            createConstantPrincipalDecoderInModel(constantPrincipalDecoderAddress, constantValue);
            constantPrincipalDecoderVerifier.verifyExists();
            page.navigateToDecoder()
                    .selectResource(CONSTANT_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .removeResource(constantPrincipalDecoderName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(constantPrincipalDecoderName));
            constantPrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(constantPrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }
}
