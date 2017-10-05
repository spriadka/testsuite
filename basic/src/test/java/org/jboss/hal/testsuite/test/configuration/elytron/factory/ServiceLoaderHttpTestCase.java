package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddServiceLoaderWizard;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class ServiceLoaderHttpTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory";
    private static final String SERVICE_LOADER_HTTP_SERVER_LABEL = "Service Loader HTTP Server";

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
    public void addServiceLoaderHttpServerRequiredFieldsTest() throws Exception {
        final String factoryName = "HttpServiceLoaderFactory_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, factoryName);
        try {
            page.navigateToApplication()
                    .selectFactory(SERVICE_LOADER_HTTP_SERVER_LABEL)
                    .getResourceManager()
                    .addResource(AddServiceLoaderWizard.class)
                    .name(factoryName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created Service Loader HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client)
                    .verifyExists();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addServiceLoaderHttpServerOptionalFieldsTest() throws Exception {
        final String factoryName = "HttpServiceLoaderFactory_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, factoryName);
        try {
            page.navigateToApplication()
                    .selectFactory(SERVICE_LOADER_HTTP_SERVER_LABEL)
                    .getResourceManager()
                    .addResource(AddServiceLoaderWizard.class)
                    .name(factoryName)
                    .module(MODULE_NAME_1)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created Service Loader HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client)
                    .verifyExists()
                    .verifyAttribute(MODULE, MODULE_NAME_1);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addServiceLoaderHttpMissingFieldsTest() {
        page.navigateToApplication()
                .selectFactory(SERVICE_LOADER_HTTP_SERVER_LABEL)
                .getResourceManager()
                .addResource(AddServiceLoaderWizard.class)
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowOpen();
        assertTrue("Page should be showing validation error regarding missing name",
                page.getWindowFragment().isErrorShownInForm());
    }

    @Test
    public void editServiceLoaderHttpServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(SERVICE_LOADER_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(factoryName);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MODULE, MODULE_NAME_2).verifyFormSaved()
                    .verifyAttribute(MODULE, MODULE_NAME_2);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeServiceLoaderHttpServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(SERVICE_LOADER_HTTP_SERVER_LABEL)
                    .getResourceManager()
                    .removeResource(factoryName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed Service Loader HTTP Server Factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }
}
