package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class RemotingConnectorAttributesTestCase extends RemotingConnectorTestCaseAbstract {

    private static final String AUTHENTICATION_PROVIDER = "authentication-provider";
    private static final String SASL_AUTHENTICATION_FACTORY = "sasl-authentication-factory";
    private static final String SASL_PROTOCOL = "sasl-protocol";
    private static final String SECURITY_REALM = "security-realm";
    private static final String SERVER_NAME = "server-name";
    private static final String SSL_CONTEXT = "ssl-context";

    @BeforeClass
    public static void setUp() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        createSocketBinding(CONNECTOR_SOCKET_BINDING);
        operations.add(CONNECTOR_ADDRESS, Values.of(SOCKET_BINDING, CONNECTOR_SOCKET_BINDING));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException, IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(CONNECTOR_ADDRESS);
            client.apply(new RemoveSocketBinding(CONNECTOR_SOCKET_BINDING));
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void editAuthenticationProvider() throws Exception {
        final String authenticationProvider = "auth_provider_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousAuthenticationProvider = operations.readAttribute(CONNECTOR_ADDRESS, AUTHENTICATION_PROVIDER
                , ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        previousAuthenticationProvider.assertSuccess();
        try {
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo("Attributes");
            new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_PROVIDER, authenticationProvider)
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_PROVIDER, authenticationProvider);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, AUTHENTICATION_PROVIDER, previousAuthenticationProvider.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSaslAuthenticationFactory() throws Exception {
        final String saslAuthenticationFactory = "sasl_auth_factory_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousSaslAuthenticationFactory = operations.readAttribute(CONNECTOR_ADDRESS, SASL_AUTHENTICATION_FACTORY
                , ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        previousSaslAuthenticationFactory.assertSuccess();
        try {
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo("Attributes");
            new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_AUTHENTICATION_FACTORY, saslAuthenticationFactory)
                    .verifyFormSaved()
                    .verifyAttribute(SASL_AUTHENTICATION_FACTORY, saslAuthenticationFactory);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, SASL_AUTHENTICATION_FACTORY, previousSaslAuthenticationFactory.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSaslProtocol() throws Exception {
        final String saslProtocol = "sasl_protocol_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousSaslProtocol = operations.readAttribute(CONNECTOR_ADDRESS, SASL_PROTOCOL
                , ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        previousSaslProtocol.assertSuccess();
        try {
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo("Attributes");
            new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_PROTOCOL, saslProtocol)
                    .verifyFormSaved()
                    .verifyAttribute(SASL_PROTOCOL, saslProtocol);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, SASL_PROTOCOL, previousSaslProtocol.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSecurityRealm() throws Exception {
        final String securityRealm = "security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousSecurityRealm = operations.readAttribute(CONNECTOR_ADDRESS, SECURITY_REALM
                , ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        previousSecurityRealm.assertSuccess();
        try {
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo("Attributes");
            new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_REALM, securityRealm)
                    .verifyFormSaved()
                    .verifyAttribute(SECURITY_REALM, securityRealm);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, SECURITY_REALM, previousSecurityRealm.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editServerName() throws Exception {
        final String serverName = "server_name_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousServerName = operations.readAttribute(CONNECTOR_ADDRESS, SERVER_NAME
                , ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        previousServerName.assertSuccess();
        try {
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo("Attributes");
            new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SERVER_NAME, serverName)
                    .verifyFormSaved()
                    .verifyAttribute(SERVER_NAME, serverName);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, SERVER_NAME, previousServerName.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSocketBinding() throws Exception {
        final String socketBinding = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(CONNECTOR_ADDRESS, SOCKET_BINDING);
        originalModelNodeResult.assertSuccess();
        createSocketBinding(socketBinding);
        administration.reloadIfRequired();
        try {
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo("Attributes");
            new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOCKET_BINDING, socketBinding)
                    .verifyFormSaved()
                    .verifyAttribute(SOCKET_BINDING, socketBinding);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, SOCKET_BINDING, originalModelNodeResult.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSslContext() throws Exception {
        final String clientSslContext = "client_ssl_context" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousSslContext = operations.readAttribute(CONNECTOR_ADDRESS, SSL_CONTEXT,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        previousSslContext.assertSuccess();
        final Address clientSslContextAddress = Address.subsystem("elytron").and("client-ssl-context", clientSslContext);
        try {
            operations.add(clientSslContextAddress).assertSuccess();
            administration.reloadIfRequired();
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo("Attributes");
            new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SSL_CONTEXT, clientSslContext)
                    .verifyFormSaved()
                    .verifyAttribute(SSL_CONTEXT, clientSslContext);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, SSL_CONTEXT, previousSslContext.value());
            operations.removeIfExists(clientSslContextAddress);
            administration.reloadIfRequired();
        }
    }

}
