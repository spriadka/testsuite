package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddAggregateSaslServerWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddAggregateSaslServerValidator;
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
public class AggregateSaslServerTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String SERVICE_LOADER_SASL_SERVER_FACTORY = "service-loader-sasl-server-factory";
    private static final String AGGREGATE_SASL_SERVER_LABEL = "Aggregate SASL Server";
    private static final String AGGREGATE_SASL_SERVER_FACTORY = "aggregate-sasl-server-factory";
    private static final String PROVIDER_SASL_SERVER_FACTORY = "provider-sasl-server-factory";
    private static final String SASL_SERVER_FACTORIES = "sasl-server-factories";

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
    public void addAggregateSaslServerTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String saslServerFactoryNamesSeparatedByNewLine = String.join("\n",
                saslServerFactoryName1, saslServerFactoryName2);
        final String aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1);
        final Address saslServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2);
        final Address aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_SASL_SERVER_FACTORY,
                        aggregateFactoryName);
        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(AGGREGATE_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .addResource(AddAggregateSaslServerWizard.class)
                    .name(aggregateFactoryName)
                    .saslServerMechanismFactories(saslServerFactoryNamesSeparatedByNewLine)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created Aggregate SASL Server Mechanism should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregateFactoryName));
            ModelNode expectedHttpFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
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
    public void addAggregateSaslServerWithoutRequiredAttributesTest() {
        AddAggregateSaslServerWizard wizard = page.navigateToApplication()
                .selectFactory(AGGREGATE_SASL_SERVER_LABEL)
                .getResourceManager()
                .addResource(AddAggregateSaslServerWizard.class);
        new AddAggregateSaslServerValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);

    }

    @Test
    public void editAggregateSaslServerTest() throws Exception {
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String saslServerFactoryName3 = RandomStringUtils.randomAlphanumeric(5);
        final String saslServerFactoryName4 = RandomStringUtils.randomAlphanumeric(5);
        final String newHttpServerFactoryNamesSeparatedByNewLine = String.join("\n",
                saslServerFactoryName3, saslServerFactoryName4);
        final String aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1);
        final Address saslServerFactoryAddress2 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2);
        final Address saslServerFactoryAddress3 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName3);
        final Address saslServerFactoryAddress4 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName4);
        final Address aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_SASL_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode initialHttpFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNode(saslServerFactoryName1)).addNode(new ModelNode(saslServerFactoryName2)).build();
        final ModelNode expectedEditedHttpFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNode(saslServerFactoryName3)).addNode(new ModelNode(saslServerFactoryName4)).build();

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(MODULE, MODULE_NAME_2)).assertSuccess();
            ops.add(saslServerFactoryAddress3, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress4, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_2)).assertSuccess();
            ops.add(aggregateFactoryAddress, Values.of(SASL_SERVER_FACTORIES, initialHttpFactoriesNode))
                    .assertSuccess();

            page.navigateToApplication()
                    .selectFactory(AGGREGATE_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .selectByName(aggregateFactoryName);

            new ConfigChecker.Builder(client, aggregateFactoryAddress).configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_SERVER_FACTORIES, newHttpServerFactoryNamesSeparatedByNewLine)
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
        final String saslServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String saslServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address saslServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                saslServerFactoryName1);
        final Address saslServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
                        saslServerFactoryName2);
        final Address aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_SASL_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode saslFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNode(saslServerFactoryName1)).addNode(new ModelNode(saslServerFactoryName2)).build();

        try {
            ops.add(saslServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(saslServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(aggregateFactoryAddress, Values.of(SASL_SERVER_FACTORIES, saslFactoriesNode)).assertSuccess();

            page.navigateToApplication()
                    .selectFactory(AGGREGATE_SASL_SERVER_LABEL)
                    .getResourceManager()
                    .removeResource(aggregateFactoryName)
                    .confirmAndDismissReloadRequiredMessage()
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
}
