package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddAggregateProvidersWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class AggregateProvidersTestCase extends AbstractElytronTestCase {

    private static final String PROVIDER_LOADER = "provider-loader";
    private static final String AGGREGATE_PROVIDERS = "aggregate-providers";
    private static final String AGGREGATE_PROVIDERS_LABEL = "Aggregate Providers";
    private static final String PROVIDERS = "providers";

    @Page
    private SSLPage page;

    /**
     * @tpTestDetails Try to create Elytron aggregate provider loader instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in provider loader table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void addAggregateProvidersTest() throws Exception {
        final String provider1name = randomAlphanumeric(5) + "_1";
        final String provider2name = randomAlphanumeric(5) + "_2";
        final String providersValue = String.join("\n", provider1name, provider2name);
        final String aggregateProvidersName = "aggregate_providers" + randomAlphanumeric(5);
        final Address provider1address = elyOps.getElytronAddress(PROVIDER_LOADER, provider1name);
        final Address provider2address = elyOps.getElytronAddress(PROVIDER_LOADER, provider2name);
        final Address aggregateProvidersAddress = elyOps.getElytronAddress(AGGREGATE_PROVIDERS, aggregateProvidersName);
        try {
            ops.add(provider1address).assertSuccess();
            ops.add(provider2address).assertSuccess();
            page.navigateToApplication()
                    .selectResource(AGGREGATE_PROVIDERS_LABEL)
                    .getResourceManager()
                    .addResource(AddAggregateProvidersWizard.class)
                    .name(aggregateProvidersName)
                    .providers(providersValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(aggregateProvidersName));
            new ResourceVerifier(aggregateProvidersAddress, client).verifyExists()
                    .verifyAttribute(PROVIDERS, new ModelNodeListBuilder()
                            .addNode(new ModelNode(provider1name))
                            .addNode(new ModelNode(provider2name)).build());
        } finally {
            ops.removeIfExists(aggregateProvidersAddress);
            ops.removeIfExists(provider1address);
            ops.removeIfExists(provider2address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron aggregate provider loader instance and try to remove it in Web Console's Elytron
     * subsystem configuration.
     * Validate removed resource is not any more visible in aggregate provider loader table.
     * Validate removed resource is not any more present in model.
     */
    @Test
    public void removeAggregateProvidersTest() throws Exception {
        final String provider1name = randomAlphanumeric(5) + "_1";
        final String provider2name = randomAlphanumeric(5) + "_2";
        final String aggregateProvidersName = "aggregate_providers_" + randomAlphanumeric(5);
        final Address provider1address = elyOps.getElytronAddress(PROVIDER_LOADER, provider1name);
        final Address provider2address = elyOps.getElytronAddress(PROVIDER_LOADER, provider2name);
        final Address aggregateProvidersAddress = elyOps.getElytronAddress(AGGREGATE_PROVIDERS, aggregateProvidersName);
        try {
            ops.add(provider1address).assertSuccess();
            ops.add(provider2address).assertSuccess();
            ops.add(aggregateProvidersAddress, Values.of(PROVIDERS, new ModelNodeListBuilder()
                            .addNode(new ModelNode(provider1name))
                            .addNode(new ModelNode(provider2name)).build()));

            page.navigateToApplication()
                    .selectResource(AGGREGATE_PROVIDERS_LABEL)
                    .getResourceManager()
                    .removeResource(aggregateProvidersName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(aggregateProvidersName));
            new ResourceVerifier(aggregateProvidersAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(aggregateProvidersAddress);
            ops.removeIfExists(provider1address);
            ops.removeIfExists(provider2address);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron aggregate provider loader instance and try to edit it's attribute in Web Console's
     * Elytron subsystem configuration.
     * Validate edited attribute in model.
     */
    @Test
    public void editAggregateProvidersAttributesTest() throws Exception {
        final String provider1name = randomAlphanumeric(5) + "_1";
        final String provider2name = randomAlphanumeric(5) + "_2";
        final String provider3name = randomAlphanumeric(5) + "_3";
        final String providersValue = String.join("\n", provider2name, provider3name);
        final String aggregateProvidersName = "aggregate_providers_" + randomAlphanumeric(5);
        final Address provider1address = elyOps.getElytronAddress(PROVIDER_LOADER, provider1name);
        final Address provider2address = elyOps.getElytronAddress(PROVIDER_LOADER, provider2name);
        final Address provider3address = elyOps.getElytronAddress(PROVIDER_LOADER, provider3name);
        final Address aggregateProvidersAddress = elyOps.getElytronAddress(AGGREGATE_PROVIDERS, aggregateProvidersName);
        try {
            ops.add(provider1address).assertSuccess();
            ops.add(provider2address).assertSuccess();
            ops.add(provider3address).assertSuccess();
            ops.add(aggregateProvidersAddress, Values.of(PROVIDERS, new ModelNodeListBuilder()
                            .addNode(new ModelNode(provider1name))
                            .addNode(new ModelNode(provider2name)).build()));
            page.navigateToApplication()
                    .selectResource(AGGREGATE_PROVIDERS_LABEL)
                    .getResourceManager()
                    .selectByName(aggregateProvidersName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, aggregateProvidersAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, PROVIDERS, providersValue)
                    .verifyFormSaved()
                    .verifyAttribute(PROVIDERS, new ModelNodeListBuilder()
                            .addNode(new ModelNode(provider2name))
                            .addNode(new ModelNode(provider3name)).build());
        } finally {
            ops.removeIfExists(aggregateProvidersAddress);
            ops.removeIfExists(provider1address);
            ops.removeIfExists(provider2address);
            ops.removeIfExists(provider3address);
            adminOps.reloadIfRequired();
        }
    }

}
