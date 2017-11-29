package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.other.ssl.AddServerSSLContextWizard;
import org.jboss.hal.testsuite.page.config.elytron.SSLPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.CHECKBOX;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class ServerSSLContextTestCase extends AbstractElytronTestCase {

    private static final String KEY_MANAGER = "key-manager";
    private static final String KEY_STORE = "key-store";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String SERVER_SSL_CONTEXT = "server-ssl-context";
    private static final String SERVER_SSL_CONTEXT_LABEL = "Server SSL Context";
    private static final String PKIX = "PKIX";
    private static final String ALGORITHM = "algorithm";
    private static final String PROTOCOLS = "protocols";
    private static final String TRUST_MANAGER = "trust-manager";
    private static final String TLS_V11 = "TLSv1.1";
    private static final String TLS_V12 = "TLSv1.2";
    private static final String TYPE = "type";
    private static final String JKS = "jks";
    private static final String NEED_CLIENT_AUTH = "need-client-auth";

    @Page
    private SSLPage page;

    /**
     * @tpTestDetails Try to create Elytron Server SSL Context instance in Web Console's Elytron subsystem configuration.
     * Validate created resource is visible in Server SSL Context table.
     * Validate created resource is in model.
     * Validate attribute values of created resource in model.
     */
    @Test
    public void addServerSSLContextTest() throws Exception {
        final String serverSSLContextName = "server_ssl_context_" + randomAlphanumeric(5);
        final String keyManagerName = "key_manager_" + randomAlphanumeric(5);
        final String password = randomAlphanumeric(5);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        final Address serverSSLContextAddress = elyOps.getElytronAddress(SERVER_SSL_CONTEXT, serverSSLContextName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password).build();
        try {
            createKeyStore(keyStoreAddress);
            ops.add(keyManagerAddress, Values.of(ALGORITHM, PKIX).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            page.navigateToApplication()
                    .selectResource(SERVER_SSL_CONTEXT_LABEL)
                    .getResourceManager()
                    .addResource(AddServerSSLContextWizard.class)
                    .name(serverSSLContextName)
                    .keyManager(keyManagerName)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            assertTrue("Created resource should be present in the table!",
                    page.resourceIsPresentInMainTable(serverSSLContextName));
            new ResourceVerifier(serverSSLContextAddress, client).verifyExists()
                    .verifyAttribute(KEY_MANAGER, keyManagerName);
        } finally {
            ops.removeIfExists(serverSSLContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createKeyStore(Address keyStoreAddress) throws IOException {
        final String password = RandomStringUtils.randomAlphanumeric(7);
        ops.add(keyStoreAddress, Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password).build()))
                .assertSuccess();
    }

    /**
     * @tpTestDetails Create Elytron Server SSL Context instance in model
     * and try to remove it in Web Console's Elytron subsystem configuration.
     * Validate the resource is not any more visible in Server SSL Context table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeServerSSLContextTest() throws Exception {
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String serverSSLContextName = "server_ssl_context_" + randomAlphanumeric(5);
        final String keyManagerName = randomAlphanumeric(5);
        final String password = randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        final Address serverSSLContextAddress = elyOps.getElytronAddress(SERVER_SSL_CONTEXT, serverSSLContextName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password)
                .build();
        final ModelNode protocolList = new ModelNodeGenerator.ModelNodeListBuilder()
                .addAll(TLS_V11, TLS_V12)
                .build();
        final ResourceVerifier serverSSLContextVerifier = new ResourceVerifier(serverSSLContextAddress, client);

        try {
            createKeyStore(keyStoreAddress);
            ops.add(keyManagerAddress, Values.of(ALGORITHM, PKIX).and(KEY_STORE,
                    keyStoreName).and(CREDENTIAL_REFERENCE, credentialReferenceNode));
            ops.add(serverSSLContextAddress, Values.of(KEY_MANAGER, keyManagerName).and(PROTOCOLS, protocolList));
            serverSSLContextVerifier.verifyExists();
            page.navigateToApplication()
                    .selectResource(SERVER_SSL_CONTEXT_LABEL)
                    .getResourceManager()
                    .removeResource(serverSSLContextName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Removed resource should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(serverSSLContextName));
            serverSSLContextVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(serverSSLContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Server SSL Context instance in model
     * and try to edit it's attributes in Web Console's Elytron subsystem configuration.
     * Validate edited attribute values in the model.
     */
    @Test
    public void editServerSSLContextAttributesTest() throws Exception {
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String serverSSLContextName = "server_ssl_context_" + randomAlphanumeric(5);
        final String keyManagerName = "key_manager_" + randomAlphanumeric(5);
        final String password = randomAlphanumeric(5);
        final String trustManagerName = "trust_manager_" + randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        final Address serverSSLContextAddress = elyOps.getElytronAddress(SERVER_SSL_CONTEXT, serverSSLContextName);
        final Address trustManagerAddress = elyOps.getElytronAddress(TRUST_MANAGER, trustManagerName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(CLEAR_TEXT, password)
                .build();
        final ModelNode protocolList = new ModelNodeGenerator.ModelNodeListBuilder()
                .addAll(TLS_V11, TLS_V12)
                .build();
        final ModelNode newProtocolList = new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(TLS_V12)).build();

        try {
            createKeyStore(keyStoreAddress);
            ops.add(keyManagerAddress, Values.of(ALGORITHM, PKIX).and(KEY_STORE,
                    keyStoreAddress.getLastPairValue()).and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                    .assertSuccess();
            ops.add(serverSSLContextAddress, Values.of(KEY_MANAGER, keyManagerName).and(PROTOCOLS, protocolList))
                    .assertSuccess();
            ops.add(trustManagerAddress, Values.of(ALGORITHM, PKIX)
                    .and(KEY_STORE, keyStoreAddress.getLastPairValue())).assertSuccess();

            page.navigateToApplication()
                    .selectResource(SERVER_SSL_CONTEXT_LABEL)
                    .getResourceManager()
                    .selectByName(serverSSLContextName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, serverSSLContextAddress).configFragment(page.getConfigFragment())
                    .edit(TEXT, PROTOCOLS, TLS_V12)
                    .edit(TEXT, TRUST_MANAGER, trustManagerName)
                    .edit(CHECKBOX, NEED_CLIENT_AUTH, true)
                    .andSave().verifyFormSaved()
                    .verifyAttribute(PROTOCOLS, newProtocolList)
                    .verifyAttribute(TRUST_MANAGER, trustManagerName)
                    .verifyAttribute(NEED_CLIENT_AUTH, true);
        } finally {
            ops.removeIfExists(serverSSLContextAddress);
            ops.removeIfExists(trustManagerAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }
}
