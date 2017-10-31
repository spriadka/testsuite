package org.jboss.hal.testsuite.test.configuration.elytron.decoder;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.decoder.AddX500PrincipalDecoderWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class X500AttributePrincipalDecoderTestCase extends AbstractElytronTestCase {
    private static final String JOINER = "joiner";
    private static final String X500_ATTRIBUTE_PRINCIPAL_DECODER = "x500-attribute-principal-decoder";
    private static final String X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL = "X500 Attribute Principal Decoder";
    private static final String MAXIMUM_SEGMENTS = "maximum-segments";
    private static final String OID = "oid";
    private static final String REVERSE = "reverse";

    @Page
    private MapperDecoderPage page;

    /**
     * @tpTestDetails Try to create Elytron X500 Attribute Principal Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in X500 Attribute Principal Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addX500AttributePrincipalDecoderTest() throws Exception {
        final String x500AttributePrincipalDecoderName = randomAlphanumeric(5);
        final String oidValue = randomAlphanumeric(5);
        final Address x500AttributePrincipalDecoderAddress = elyOps.getElytronAddress(X500_ATTRIBUTE_PRINCIPAL_DECODER,
                x500AttributePrincipalDecoderName);

        try {
            page.navigateToDecoder()
                    .selectResource(X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .addResource(AddX500PrincipalDecoderWizard.class)
                    .name(x500AttributePrincipalDecoderName)
                    .oid(oidValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(x500AttributePrincipalDecoderName));
            new ResourceVerifier(x500AttributePrincipalDecoderAddress, client).verifyExists()
                    .verifyAttribute(OID, oidValue);
        } finally {
            ops.removeIfExists(x500AttributePrincipalDecoderAddress);
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
    public void addX500AttributePrincipalDecoderInvalidCombinationTest() throws Exception {
        final String x500AttributePrincipalDecoderName = randomAlphanumeric(5);
        final String attributeNameValue = randomAlphanumeric(5);
        final String oidValue = randomAlphanumeric(5);
        final Address x500AttributePrincipalDecoderAddress = elyOps.getElytronAddress(X500_ATTRIBUTE_PRINCIPAL_DECODER,
                x500AttributePrincipalDecoderName);

        try {
            AddX500PrincipalDecoderWizard wizard = page.navigateToDecoder()
                    .selectResource(X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .addResource(AddX500PrincipalDecoderWizard.class);
            wizard.name(x500AttributePrincipalDecoderName)
                    .attributeName(attributeNameValue)
                    .oid(oidValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowOpen();
            assertTrue("Validation error regarding using both \"attribute-name\" and \"oid\" at the same time should be visible",
                    page.getWindowFragment().isErrorShownInForm());
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
        final String x500AttributePrincipalDecoderName = randomAlphanumeric(5);
        final String originalOidValue = randomAlphanumeric(5);
        final String newOidValue = randomAlphanumeric(5);
        final String newJoinerValue = randomAlphanumeric(5);
        int newMaximumSegmentsValue = 17;
        final Address x500AttributePrincipalDecoderAddress = elyOps.getElytronAddress(X500_ATTRIBUTE_PRINCIPAL_DECODER,
                x500AttributePrincipalDecoderName);

        try {
            ops.add(x500AttributePrincipalDecoderAddress, Values.of(OID, originalOidValue)).assertSuccess();

            page.navigateToDecoder()
                    .selectResource(X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
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
        final String x500AttributePrincipalDecoderName = randomAlphanumeric(5);
        final String oidValue = randomAlphanumeric(5);
        final Address x500AttributePrincipalDecoderAddress = elyOps.getElytronAddress(X500_ATTRIBUTE_PRINCIPAL_DECODER,
                x500AttributePrincipalDecoderName);
        ResourceVerifier x500AttributePrincipalDecoderVerifier = new ResourceVerifier(x500AttributePrincipalDecoderAddress, client);

        try {
            ops.add(x500AttributePrincipalDecoderAddress, Values.of(OID, oidValue)).assertSuccess();
            x500AttributePrincipalDecoderVerifier.verifyExists();
            page.navigateToDecoder()
                    .selectResource(X500_ATTRIBUTE_PRINCIPAL_DECODER_LABEL)
                    .getResourceManager()
                    .removeResource(x500AttributePrincipalDecoderName)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(x500AttributePrincipalDecoderName));
            x500AttributePrincipalDecoderVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(x500AttributePrincipalDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }
}
