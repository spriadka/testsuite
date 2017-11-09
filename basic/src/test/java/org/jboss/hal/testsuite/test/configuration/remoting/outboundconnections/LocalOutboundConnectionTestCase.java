package org.jboss.hal.testsuite.test.configuration.remoting.outboundconnections;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.RemotingSubsystemPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class LocalOutboundConnectionTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private RemotingSubsystemPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final OutboundSocketBindingProvider socketBindingProvider = new OutboundSocketBindingProvider
            .Builder(OutboundSocketBindingProvider.Type.LOCAL)
            .client(client)
            .build();


    private static final String
            SOCKET_BINDING_1 = "local-outbound-socket-binding1_" + RandomStringUtils.randomAlphanumeric(5),
            SOCKET_BINDING_2 = "local-outbound-socket-binding2_" + RandomStringUtils.randomAlphanumeric(5),
            SOCKET_BINDING_3 = "local-outbound-socket-binding3_" + RandomStringUtils.randomAlphanumeric(5),
            SOCKET_BINDING_4 = "local-outbound-socket-binding4_" + RandomStringUtils.randomAlphanumeric(5),

            LOCAL_OUTBOUND_CONNECTION = "local-outbound-connection",
            OUTBOUND_SOCKET_BINDING_REF = "outbound-socket-binding-ref";

    private static final Address
            REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting"),
            LOCAL_OUTBOUND_CONNECTION_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and(LOCAL_OUTBOUND_CONNECTION,
                    "local_outbound_" + RandomStringUtils.randomAlphanumeric(5)),
            LOCAL_OUTBOUND_CONNECTION_TBA_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and(LOCAL_OUTBOUND_CONNECTION,
                    "local_outbound_tba_" + RandomStringUtils.randomAlphanumeric(5)),
            LOCAL_OUTBOUND_CONNECTION_TBR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and(LOCAL_OUTBOUND_CONNECTION,
                    "local_outbound_tbr_" + RandomStringUtils.randomAlphanumeric(5));


    @BeforeClass
    public static void beforeClass() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        socketBindingProvider.createOutboundSocketBinding(SOCKET_BINDING_1);
        socketBindingProvider.createOutboundSocketBinding(SOCKET_BINDING_2);
        socketBindingProvider.createOutboundSocketBinding(SOCKET_BINDING_3);
        socketBindingProvider.createOutboundSocketBinding(SOCKET_BINDING_4);

        operations.add(LOCAL_OUTBOUND_CONNECTION_ADDRESS, Values.of(OUTBOUND_SOCKET_BINDING_REF, SOCKET_BINDING_1)).assertSuccess();
        operations.add(LOCAL_OUTBOUND_CONNECTION_TBR_ADDRESS, Values.of(OUTBOUND_SOCKET_BINDING_REF, SOCKET_BINDING_2)).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.switchToOutboundConnectionsTab();
        page.getResourceManager().selectByName(LOCAL_OUTBOUND_CONNECTION_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException, InterruptedException, TimeoutException, IOException, OperationException {
        try {
            operations.removeIfExists(LOCAL_OUTBOUND_CONNECTION_ADDRESS);
            operations.removeIfExists(LOCAL_OUTBOUND_CONNECTION_TBA_ADDRESS);
            operations.removeIfExists(LOCAL_OUTBOUND_CONNECTION_TBR_ADDRESS);
            administration.reloadIfRequired();
            socketBindingProvider.clean();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addLocalOutboundConnection() throws Exception {
        page.addLocalOutboundConnection()
                .name(LOCAL_OUTBOUND_CONNECTION_TBA_ADDRESS.getLastPairValue())
                .outboundSocketBindingRef(SOCKET_BINDING_3)
                .saveAndDismissReloadRequiredWindow();

        Assert.assertTrue(page.getResourceManager().isResourcePresent(LOCAL_OUTBOUND_CONNECTION_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(LOCAL_OUTBOUND_CONNECTION_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeLocalOutboundConnection() throws Exception {
        page.getResourceManager()
                .removeResource(LOCAL_OUTBOUND_CONNECTION_TBR_ADDRESS.getLastPairValue())
                .confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(page.getResourceManager().isResourcePresent(LOCAL_OUTBOUND_CONNECTION_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(LOCAL_OUTBOUND_CONNECTION_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editOutboundSocketBindingRef() throws Exception {
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(LOCAL_OUTBOUND_CONNECTION_ADDRESS, OUTBOUND_SOCKET_BINDING_REF);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, LOCAL_OUTBOUND_CONNECTION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, OUTBOUND_SOCKET_BINDING_REF, SOCKET_BINDING_4)
                    .verifyFormSaved()
                    .verifyAttribute(OUTBOUND_SOCKET_BINDING_REF, SOCKET_BINDING_4);
        } finally {
            operations.writeAttribute(LOCAL_OUTBOUND_CONNECTION_ADDRESS, OUTBOUND_SOCKET_BINDING_REF, SOCKET_BINDING_4);
        }
    }

}
