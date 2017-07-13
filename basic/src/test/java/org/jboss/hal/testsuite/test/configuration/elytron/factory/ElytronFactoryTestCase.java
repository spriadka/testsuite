package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;
import org.jboss.hal.testsuite.page.config.elytron.FactoryPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ConfigChecker.InputType;
import org.jboss.hal.testsuite.util.ModuleUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import static org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations.PROVIDER_LOADER;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ElytronFactoryTestCase extends AbstractElytronTestCase {

    private static final String SERVICE_LOADER_HTTP_SERVER_LABEL = "Service Loader HTTP Server",
            SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory",
            SERVICE_LOADER_SASL_SERVER_LABEL = "Service Loader SASL Server",
            SERVICE_LOADER_SASL_SERVER_FACTORY = "service-loader-sasl-server-factory",
            PROVIDER_HTTP_SERVER_LABEL = "Provider HTTP Server Mechanism",
            PROVIDER_HTTP_SERVER_MECHANISM_FACTORY = "provider-http-server-mechanism-factory",
            PROVIDER_SASL_SERVER_LABEL = "Provider SASL Server",
            PROVIDER_SASL_SERVER_FACTORY = "provider-sasl-server-factory",
            CONFIGURABLE_HTTP_SERVER_LABEL = "Configurable HTTP Server",
            CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY = "configurable-http-server-mechanism-factory",
            HTTP_SERVER_MECHANISM_FACTORY = "http-server-mechanism-factory",
            CONFIGURABLE_SASL_SERVER_LABEL = "Configurable SASL Server",
            CONFIGURABLE_SASL_SERVER_FACTORY = "configurable-sasl-server-factory",
            AGGREGATE_HTTP_SERVER_LABEL = "Aggregate HTTP Server",
            AGGREGATE_HTTP_SERVER_FACTORY = "aggregate-http-server-mechanism-factory",
            AGGREGATE_SASL_SERVER_LABEL = "Aggregate SASL Server",
            AGGREGATE_SASL_SERVER_FACTORY = "aggregate-sasl-server-factory",
            PROVIDER_FILTERING_SASL_SERVER_LABEL = "Mechanism Provider Filtering SASL",
            PROVIDER_FILTERING_SASL_SERVER_FACTORY = "mechanism-provider-filtering-sasl-server-factory",
            KERBEROS_SECURITY_LABEL = "Kerberos Security", KERBEROS_SECURITY_FACTORY = "kerberos-security-factory",
            SASL_SERVER_FACTORY = "sasl-server-factory", MODULE = "module", PROPERTIES = "properties",
            FILTERS_LABEL = "Filters", FILTERS_ATTR = "filters", PATTERN_FILTER = "pattern-filter",
            ENABLING = "enabling", PROTOCOL = "protocol", SERVER_NAME = "server-name",
            PREDEFINED_FILTER = "predefined-filter", HTTP_SERVER_MECHANISM_FACTORIES = "http-server-mechanism-factories",
            SASL_SERVER_FACTORIES = "sasl-server-factories",
            VERSION_COMPARISON = "version-comparison", MECHANISM_NAME = "mechanism-name",
            PROVIDER_VERSION = "provider-version", MECHANISM_OIDS = "mechanism-oids", PATH = "path",
            PRINCIPAL = "principal", REQUEST_LIFETIME = "request-lifetime", SERVER = "server", PROVIDERS = "providers",
            PROVIDER_LOADER_NAME_1 = RandomStringUtils.randomAlphanumeric(5),
            PROVIDER_LOADER_NAME_2 = RandomStringUtils.randomAlphanumeric(5),
            PROVIDER_LOADER_NAME_ELYTRON = "elytron",
            ARCHIVE_NAME = "elytron.customer.credential.security.factory.jar",
            CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL = "Custom Credential Security",
            CUSTOM_CREDENTIAL_SECURITY_FACTORY = "custom-credential-security-factory";

    private static final Path CUSTOM_CREDENTIAL_SECURITY_FACTORY_PATH = Paths.get("test", "elytron",
            "credential", "security", "factory_" + randomAlphanumeric(5));

    private static String customCredentialSecurityFactoryModuleName;

    private static ModuleUtils moduleUtils;

    @Page
    private FactoryPage page;

    @BeforeClass
    public static void beforeClass() throws IOException {
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_2);
        moduleUtils = new ModuleUtils(client);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME);
        jar.addClasses(BubuCustomCredentialFactory.class, ChachaCustomCredentialFactory.class);
        customCredentialSecurityFactoryModuleName = moduleUtils.createModule(CUSTOM_CREDENTIAL_SECURITY_FACTORY_PATH,
                jar, "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_2);
        moduleUtils.removeModule(CUSTOM_CREDENTIAL_SECURITY_FACTORY_PATH);
        adminOps.reloadIfRequired();
    }

    @Test
    public void addServiceLoaderHttpServerTest() throws Exception {
        final String factoryName1 = RandomStringUtils.randomAlphanumeric(5),
                factoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final Address factory1address = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName1),
                factory2address = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, factoryName2);
        final ResourceVerifier factory1verifier = new ResourceVerifier(factory1address, client),
                factory2verifier = new ResourceVerifier(factory2address, client);

        page.navigateToApplication().selectFactory(SERVICE_LOADER_HTTP_SERVER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, factoryName1);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Service Loader HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            factory1verifier.verifyExists();

            wizard = page.getResourceManager().addResource();
            editor = wizard.getEditor();
            editor.text(NAME, factoryName2);
            editor.text(MODULE, MODULE_NAME_1);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Old Service Loader HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            assertTrue("Newly created Service Loader HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName2));
            factory2verifier.verifyExists().verifyAttribute(MODULE, MODULE_NAME_1);
        } finally {
            ops.removeIfExists(factory1address);
            ops.removeIfExists(factory2address);
            adminOps.reloadIfRequired();
        }
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
                    .editAndSave(InputType.TEXT, MODULE, MODULE_NAME_2).verifyFormSaved()
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

            page.navigateToApplication().selectFactory(SERVICE_LOADER_HTTP_SERVER_LABEL);

            page.getResourceManager().removeResource(factoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed Service Loader HTTP Server Factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addServiceLoaderSaslServerTest() throws Exception {
        final String factoryName1 = RandomStringUtils.randomAlphanumeric(5),
                factoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final Address factory1address = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY, factoryName1),
                factory2address = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY, factoryName2);

        page.navigateToApplication().selectFactory(SERVICE_LOADER_SASL_SERVER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, factoryName1);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Service Loader SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            new ResourceVerifier(factory1address, client).verifyExists();

            wizard = page.getResourceManager().addResource();
            editor = wizard.getEditor();
            editor.text(NAME, factoryName2);
            editor.text(MODULE, MODULE_NAME_1);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Old Service Loader SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            assertTrue("Newly created Service Loader SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName2));
            new ResourceVerifier(factory2address, client).verifyExists().verifyAttribute(MODULE, MODULE_NAME_1);
        } finally {
            ops.removeIfExists(factory1address);
            ops.removeIfExists(factory2address);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editServiceLoaderSaslServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY, factoryName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(SERVICE_LOADER_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(factoryName);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, MODULE, MODULE_NAME_2).verifyFormSaved()
                    .verifyAttribute(MODULE, MODULE_NAME_2);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeServiceLoaderSaslServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY, factoryName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(SERVICE_LOADER_SASL_SERVER_LABEL);

            page.getResourceManager().removeResource(factoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed Service Loader SASL Server Factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addProviderHttpServerTest() throws Exception {
        final String factoryName1 = RandomStringUtils.randomAlphanumeric(5),
                factoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final Address factory1address = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY, factoryName1),
                factory2address = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY, factoryName2);

        page.navigateToApplication().selectFactory(PROVIDER_HTTP_SERVER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, factoryName1);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Provider HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            new ResourceVerifier(factory1address, client).verifyExists();

            wizard = page.getResourceManager().addResource();
            editor = wizard.getEditor();
            editor.text(NAME, factoryName2);
            editor.text(PROVIDERS, PROVIDER_LOADER_NAME_ELYTRON);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Old Provider HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            assertTrue("Newly created Provider HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName2));
            new ResourceVerifier(factory2address, client)
                    .verifyExists().verifyAttribute(PROVIDERS, PROVIDER_LOADER_NAME_ELYTRON);
        } finally {
            ops.removeIfExists(factory1address);
            ops.removeIfExists(factory2address);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editProviderHttpServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY, factoryName);

        try {
            ops.add(factoryAddress, Values.of(PROVIDERS, PROVIDER_LOADER_NAME_ELYTRON)).assertSuccess();

            page.navigateToApplication().selectFactory(PROVIDER_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(factoryName);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, PROVIDERS, PROVIDER_LOADER_NAME_2).verifyFormSaved()
                    .verifyAttribute(PROVIDERS, PROVIDER_LOADER_NAME_2);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeProviderHttpServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY, factoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(factoryAddress, client);

        try {
            ops.add(factoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            factoryVerifier.verifyExists();

            page.navigateToApplication().selectFactory(PROVIDER_HTTP_SERVER_LABEL);

            page.getResourceManager().removeResource(factoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed Provider HTTP Server Factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(factoryName));
            factoryVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addProviderSaslServerTest() throws Exception {
        final String factoryName1 = RandomStringUtils.randomAlphanumeric(5),
                factoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final Address factory1address = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, factoryName1),
                factory2address = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, factoryName2);

        page.navigateToApplication().selectFactory(PROVIDER_SASL_SERVER_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, factoryName1);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Provider SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            new ResourceVerifier(factory1address, client).verifyExists();

            wizard = page.getResourceManager().addResource();
            editor = wizard.getEditor();
            editor.text(NAME, factoryName2);
            editor.text(PROVIDERS, PROVIDER_LOADER_NAME_1);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Old Provider SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName1));
            assertTrue("Newly created Provider SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName2));
            new ResourceVerifier(factory2address, client)
                    .verifyExists().verifyAttribute(PROVIDERS, PROVIDER_LOADER_NAME_1);
        } finally {
            ops.removeIfExists(factory1address);
            ops.removeIfExists(factory2address);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editProviderSaslServerTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, factoryName);

        try {
            ops.add(factoryAddress, Values.of(PROVIDERS, PROVIDER_LOADER_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(PROVIDER_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(factoryName);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, PROVIDERS, PROVIDER_LOADER_NAME_2).verifyFormSaved()
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

    @Test
    public void addConfigurableHttpServerTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                property1key = RandomStringUtils.randomAlphanumeric(5),
                property2key = RandomStringUtils.randomAlphanumeric(5),
                property1value = RandomStringUtils.randomAlphanumeric(5),
                property2value = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1),
                httpServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2),
                configurableFactoryAddress1 = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName1),
                configurableFactoryAddress2 = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName2);

        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL);

            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.text(NAME, configurableFactoryName1);
            editor.text(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName1);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Configurable HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName1));
            new ResourceVerifier(configurableFactoryAddress1, client)
                    .verifyExists().verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName1);

            wizard = page.getResourceManager().addResource(WizardWindowWithOptionalFields.class);
            wizard.openOptionalFieldsTab();
            editor = wizard.getEditor();
            editor.text(NAME, configurableFactoryName2);
            editor.text(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName2);
            editor.text(PROPERTIES, property1key + "=" + property1value + "\n" + property2key + "=" + property2value);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Old Configurable HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName1));
            assertTrue("Newly created Configurable HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName2));

            final ModelNode expectedPropertiesNode = new ModelNodePropertiesBuilder()
                    .addProperty(property1key, property1value).addProperty(property2key, property2value).build();

            new ResourceVerifier(configurableFactoryAddress2, client)
                    .verifyExists().verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName2)
                    .verifyAttribute(PROPERTIES, expectedPropertiesNode);

        } finally {
            ops.removeIfExists(configurableFactoryAddress1);
            ops.removeIfExists(configurableFactoryAddress2);
            ops.removeIfExists(httpServerFactoryAddress1);
            ops.removeIfExists(httpServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeConfigurableHttpServerTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(configurableFactoryAddress, client);

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName));
            factoryVerifier.verifyExists();

            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL);

            page.getResourceManager().removeResource(configurableFactoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(configurableFactoryName));
            factoryVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editConfigurableHttpServerAttributesTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5),
                property1key = RandomStringUtils.randomAlphanumeric(5),
                property2key = RandomStringUtils.randomAlphanumeric(5),
                property3key = RandomStringUtils.randomAlphanumeric(5),
                property1value = RandomStringUtils.randomAlphanumeric(5),
                property2value = RandomStringUtils.randomAlphanumeric(5),
                property3value = RandomStringUtils.randomAlphanumeric(5);

        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1),
                httpServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ModelNode initialProps = new ModelNodePropertiesBuilder().addProperty(property1key, property1value).build();
            ops.add(configurableFactoryAddress,
                    Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName1).and(PROPERTIES, initialProps))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName2)
                    .verifyFormSaved().verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName2);

            final ModelNode expectedPropertiesNode = new ModelNodePropertiesBuilder()
                    .addProperty(property2key, property2value).addProperty(property3key, property3value).build();
            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, PROPERTIES,
                            property2key + "=" + property2value + "\n" + property3key + "=" + property3value)
                    .verifyFormSaved().verifyAttribute(PROPERTIES, expectedPropertiesNode);
        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress1);
            ops.removeIfExists(httpServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void configurableHttpServerAddFiltersTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5),
                patternFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            WizardWindowWithOptionalFields wizard = page.getConfigAreaResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            wizard.openOptionalFieldsTab();
            Editor editor = wizard.getEditor();
            editor.text(PATTERN_FILTER, patternFilterValue);
            editor.checkbox(ENABLING, true);
            boolean closed = wizard.finishAndDismissReloadRequiredWindow();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created pattern filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(patternFilterValue));

            final ModelNode expectedFiltersNode = new ModelNodeListBuilder().addNode(new ModelNodePropertiesBuilder()
                    .addProperty(PATTERN_FILTER, patternFilterValue).addProperty(ENABLING, new ModelNode(true)).build())
                    .build();

            new ResourceVerifier(configurableFactoryAddress, client)
                    .verifyExists().verifyAttribute(FILTERS_ATTR, expectedFiltersNode,
                            "See https://issues.jboss.org/browse/HAL-1315");
        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void configurableHttpServerRemoveFiltersTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5),
                patternFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);
        final ModelNode filtersNode = new ModelNodeListBuilder().addNode(new ModelNodePropertiesBuilder()
                .addProperty(PATTERN_FILTER, patternFilterValue).addProperty(ENABLING, new ModelNode(true)).build())
                .build();

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress,
                    Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName).and(FILTERS_ATTR, filtersNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            page.getConfigAreaResourceManager().removeResource(patternFilterValue)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted pattern filter should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(patternFilterValue));
            ModelNode emptyNodeList = new ModelNodeListBuilder().empty().build();
            new ResourceVerifier(configurableFactoryAddress, client).verifyAttribute(FILTERS_ATTR, emptyNodeList);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addConfigurableSaslServerTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                property1key = RandomStringUtils.randomAlphanumeric(5),
                property2key = RandomStringUtils.randomAlphanumeric(5),
                property1value = RandomStringUtils.randomAlphanumeric(5),
                property2value = RandomStringUtils.randomAlphanumeric(5),
                protocolName = RandomStringUtils.randomAlphanumeric(5),
                serverNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1),
                saslServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2),
                configurableFactoryAddress1 = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName1),
                configurableFactoryAddress2 = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName2);

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL);

            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.text(NAME, configurableFactoryName1);
            editor.text(SASL_SERVER_FACTORY, saslServerFactoryName1);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Configurable SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName1));
            new ResourceVerifier(configurableFactoryAddress1, client)
                    .verifyExists().verifyAttribute(SASL_SERVER_FACTORY, saslServerFactoryName1);

            wizard = page.getResourceManager().addResource(WizardWindowWithOptionalFields.class);
            wizard.openOptionalFieldsTab();
            editor = wizard.getEditor();
            editor.text(NAME, configurableFactoryName2);
            editor.text(SASL_SERVER_FACTORY, saslServerFactoryName2);
            editor.text(PROPERTIES, property1key + "=" + property1value + "\n" + property2key + "=" + property2value);
            editor.text(PROTOCOL, protocolName);
            editor.text(SERVER_NAME, serverNameValue);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Old Configurable SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName1));
            assertTrue("Newly created Configurable SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName2));

            final ModelNode expectedPropertiesNode = new ModelNodePropertiesBuilder()
                    .addProperty(property1key, property1value).addProperty(property2key, property2value).build();

            new ResourceVerifier(configurableFactoryAddress2, client)
                    .verifyExists().verifyAttribute(SASL_SERVER_FACTORY, saslServerFactoryName2)
                    .verifyAttribute(PROPERTIES, expectedPropertiesNode).verifyAttribute(PROTOCOL, protocolName)
                    .verifyAttribute(SERVER_NAME, serverNameValue);

        } finally {
            ops.removeIfExists(configurableFactoryAddress1);
            ops.removeIfExists(configurableFactoryAddress2);
            ops.removeIfExists(saslServerFactoryAddress1);
            ops.removeIfExists(saslServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeConfigurableSaslServerTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(configurableFactoryAddress, client);

        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslServerFactoryName)).assertSuccess();
            factoryVerifier.verifyExists();

            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL);

            page.getResourceManager().removeResource(configurableFactoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(configurableFactoryName));
            factoryVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editConfigurableSaslServerAttributesTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5),
                property1key = RandomStringUtils.randomAlphanumeric(5),
                property2key = RandomStringUtils.randomAlphanumeric(5),
                property3key = RandomStringUtils.randomAlphanumeric(5),
                property1value = RandomStringUtils.randomAlphanumeric(5),
                property2value = RandomStringUtils.randomAlphanumeric(5),
                property3value = RandomStringUtils.randomAlphanumeric(5),
                protocolName = RandomStringUtils.randomAlphanumeric(5),
                serverNameValue = RandomStringUtils.randomAlphanumeric(5);

        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1),
                saslServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ModelNode initialProps = new ModelNodePropertiesBuilder().addProperty(property1key, property1value).build();
            ops.add(configurableFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslServerFactoryName1).and(PROPERTIES, initialProps))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, SASL_SERVER_FACTORY, saslServerFactoryName2)
                    .verifyFormSaved().verifyAttribute(SASL_SERVER_FACTORY, saslServerFactoryName2);

            final ModelNode expectedPropertiesNode = new ModelNodePropertiesBuilder()
                    .addProperty(property2key, property2value).addProperty(property3key, property3value).build();
            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, PROPERTIES,
                            property2key + "=" + property2value + "\n" + property3key + "=" + property3value)
                    .verifyFormSaved().verifyAttribute(PROPERTIES, expectedPropertiesNode);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, PROTOCOL, protocolName).verifyFormSaved()
                    .verifyAttribute(PROTOCOL, protocolName);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, SERVER_NAME, serverNameValue).verifyFormSaved()
                    .verifyAttribute(SERVER_NAME, serverNameValue);
        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress1);
            ops.removeIfExists(saslServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void configurableSaslServerAddFiltersTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5),
                patternFilterValue = RandomStringUtils.randomAlphanumeric(5),
                predefinedFilterValue = "DIGEST";
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(configurableFactoryAddress, client);

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslServerFactoryName)).assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            WizardWindowWithOptionalFields wizard = page.getConfigAreaResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.select(PREDEFINED_FILTER, predefinedFilterValue);
            editor.text(PATTERN_FILTER, patternFilterValue);
            wizard.openOptionalFieldsTab();
            editor.checkbox(ENABLING, true);
            boolean closed = wizard.finishAndDismissReloadRequiredWindow();
            assertFalse("Dialog should not be closed since it should not be possible to set both " + PREDEFINED_FILTER
                    + " as well as " + PATTERN_FILTER + "!", closed);

            editor.text(PATTERN_FILTER, "");
            closed = wizard.finishAndDismissReloadRequiredWindow();
            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(predefinedFilterValue));

            ModelNode expectedFiltersNode = new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder().addProperty(PREDEFINED_FILTER, predefinedFilterValue)
                            .addProperty(ENABLING, new ModelNode(true)).build())
                    .build();
            factoryVerifier.verifyExists().verifyAttribute(FILTERS_ATTR, expectedFiltersNode);

            wizard = page.getConfigAreaResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            editor = wizard.getEditor();
            editor.text(PATTERN_FILTER, patternFilterValue);
            wizard.openOptionalFieldsTab();
            editor.checkbox(ENABLING, false);
            closed = wizard.finishAndDismissReloadRequiredWindow();
            assertTrue("Dialog should be closed!", closed);
            assertTrue("2nd created filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable2ndColumn(patternFilterValue));
            assertTrue("1st created filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(predefinedFilterValue));

            expectedFiltersNode = new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder().addProperty(PREDEFINED_FILTER, predefinedFilterValue)
                            .addProperty(ENABLING, new ModelNode(true)).build())
                    .addNode(new ModelNodePropertiesBuilder().addProperty(PATTERN_FILTER, patternFilterValue)
                            .addProperty(ENABLING, new ModelNode(false)).build())
                    .build();
            factoryVerifier.verifyExists().verifyAttribute(FILTERS_ATTR, expectedFiltersNode);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void configurableSaslServerRemoveFiltersTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5),
                predefinedFilterValue = "HASH_MD5";
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);
        final ModelNode filtersNode = new ModelNodeListBuilder().addNode(new ModelNodePropertiesBuilder()
                .addProperty(PREDEFINED_FILTER, predefinedFilterValue).addProperty(ENABLING, new ModelNode(true)).build())
                .build();

        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslServerFactoryName).and(FILTERS_ATTR, filtersNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            page.getConfigAreaResourceManager().removeResource(predefinedFilterValue)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted filter should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(predefinedFilterValue));
            ModelNode emptyNodeList = new ModelNodeListBuilder().empty().build();
            new ResourceVerifier(configurableFactoryAddress, client).verifyAttribute(FILTERS_ATTR, emptyNodeList);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addAggregateHttpServerTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryNamesSeparatedByNewLine = httpServerFactoryName1 + "\n" + httpServerFactoryName2,
                aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1),
                httpServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2),
                aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_HTTP_SERVER_FACTORY,
                        aggregateFactoryName);

        page.navigateToApplication().selectFactory(AGGREGATE_HTTP_SERVER_LABEL);

        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();

            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, aggregateFactoryName);
            editor.text(HTTP_SERVER_MECHANISM_FACTORIES, httpServerFactoryNamesSeparatedByNewLine);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Aggregate HTTP Server Mechanism should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregateFactoryName));

            ModelNode expectedHttpFactoriesNode = new ModelNodeListBuilder()
                    .addNode(new ModelNode(httpServerFactoryName1)).addNode(new ModelNode(httpServerFactoryName2))
                    .build();
            new ResourceVerifier(aggregateFactoryAddress, client).verifyExists().verifyAttribute(HTTP_SERVER_MECHANISM_FACTORIES,
                    expectedHttpFactoriesNode);
        } finally {
            ops.removeIfExists(aggregateFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress1);
            ops.removeIfExists(httpServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editAggregateHttpServerTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryName3 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryName4 = RandomStringUtils.randomAlphanumeric(5),
                newHttpServerFactoryNamesSeparatedByNewLine = httpServerFactoryName3 + "\n" + httpServerFactoryName4,
                aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1),
                httpServerFactoryAddress2 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2),
                httpServerFactoryAddress3 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName3),
                        httpServerFactoryAddress4 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                                httpServerFactoryName4),
                aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_HTTP_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode initialHttpFactoriesNode = new ModelNodeListBuilder()
                .addNode(new ModelNode(httpServerFactoryName1)).addNode(new ModelNode(httpServerFactoryName2)).build();
        final ModelNode expectedEditedHttpFactoriesNode = new ModelNodeListBuilder()
                .addNode(new ModelNode(httpServerFactoryName3)).addNode(new ModelNode(httpServerFactoryName4)).build();

        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(MODULE, MODULE_NAME_2)).assertSuccess();
            ops.add(httpServerFactoryAddress3, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress4, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_2)).assertSuccess();
            ops.add(aggregateFactoryAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORIES, initialHttpFactoriesNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(AGGREGATE_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(aggregateFactoryName);

            new ConfigChecker.Builder(client, aggregateFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, HTTP_SERVER_MECHANISM_FACTORIES, newHttpServerFactoryNamesSeparatedByNewLine)
                    .verifyFormSaved().verifyAttribute(HTTP_SERVER_MECHANISM_FACTORIES, expectedEditedHttpFactoriesNode);
        } finally {
            ops.removeIfExists(aggregateFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress1);
            ops.removeIfExists(httpServerFactoryAddress2);
            ops.removeIfExists(httpServerFactoryAddress3);
            ops.removeIfExists(httpServerFactoryAddress4);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeAggregateHttpServerTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1),
                httpServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2),
                aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_HTTP_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode httpFactoriesNode = new ModelNodeListBuilder()
                .addNode(new ModelNode(httpServerFactoryName1)).addNode(new ModelNode(httpServerFactoryName2)).build();

        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(aggregateFactoryAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORIES, httpFactoriesNode)).assertSuccess();

            page.navigateToApplication().selectFactory(AGGREGATE_HTTP_SERVER_LABEL);

            page.getResourceManager().removeResource(aggregateFactoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed Aggregate HTTP Server Mechanism should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(aggregateFactoryName));
            new ResourceVerifier(aggregateFactoryAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregateFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress1);
            ops.removeIfExists(httpServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addAggregateSaslServerTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryNamesSeparatedByNewLine = saslServerFactoryName1 + "\n" + saslServerFactoryName2,
                aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1),
                saslServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2),
                aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_SASL_SERVER_FACTORY,
                        aggregateFactoryName);

        page.navigateToApplication().selectFactory(AGGREGATE_SASL_SERVER_LABEL);

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();

            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, aggregateFactoryName);
            editor.text(SASL_SERVER_FACTORIES, saslServerFactoryNamesSeparatedByNewLine);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created Aggregate SASL Server Mechanism should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregateFactoryName));

            ModelNode expectedHttpFactoriesNode = new ModelNodeListBuilder()
                    .addNode(new ModelNode(saslServerFactoryName1)).addNode(new ModelNode(saslServerFactoryName2))
                    .build();
            new ResourceVerifier(aggregateFactoryAddress, client).verifyExists().verifyAttribute(SASL_SERVER_FACTORIES,
                    expectedHttpFactoriesNode);
        } finally {
            ops.removeIfExists(aggregateFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress1);
            ops.removeIfExists(saslServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editAggregateSaslServerTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryName3 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryName4 = RandomStringUtils.randomAlphanumeric(5),
                newHttpServerFactoryNamesSeparatedByNewLine = saslServerFactoryName3 + "\n" + saslServerFactoryName4,
                aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1),
                saslServerFactoryAddress2 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2),
                saslServerFactoryAddress3 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName3),
                        saslServerFactoryAddress4 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                                saslServerFactoryName4),
                aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_SASL_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode initialHttpFactoriesNode = new ModelNodeListBuilder()
                .addNode(new ModelNode(saslServerFactoryName1)).addNode(new ModelNode(saslServerFactoryName2)).build();
        final ModelNode expectedEditedHttpFactoriesNode = new ModelNodeListBuilder()
                .addNode(new ModelNode(saslServerFactoryName3)).addNode(new ModelNode(saslServerFactoryName4)).build();

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(MODULE, MODULE_NAME_2)).assertSuccess();
            ops.add(saslServerFactoryAddress3, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress4, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_2)).assertSuccess();
            ops.add(aggregateFactoryAddress, Values.of(SASL_SERVER_FACTORIES, initialHttpFactoriesNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(AGGREGATE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(aggregateFactoryName);

            new ConfigChecker.Builder(client, aggregateFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, SASL_SERVER_FACTORIES, newHttpServerFactoryNamesSeparatedByNewLine)
                    .verifyFormSaved().verifyAttribute(SASL_SERVER_FACTORIES, expectedEditedHttpFactoriesNode);
        } finally {
            ops.removeIfExists(aggregateFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress1);
            ops.removeIfExists(saslServerFactoryAddress2);
            ops.removeIfExists(saslServerFactoryAddress3);
            ops.removeIfExists(saslServerFactoryAddress4);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeAggregateSaslServerTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1),
                saslServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2),
                aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_SASL_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode saslFactoriesNode = new ModelNodeListBuilder()
                .addNode(new ModelNode(saslServerFactoryName1)).addNode(new ModelNode(saslServerFactoryName2)).build();

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(aggregateFactoryAddress, Values.of(SASL_SERVER_FACTORIES, saslFactoriesNode)).assertSuccess();

            page.navigateToApplication().selectFactory(AGGREGATE_SASL_SERVER_LABEL);

            page.getResourceManager().removeResource(aggregateFactoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed Aggregate SASL Server Mechanism should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(aggregateFactoryName));
            new ResourceVerifier(aggregateFactoryAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregateFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress1);
            ops.removeIfExists(saslServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addProviderFilteringSaslServerTest() throws Exception {
        final String saslFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                saslFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                filteringSaslFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                filteringSaslFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress1 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName1),
                saslFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName2),
                filteringSaslFactoryAddress1 = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName1),
                filteringSaslFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName2);

        try {
            ops.add(saslFactoryAddress1, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(saslFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_2)).assertSuccess();

            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL);

            WizardWindowWithOptionalFields wizard = page.getResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            Editor editor = wizard.getEditor();
            editor.text(NAME, filteringSaslFactoryName1);
            editor.text(SASL_SERVER_FACTORY, saslFactoryName1);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created factory should be present in the table! "
                    + "Error might be related to https://issues.jboss.org/browse/WFLY-7575.",
                    page.resourceIsPresentInMainTable(filteringSaslFactoryName1));
            new ResourceVerifier(filteringSaslFactoryAddress1, client)
                    .verifyExists().verifyAttribute(SASL_SERVER_FACTORY, saslFactoryName1);

            wizard = page.getResourceManager().addResource(WizardWindowWithOptionalFields.class);
            wizard.openOptionalFieldsTab();
            editor = wizard.getEditor();
            editor.text(NAME, filteringSaslFactoryName2);
            editor.text(SASL_SERVER_FACTORY, saslFactoryName2);
            editor.checkbox(ENABLING, true);
            closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Old factory should be present in the table!",
                    page.resourceIsPresentInMainTable(filteringSaslFactoryName1));
            assertTrue("Newly created factory should be present in the table!",
                    page.resourceIsPresentInMainTable(filteringSaslFactoryName2));

            new ResourceVerifier(filteringSaslFactoryAddress2, client)
                    .verifyExists().verifyAttribute(SASL_SERVER_FACTORY, saslFactoryName2)
                    .verifyAttribute(ENABLING, true);

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress1);
            ops.removeIfExists(filteringSaslFactoryAddress2);
            ops.removeIfExists(saslFactoryAddress1);
            ops.removeIfExists(saslFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeProviderFilteringSaslServerTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                providerNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                saslFactoryName),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);
        final ResourceVerifier filteringSaslFactoryVerifier = new ResourceVerifier(filteringSaslFactoryAddress, client);

        try {
            ModelNode filtersNode = new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerNameValue).build())
                    .build();
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName).and(FILTERS_ATTR, filtersNode))
                    .assertSuccess();
            filteringSaslFactoryVerifier.verifyExists();

            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL);

            page.getResourceManager().removeResource(filteringSaslFactoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(filteringSaslFactoryName));
            filteringSaslFactoryVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editProviderFilteringSaslServerAttributesTest() throws Exception {
        final String saslFactoryName1 = RandomStringUtils.randomAlphanumeric(5),
                saslFactoryName2 = RandomStringUtils.randomAlphanumeric(5),
                filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                providerNameValue = RandomStringUtils.randomAlphanumeric(5);

        final Address saslFactoryAddress1 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                saslFactoryName1),
                saslFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslFactoryName2),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);

        try {
            ops.add(saslFactoryAddress1, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(saslFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_2)).assertSuccess();
            ModelNode filtersNode = new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerNameValue).build())
                    .build();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName1).and(FILTERS_ATTR, filtersNode)).assertSuccess();

            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(filteringSaslFactoryName);

            new ConfigChecker.Builder(client, filteringSaslFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, SASL_SERVER_FACTORY, saslFactoryName2)
                    .verifyFormSaved().verifyAttribute(SASL_SERVER_FACTORY, saslFactoryName2);

            new ConfigChecker.Builder(client, filteringSaslFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.CHECKBOX, ENABLING, false).verifyFormSaved()
                    .verifyAttribute(ENABLING, false);
        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress1);
            ops.removeIfExists(saslFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void providerFilteringSaslServerAddFiltersTest() throws Exception {
        final double correctProviderVersion = 15987.0;
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                providerName1 = RandomStringUtils.randomAlphanumeric(5),
                providerName2 = RandomStringUtils.randomAlphanumeric(5),
                mechanismNameValue = RandomStringUtils.randomAlphanumeric(5),
                nonNumericProviderVersion = RandomStringUtils.randomAlphabetic(5),
                versionComparisonValue = "greater-than";
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);
        final ModelNode filterProperties1 = new ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName1)
                .build(),
                filterProperties2 = new ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName2)
                        .addProperty(VERSION_COMPARISON, versionComparisonValue)
                        .addProperty(MECHANISM_NAME, mechanismNameValue)
                        .addProperty(PROVIDER_VERSION, new ModelNode(correctProviderVersion)).build(),
                initialFiltersNode = new ModelNodeListBuilder().addNode(filterProperties1).build(),
                expectedEditedFiltersNode = new ModelNodeListBuilder().addNode(filterProperties1)
                        .addNode(filterProperties2).build();

        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName).and(FILTERS_ATTR, initialFiltersNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(filteringSaslFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            WizardWindowWithOptionalFields wizard = page.getConfigAreaResourceManager()
                    .addResource(WizardWindowWithOptionalFields.class);
            wizard.openOptionalFieldsTab();
            Editor editor = wizard.getEditor();
            editor.text(MECHANISM_NAME, mechanismNameValue);
            boolean closed = wizard.finishAndDismissReloadRequiredWindow();
            assertFalse("Dialog should not be closed since " + PROVIDER_NAME + " is mandatory!", closed);

            editor.text(PROVIDER_NAME, providerName2);
            editor.text(PROVIDER_VERSION, nonNumericProviderVersion);
            closed = wizard.finishAndDismissReloadRequiredWindow();
            assertFalse("Dialog should not be closed since " + nonNumericProviderVersion
                    + " is not number and thus not correct " + PROVIDER_VERSION + "!", closed);

            editor.text(PROVIDER_VERSION, String.valueOf(correctProviderVersion));
            editor.select(VERSION_COMPARISON, versionComparisonValue);
            closed = wizard.finishAndDismissReloadRequiredWindow();
            assertTrue("Dialog should be closed.", closed);
            assertTrue("Created filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(providerName2));

            new ResourceVerifier(filteringSaslFactoryAddress, client)
                    .verifyExists().verifyAttribute(FILTERS_ATTR, expectedEditedFiltersNode);

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void providerFilteringSaslServerRemoveFiltersTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                providerName1 = RandomStringUtils.randomAlphanumeric(5),
                providerName2 = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                saslFactoryName),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);
        final ModelNode filterProperties1 = new ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName1)
                .build(),
                filterProperties2 = new ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName2).build(),
                initialFiltersNode = new ModelNodeListBuilder().addNode(filterProperties1).addNode(filterProperties2)
                        .build(),
                expectedEditedFiltersNode = new ModelNodeListBuilder().addNode(filterProperties2).build();

        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName).and(FILTERS_ATTR, initialFiltersNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(filteringSaslFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            page.getConfigAreaResourceManager().removeResource(providerName1)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted filter should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(providerName1));
            new ResourceVerifier(filteringSaslFactoryAddress, client)
                    .verifyAttribute(FILTERS_ATTR, expectedEditedFiltersNode);

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Custom Credential Security Factory instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Custom Credential Security Factory table.
     * Validate created resource is present in model.
     * Validate value of created resource in model.
     */
    @Test
    public void addCustomCredentialSecurityFactoryTest() throws Exception {
        String customCredentialSecurityFactoryName = randomAlphanumeric(5);
        Address customCredentialSecurityFactoryAddress =
                elyOps.getElytronAddress(CUSTOM_CREDENTIAL_SECURITY_FACTORY, customCredentialSecurityFactoryName);

        try {
            page.navigateToApplication()
                .selectResource(CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL)
                .getResourceManager()
                .addResource(AddResourceWizard.class)
                .name(customCredentialSecurityFactoryName)
                .text(CLASS_NAME, BubuCustomCredentialFactory.class.getName())
                .text(MODULE, customCredentialSecurityFactoryModuleName)
                .saveWithState().assertWindowClosed();

            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(customCredentialSecurityFactoryName));
            new ResourceVerifier(customCredentialSecurityFactoryAddress, client).verifyExists()
                    .verifyAttribute(CLASS_NAME, BubuCustomCredentialFactory.class.getName())
                    .verifyAttribute(MODULE, customCredentialSecurityFactoryModuleName);
        } finally {
            ops.removeIfExists(customCredentialSecurityFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Credential Security Factory instance in model
     * and try to edit it's value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editCustomCredentialSecurityFactoryAttributesTest() throws Exception {
        String
            customCredentialSecurityFactoryName = randomAlphanumeric(5),
            key1 = randomAlphanumeric(5),
            value1 = randomAlphanumeric(5),
            key2 = randomAlphanumeric(5),
            value2 = randomAlphanumeric(5);
        Address customCredentialSecurityFactoryAddress =
                elyOps.getElytronAddress(CUSTOM_CREDENTIAL_SECURITY_FACTORY, customCredentialSecurityFactoryName);

        try {
            ops.add(customCredentialSecurityFactoryAddress, Values.of(CLASS_NAME, BubuCustomCredentialFactory.class.getName())
                    .and(MODULE, customCredentialSecurityFactoryModuleName)).assertSuccess();

            page.navigateToApplication().selectResource(CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL).getResourceManager()
                    .selectByName(customCredentialSecurityFactoryName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, customCredentialSecurityFactoryAddress)
                .configFragment(page.getConfigFragment())
                .edit(TEXT, CLASS_NAME, ChachaCustomCredentialFactory.class.getName())
                .edit(TEXT, CONFIGURATION, key1 + "=" + value1 + "\n" + key2 + "=" + value2)
                .andSave().verifyFormSaved()
                .verifyAttribute(CLASS_NAME, ChachaCustomCredentialFactory.class.getName())
                .verifyAttribute(CONFIGURATION, new ModelNodePropertiesBuilder()
                        .addProperty(key1, value1)
                        .addProperty(key2, value2)
                        .build());
        } finally {
            ops.removeIfExists(customCredentialSecurityFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Custom Credential Security Factory instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Custom Credential Security Factory table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeCustomCredentialSecurityFactoryTest() throws Exception {
        String customCredentialSecurityFactoryName = randomAlphanumeric(5);
        Address customCredentialSecurityFactoryAddress =
                elyOps.getElytronAddress(CUSTOM_CREDENTIAL_SECURITY_FACTORY, customCredentialSecurityFactoryName);
        ResourceVerifier customCredentialSecurityFactoryVerifier = new ResourceVerifier(customCredentialSecurityFactoryAddress, client);

        try {
            ops.add(customCredentialSecurityFactoryAddress, Values.of(CLASS_NAME, BubuCustomCredentialFactory.class.getName())
                    .and(MODULE, customCredentialSecurityFactoryModuleName)).assertSuccess();
            customCredentialSecurityFactoryVerifier.verifyExists();

            page.navigateToApplication().selectResource(CUSTOM_CREDENTIAL_SECURITY_FACTORY_LABEL).getResourceManager()
                    .removeResource(customCredentialSecurityFactoryName).confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(customCredentialSecurityFactoryName));
            customCredentialSecurityFactoryVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(customCredentialSecurityFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addKerberosSecurityFactoryTest() throws Exception {
        final String factoryName = "factoryName_" + RandomStringUtils.randomAlphanumeric(5),
                pathValue = "path_" + RandomStringUtils.randomAlphanumeric(5),
                principalValue = "principal_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(KERBEROS_SECURITY_FACTORY, factoryName);

        page.navigateToApplication().selectFactory(KERBEROS_SECURITY_LABEL);

        try {
            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, factoryName);
            editor.text(PATH, pathValue);
            editor.text(PRINCIPAL, principalValue);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created factory should be present in the table!",
                    page.resourceIsPresentInMainTable(factoryName));
            new ResourceVerifier(factoryAddress, client).verifyExists()
                    .verifyAttribute(PATH, pathValue)
                    .verifyAttribute(PRINCIPAL, principalValue);

        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editKerberosSecurityFactoryTest() throws Exception {
        final int newRequestLifetimeValue = 7532159;
        final String factoryName = "factoryName_" + RandomStringUtils.randomAlphanumeric(5),
                oid1 = "1.2.840.113554.1.2.2",
                oid2 = "1.3.6.1.5.5.2",
                oid3 = "1.2.3.4.5.6.7",
                newOidsSeparatedByNewLine = oid2 + "\n" + oid3,
                pathValue = "path_" + RandomStringUtils.randomAlphanumeric(5),
                principalValue = "principal_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(KERBEROS_SECURITY_FACTORY, factoryName);
        final ModelNode initialOidsNode = new ModelNodeListBuilder().addNode(new ModelNode(oid1))
                .addNode(new ModelNode(oid2)).build(),
                expectedEditedOidsNode = new ModelNodeListBuilder().addNode(new ModelNode(oid2))
                        .addNode(new ModelNode(oid3)).build();

        try {
            ops.add(factoryAddress,
                    Values.of(MECHANISM_OIDS, initialOidsNode).and(PATH, pathValue).and(PRINCIPAL, principalValue))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(KERBEROS_SECURITY_LABEL).getResourceManager()
                    .selectByName(factoryName);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, MECHANISM_OIDS, newOidsSeparatedByNewLine).verifyFormSaved()
                    .verifyAttribute(MECHANISM_OIDS, expectedEditedOidsNode);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, REQUEST_LIFETIME, String.valueOf(newRequestLifetimeValue))
                    .verifyFormSaved().verifyAttribute(REQUEST_LIFETIME, newRequestLifetimeValue);

            new ConfigChecker.Builder(client, factoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.CHECKBOX, SERVER, false).verifyFormSaved()
                    .verifyAttribute(SERVER, false);
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeKerberosSecurityFactoryTest() throws Exception {
        final String factoryName = "factoryName_" + RandomStringUtils.randomAlphanumeric(5),
                oid1 = "1.2.840.113554.1.2.2",
                oid2 = "1.3.6.1.5.5.2",
                pathValue = "path_" + RandomStringUtils.randomAlphanumeric(5),
                principalValue = "principal_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(KERBEROS_SECURITY_FACTORY, factoryName);
        final ModelNode oidsNode = new ModelNodeListBuilder().addNode(new ModelNode(oid1))
                .addNode(new ModelNode(oid2)).build();
        final ResourceVerifier factoryVerifier = new ResourceVerifier(factoryAddress, client);

        try {
            ops.add(factoryAddress,
                    Values.of(MECHANISM_OIDS, oidsNode).and(PATH, pathValue).and(PRINCIPAL, principalValue))
                    .assertSuccess();
            factoryVerifier.verifyExists();

            page.navigateToApplication().selectFactory(KERBEROS_SECURITY_LABEL);

            page.getResourceManager().removeResource(factoryName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(factoryName));
            factoryVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }
}
