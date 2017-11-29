package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.transformer.AddRegexValidatingPrincipalTransformerWizard;
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
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class RegexValidatingPrincipalTransformerTestCase extends AbstractElytronTestCase {

    private static final String REGEX_VALIDATING_PRINCIPAL_TRANSFORMER = "regex-validating-principal-transformer";
    private static final String REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_LABEL = "Regex Validating";
    private static final String PATTERN = "pattern";
    private static final String MATCH = "match";

    @Page
    private TransformerPage page;

    /**
     * @tpTestDetails Try to create Elytron Regex Validating Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Regex Validating Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addRegexValidatingPrincipalTransformerTest() throws Exception {
        final String regexValidatingPrincipalTransformerName = "regex_validating_transformer_" + randomAlphanumeric(5);
        final String regexValidatingPrincipalTransformerPattern = randomAlphanumeric(5);
        final Address regexValidatingPrincipalTransformerAddress = elyOps.getElytronAddress(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER,
                        regexValidatingPrincipalTransformerName);
        try {
            page.navigateToApplication()
                    .selectResource(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .addResource(AddRegexValidatingPrincipalTransformerWizard.class)
                    .name(regexValidatingPrincipalTransformerName)
                    .pattern(regexValidatingPrincipalTransformerPattern)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(regexValidatingPrincipalTransformerName));
            new ResourceVerifier(regexValidatingPrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(PATTERN, regexValidatingPrincipalTransformerPattern);
        } finally {
            ops.removeIfExists(regexValidatingPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Regex Validating Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Regex Validating Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeRegexValidatingPrincipalTransformerTest() throws Exception {
        final String regexValidatingPrincipalTransformerName = "regex_validating_transformer_" + randomAlphanumeric(5);
        final String patternValue = randomAlphanumeric(5);
        final Address regexValidatingPrincipalTransformerAddress = elyOps.getElytronAddress(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER,
                regexValidatingPrincipalTransformerName);
        final ResourceVerifier regexValidatingPrincipalTransformerVerifier =
                new ResourceVerifier(regexValidatingPrincipalTransformerAddress, client);
        try {
            createRegexValidatingPrincipalTransformerInModel(patternValue, regexValidatingPrincipalTransformerAddress);
            regexValidatingPrincipalTransformerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .removeResource(regexValidatingPrincipalTransformerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(regexValidatingPrincipalTransformerName));
            regexValidatingPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(regexValidatingPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createRegexValidatingPrincipalTransformerInModel(String patternValue, Address regexValidatingPrincipalTransformerAddress) throws IOException {
        ops.add(regexValidatingPrincipalTransformerAddress, Values.of(PATTERN, patternValue)).assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Regex Validating Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editRegexValidatingPrincipalTransformerAttributesTest() throws Exception {
        final String regexValidatingPrincipalTransformerName = "regex_validating_transformer_" + randomAlphanumeric(5);
        final String initialPattern = randomAlphanumeric(5);
        final String pattern = randomAlphanumeric(5);
        final Address regexValidatingPrincipalTransformerAddress = elyOps.getElytronAddress(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER,
                        regexValidatingPrincipalTransformerName);
        try {
            createRegexValidatingPrincipalTransformerInModel(initialPattern, regexValidatingPrincipalTransformerAddress);
            page.navigateToApplication()
                    .selectResource(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager().selectByName(regexValidatingPrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, regexValidatingPrincipalTransformerAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, PATTERN, pattern)
                    .edit(CHECKBOX, MATCH, false)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(PATTERN, pattern)
                    .verifyAttribute(MATCH, false);
        } finally {
            ops.removeIfExists(regexValidatingPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

}
