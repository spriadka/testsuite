package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddConfigurableHttpServerWizard;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddHttpPatternFilterWizard;
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
public class ConfigurableHttpServerTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String CONFIGURABLE_HTTP_SERVER_LABEL = "Configurable HTTP Server";
    private static final String CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY = "configurable-http-server-mechanism-factory";
    private static final String HTTP_SERVER_MECHANISM_FACTORY = "http-server-mechanism-factory";
    private static final String PROVIDER_HTTP_SERVER_MECHANISM_FACTORY = "provider-http-server-mechanism-factory";
    private static final String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory";
    private static final String PROPERTIES = "properties";
    private static final String PATTERN_FILTER = "pattern-filter";
    private static final String FILTERS_LABEL = "Filters";
    private static final String FILTERS_ATTR = "filters";
    private static final String ENABLING = "enabling";

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
    public void addConfigurableHttpServerRequiredFieldsTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL);
            page.getResourceManager()
                    .addResource(AddConfigurableHttpServerWizard.class)
                    .name(configurableFactoryName)
                    .httpServerMechanismFactory(httpServerFactoryName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created Configurable HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName));
            new ResourceVerifier(configurableFactoryAddress, client)
                    .verifyExists()
                    .verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addConfigurableHttpServerOptionalFieldsTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String property1key = RandomStringUtils.randomAlphanumeric(5);
        final String property2key = RandomStringUtils.randomAlphanumeric(5);
        final String property1value = RandomStringUtils.randomAlphanumeric(5);
        final String property2value = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL)
                    .getResourceManager()
                    .addResource(AddConfigurableHttpServerWizard.class)
                    .name(configurableFactoryName)
                    .httpServerMechanismFactory(httpServerFactoryName)
                    .properties(property1key + "=" + property1value + "\n" + property2key + "=" + property2value)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Newly created Configurable HTTP Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName));

            final ModelNode expectedPropertiesNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty(property1key, property1value).addProperty(property2key, property2value).build();

            new ResourceVerifier(configurableFactoryAddress, client)
                    .verifyExists().verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName)
                    .verifyAttribute(PROPERTIES, expectedPropertiesNode);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeServerTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(configurableFactoryAddress, client);

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName));
            factoryVerifier.verifyExists();
            page.navigateToApplication()
                    .selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL);
            page.getResourceManager()
                    .removeResource(configurableFactoryName)
                    .confirmAndDismissReloadRequiredMessage()
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
    public void editAttributesTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String property1key = RandomStringUtils.randomAlphanumeric(5);
        final String property2key = RandomStringUtils.randomAlphanumeric(5);
        final String property3key = RandomStringUtils.randomAlphanumeric(5);
        final String property1value = RandomStringUtils.randomAlphanumeric(5);
        final String property2value = RandomStringUtils.randomAlphanumeric(5);
        final String property3value = RandomStringUtils.randomAlphanumeric(5);

        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1);
        final Address httpServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ModelNode initialProps = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(property1key, property1value).build();
            ops.add(configurableFactoryAddress,
                    Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName1).and(PROPERTIES, initialProps))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName2)
                    .verifyFormSaved().verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName2);

            final ModelNode expectedPropertiesNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty(property2key, property2value).addProperty(property3key, property3value).build();
            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PROPERTIES,
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
    public void addFiltersTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String patternFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(httpServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpServerFactoryName))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_HTTP_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            page.getConfigAreaResourceManager()
                    .addResource(AddHttpPatternFilterWizard.class)
                    .patternFilter(patternFilterValue)
                    .enabling(true)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created pattern filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(patternFilterValue));
            final ModelNode expectedFiltersNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
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
    public void removeFiltersTest() throws Exception {
        final String httpServerFactoryName = RandomStringUtils.randomAlphanumeric(5),
                configurableFactoryName = RandomStringUtils.randomAlphanumeric(5),
                patternFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName),
                configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                        configurableFactoryName);
        final ModelNode filtersNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
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
            page.getConfigAreaResourceManager()
                    .removeResource(patternFilterValue)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted pattern filter should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(patternFilterValue));
            ModelNode emptyNodeList = new ModelNodeGenerator.ModelNodeListBuilder().empty().build();
            new ResourceVerifier(configurableFactoryAddress, client).verifyAttribute(FILTERS_ATTR, emptyNodeList);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(httpServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

}
