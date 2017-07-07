package org.jboss.hal.testsuite.test.configuration.elytron;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Encapsulates Creaper operations in Elytron subsystem
 *
 */
public class ElytronOperations {

    private static final Address ELYTRON_SUBSYSTEM_ADDRESS = Address.subsystem("elytron");

    private static final String
            MODULE = "module",
            TLS_V11 = "TLSv1.1",
            TLS_V12 = "TLSv1.2",
            PROTOCOLS = "protocols",
            ALGORITHM = "algorithm",
            MODULE_NAME_1 = "org.jboss.as.cli";

    public static final String
            PROVIDER_LOADER = "provider-loader",
            KEY_STORE = "key-store",
            CREDENTIAL_REFERENCE = "credential-reference",
            CLEAR_TEXT = "clear-text",
            SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory",
            HTTP_AUTHENTICATION_FACTORY = "http-authentication-factory",
            HTTP_SERVER_MECHANISM_FACTORY = "http-server-mechanism-factory",
            SECURITY_DOMAIN = "security-domain",
            KEY_MANAGER = "key-manager",
            CLIENT_SSL_CONTEXT = "client-ssl-context";

    private final Operations ops;

    public ElytronOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
    }

    public void addProviderLoader(final String providerLoaderName) throws IOException {
        ops.add(ELYTRON_SUBSYSTEM_ADDRESS.and(PROVIDER_LOADER, providerLoaderName)).assertSuccess();
    }

    public void removeProviderLoader(final String providerLoaderName) throws IOException {
        ops.remove(ELYTRON_SUBSYSTEM_ADDRESS.and(PROVIDER_LOADER, providerLoaderName));
    }

    public Address getElytronAddress(final String key, final String value) {
        return ELYTRON_SUBSYSTEM_ADDRESS.and(key, value);
    }

    public static Address getElytronSubsystemAddress() {
        return ELYTRON_SUBSYSTEM_ADDRESS;
    }

    /**
     * Create properties-realm capability resource with random name
     * @return address of newly created security realm
     */
    public Address createSecurityRealm() throws IOException {
        Address realmAddress = getElytronAddress("properties-realm", RandomStringUtils.randomAlphanumeric(7));
        ModelNode userPropertiesNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty("path", "mgmt-users.properties")
                .addProperty("relative-to", ConfigUtils.getConfigDirPathName()).build();
        ops.add(realmAddress, Values.of("users-properties", userPropertiesNode)).assertSuccess();
        return realmAddress;
    }

    /**
     * Create security domain resource with random name
     * @param realmName name of security realm needed to create security domain
     * @return address of new security domain
     */
    public Address createSecurityDomain(String realmName) throws IOException {
        Address domainAddress = getElytronAddress(SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7));
        ModelNode realmsNode = new ModelNodeGenerator.ModelNodeListBuilder()
                .addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty("realm", realmName).build()).build();
        ops.add(domainAddress, Values.of("default-realm", realmName).and("realms", realmsNode)).assertSuccess();
        return domainAddress;
    }

    /**
     * Create new http-authentication-factory with random name
     * @param serviceLoaderFactoryName service loader factory needed for resource reation
     * @param securityDomainName security domain name needed for resource creation
     * @return address of new http-authentication-factory
     */
    public Address createHTTPAuthentication(String serviceLoaderFactoryName, String securityDomainName) throws IOException {
        final String authenticationName = RandomStringUtils.randomAlphanumeric(7);
        final Address authenticationAddress = getElytronAddress(HTTP_AUTHENTICATION_FACTORY, authenticationName);

        ops.add(authenticationAddress,
                Values.of(HTTP_SERVER_MECHANISM_FACTORY, serviceLoaderFactoryName).and(SECURITY_DOMAIN, securityDomainName))
                .assertSuccess();

        return authenticationAddress;
    }

    /**
     * Create service-loader-http-server-mechanism-factory with random name
     * @return address of new service-loader-http-server-mechanism-factory
     */
    public Address createServiceLoaderFactory() throws IOException {
        final Address factoryAddress = getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, RandomStringUtils.randomAlphanumeric(7));
        ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
        return factoryAddress;
    }

    /**
     * Create new key-store with random name
     * @return address of new key-store
     */
    public Address createKeyStore() throws IOException {
        final String keyStoreName = RandomStringUtils.randomAlphanumeric(5);
        final Address keyStoreAddress = getElytronAddress(KEY_STORE, keyStoreName);
        return createKeyStore(keyStoreAddress);
    }

    /**
     * Create new key-store at passed address
     * @param keyStoreAddress address of new key-store
     * @return passed address of new key-store
     */
    public Address createKeyStore(Address keyStoreAddress) throws IOException {
        final String password = RandomStringUtils.randomAlphanumeric(5);
        final ModelNode credentialReferenceNode = new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(CLEAR_TEXT, password)
                .build();
        ops.add(keyStoreAddress, Values.of("type", "jks").and(CREDENTIAL_REFERENCE, credentialReferenceNode))
                .assertSuccess();
        return keyStoreAddress;
    }

    /**
     * Create new key-manager with random name
     * @param keyStoreAddress key-store needed for key-manager creation
     * @return address of new key-manager
     */
    public Address createKeyManager(Address keyStoreAddress) throws IOException {
        final Address keyManagerAddress = getElytronAddress(KEY_MANAGER, RandomStringUtils.randomAlphanumeric(7));
        return createKeyManager(keyManagerAddress, keyStoreAddress);
    }

    /**
     * Create new key-manager on passed address
     * @param keyManagerAddress address of new key-manager
     * @param keyStoreAddress key-store needed for key-manager creation
     * @return passed address of new key-manager
     */
    public Address createKeyManager(Address keyManagerAddress, Address keyStoreAddress) throws IOException {
        final String algorithmValue = "PKIX";
        ops.add(keyManagerAddress, Values.of(ALGORITHM, algorithmValue)
                .and(KEY_STORE, keyStoreAddress.getLastPairValue())
                .and(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                        .addProperty(CLEAR_TEXT, RandomStringUtils.randomAlphanumeric(7))
                        .build()))
                .assertSuccess();
        return keyManagerAddress;
    }

    /**
     * Create client-ssl-context with random name
     * @param keyManagerAddress key-manager needed for SSL context creation
     * @return address of new client-ssl-context
     */
    public Address createClientSSLContext(Address keyManagerAddress) throws IOException {
        return createSSLContext(keyManagerAddress, SSLContext.CLIENT);
    }

    /**
     * Create server-ssl-context with random name
     * @param keyManagerAddress key-manager needed for SSL context creation
     * @return address of new server-ssl-context
     */
    public Address createServerSSLContext(Address keyManagerAddress) throws IOException {
        return createSSLContext(keyManagerAddress, SSLContext.SERVER);
    }

    private Address createSSLContext(Address keyManagerAddress, SSLContext type) throws IOException {
        final Address clientSSLContextAddress = getElytronAddress(type.getAttributeName(), RandomStringUtils.randomAlphanumeric(7));
        return createSSLContext(clientSSLContextAddress, keyManagerAddress);
    }

    /**
     * Create new SSL context on target address
     * @param sslContextAddress address of new SSL context
     * @param keyManagerAddress address of key-manager needed for SSL context creation
     * @return passed address of SSL context
     */
    public Address createSSLContext(Address sslContextAddress, Address keyManagerAddress) throws IOException {
        final ModelNode protocolList = new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(TLS_V11))
                .addNode(new ModelNode(TLS_V12))
                .build();

        ops.add(sslContextAddress, Values.of(KEY_MANAGER, keyManagerAddress.getLastPairValue()).and(PROTOCOLS, protocolList))
                .assertSuccess();
        return sslContextAddress;
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
