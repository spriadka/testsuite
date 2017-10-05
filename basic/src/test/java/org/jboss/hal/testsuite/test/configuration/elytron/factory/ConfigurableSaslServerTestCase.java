package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddConfigurableSaslServerWizard;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddSaslPatternFilterWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddSaslPatternFilterValidator;
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
public class ConfigurableSaslServerTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String CONFIGURABLE_SASL_SERVER_LABEL = "Configurable SASL Server";
    private static final String CONFIGURABLE_SASL_SERVER_FACTORY = "configurable-sasl-server-factory";
    private static final String PROPERTIES = "properties";
    private static final String PATTERN_FILTER = "pattern-filter";
    private static final String PROTOCOL = "protocol";
    private static final String SERVER_NAME = "server-name";
    private static final String PREDEFINED_FILTER = "predefined-filter";
    private static final String SASL_SERVER_FACTORY = "sasl-server-factory";
    private static final String SERVICE_LOADER_SASL_SERVER_FACTORY = "service-loader-sasl-server-factory";
    private static final String PROVIDER_SASL_SERVER_FACTORY = "provider-sasl-server-factory";

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
    public void addServerRequiredFieldsTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);
        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL);
            page.getResourceManager()
                    .addResource(AddConfigurableSaslServerWizard.class)
                    .name(configurableFactoryName)
                    .saslServerFactory(saslServerFactoryName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created Configurable SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName));
            new ResourceVerifier(configurableFactoryAddress, client)
                    .verifyExists()
                    .verifyAttribute(SASL_SERVER_FACTORY, saslServerFactoryName);
        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addServerOptionalFieldsTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String property1key = RandomStringUtils.randomAlphanumeric(5);
        final String property2key = RandomStringUtils.randomAlphanumeric(5);
        final String property1value = RandomStringUtils.randomAlphanumeric(5);
        final String property2value = RandomStringUtils.randomAlphanumeric(5);
        final String protocolName = RandomStringUtils.randomAlphanumeric(5);
        final String serverNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(saslServerFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL);
            page.getResourceManager()
                    .addResource(AddConfigurableSaslServerWizard.class)
            .name(configurableFactoryName)
                    .saslServerFactory(saslServerFactoryName)
                    .properties(property1key + "=" + property1value + "\n" + property2key + "=" + property2value)
                    .protocol(protocolName)
                    .serverName(serverNameValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Newly created Configurable SASL Server Factory should be present in the table!",
                    page.resourceIsPresentInMainTable(configurableFactoryName));

            final ModelNode expectedPropertiesNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty(property1key, property1value).addProperty(property2key, property2value).build();

            new ResourceVerifier(configurableFactoryAddress, client)
                    .verifyExists().verifyAttribute(SASL_SERVER_FACTORY, saslServerFactoryName)
                    .verifyAttribute(PROPERTIES, expectedPropertiesNode).verifyAttribute(PROTOCOL, protocolName)
                    .verifyAttribute(SERVER_NAME, serverNameValue);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeServerTest() throws Exception {
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
    public void editAttributesTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String property1key = RandomStringUtils.randomAlphanumeric(5);
        final String property2key = RandomStringUtils.randomAlphanumeric(5);
        final String property3key = RandomStringUtils.randomAlphanumeric(5);
        final String property1value = RandomStringUtils.randomAlphanumeric(5);
        final String property2value = RandomStringUtils.randomAlphanumeric(5);
        final String property3value = RandomStringUtils.randomAlphanumeric(5);
        final String protocolName = RandomStringUtils.randomAlphanumeric(5);
        final String serverNameValue = RandomStringUtils.randomAlphanumeric(5);

        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1);
        final Address saslServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ModelNode initialProps = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(property1key, property1value).build();
            ops.add(configurableFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslServerFactoryName1).and(PROPERTIES, initialProps))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_SERVER_FACTORY, saslServerFactoryName2)
                    .verifyFormSaved().verifyAttribute(SASL_SERVER_FACTORY, saslServerFactoryName2);

            final ModelNode expectedPropertiesNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty(property2key, property2value).addProperty(property3key, property3value).build();
            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PROPERTIES,
                            property2key + "=" + property2value + "\n" + property3key + "=" + property3value)
                    .verifyFormSaved().verifyAttribute(PROPERTIES, expectedPropertiesNode);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PROTOCOL, protocolName).verifyFormSaved()
                    .verifyAttribute(PROTOCOL, protocolName);

            new ConfigChecker.Builder(client, configurableFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SERVER_NAME, serverNameValue).verifyFormSaved()
                    .verifyAttribute(SERVER_NAME, serverNameValue);
        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress1);
            ops.removeIfExists(saslServerFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFilterMissingFieldsTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                configurableFactoryName);
        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslServerFactoryName)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(CONFIGURABLE_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);
            page.getConfigAreaResourceManager()
                    .addResource(AddSaslPatternFilterWizard.class)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowOpen();
            assertTrue("Page should be displaying validation error regarding missing attribute",
                    page.getWindowFragment().isErrorShownInForm());
        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFilterInvalidFieldsCombinationTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                configurableFactoryName);
        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslServerFactoryName)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(CONFIGURABLE_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);
            AddSaslPatternFilterWizard wizard = page.getConfigAreaResourceManager()
                    .addResource(AddSaslPatternFilterWizard.class);
            new AddSaslPatternFilterValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);
        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFilterWithPredefinedFilterTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String predefinedFilterValue = "DIGEST";
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(configurableFactoryAddress, client);
        final boolean enablingDefault = false;
        ModelNodeGenerator.ModelNodeListBuilder filterListNodeBuilder = new ModelNodeGenerator.ModelNodeListBuilder();
        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslServerFactoryName)).assertSuccess();
            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            page.getConfigAreaResourceManager()
                    .addResource(AddSaslPatternFilterWizard.class)
                    .predefinedFilter(predefinedFilterValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(predefinedFilterValue));
            ModelNode predefinedFilterNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty(PREDEFINED_FILTER, predefinedFilterValue)
                    .addProperty(ENABLING, new ModelNode(enablingDefault)).build();
            filterListNodeBuilder.addNode(predefinedFilterNode);
            factoryVerifier.verifyExists()
                    .verifyAttribute(FILTERS_ATTR, filterListNodeBuilder.build());

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFilterWithPatternFilterTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String patternFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                configurableFactoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(configurableFactoryAddress, client);
        final boolean enablingDefault = false;
        ModelNodeGenerator.ModelNodeListBuilder filterListNodeBuilder = new ModelNodeGenerator.ModelNodeListBuilder();
        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslServerFactoryName)).assertSuccess();
            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);
            page.getConfigAreaResourceManager()
                    .addResource(AddSaslPatternFilterWizard.class)
                    .patternFilter(patternFilterValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Newly created filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable2ndColumn(patternFilterValue));
            ModelNode patternFilterNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty(PATTERN_FILTER, patternFilterValue)
                    .addProperty(ENABLING, new ModelNode(enablingDefault))
                    .build();
            filterListNodeBuilder.addNode(patternFilterNode);
            factoryVerifier.verifyExists()
                    .verifyAttribute(FILTERS_ATTR, filterListNodeBuilder.build());

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFilterOptionalFieldsTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String predefinedFilterValue = "DIGEST";
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                configurableFactoryName);
        final ResourceVerifier factoryVerifier = new ResourceVerifier(configurableFactoryAddress, client);
        final boolean enabling = true;
        ModelNodeGenerator.ModelNodeListBuilder filterListNodeBuilder = new ModelNodeGenerator.ModelNodeListBuilder();
        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslServerFactoryName)).assertSuccess();
            page.navigateToApplication().selectFactory(CONFIGURABLE_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);

            page.getConfigAreaResourceManager()
                    .addResource(AddSaslPatternFilterWizard.class)
                    .predefinedFilter(predefinedFilterValue)
                    .enabling(enabling)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created filter should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(predefinedFilterValue));
            ModelNode predefinedFilterNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty(PREDEFINED_FILTER, predefinedFilterValue)
                    .addProperty(ENABLING, new ModelNode(enabling)).build();
            filterListNodeBuilder.addNode(predefinedFilterNode);
            factoryVerifier.verifyExists().verifyAttribute(FILTERS_ATTR, filterListNodeBuilder.build());

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeFiltersTest() throws Exception {
        final String saslServerFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String configurableFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String predefinedFilterValue = "HASH_MD5";
        final Address saslServerFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName);
        final Address configurableFactoryAddress = elyOps.getElytronAddress(CONFIGURABLE_SASL_SERVER_FACTORY,
                        configurableFactoryName);
        final ModelNode filtersNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(PREDEFINED_FILTER, predefinedFilterValue).addProperty(ENABLING, new ModelNode(true)).build())
                .build();

        try {
            ops.add(saslServerFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(configurableFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslServerFactoryName).and(FILTERS_ATTR, filtersNode))
                    .assertSuccess();
            page.navigateToApplication()
                    .selectFactory(CONFIGURABLE_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .selectByName(configurableFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);
            page.getConfigAreaResourceManager()
                    .removeResource(predefinedFilterValue)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted filter should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(predefinedFilterValue));
            ModelNode emptyNodeList = new ModelNodeGenerator.ModelNodeListBuilder().empty().build();
            new ResourceVerifier(configurableFactoryAddress, client).verifyAttribute(FILTERS_ATTR, emptyNodeList);

        } finally {
            ops.removeIfExists(configurableFactoryAddress);
            ops.removeIfExists(saslServerFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }
}
