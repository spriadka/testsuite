package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.transformer.AddRegexPrincipalTransformerWizard;
import org.jboss.hal.testsuite.page.config.elytron.TransformerPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class RegexPrincipalTransformerTestCase extends AbstractElytronTestCase {

    private static final String REGEX_PRINCIPAL_TRANSFORMER = "regex-principal-transformer";
    private static final String REGEX_PRINCIPAL_TRANSFORMER_LABEL = "Regex";
    private static final String PATTERN = "pattern";
    private static final String REPLACEMENT = "replacement";
    private static final String REPLACE_ALL = "replace-all";

    @Page
    private TransformerPage page;

    /**
     * @tpTestDetails Try to create Elytron Regex Principal Transformer instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Regex Principal Transformer table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addRegexPrincipalTransformerTest() throws Exception {
        final String regexPrincipalTransformerName = "regex_principal_transformer_" + randomAlphanumeric(5);
        final String regexPrincipalTransformerPattern = randomAlphanumeric(5);
        final String regexPrincipalTransformerReplacement = randomAlphanumeric(5);
        final Address regexPrincipalTransformerAddress = elyOps.getElytronAddress(REGEX_PRINCIPAL_TRANSFORMER,
                regexPrincipalTransformerName);
        try {
            page.navigateToApplication()
                    .selectResourceWithExactLabelMatch(REGEX_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .addResource(AddRegexPrincipalTransformerWizard.class)
                    .name(regexPrincipalTransformerName)
                    .pattern(regexPrincipalTransformerPattern)
                    .replacement(regexPrincipalTransformerReplacement)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(regexPrincipalTransformerName));
            new ResourceVerifier(regexPrincipalTransformerAddress, client).verifyExists()
                    .verifyAttribute(PATTERN, regexPrincipalTransformerPattern)
                    .verifyAttribute(REPLACEMENT, regexPrincipalTransformerReplacement);
        } finally {
            ops.removeIfExists(regexPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Regex Principal Transformer instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Regex Principal Transformer table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeRegexPrincipalTransformerTest() throws Exception {
        final String regexPrincipalTransformerName = "regex_principal_transformer_" + randomAlphanumeric(5);
        final Address regexPrincipalTransformerAddress = elyOps.getElytronAddress(REGEX_PRINCIPAL_TRANSFORMER,
                regexPrincipalTransformerName);
        final ResourceVerifier regexPrincipalTransformerVerifier =
            new ResourceVerifier(regexPrincipalTransformerAddress, client);
        try {
            createRegexPrincipalTransformerInModel(regexPrincipalTransformerAddress);
            regexPrincipalTransformerVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResourceWithExactLabelMatch(REGEX_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .removeResource(regexPrincipalTransformerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(regexPrincipalTransformerName));
            regexPrincipalTransformerVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(regexPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createRegexPrincipalTransformerInModel(Address regexPrincipalTransformerAddress) throws IOException {
        final String patternValue = randomAlphanumeric(7);
        final String replacementValue = randomAlphanumeric(7);
        ops.add(regexPrincipalTransformerAddress, Values.of(PATTERN, patternValue)
                .and(REPLACEMENT, replacementValue)).assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Regex Principal Transformer instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editRegexPrincipalTransformerAttributesTest() throws Exception {
        final String regexPrincipalTransformerName = "regex_principal_transformer_" + randomAlphanumeric(5);
        final String patternValue = randomAlphanumeric(5);
        final String replacementValue = randomAlphanumeric(5);
        final Address regexPrincipalTransformerAddress = elyOps.getElytronAddress(REGEX_PRINCIPAL_TRANSFORMER,
                        regexPrincipalTransformerName);
        try {
            createRegexPrincipalTransformerInModel(regexPrincipalTransformerAddress);
            page.navigateToApplication()
                    .selectResourceWithExactLabelMatch(REGEX_PRINCIPAL_TRANSFORMER_LABEL)
                    .getResourceManager()
                    .selectByName(regexPrincipalTransformerName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, regexPrincipalTransformerAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, PATTERN, patternValue)
                    .edit(CHECKBOX, REPLACE_ALL, true)
                    .edit(TEXT, REPLACEMENT, replacementValue)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(PATTERN, patternValue)
                    .verifyAttribute(REPLACE_ALL, true)
                    .verifyAttribute(REPLACEMENT, replacementValue);
        } finally {
            ops.removeIfExists(regexPrincipalTransformerAddress);
            adminOps.reloadIfRequired();
        }
    }

}
