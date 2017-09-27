package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.socketbindings.RemoveSocketBinding;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Testing settings of Remote Connectors
 * TODO: Expand tests
 */
@RunWith(Arquillian.class)
@RunAsClient
public class RemoteConnectorTestCase extends RemotingConnectorTestCaseAbstract {

    private static final String SOCKET_BINDING_2 = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_3 = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_4 = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7);

    private static final Address CONNECTOR_TBA_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS
            .and("connector", "connector_tba" + RandomStringUtils.randomAlphanumeric(7));
    private static final Address CONNECTOR_TBR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS
            .and("connector", "connector_tbr" + RandomStringUtils.randomAlphanumeric(7));

    @BeforeClass
    public static void beforeClass() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        createSocketBinding(CONNECTOR_SOCKET_BINDING);
        createSocketBinding(SOCKET_BINDING_2);
        createSocketBinding(SOCKET_BINDING_3);
        createSocketBinding(SOCKET_BINDING_4);
        operations.add(CONNECTOR_ADDRESS, Values.of(SOCKET_BINDING, CONNECTOR_SOCKET_BINDING)).assertSuccess();
        operations.add(CONNECTOR_TBR_ADDRESS, Values.of(SOCKET_BINDING, SOCKET_BINDING_2)).assertSuccess();
        administration.reloadIfRequired();
    }

    private void navigateToRemotingConnector() {
        navigateToRemotingConnectorTab();
        page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException, CommandFailedException {
        try {
            operations.removeIfExists(CONNECTOR_ADDRESS);
            operations.removeIfExists(CONNECTOR_TBR_ADDRESS);
            operations.removeIfExists(CONNECTOR_TBA_ADDRESS);
            client.apply(new RemoveSocketBinding(CONNECTOR_SOCKET_BINDING));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_2));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_3));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_4));
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addConnector() throws Exception {
        navigateToRemotingConnector();
        page.addNativeConnector()
                .name(CONNECTOR_TBA_ADDRESS.getLastPairValue())
                .socketBinding(SOCKET_BINDING_3)
                .saveAndDismissReloadRequiredWindow();
        Assert.assertTrue("Newly created remoting connector should be present in the remoting connectors table",
                page.getResourceManager().isResourcePresent(CONNECTOR_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(CONNECTOR_TBA_ADDRESS, client)
                .verifyExists()
                .verifyAttribute(SOCKET_BINDING, SOCKET_BINDING_3);
    }

    @Test
    public void removeConnector() throws Exception {
        navigateToRemotingConnector();
        page.getResourceManager().removeResource(CONNECTOR_TBR_ADDRESS.getLastPairValue())
                .confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse("Newly removed remoting connector should not be present in the remoting connectors table"
                , page.getResourceManager().isResourcePresent(CONNECTOR_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(CONNECTOR_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
