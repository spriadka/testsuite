package org.jboss.hal.testsuite.test.configuration.elytron.decoder;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.decoder.AddSimpleRoleDecoderWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class SimpleRoleDecoderTestCase extends AbstractElytronTestCase {

    private static final String SIMPLE_ROLE_DECODER_LABEL = "Simple Role Decoder";
    private static final String SIMPLE_ROLE_DECODER = "simple-role-decoder";
    private static final String ATTRIBUTE = "attribute";

    @Page
    private MapperDecoderPage page;

    /**
     * @tpTestDetails Try to create Elytron Simple Role Decoder instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Simple Role Decoder table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addSimpleRoleDecoderTest() throws Exception {

        final String simpleRoleDecoderName = "simple_role_decoder_" + RandomStringUtils.randomAlphanumeric(7);
        final String attributeValue = RandomStringUtils.randomAlphanumeric(7);
        final Address simpleRoleDecoderAddress = elyOps.getElytronAddress(SIMPLE_ROLE_DECODER, simpleRoleDecoderName);
        try {
            page.navigateToDecoder()
                    .selectResource(SIMPLE_ROLE_DECODER_LABEL)
                    .getResourceManager()
                    .addResource(AddSimpleRoleDecoderWizard.class)
                    .name(simpleRoleDecoderName)
                    .attribute(attributeValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created simple role decoder should be present in the table",
                    page.getResourceManager().isResourcePresent(simpleRoleDecoderName));
            new ResourceVerifier(simpleRoleDecoderAddress, client)
                    .verifyExists()
                    .verifyAttribute(ATTRIBUTE, attributeValue);
        } finally {
            ops.removeIfExists(simpleRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Simple Role Decoder instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Simple Role Decoder table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeSimpleRoleDecoderTest() throws Exception {
        final String simpleRoleDecoderName = "simple_role_decoder_" + RandomStringUtils.randomAlphanumeric(7);
        final Address simpleRoleDecoderAddress = elyOps.getElytronAddress(SIMPLE_ROLE_DECODER, simpleRoleDecoderName);
        final ResourceVerifier verifier = new ResourceVerifier(simpleRoleDecoderAddress, client);
        try {
            createSimpleRoleDecoderInModel(simpleRoleDecoderAddress);
            verifier.verifyExists();
            page.navigateToDecoder()
                    .selectResource(SIMPLE_ROLE_DECODER_LABEL)
                    .getResourceManager()
                    .removeResource(simpleRoleDecoderName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed simple role decoder should not be present in the table",
                    page.getResourceManager().isResourcePresent(simpleRoleDecoderName));
            verifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(simpleRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createSimpleRoleDecoderInModel(Address simpleRoleDecoderAddress) throws IOException, TimeoutException, InterruptedException {
        final String attributeValue = RandomStringUtils.randomAlphanumeric(7);
        ops.add(simpleRoleDecoderAddress, Values.of(ATTRIBUTE, attributeValue)).assertSuccess();
        adminOps.reloadIfRequired();
    }

    /**
     * @tpTestDetails Create Elytron Simple Role Decoder instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editAttributesTest() throws Exception {
        final String simpleRoleDecoderName = "simple_role_decoder_" + RandomStringUtils.randomAlphanumeric(7);
        final String attributeValue = RandomStringUtils.randomAlphanumeric(7);
        final Address simpleRoleDecoderAddress = elyOps.getElytronAddress(SIMPLE_ROLE_DECODER, simpleRoleDecoderName);
        try {
            createSimpleRoleDecoderInModel(simpleRoleDecoderAddress);
            page.navigateToDecoder()
                    .selectResource(SIMPLE_ROLE_DECODER_LABEL)
                    .getResourceManager()
                    .selectByName(simpleRoleDecoderName);
            new ConfigChecker.Builder(client, simpleRoleDecoderAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, ATTRIBUTE, attributeValue)
                    .verifyFormSaved()
                    .verifyAttribute(ATTRIBUTE, attributeValue);
        } finally {
            ops.removeIfExists(simpleRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }
}
