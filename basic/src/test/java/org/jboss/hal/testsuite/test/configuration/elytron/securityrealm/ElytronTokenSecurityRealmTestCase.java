package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.securityrealm.AddTokenSecurityRealmWizard;
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
public class ElytronTokenSecurityRealmTestCase extends AbstractElytronTestCase {

    private static final String JWT = "jwt";
    private static final String OAUTH2_INTROSPECTION_TAB_LABEL = "OAuth 2 Introspection";
    private static final String OAUTH2_INTROSPECTION = "oauth2-introspection";
    private static final String CLEAR_TEXT = "clear-text";
    private static final String CREDENTIAL_REFERENCE = "credential-reference";
    private static final String KEY_STORE = "key-store";
    private static final String PRINCIPAL_CLAIM = "principal-claim";
    private static final String TOKEN_REALM = "token-realm";
    private static final String AUDIENCE = "audience";
    private static final String CERTIFICATE = "certificate";
    private static final String ISSUER = "issuer";
    private static final String PUBLIC_KEY = "public-key";
    private static final String KEY_MANAGER = "key-manager";
    private static final String CLIENT_SSL_CONTEXT = "client-ssl-context";
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String HOST_NAME_VERIFICATION_POLICY = "host-name-verification-policy";
    private static final String INTROSPECTION_URL = "introspection-url";




    @Page
    private SecurityRealmPage page;

