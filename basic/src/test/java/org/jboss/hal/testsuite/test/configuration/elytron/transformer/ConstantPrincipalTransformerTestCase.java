package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.transformer.AddConstantPrincipalTransformerWizard;
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
public class ConstantPrincipalTransformerTestCase extends AbstractElytronTestCase {

    private static final String CONSTANT_PRINCIPAL_TRANSFORMER = "constant-principal-transformer";
    private static final String CONSTANT_PRINCIPAL_TRANSFORMER_LABEL = "Constant";
    private static final String CONSTANT = "constant";


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
        final String constantPrincipalTransformerName = "constant_principal_transformer_" + randomAlphanumeric(5);
        final String constantPrincipalTransformerValue = randomAlphanumeric(5);
        final Address constantPrincipalTransformerAddress = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER,
                constantPrincipalTransformerName);
        try {
            page.navigateToApplication()
                    .selectResource(CONSTANT_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .addResource(AddConstantPrincipalTransformerWizard.class)
                    .name(constantPrincipalTransformerName)
                    .constant(constantPrincipalTransformerValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
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
        final String constantPrincipalTransformerName = "constant_principal_transformer_" + randomAlphanumeric(5);
        final String constantPrincipalTransformerValue = randomAlphanumeric(5);
        final Address constantPrincipalTransformerAddress = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER,
                constantPrincipalTransformerName);
        final ResourceVerifier constantPrincipalTransformerVerifier =
                new ResourceVerifier(constantPrincipalTransformerAddress, client);
        try {
            createConstantPrincipalTransformerInModel(constantPrincipalTransformerAddress, constantPrincipalTransformerValue
            );
            constantPrincipalTransformerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(CONSTANT_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .removeResource(constantPrincipalTransformerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(constantPrincipalTransformerName));
            constantPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(constantPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createConstantPrincipalTransformerInModel(Address constantPrincipalTransformerAddress,
                                                           String constantPrincipalTransformerValue) throws IOException {
        ops.add(constantPrincipalTransformerAddress, Values.of(CONSTANT, constantPrincipalTransformerValue))
                .assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Constant Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editConstantPrincipalTransformerAttributesTest() throws Exception {
        final String constantPrincipalTransformerName = "constant_principal_transformer_" + randomAlphanumeric(5);
        final String constantPrincipalTransformerOriginalValue = randomAlphanumeric(5);
        final String constantPrincipalTransformerNewValue = randomAlphanumeric(5);
        final Address constantPrincipalTransformerAddress = elyOps.getElytronAddress(CONSTANT_PRINCIPAL_TRANSFORMER,
                constantPrincipalTransformerName);
        try {
            createConstantPrincipalTransformerInModel(constantPrincipalTransformerAddress,
                    constantPrincipalTransformerOriginalValue);
            page.navigateToApplication()
                    .selectResource(CONSTANT_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .selectByName(constantPrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, constantPrincipalTransformerAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, CONSTANT, constantPrincipalTransformerNewValue)
                    .verifyFormSaved()
                    .verifyAttribute(CONSTANT, constantPrincipalTransformerNewValue);
        } finally {
            ops.removeIfExists(constantPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }
}
