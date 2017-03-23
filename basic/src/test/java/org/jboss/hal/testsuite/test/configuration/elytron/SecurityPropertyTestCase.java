package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecurityPropertyTestCase extends AbstractElytronTestCase {

    private static final String
        VALUE = "value",
        SECURITY_PROPERTY = "security-property",
        SECURITY_PROPERTY_LABEL = "Security Property";

    @Page
    private SSLPage page;

    /**
     * @tpTestDetails Try to create Elytron Security Property instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Security Property table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addSecurityPropertyTest() throws Exception {
        String securityPropertyName = randomAlphanumeric(5), securityPropertyValue = randomAlphanumeric(5);
        Address securityPropertyAddress = elyOps.getElytronAddress(SECURITY_PROPERTY, securityPropertyName);

        page.navigateToApplication().selectResource(SECURITY_PROPERTY_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, securityPropertyName);
            editor.text(VALUE, securityPropertyValue);

            assertTrue("Dialog should be closed!", wizard.finish());
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(securityPropertyName));
            new ResourceVerifier(securityPropertyAddress, client).verifyExists()
                    .verifyAttribute(VALUE, securityPropertyValue);
        } finally {
            ops.removeIfExists(securityPropertyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Security Property instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Security Property table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeSecurityPropertyTest() throws Exception {
        String securityPropertyName = randomAlphanumeric(5), securityPropertyValue = randomAlphanumeric(5);
        Address securityPropertyAddress = elyOps.getElytronAddress(SECURITY_PROPERTY, securityPropertyName);
        ResourceVerifier securityPropertyVerifier = new ResourceVerifier(securityPropertyAddress, client);

        try {
            ops.add(securityPropertyAddress, Values.of(VALUE, securityPropertyValue));
            securityPropertyVerifier.verifyExists();

            page.navigateToApplication().selectResource(SECURITY_PROPERTY_LABEL).getResourceManager()
                    .removeResource(securityPropertyName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(securityPropertyName));
            securityPropertyVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(securityPropertyAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Security Property instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editSecurityPropertyAttributesTest() throws Exception {
        String securityPropertyName = randomAlphanumeric(5), securityPropertyOriginalValue = randomAlphanumeric(5),
                    securityPropertyNewValue = randomAlphanumeric(5);
        Address securityPropertyAddress = elyOps.getElytronAddress(SECURITY_PROPERTY, securityPropertyName);

        try {
            ops.add(securityPropertyAddress, Values.of(VALUE, securityPropertyOriginalValue)).assertSuccess();

            page.navigateToApplication().selectResource(SECURITY_PROPERTY_LABEL).getResourceManager()
                    .selectByName(securityPropertyName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, securityPropertyAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, VALUE, securityPropertyNewValue).verifyFormSaved()
                    .verifyAttribute(VALUE, securityPropertyNewValue);
        } finally {
            ops.removeIfExists(securityPropertyAddress);
            adminOps.reloadIfRequired();
        }
    }
}
