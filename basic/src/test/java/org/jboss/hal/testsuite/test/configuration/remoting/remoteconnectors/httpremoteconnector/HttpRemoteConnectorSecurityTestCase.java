package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors.httpremoteconnector;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class HttpRemoteConnectorSecurityTestCase extends HttpRemotingConnectorTestCaseAbstract {

    private static final String SECURITY = "security";
    private static final String SASL_SECURITY = "sasl";
    private static final Address HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS = HTTP_CONNECTOR_ADDRESS
            .and(SECURITY, SASL_SECURITY);
    private static final String SECURITY_TAB_LABEL = "Security";

    private static final String INCLUDE_MECHANISMS = "include-mechanisms";
    private static final String QOP = "qop";
    private static final String REUSE_SESSION = "reuse-session";
    private static final String SERVER_AUTH = "server-auth";
    private static final String STRENGTH = "strength";

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        createHttpRemotingConnector(HTTP_CONNECTOR_ADDRESS);
        operations.add(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, OperationException {
        try {
            operations.removeIfExists(HTTP_CONNECTOR_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void editIncludeMechanisms() throws Exception {
        final String firstMechanism = "first";
        final String secondMechanism = "second";
        final String thirdMechanism = "third";
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        remotingSubsystemPage.getConfig().switchTo(SECURITY_TAB_LABEL);
        ModelNodeResult previousIncludeMechanisms = operations.readAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS
                , INCLUDE_MECHANISMS);
        previousIncludeMechanisms.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, INCLUDE_MECHANISMS,
                            String.join("\n", Arrays.asList(firstMechanism, secondMechanism, thirdMechanism)))
                    .verifyFormSaved()
                    .verifyAttribute(INCLUDE_MECHANISMS, new ModelNodeGenerator.ModelNodeListBuilder()
                            .addAll(firstMechanism, secondMechanism, thirdMechanism).build());
        } finally {
            operations.writeAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS, INCLUDE_MECHANISMS, previousIncludeMechanisms.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editQOP() throws Exception {
        final String qop = "auth";
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        remotingSubsystemPage.getConfig().switchTo(SECURITY_TAB_LABEL);
        ModelNodeResult previousQOP = operations.readAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS, QOP);
        previousQOP.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, QOP, qop)
                    .verifyFormSaved()
                    .verifyAttribute(QOP, new ModelNodeGenerator.ModelNodeListBuilder().addAll(qop).build());
        } finally {
            operations.writeAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS, QOP, previousQOP.value());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void toggleReuseSession() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        remotingSubsystemPage.getConfig().switchTo(SECURITY_TAB_LABEL);
        ModelNodeResult previousReuseSession = operations.readAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS,
                REUSE_SESSION);
        previousReuseSession.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, REUSE_SESSION, !previousReuseSession.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(REUSE_SESSION, !previousReuseSession.booleanValue());
        } finally {
            operations.writeAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS, REUSE_SESSION, previousReuseSession.booleanValue());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void toggleServerAuth() throws Exception {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        remotingSubsystemPage.getConfig().switchTo(SECURITY_TAB_LABEL);
        ModelNodeResult previousServerAuth = operations.readAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS,
                SERVER_AUTH);
        previousServerAuth.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, SERVER_AUTH, !previousServerAuth.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(SERVER_AUTH, !previousServerAuth.booleanValue());
        } finally {
            operations.writeAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS, SERVER_AUTH, previousServerAuth.booleanValue());
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editStrength() throws Exception {
        final String strength = "high";
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        remotingSubsystemPage.getConfig().switchTo(SECURITY_TAB_LABEL);
        ModelNodeResult previousStrength = operations.readAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS, STRENGTH);
        previousStrength.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, STRENGTH, strength)
                    .verifyFormSaved()
                    .verifyAttribute(STRENGTH, new ModelNodeGenerator.ModelNodeListBuilder().addAll(strength).build());
        } finally {
            operations.writeAttribute(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS, STRENGTH, previousStrength.value());
            administration.reloadIfRequired();
        }
    }

}
