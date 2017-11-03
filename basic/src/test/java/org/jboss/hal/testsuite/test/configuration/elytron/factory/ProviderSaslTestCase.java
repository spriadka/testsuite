package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddProviderServerWizard;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations.PROVIDER_LOADER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class ProviderSaslTestCase extends ElytronFactoryTestCaseAbstract {


    private static final String PROVIDERS = "providers";
    private static final String PROVIDER_SASL_SERVER_LABEL = "Provider SASL Server";
    private static final String PROVIDER_SASL_SERVER_FACTORY = "provider-sasl-server-factory";

    @BeforeClass
    public static void beforeClass() throws IOException {
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_2);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(BubuCustomCredentialFactory.class, ChachaCustomCredentialFactory.class);
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_2);
        adminOps.reloadIfRequired();
    }

    @Test
    public void addProviderSaslServerRequiredFieldsTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, factoryName);
        try {
            page.navigateToApplication()
                    .selectFactory(PROVIDER_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .addResource(AddProviderServerWizard.class)
                    .name(factoryName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created Provider SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addProviderSaslServerOptionalFieldsTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, factoryName);

        try {
            page.navigateToApplication()
                    .selectFactory(PROVIDER_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .addResource(AddProviderServerWizard.class)
                    .name(factoryName)
                    .providers(PROVIDER_LOADER_NAME_1)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Newly created Provider SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client)
                    .verifyExists()
                    .verifyAttribute(PROVIDERS, PROVIDER_LOADER_NAME_1);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addProviderSaslServerWithoutNameTest() {
        page.navigateToApplication()
                .selectFactory(PROVIDER_SASL_SERVER_LABEL)
                .getResourceManager()
                .addResource(AddProviderServerWizard.class)
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowOpen();
        assertTrue("Page should be displaying validation error regarding missing name attribute",
                page.getWindowFragment().isErrorShownInForm());
    }

    @Test
    public void editProviderSaslServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, factoryName);

        try {
            ops.add(factoryAddress, Values.of(PROVIDERS, PROVIDER_LOADER_NAME_1)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(PROVIDER_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .selectByName(factoryName);
            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PROVIDERS, PROVIDER_LOADER_NAME_2).verifyFormSaved()
                    .verifyAttribute(PROVIDERS, PROVIDER_LOADER_NAME_2);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeProviderSaslServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, factoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(factoryAddress, client);

        try {
            ops.add(factoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            factoryVerifier.verifyExists();

            page.navigateToApplication().selectFactory(PROVIDER_SASL_SERVER_LABEL);

            page.getResourceManager().removeResource(factoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("2st removed Provider SASL Server Factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(factoryName));
            factoryVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }
}
