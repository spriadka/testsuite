package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddProviderFilteringSaslFilterWizard;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddProviderFilteringSaslServerWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddProviderFilteringSaslServerValidator;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.Assert;
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
public class ProviderFilteringSaslTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String PROVIDER_SASL_SERVER_FACTORY = "provider-sasl-server-factory";
    private static final String PROVIDER_FILTERING_SASL_SERVER_LABEL = "Mechanism Provider Filtering SASL";
    private static final String PROVIDER_FILTERING_SASL_SERVER_FACTORY = "mechanism-provider-filtering-sasl-server-factory";
    private static final String SASL_SERVER_FACTORY = "sasl-server-factory";
    private static final String FILTERS_LABEL = "Filters";
    private static final String FILTERS_ATTR = "filters";
    private static final String ENABLING = "enabling";
    private static final String VERSION_COMPARISON = "version-comparison";
    private static final String MECHANISM_NAME = "mechanism-name";
    private static final String PROVIDER_VERSION = "provider-version";

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
    public void addProviderFilteringSaslServerRequiredFieldsTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName);
        final Address filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                filteringSaslFactoryName);

        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL);
            page.getResourceManager()
                    .addResource(AddProviderFilteringSaslServerWizard.class)
                    .name(filteringSaslFactoryName)
                    .saslServerFactory(saslFactoryName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created factory should be present in the table! "
                            + "Error might be related to https://issues.jboss.org/browse/WFLY-7575.",
                    page.resourceIsPresentInMainTable(filteringSaslFactoryName));
            new ResourceVerifier(filteringSaslFactoryAddress, client)
                    .verifyExists()
                    .verifyAttribute(SASL_SERVER_FACTORY, saslFactoryName);

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addProviderFilteringSaslServerOptionalFieldsTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName);
        final Address filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                filteringSaslFactoryName);

        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_2)).assertSuccess();
            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL);
            page.getResourceManager()
                    .addResource(AddProviderFilteringSaslServerWizard.class)
                    .name(filteringSaslFactoryName)
                    .saslServerFactory(saslFactoryName)
                    .enabling(true)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Newly created factory should be present in the table!",
                    page.resourceIsPresentInMainTable(filteringSaslFactoryName));
            new ResourceVerifier(filteringSaslFactoryAddress, client)
                    .verifyExists()
                    .verifyAttribute(SASL_SERVER_FACTORY, saslFactoryName)
                    .verifyAttribute(ENABLING, true);

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addProviderFilteringSaslServerMissingFields() {
        page.navigateToApplication()
                .selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL);
        AddProviderFilteringSaslServerWizard wizard = page.getResourceManager().addResource(AddProviderFilteringSaslServerWizard.class);
        new AddProviderFilteringSaslServerValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);
    }

    @Test
    public void removeProviderFilteringSaslServerTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String providerNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                saslFactoryName);
        final Address filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                filteringSaslFactoryName);
        final ResourceVerifier filteringSaslFactoryVerifier = new ResourceVerifier(filteringSaslFactoryAddress, client);
        final ModelNode filtersNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerNameValue).build())
                .build();

        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName).and(FILTERS_ATTR, filtersNode))
                    .assertSuccess();
            filteringSaslFactoryVerifier.verifyExists();
            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL);
            page.getResourceManager()
                    .removeResource(filteringSaslFactoryName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted factory should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(filteringSaslFactoryName));
            filteringSaslFactoryVerifier
                    .verifyDoesNotExist();

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editProviderFilteringSaslServerAttributesTest() throws Exception {
        final String saslFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String saslFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String providerNameValue = RandomStringUtils.randomAlphanumeric(5);

        final Address saslFactoryAddress1 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                saslFactoryName1);
        final Address saslFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                saslFactoryName2);
        final Address filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                filteringSaslFactoryName);
        final ModelNode filtersNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerNameValue).build())
                .build();

        try {
            ops.add(saslFactoryAddress1, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(saslFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_2)).assertSuccess();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName1).and(FILTERS_ATTR, filtersNode)).assertSuccess();

            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(filteringSaslFactoryName);

            new ConfigChecker.Builder(client, filteringSaslFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_SERVER_FACTORY, saslFactoryName2)
                    .verifyFormSaved().verifyAttribute(SASL_SERVER_FACTORY, saslFactoryName2);

            new ConfigChecker.Builder(client, filteringSaslFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ENABLING, false).verifyFormSaved()
                    .verifyAttribute(ENABLING, false);
        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress1);
            ops.removeIfExists(saslFactoryAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFiltersWithoutRequiredFieldsTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String mechanismNameValue = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);
        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName))
                    .assertSuccess();
            page.navigateToApplication()
                    .selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .selectByName(filteringSaslFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);
            page.getConfigAreaResourceManager()
                    .addResource(AddProviderFilteringSaslFilterWizard.class)
                    .mechanismName(mechanismNameValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowOpen();
        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFiltersInvalidProviderVersionTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String providerName1 = RandomStringUtils.randomAlphanumeric(5);
        final String nonNumericProviderVersion = RandomStringUtils.randomAlphabetic(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);
        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(filteringSaslFactoryAddress, Values.of(SASL_SERVER_FACTORY, saslFactoryName))
                    .assertSuccess();
            page.navigateToApplication()
                    .selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(filteringSaslFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);
            page.getConfigAreaResourceManager()
                    .addResource(AddProviderFilteringSaslFilterWizard.class)
                    .providerName(providerName1)
                    .providerVersion(nonNumericProviderVersion)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowOpen();
            Assert.assertTrue("Page should be displaying validation error regarding  invalid numeric value for " +
                    "provider version: " + nonNumericProviderVersion, page.getWindowFragment().isErrorShownInForm());

        } finally {
            ops.removeIfExists(filteringSaslFactoryAddress);
            ops.removeIfExists(saslFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addFiltersTest() throws Exception {
        final double correctProviderVersion = 15987.0;
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final String providerName1 = RandomStringUtils.randomAlphanumeric(5);
        final String providerName2 = RandomStringUtils.randomAlphanumeric(5);
        final String mechanismNameValue = RandomStringUtils.randomAlphanumeric(5);
        final String versionComparisonValue = "greater-than";
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslFactoryName),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);
        final ModelNode filterProperties1 = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName1)
                .build(),
                filterProperties2 = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName2)
                        .addProperty(VERSION_COMPARISON, versionComparisonValue)
                        .addProperty(MECHANISM_NAME, mechanismNameValue)
                        .addProperty(PROVIDER_VERSION, new ModelNode(correctProviderVersion)).build(),
                initialFiltersNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(filterProperties1).build(),
                expectedEditedFiltersNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(filterProperties1)
                        .addNode(filterProperties2).build();

        try {
            ops.add(saslFactoryAddress, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(filteringSaslFactoryAddress,
                    Values.of(SASL_SERVER_FACTORY, saslFactoryName).and(FILTERS_ATTR, initialFiltersNode))
                    .assertSuccess();
            page.navigateToApplication().selectFactory(PROVIDER_FILTERING_SASL_SERVER_LABEL).getResourceManager()
                    .selectByName(filteringSaslFactoryName);
            page.switchToConfigAreaTab(FILTERS_LABEL);
            page.getConfigAreaResourceManager()
                    .addResource(AddProviderFilteringSaslFilterWizard.class)
                    .providerName(providerName2)
                    .providerVersion(String.valueOf(correctProviderVersion))
                    .mechanismName(mechanismNameValue)
                    .versionComparison(versionComparisonValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
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
    public void removeFiltersTest() throws Exception {
        final String saslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                filteringSaslFactoryName = RandomStringUtils.randomAlphanumeric(5),
                providerName1 = RandomStringUtils.randomAlphanumeric(5),
                providerName2 = RandomStringUtils.randomAlphanumeric(5);
        final Address saslFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                saslFactoryName),
                filteringSaslFactoryAddress = elyOps.getElytronAddress(PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                        filteringSaslFactoryName);
        final ModelNode filterProperties1 = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName1)
                .build(),
                filterProperties2 = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(PROVIDER_NAME, providerName2).build(),
                initialFiltersNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(filterProperties1).addNode(filterProperties2)
                        .build(),
                expectedEditedFiltersNode = new ModelNodeGenerator.ModelNodeListBuilder().addNode(filterProperties2).build();

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
}
