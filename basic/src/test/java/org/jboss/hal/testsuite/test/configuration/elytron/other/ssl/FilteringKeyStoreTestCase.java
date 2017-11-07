package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddFilteringKeyStoreWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class FilteringKeyStoreTestCase extends AbstractElytronTestCase {

    private static final String KEY_STORE = "key-store";
    private static final String TYPE = "type";
    private static final String JKS = "jks";
    private static final String ALIAS_FILTER = "alias-filter";
    private static final String FILTERING_KEY_STORE = "filtering-key-store";
    private static final String FILTERING_KEY_STORE_LABEL = "Filtering Key Store";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String CLEAR_TEXT = "clear-text";

    @Page
    private SSLPage page;

    @Test
    public void addFilteringKeyStoreTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String aliasFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName),
                filteringKeyStoreAddress = elyOps.getElytronAddress(FILTERING_KEY_STORE, filteringKeyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            page.navigateToApplication()
                    .selectResource(FILTERING_KEY_STORE_LABEL)
                    .getResourceManager()
                    .addResource(AddFilteringKeyStoreWizard.class)
                    .name(filteringKeyStoreName)
                    .aliasFilter(aliasFilterValue)
                    .keyStore(keyStoreName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(filteringKeyStoreName));
            new ResourceVerifier(filteringKeyStoreAddress, client).verifyExists()
                    .verifyAttribute(ALIAS_FILTER, aliasFilterValue)
                    .verifyAttribute(KEY_STORE, keyStoreName);

        } finally {
            ops.removeIfExists(filteringKeyStoreAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeFilteringKeyStoreTest() throws Exception {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String aliasFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address filteringKeyStoreAddress = elyOps.getElytronAddress(FILTERING_KEY_STORE, filteringKeyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        final ResourceVerifier filteringKeyStoreVerifier = new ResourceVerifier(filteringKeyStoreAddress, client);

        try {
            ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(filteringKeyStoreAddress, Values.of(ALIAS_FILTER, aliasFilterValue).and(KEY_STORE, keyStoreName))
                    .assertSuccess();
            filteringKeyStoreVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(FILTERING_KEY_STORE_LABEL)
                    .getResourceManager()
                    .removeResource(filteringKeyStoreName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(filteringKeyStoreName));
            filteringKeyStoreVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(filteringKeyStoreAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editFilteringKeyStoreAttributesTest() throws Exception {
        final String originalKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String newKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String filteringKeyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final String originalAliasFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final String newAliasFilterValue = RandomStringUtils.randomAlphanumeric(5);
        final Address originalKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, originalKeyStoreName);
        final Address newKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, newKeyStoreName);
        final Address filteringKeyStoreAddress = elyOps.getElytronAddress(FILTERING_KEY_STORE, filteringKeyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();

        try {
            ops.add(originalKeyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(newKeyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(filteringKeyStoreAddress, Values.of(ALIAS_FILTER, originalAliasFilterValue)
                    .and(KEY_STORE, originalKeyStoreName)).assertSuccess();

            page.navigateToApplication()
                    .selectResource(FILTERING_KEY_STORE_LABEL)
                    .getResourceManager()
                    .selectByName(filteringKeyStoreName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);

            new ConfigChecker.Builder(client, filteringKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, ALIAS_FILTER, newAliasFilterValue).verifyFormSaved()
                    .verifyAttribute(ALIAS_FILTER, newAliasFilterValue);

            new ConfigChecker.Builder(client, filteringKeyStoreAddress).configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, KEY_STORE, newKeyStoreName).verifyFormSaved()
                    .verifyAttribute(KEY_STORE, newKeyStoreName);

        } finally {
            ops.removeIfExists(filteringKeyStoreAddress);
            ops.removeIfExists(originalKeyStoreAddress);
            ops.removeIfExists(newKeyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }
}
