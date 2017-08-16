package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm;

import org.apache.commons.lang.RandomStringUtils;
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

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronTokenSecurityRealmTestCase extends AbstractElytronTestCase {

    private static final String
            JWT = "jwt",
            OAUTH2_INTROSPECTION_TABLABEL = "OAuth 2 Introspection",
            OAUTH2_INTROSPECTION = "oauth2-introspection",
            CLEAR_TEXT = "clear-text",
            CREDENTIAL_REFERENCE = "credential-reference",
            KEY_STORE = "key-store",
            PRINCIPAL_CLAIM = "principal-claim",
            TOKEN_REALM = "token-realm";

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
        final Address realmAddress = elyOps.getElytronAddress(TOKEN_REALM, RandomStringUtils.randomAlphabetic(7));
        final String principalClaimValue = RandomStringUtils.randomAlphabetic(7);

        page.navigate();
        page.switchToTokenRealms();

        try {
            page.getResourceManager().addResource(AddTokenSecurityRealmWizard.class)
                    .name(realmAddress.getLastPairValue())
                    .principalClaim(principalClaimValue)
                    .saveWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Resource should be present in table!",
                    page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

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
        final Address realmAddress = createTokenRealm();

        try {
            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .removeResource(realmAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(realmAddress.getLastPairValue()));

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
        final Address tokenRealmAddress = createTokenRealm();

        final String
                identifier = "principal-claim",
                value = RandomStringUtils.randomAlphanumeric(7);

        try {
            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(tokenRealmAddress.getLastPairValue());

            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, identifier, value)
                    .verifyFormSaved()
                    .verifyAttribute(identifier, value);
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
        Address tokenRealmAddress = null,
                keyStoreAddress = null;

        final String
                audience = "audience",
                certificate = "certificate",
                certificateValue = RandomStringUtils.randomAlphanumeric(7),
                issuer = "issuer";

        final String[] issuerValues = new String[] {
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7)
                },
                audienceValues = new String[] {
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7)
                };

        try {
            tokenRealmAddress = createTokenRealm();
            keyStoreAddress = createKeyStore();

            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(tokenRealmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(JWT.toUpperCase());

            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, audience, String.join("\n", audienceValues))
                    .edit(ConfigChecker.InputType.TEXT, certificate, certificateValue)
                    .edit(ConfigChecker.InputType.TEXT, issuer, String.join("\n", issuerValues))
                    .edit(ConfigChecker.InputType.TEXT, KEY_STORE, keyStoreAddress.getLastPairValue())
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JWT, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(audience, new ModelNodeGenerator.ModelNodeListBuilder().addAll(audienceValues).build())
                            .addProperty(certificate, certificateValue)
                            .addProperty(issuer, new ModelNodeGenerator.ModelNodeListBuilder().addAll(issuerValues).build())
                            .addProperty(KEY_STORE, keyStoreAddress.getLastPairValue())
                            .build());
        } finally {
            if (tokenRealmAddress != null) {
                ops.removeIfExists(tokenRealmAddress);
            }
            if (keyStoreAddress != null) {
                ops.removeIfExists(keyStoreAddress);
            }
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
        final Address tokenRealmAddress = createTokenRealm();

        final String
                audience = "audience",
                issuer = "issuer",
                publicKey = "public-key",
                publicKeyValue = RandomStringUtils.randomAlphanumeric(7);

        final String[] issuerValues = new String[] {
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7)
                },
                audienceValues = new String[] {
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7),
                        RandomStringUtils.randomAlphanumeric(7)
                };

        try {

            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(tokenRealmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(JWT.toUpperCase());

            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, audience, String.join("\n", audienceValues))
                    .edit(ConfigChecker.InputType.TEXT, issuer, String.join("\n", issuerValues))
                    .edit(ConfigChecker.InputType.TEXT, publicKey, publicKeyValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(JWT, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(audience, new ModelNodeGenerator.ModelNodeListBuilder().addAll(audienceValues).build())
                            .addProperty(issuer, new ModelNodeGenerator.ModelNodeListBuilder().addAll(issuerValues).build())
                            .addProperty(publicKey, publicKeyValue)
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
        Address tokenRealmAddress = createTokenRealm(),
                keyStoreAddress = null,
                keyManagerAddress = null,
                clientSSLContextAddress = null;

        final String
                clientID = "client-id",
                clientIDValue = RandomStringUtils.randomAlphanumeric(7),
                clientSecret = "client-secret",
                clientSecretValue = RandomStringUtils.randomAlphanumeric(7),
                clientSSLContext = "client-ssl-context",
                hostnameVerificationPolicy = "host-name-verification-policy",
                hostnameVerificationPolicyValue = "ANY",
                introspectionURL = "introspection-url",
                introspectionURLValue = "http://foo.bar.co";

        try {
            keyStoreAddress = createKeyStore();
            keyManagerAddress = createKeyManager(keyStoreAddress);
            clientSSLContextAddress = createClientSSLContext(keyManagerAddress);

            page.navigate();
            page.switchToTokenRealms()
                    .getResourceManager()
                    .selectByName(tokenRealmAddress.getLastPairValue());

            final ConfigFragment configFragment = page.getConfig().switchTo(OAUTH2_INTROSPECTION_TABLABEL);

            new ConfigChecker.Builder(client, tokenRealmAddress)
                    .configFragment(configFragment)
                    .edit(ConfigChecker.InputType.TEXT, clientID, clientIDValue)
                    .edit(ConfigChecker.InputType.TEXT, clientSecret, clientSecretValue)
                    .edit(ConfigChecker.InputType.TEXT, clientSSLContext, clientSSLContextAddress.getLastPairValue())
                    .edit(ConfigChecker.InputType.SELECT, hostnameVerificationPolicy, hostnameVerificationPolicyValue)
                    .edit(ConfigChecker.InputType.TEXT, introspectionURL, introspectionURLValue)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(OAUTH2_INTROSPECTION, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(clientID, clientIDValue)
                            .addProperty(clientSecret, clientSecretValue)
                            .addProperty(clientSSLContext, clientSSLContextAddress.getLastPairValue())
                            .addProperty(hostnameVerificationPolicy, hostnameVerificationPolicyValue)
                            .addProperty(introspectionURL, introspectionURLValue)
                            .build());
        } finally {
            ops.removeIfExists(tokenRealmAddress);
            if (clientSSLContextAddress != null) {
                ops.removeIfExists(clientSSLContextAddress);
            }
            if (keyManagerAddress != null) {
                ops.removeIfExists(keyManagerAddress);
            }
            if (keyStoreAddress != null) {
                ops.removeIfExists(keyStoreAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    private Address createTokenRealm() throws IOException {
        final Address realmAddress = elyOps.getElytronAddress(TOKEN_REALM, RandomStringUtils.randomAlphabetic(7));
        ops.add(realmAddress).assertSuccess();
        return realmAddress;
    }

    private Address createKeyStore() throws IOException {
        final String keyStoreName = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(5),
                password = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        ops.add(keyStoreAddress, Values.of("type", "jks").and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                .assertSuccess();
        return keyStoreAddress;
    }

    public Address createKeyManager(Address keyStoreAddress) throws IOException {
        final String algorithmValue = "PKIX";
        final Address keyManagerAddress = elyOps.getElytronAddress("key-manager", org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(7));
        ops.add(keyManagerAddress, Values.of("algorithm", algorithmValue)
                .and(KEY_STORE, keyStoreAddress.getLastPairValue())
                .and(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(CLEAR_TEXT, org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(7))
                        .build()))
                .assertSuccess();
        return keyManagerAddress;
    }

    private Address createClientSSLContext(Address keyManagerAddress) throws IOException {
        final Address clientSSLContextAddress = elyOps.getElytronAddress("client-ssl-context", org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(7));
        final ModelNode protocolList = new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode("TLSv1.1"))
                .addNode(new ModelNode("TLSv1.2"))
                .build();

        ops.add(clientSSLContextAddress, Values.of("key-manager", keyManagerAddress.getLastPairValue()).and("protocols", protocolList))
                .assertSuccess();
        return clientSSLContextAddress;
    }
}
