package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddProviderLoaderWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
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
public class ProviderLoaderTestCase extends AbstractElytronTestCase {

    private static final String ELYTRON_MODULE_NAME = "org.wildfly.security.elytron";
    private static final String BC_MODULE_NAME = "org.bouncycastle";
    private static final String BC_CLASS_NAME = "org.bouncycastle.jce.provider.BouncyCastleProvider";
    private static final String PROVIDER_LOADER = "provider-loader";
    private static final String PROVIDER_LOADER_LABEL = "Provider Loader";
    private static final String CLASS_NAMES = "class-names";
    private static final String MODULE = "module";

    @Page
    private SSLPage page;

    /**
     * @tpTestDetails Try to create Elytron provider loader instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in provider loader table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void addProviderLoaderTest() throws Exception {
        final String providerLoaderName = "provider_loader_" + randomAlphanumeric(5);
        final Address providerLoaderAddress = elyOps.getElytronAddress(PROVIDER_LOADER, providerLoaderName);
        try {
            page.navigateToApplication()
                    .selectResource(PROVIDER_LOADER_LABEL)
                    .getResourceManager()
                    .addResource(AddProviderLoaderWizard.class)
                    .name( providerLoaderName)
                    .classNames(BC_CLASS_NAME)
                    .module(BC_MODULE_NAME)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(providerLoaderName));
            new ResourceVerifier(providerLoaderAddress, client).verifyExists()
                    .verifyAttribute(MODULE, BC_MODULE_NAME)
                    .verifyAttribute(CLASS_NAMES, new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(BC_CLASS_NAME)).build());
        } finally {
            ops.removeIfExists(providerLoaderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron provider loader instance and try to remove it in Web Console's Elytron subsystem
     * configuration.
     * Validate removed resource is not any more visible in provider loader table.
     * Validate removed resource is not any more present in model.
     */
    @Test
    public void removeProviderLoaderTest() throws Exception {
        final String providerLoaderName = "provider_loader_" + randomAlphanumeric(5);
        final Address providerLoaderAddress = elyOps.getElytronAddress(PROVIDER_LOADER, providerLoaderName);
        try {
            ops.add(providerLoaderAddress).assertSuccess();
            page.navigateToApplication().selectResource(PROVIDER_LOADER_LABEL).getResourceManager()
                    .removeResource(providerLoaderName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(providerLoaderName));
            new ResourceVerifier(providerLoaderAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(providerLoaderAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron provider loader instance and try to edit it's attributes in Web Console's Elytron
     * subsystem configuration.
     * Validate edited attributes in model.
     */
    @Test
    public void editAttributesTest() throws Exception {
        final String providerLoaderName = "provider_loader_" + randomAlphanumeric(5);
        final Address providerLoaderAddress = elyOps.getElytronAddress(PROVIDER_LOADER, providerLoaderName);
        try {
            ops.add(providerLoaderAddress, Values.of(MODULE, BC_MODULE_NAME)
                    .and(CLASS_NAMES, new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(BC_CLASS_NAME)).build()));
            page.navigateToApplication().selectResource(PROVIDER_LOADER_LABEL).getResourceManager()
                    .selectByName(providerLoaderName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, providerLoaderAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, CLASS_NAMES, "")
                    .edit(TEXT, MODULE, ELYTRON_MODULE_NAME)
                    .andSave().verifyFormSaved()
                    .verifyAttributeIsUndefined(CLASS_NAMES)
                    .verifyAttribute(MODULE, ELYTRON_MODULE_NAME);
        } finally {
            ops.removeIfExists(providerLoaderAddress);
            adminOps.reloadIfRequired();
        }
    }
}
