package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
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
import java.util.concurrent.TimeoutException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ElytronKeyStoreSecurityRealmTestCase extends AbstractElytronTestCase {

    @Page
    private SecurityRealmPage page;

    private static final String KEY_STORE = "key-store";
    private static final String KEY_STORE_REALM = "key-store-realm";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";

    /**
     * @tpTestDetails Try to create Elytron Keystore security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Keystore security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddKeystoreSecurityRealm() throws Exception {
        final String keyStoreRealmName = "key_store_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address realmAddress = elyOps.getElytronAddress(KEY_STORE_REALM, keyStoreRealmName);
        try {
            createKeyStoreInModel(keyStoreAddress);
            page.navigate();
            page.switchToKeyStoreRealms();
            page.getResourceManager()
                    .addResource(AddKeyStoreSecurityRealmWizard.class)
                    .name(keyStoreRealmName)
                    .keyStore(keyStoreName)
                    .saveWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(keyStoreRealmName));
            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(KEY_STORE, keyStoreName);
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
        final String keyStoreRealmName = "key_store_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String initialKeyStoreName = "initial_key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address initialKeyStoreAddress = elyOps.getElytronAddress(KEY_STORE, initialKeyStoreName);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyStoreRealmAddress = elyOps.getElytronAddress(KEY_STORE_REALM, keyStoreRealmName);
        try {
            createKeyStoreInModel(initialKeyStoreAddress);
            createKeyStoreInModel(keyStoreAddress);
            createKeyStoreRealmInModel(keyStoreRealmAddress, initialKeyStoreName);
            page.navigate();
            page.switchToKeyStoreRealms()
                    .getResourceManager()
                    .selectByName(keyStoreRealmName);
            new ConfigChecker.Builder(client, keyStoreRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, KEY_STORE, keyStoreName)
                    .verifyFormSaved()
                    .verifyAttribute(KEY_STORE, keyStoreName);
        } finally {
            ops.removeIfExists(keyStoreRealmAddress);
            ops.removeIfExists(keyStoreAddress);
            ops.removeIfExists(initialKeyStoreAddress);
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
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyStoreRealmName = "key_store_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyStoreRealmAddress = elyOps.getElytronAddress(KEY_STORE_REALM, keyStoreRealmName);
        try {
            createKeyStoreInModel(keyStoreAddress);
            createKeyStoreRealmInModel(keyStoreRealmAddress, keyStoreName);
            page.navigate();
            page.switchToKeyStoreRealms()
                    .getResourceManager()
                    .removeResource(keyStoreRealmName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Resource should not be present!",
                    page.getResourceManager().isResourcePresent(keyStoreRealmName));
            new ResourceVerifier(keyStoreRealmAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(keyStoreRealmAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createKeyStoreRealmInModel(Address keyStoreRealmAddress, String keyStoreName) throws IOException, TimeoutException, InterruptedException {
        ops.add(keyStoreRealmAddress, Values.of(KEY_STORE, keyStoreName)).assertSuccess();
        adminOps.reloadIfRequired();
    }

    private void createKeyStoreInModel(Address keyStoreAddress) throws IOException, TimeoutException, InterruptedException {
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        ops.add(keyStoreAddress, Values.of("type", "jks").and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                .assertSuccess();
        adminOps.reloadIfRequired();
    }
}
