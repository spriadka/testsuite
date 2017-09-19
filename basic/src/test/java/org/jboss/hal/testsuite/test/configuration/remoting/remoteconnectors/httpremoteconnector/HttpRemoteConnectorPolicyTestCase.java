package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors.httpremoteconnector;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class HttpRemoteConnectorPolicyTestCase extends HttpRemotingConnectorTestCaseAbstract {

    private static final String POLICY_LABEL = "Policy";
    private static final String FORWARD_SECRECY = "forward-secrecy";
    private static final String NO_ACTIVE = "no-active";
    private static final String NO_ANONYMOUS = "no-anonymous";
    private static final String NO_DICTIONARY = "no-dictionary";
    private static final String NO_PLAIN_TEXT = "no-plain-text";
    private static final String PASS_CREDENTIALS = "pass-credentials";

    private static final String SECURITY = "security";
    private static final String SASL_SECURITY = "sasl";
    private static final String SASL_POLICY = "sasl-policy";
    private static final String POLICY = "policy";
    private static final Address HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS = HTTP_CONNECTOR_ADDRESS
            .and(SECURITY, SASL_SECURITY);

    private static final Address HTTP_REMOTE_CONNECTOR_POLICY_ADDRESS = HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS
            .and(SASL_POLICY, POLICY);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        createHttpRemotingConnector(HTTP_CONNECTOR_ADDRESS);
        operations.add(HTTP_REMOTE_CONNECTOR_SECURITY_ADDRESS);
        operations.add(HTTP_REMOTE_CONNECTOR_POLICY_ADDRESS);
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

    @Before
    public void before() {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
        remotingSubsystemPage.getConfig().switchTo(POLICY_LABEL);
    }

    @Test
    public void toggleForwardSecrecy() throws Exception {
        toggleCheckboxAndVerify(FORWARD_SECRECY);
    }

    private void toggleCheckboxAndVerify(String identifier) throws Exception {
        ModelNodeResult originalValue = operations.readAttribute(HTTP_REMOTE_CONNECTOR_POLICY_ADDRESS, identifier);
        try {
            new ConfigChecker.Builder(client, HTTP_REMOTE_CONNECTOR_POLICY_ADDRESS)
                    .configFragment(remotingSubsystemPage.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, identifier, !originalValue.booleanValue())
                    .verifyFormSaved()
                    .verifyAttribute(identifier, !originalValue.booleanValue());
        } finally {
            operations.writeAttribute(HTTP_REMOTE_CONNECTOR_POLICY_ADDRESS, identifier, originalValue.booleanValue());
        }
    }

    @Test
    public void toggleNoActive() throws Exception {
        toggleCheckboxAndVerify(NO_ACTIVE);
    }

    @Test
    public void toggleNoAnonymous() throws Exception {
        toggleCheckboxAndVerify(NO_ANONYMOUS);
    }

    @Test
    public void toggleNoDictionary() throws Exception {
        toggleCheckboxAndVerify(NO_DICTIONARY);
    }

    @Test
    public void toggleNoPlainText() throws Exception {
        toggleCheckboxAndVerify(NO_PLAIN_TEXT);
    }

    @Test
    public void togglePassCredentials() throws Exception {
        toggleCheckboxAndVerify(PASS_CREDENTIALS);
    }

}
