package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddAggregateHttpServerWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddAggregateHttpServerValidator;
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
public class AggregateHttpServerTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory";
    private static final String PROVIDER_HTTP_SERVER_MECHANISM_FACTORY = "provider-http-server-mechanism-factory";
    private static final String AGGREGATE_HTTP_SERVER_LABEL = "Aggregate HTTP Server";
    private static final String AGGREGATE_HTTP_SERVER_FACTORY = "aggregate-http-server-mechanism-factory";
    private static final String HTTP_SERVER_MECHANISM_FACTORIES = "http-server-mechanism-factories";

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
    public void addAggregateHttpServerTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String httpServerFactoryNamesSeparatedByNewLine = httpServerFactoryName1 + "\n" + httpServerFactoryName2;
        final String aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1);
        final Address httpServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2);
        final Address aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_HTTP_SERVER_FACTORY,
                        aggregateFactoryName);


        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            adminOps.reloadIfRequired();
            page.navigateToApplication().selectFactory(AGGREGATE_HTTP_SERVER_LABEL);
            page.getResourceManager()
                    .addResource(AddAggregateHttpServerWizard.class)
                    .name(aggregateFactoryName)
                    .httpServerMechanismFactories(httpServerFactoryNamesSeparatedByNewLine)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created Aggregate HTTP Server Mechanism should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregateFactoryName));

            ModelNode expectedHttpFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
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
    public void addAggregateHttpServerWithoutRequiredAttributesTest() {
        page.navigateToApplication()
                .selectFactory(AGGREGATE_HTTP_SERVER_LABEL);
        AddAggregateHttpServerWizard wizard = page.getResourceManager().addResource(AddAggregateHttpServerWizard.class);
        new AddAggregateHttpServerValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);
    }

    @Test
    public void editAggregateHttpServerTest() throws Exception {
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String httpServerFactoryName3 = RandomStringUtils.randomAlphanumeric(5);
        final String httpServerFactoryName4 = RandomStringUtils.randomAlphanumeric(5);
        final String newHttpServerFactoryNamesSeparatedByNewLine = String.join("\n",
                httpServerFactoryName3, httpServerFactoryName4);
        final String aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1);
        final Address httpServerFactoryAddress2 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2);
        final Address httpServerFactoryAddress3 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName3);
        final Address httpServerFactoryAddress4 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName4);
        final Address aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_HTTP_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode initialHttpFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNode(httpServerFactoryName1)).addNode(new ModelNode(httpServerFactoryName2)).build();
        final ModelNode expectedEditedHttpFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
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
                    .editAndSave(ConfigChecker.InputType.TEXT, HTTP_SERVER_MECHANISM_FACTORIES, newHttpServerFactoryNamesSeparatedByNewLine)
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
        final String httpServerFactoryName1 = RandomStringUtils.randomAlphanumeric(5);
        final String httpServerFactoryName2 = RandomStringUtils.randomAlphanumeric(5);
        final String aggregateFactoryName = RandomStringUtils.randomAlphanumeric(5);
        final Address httpServerFactoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                httpServerFactoryName1);
        final Address httpServerFactoryAddress2 = elyOps.getElytronAddress(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                        httpServerFactoryName2);
        final Address aggregateFactoryAddress = elyOps.getElytronAddress(AGGREGATE_HTTP_SERVER_FACTORY,
                        aggregateFactoryName);
        final ModelNode httpFactoriesNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNode(httpServerFactoryName1)).addNode(new ModelNode(httpServerFactoryName2)).build();

        try {
            ops.add(httpServerFactoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpServerFactoryAddress2, Values.of(PROVIDER_LOADER, PROVIDER_LOADER_NAME_1)).assertSuccess();
            ops.add(aggregateFactoryAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORIES, httpFactoriesNode)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(AGGREGATE_HTTP_SERVER_LABEL)
                    .getResourceManager()
                    .removeResource(aggregateFactoryName)
                    .confirmAndDismissReloadRequiredMessage()
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
}