    /**
     * @tpTestDetails Try to create Elytron Token security realm instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Token security realm table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddTokenSecurityRealm() throws Exception {
        final String realmName = "token_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String principalClaimValue = RandomStringUtils.randomAlphabetic(7);
        final Address realmAddress = elyOps.getElytronAddress(TOKEN_REALM, realmName);
        try {
            page.navigate();
            page.switchToTokenRealms();
            page.getResourceManager()
                    .addResource(AddTokenSecurityRealmWizard.class)
                    .name(realmName)
                    .principalClaim(principalClaimValue)
                    .saveWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmName));
            new ResourceVerifier(realmAddress, client)
                    .verifyExists()
                    .verifyAttribute(PRINCIPAL_CLAIM, principalClaimValue);
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Token security realm instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Token security realm table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveTokenSecurityRealm() throws Exception {
        final String realmName = "token_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final Address realmAddress = elyOps.getElytronAddress(TOKEN_REALM, realmName);
        try {
            createTokenRealmInModel(realmAddress);
            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .removeResource(realmName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed token security realm should not be present in the table anymore",
                    page.getResourceManager().isResourcePresent(realmName));
            new ResourceVerifier(realmAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Token security realm instance in model and try to edit its principal-claim
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editPrincipalClaim() throws Exception {
        final String realmName = "token_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String principalClaimValue = RandomStringUtils.randomAlphanumeric(7);
        final Address tokenRealmAddress = elyOps.getElytronAddress(TOKEN_REALM, realmName);
        try {
            createTokenRealmInModel(tokenRealmAddress);
            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(realmName);
            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PRINCIPAL_CLAIM, principalClaimValue)
                    .verifyFormSaved()
                    .verifyAttribute(PRINCIPAL_CLAIM, principalClaimValue);
        } finally {
            ops.removeIfExists(tokenRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Token security realm instance in model and try to edit its jwt attribute value in
     * Web Console's Elytron subsystem configuration while setting certificate to it.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editJWTAttributesWithCertificate() throws Exception {
        final String realmName = "token_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String certificateValue = RandomStringUtils.randomAlphanumeric(7);
        final String[] issuerValues = {
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7)
        };
        final String[] audienceValues = {
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7)
        };
        final Address tokenRealmAddress = elyOps.getElytronAddress(TOKEN_REALM, realmName);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        try {
            createTokenRealmInModel(tokenRealmAddress);
            createKeyStoreInModel(keyStoreAddress);
            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(realmName);
            final ConfigFragment configFragment = page.getConfig().switchTo(JWT.toUpperCase());
            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, AUDIENCE, String.join("\n", audienceValues))
                    .edit(ConfigChecker.InputType.TEXT, CERTIFICATE, certificateValue)
                    .edit(ConfigChecker.InputType.TEXT, ISSUER, String.join("\n", issuerValues))
                    .edit(ConfigChecker.InputType.TEXT, KEY_STORE, keyStoreName)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JWT, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(AUDIENCE, new ModelNodeGenerator.ModelNodeListBuilder().addAll(audienceValues).build())
                            .addProperty(CERTIFICATE, certificateValue)
                            .addProperty(ISSUER, new ModelNodeGenerator.ModelNodeListBuilder().addAll(issuerValues).build())
                            .addProperty(KEY_STORE, keyStoreName)
                            .build());
        } finally {
            ops.removeIfExists(tokenRealmAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Token security realm instance in model and try to edit its jwt attribute value in
     * Web Console's Elytron subsystem configuration while setting public key to it.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editJWTAttributesWithPublicKey() throws Exception {
        final String realmName = "token_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String publicKeyValue = RandomStringUtils.randomAlphanumeric(7);
        final String[] issuerValues = {
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7)
        };
        final String[] audienceValues = {
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7),
                RandomStringUtils.randomAlphanumeric(7)
        };
        final Address tokenRealmAddress = elyOps.getElytronAddress(TOKEN_REALM, realmName);
        try {
            createTokenRealmInModel(tokenRealmAddress);
            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(realmName);
            final ConfigFragment configFragment = page.getConfig().switchTo(JWT.toUpperCase());
            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, AUDIENCE, String.join("\n", audienceValues))
                    .edit(ConfigChecker.InputType.TEXT, ISSUER, String.join("\n", issuerValues))
                    .edit(ConfigChecker.InputType.TEXT, PUBLIC_KEY, publicKeyValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JWT, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(AUDIENCE, new ModelNodeGenerator.ModelNodeListBuilder().addAll(audienceValues).build())
                            .addProperty(ISSUER, new ModelNodeGenerator.ModelNodeListBuilder().addAll(issuerValues).build())
                            .addProperty(PUBLIC_KEY, publicKeyValue)
                            .build());
        } finally {
            ops.removeIfExists(tokenRealmAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Token security realm instance in model and try to edit its oauth2-introspection
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editOAuth2IntrospectionAttributes() throws Exception {

        final String tokenRealmName = "token_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyStoreName = "key_store_" + RandomStringUtils.randomAlphanumeric(7);
        final String keyManagerName = "key_manager_" + RandomStringUtils.randomAlphanumeric(7);
        final String clientSSLContextName = "client_ssl_context_" + RandomStringUtils.randomAlphanumeric(7);

        final Address tokenRealmAddress = elyOps.getElytronAddress(TOKEN_REALM, tokenRealmName);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, keyManagerName);
        final Address clientSSLContextAddress = elyOps.getElytronAddress(CLIENT_SSL_CONTEXT, clientSSLContextName);

        final String clientIDValue = RandomStringUtils.randomAlphanumeric(7);
        final String clientSecretValue = RandomStringUtils.randomAlphanumeric(7);
        final String hostnameVerificationPolicyValue = "ANY";
        final String introspectionURLValue = "http://foo.bar.co";

        try {
            createTokenRealmInModel(tokenRealmAddress);
            createKeyStoreInModel(keyStoreAddress);
            createKeyManagerInModel(keyManagerAddress, keyStoreName);
            createClientSSLContextInModel(clientSSLContextAddress, keyManagerName);
            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(tokenRealmAddress.getLastPairValue());
            final ConfigFragment configFragment = page.getConfig().switchTo(OAUTH2_INTROSPECTION_TAB_LABEL);
            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, CLIENT_ID, clientIDValue)
                    .edit(ConfigChecker.InputType.TEXT, CLIENT_SECRET, clientSecretValue)
                    .edit(ConfigChecker.InputType.TEXT, CLIENT_SSL_CONTEXT, clientSSLContextName)
                    .edit(ConfigChecker.InputType.SELECT, HOST_NAME_VERIFICATION_POLICY, hostnameVerificationPolicyValue)
                    .edit(ConfigChecker.InputType.TEXT, INTROSPECTION_URL, introspectionURLValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(OAUTH2_INTROSPECTION, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(CLIENT_ID, clientIDValue)
                            .addProperty(CLIENT_SECRET, clientSecretValue)
                            .addProperty(CLIENT_SSL_CONTEXT, clientSSLContextName)
                            .addProperty(HOST_NAME_VERIFICATION_POLICY, hostnameVerificationPolicyValue)
                            .addProperty(INTROSPECTION_URL, introspectionURLValue)
                            .build());
        } finally {
            ops.removeIfExists(tokenRealmAddress);
            ops.removeIfExists(clientSSLContextAddress);
            ops.removeIfExists(keyManagerAddress);
            ops.removeIfExists(keyStoreAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createTokenRealmInModel(Address tokenRealmAddress) throws IOException, TimeoutException, InterruptedException {
        ops.add(tokenRealmAddress).assertSuccess();
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

    public void createKeyManagerInModel(Address keyManagerAddress, String keyStoreName) throws IOException, TimeoutException, InterruptedException {
        final String algorithmValue = "PKIX";
        ops.add(keyManagerAddress, Values.of("algorithm", algorithmValue)
                .and(KEY_STORE, keyStoreName)
                .and(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(CLEAR_TEXT, RandomStringUtils.randomAlphanumeric(7))
                        .build()))
                .assertSuccess();
        adminOps.reloadIfRequired();
    }

    private void createClientSSLContextInModel(Address clientSSLContextAddress, String keyManagerName) throws IOException, TimeoutException, InterruptedException {
        final ModelNode protocolList = new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode("TLSv1.1"))
                .addNode(new ModelNode("TLSv1.2"))
                .build();

        ops.add(clientSSLContextAddress, Values.of(KEY_MANAGER, keyManagerName).and("protocols", protocolList))
                .assertSuccess();
        adminOps.reloadIfRequired();
    }
}
