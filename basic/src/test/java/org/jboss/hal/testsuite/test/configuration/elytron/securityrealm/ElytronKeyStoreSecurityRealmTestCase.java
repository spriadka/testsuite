package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddKeyStoreSecurityRealmWizard;
import org.jboss.hal.testsuite.page.config.elytron.SecurityRealmPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronKeyStoreSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String
            KEY_STORE = "key-store",
            KEY_STORE_REALM = "key-store-realm",
            CLEAR_TEXT = "clear-text",
            CREDENTIAL_REFERENCE = "credential-reference";

    /**
     * @tpTestDetails Try to create Elytron Keystore security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Keystore security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddKeystoreSecurityRealm() throws Exception {
        final Address realmAddress = elyOps.getElytronAddress(KEY_STORE_REALM, org.apache.commons.lang.RandomStringUtils.randomAlphabetic(7)),
                keyStoreAddress = createKeyStore();

        page.navigate();
        page.switchToKeyStoreRealms();

        try {
            page.getResourceManager().addResource(AddKeyStoreSecurityRealmWizard.class)
                    .name(realmAddress.getLastPairValue())
                    .keyStore(keyStoreAddress.getLastPairValue())
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(KEY_STORE, keyStoreAddress.getLastPairValue());
        } finally {
            ops.removeIfExists(realmAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Keystore security realm instance in model and try to edit its key-store attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editKeyStore() throws Exception {
        final Address
                initialKeyStore = createKeyStore(),
                keyStoreRealmAddress = createKeyStoreRealm(initialKeyStore),
                keyStoreAddress = createKeyStore();

        try {
            page.navigate();
            page.switchToKeyStoreRealms()
                    .getResourceManager()
                    .selectByName(keyStoreRealmAddress.getLastPairValue());

            final String value = keyStoreAddress.getLastPairValue();
            new ConfigChecker.Builder(client, keyStoreRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, KEY_STORE, value)
                    .verifyFormSaved()
                    .verifyAttribute(KEY_STORE, value);
        } finally {
            ops.removeIfExists(keyStoreRealmAddress);
            ops.removeIfExists(keyStoreAddress);
            ops.removeIfExists(initialKeyStore);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Keystore security realm instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Keystore security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveKeyStoreSecurityRealm() throws Exception {
        final Address
                keyStoreAddress = createKeyStore(),
                keyStoreRealmAddress = createKeyStoreRealm(keyStoreAddress);

        try {
            page.navigate();
            page.switchToKeyStoreRealms()
                    .getResourceManager()
                    .removeResource(keyStoreRealmAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse("Resource should not be present!",
                    page.getResourceManager().isResourcePresent(keyStoreRealmAddress.getLastPairValue()));

            new ResourceVerifier(keyStoreRealmAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(keyStoreRealmAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    private Address createKeyStoreRealm(Address keyStore) throws IOException {
        final Address address = elyOps.getElytronAddress(KEY_STORE_REALM, org.apache.commons.lang.RandomStringUtils.randomAlphabetic(7));
        ops.add(address, Values.of(KEY_STORE, keyStore.getLastPairValue()));
        return address;
    }

    private Address createKeyStore() throws IOException {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        ops.add(keyStoreAddress, Values.of("type", "jks").and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                .assertSuccess();
        return keyStoreAddress;
    }
}
