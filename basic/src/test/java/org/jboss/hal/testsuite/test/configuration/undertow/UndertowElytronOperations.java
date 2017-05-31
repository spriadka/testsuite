package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

public class UndertowElytronOperations {

    private final Operations operations;
    private final ElytronOperations elyOps;

    private static final String
            KEY_STORE = "key-store",
            CREDENTIAL_REFERENCE = "credential-reference",
            CLEAR_TEXT = "clear-text",
            SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory",
            HTTP_AUTHENTICATION_FACTORY = "http-authentication-factory",
            HTTP_SERVER_MECHANISM_FACTORY = "http-server-mechanism-factory",
            SECURITY_DOMAIN = "security-domain",
            MODULE = "module",
            TLS_V11 = "TLSv1.1",
            TLS_V12 = "TLSv1.2",
            PROTOCOLS = "protocols",
            ALGORITHM = "algorithm",
            KEY_MANAGER = "key-manager",
            CLIENT_SSL_CONTEXT = "client-ssl-context",
            MODULE_NAME_1 = "org.jboss.as.cli";


    public UndertowElytronOperations(OnlineManagementClient client) {
        this.operations = new Operations(client);
        this.elyOps = new ElytronOperations(client);
    }

    public Address createSecurityRealm() throws IOException {
        String configDir = "jboss." + (ConfigUtils.isDomain() ? "domain" : "server") + ".config.dir";
        Address realmAddress = elyOps.getElytronAddress("properties-realm", RandomStringUtils.randomAlphanumeric(7));
        ModelNode userPropertiesNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty("path", "mgmt-users.properties")
                .addProperty("relative-to", configDir).build();
        operations.add(realmAddress, Values.of("users-properties", userPropertiesNode)).assertSuccess();
        return realmAddress;
    }

    public Address createSecurityDomain(String realmName) throws IOException {
        Address domainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7));
        ModelNode realmsNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty("realm", realmName).build()).build();
        operations.add(domainAddress, Values.of("default-realm", realmName).and("realms", realmsNode)).assertSuccess();
        return domainAddress;
    }

    public Address createHTTPAuthentication(String serviceLoaderFactoryName, String securityDomainName) throws IOException {
        final String authenticationName = RandomStringUtils.randomAlphanumeric(7);
        final Address authenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, authenticationName);

        operations.add(authenticationAddress,
                Values.of(HTTP_SERVER_MECHANISM_FACTORY, serviceLoaderFactoryName).and(SECURITY_DOMAIN, securityDomainName))
                .assertSuccess();

        return authenticationAddress;
    }

    public Address createServiceLoaderFactory() throws IOException {
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, RandomStringUtils.randomAlphanumeric(7));
        operations.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
        return factoryAddress;
    }

    public Address createKeyStore() throws IOException {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5),
                password = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = elyOps.getElytronAddress(KEY_STORE, keyStoreName);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        operations.add(keyStoreAddress, Values.of("type", "jks").and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                .assertSuccess();
        return keyStoreAddress;
    }

    public Address createKeyManager(Address keyStoreAddress) throws IOException {
        final String algorithmValue = "PKIX";
        final Address keyManagerAddress = elyOps.getElytronAddress(KEY_MANAGER, RandomStringUtils.randomAlphanumeric(7));
        operations.add(keyManagerAddress, Values.of(ALGORITHM, algorithmValue)
                .and(KEY_STORE, keyStoreAddress.getLastPairValue())
                .and(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(CLEAR_TEXT, RandomStringUtils.randomAlphanumeric(7))
                        .build()))
                .assertSuccess();
        return keyManagerAddress;
    }

    public Address createClientSSLContext(Address keyManagerAddress) throws IOException {
        return createSSLContext(keyManagerAddress, SSLContext.CLIENT);
    }

    public Address createServerSSLContext(Address keyManagerAddress) throws IOException {
        return createSSLContext(keyManagerAddress, SSLContext.SERVER);
    }

    private Address createSSLContext(Address keyManagerAddress, SSLContext type) throws IOException {
        final Address clientSSLContextAddress = elyOps.getElytronAddress(type.getAttributeName(), RandomStringUtils.randomAlphanumeric(7));
        final ModelNode protocolList = new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(TLS_V11))
                .addNode(new ModelNode(TLS_V12))
                .build();

        operations.add(clientSSLContextAddress, Values.of(KEY_MANAGER, keyManagerAddress.getLastPairValue()).and(PROTOCOLS, protocolList))
                .assertSuccess();
        return clientSSLContextAddress;
    }

    private enum SSLContext {
        CLIENT(CLIENT_SSL_CONTEXT),
        SERVER("server-ssl-context");

        final String attrinuteName;

        SSLContext(String attributeName) {
            this.attrinuteName = attributeName;
        }

        public String getAttributeName() {
            return attrinuteName;
        }
    }


}
