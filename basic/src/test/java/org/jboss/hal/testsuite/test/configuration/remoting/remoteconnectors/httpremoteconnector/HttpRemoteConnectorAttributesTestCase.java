package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors.httpremoteconnector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class HttpRemoteConnectorAttributesTestCase extends HttpRemotingConnectorTestCaseAbstract {

    private static final String AUTHENTICATION_PROVIDER = "authentication-provider";
    private static final String SASL_AUTHENTICATION_FACTORY = "sasl-authentication-factory";
    private static final String SASL_PROTOCOL = "sasl-protocol";
    private static final String SECURITY_REALM = "security-realm";
    private static final String SERVER_NAME = "server-name";

    private static final String APPLICATION_SASL_AUTHENTICATION = "application-sasl-authentication";

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        createHttpRemotingConnector(HTTP_CONNECTOR_ADDRESS);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(HTTP_CONNECTOR_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void editAuthenticationProvider() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        final String value = "new_authentication_provider_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousAuthenticationProvider = operations.readAttribute(HTTP_CONNECTOR_ADDRESS, AUTHENTICATION_PROVIDER);
        previousAuthenticationProvider.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_CONNECTOR_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_PROVIDER, value)
                    .verifyFormSaved()
                    .verifyAttribute(AUTHENTICATION_PROVIDER, value);
        } finally {
            operations.writeAttribute(HTTP_CONNECTOR_ADDRESS, AUTHENTICATION_PROVIDER, previousAuthenticationProvider.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editConnectionRef() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        final String value = "new_connector_ref_" + RandomStringUtils.randomAlphanumeric(5);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(HTTP_CONNECTOR_ADDRESS, CONNECTOR_REF);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_CONNECTOR_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CONNECTOR_REF, value)
                    .verifyFormSaved()
                    .verifyAttribute(CONNECTOR_REF, value);
        } finally {
            operations.writeAttribute(HTTP_CONNECTOR_ADDRESS, CONNECTOR_REF, originalModelNodeResult.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSASLAuthenticationFactory() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        final ModelNodeResult previousSASLAuthenticationFactory = operations.readAttribute(HTTP_CONNECTOR_ADDRESS,
                SASL_AUTHENTICATION_FACTORY);
        previousSASLAuthenticationFactory.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_CONNECTOR_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_AUTHENTICATION_FACTORY, APPLICATION_SASL_AUTHENTICATION)
                    .verifyFormSaved()
                    .verifyAttribute(SASL_AUTHENTICATION_FACTORY, APPLICATION_SASL_AUTHENTICATION);
        } finally {
            operations.writeAttribute(HTTP_CONNECTOR_ADDRESS, SASL_AUTHENTICATION_FACTORY, previousSASLAuthenticationFactory.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSASLProtocol() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        final String value = "new_sasl_protocol_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousSASLProtocol = operations.readAttribute(HTTP_CONNECTOR_ADDRESS, SASL_PROTOCOL);
        previousSASLProtocol.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_CONNECTOR_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SASL_PROTOCOL, value)
                    .verifyFormSaved()
                    .verifyAttribute(SASL_PROTOCOL, value);
        } finally {
            operations.writeAttribute(HTTP_CONNECTOR_ADDRESS, SASL_PROTOCOL, previousSASLProtocol.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editSecurityRealm() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        final String value = "new_security_realm_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousSecurityRealm = operations.readAttribute(HTTP_CONNECTOR_ADDRESS, SECURITY_REALM);
        previousSecurityRealm.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_CONNECTOR_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_REALM, value)
                    .verifyFormSaved()
                    .verifyAttribute(SECURITY_REALM, value);
        } finally {
            operations.writeAttribute(HTTP_CONNECTOR_ADDRESS, SECURITY_REALM, previousSecurityRealm.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editServerName() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        final String value = "new_server_name_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult previousServerName = operations.readAttribute(HTTP_CONNECTOR_ADDRESS, SERVER_NAME);
        previousServerName.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_CONNECTOR_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SERVER_NAME, value)
                    .verifyFormSaved()
                    .verifyAttribute(SERVER_NAME, value);
        } finally {
            operations.writeAttribute(HTTP_CONNECTOR_ADDRESS, SERVER_NAME, previousServerName.value());
            administration.reloadIfRequired();
        }
    }

}
