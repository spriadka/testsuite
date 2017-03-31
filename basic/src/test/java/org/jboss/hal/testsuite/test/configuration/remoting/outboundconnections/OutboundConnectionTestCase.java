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
public class OutboundConnectionTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private RemotingSubsystemPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);


    private static final String
            OUTBOUND_CONNECTION = "outbound-connection",
            URI = "uri";

    private static final Address
            REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting"),
            OUTBOUND_CONNECTION_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and(OUTBOUND_CONNECTION,
                    "outbound_" + RandomStringUtils.randomAlphanumeric(5)),
            OUTBOUND_CONNECTION_TBA_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and(OUTBOUND_CONNECTION,
                    "outbound_tba_" + RandomStringUtils.randomAlphanumeric(5)),
            OUTBOUND_CONNECTION_TBR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and(OUTBOUND_CONNECTION,
                    "outbound_tbr_" + RandomStringUtils.randomAlphanumeric(5));


    @BeforeClass
    public static void beforeClass() throws CommandFailedException, InterruptedException, TimeoutException, IOException {
        operations.add(OUTBOUND_CONNECTION_ADDRESS, Values.of(URI, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        operations.add(OUTBOUND_CONNECTION_TBR_ADDRESS, Values.of(URI, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.switchToOutboundConnectionsTab();
        page.switchSubTabByExactText("Outbound");
        page.getResourceManager().selectByName(OUTBOUND_CONNECTION_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException, InterruptedException, TimeoutException, IOException, OperationException {
        try {
            operations.removeIfExists(OUTBOUND_CONNECTION_ADDRESS);
            operations.removeIfExists(OUTBOUND_CONNECTION_TBA_ADDRESS);
            operations.removeIfExists(OUTBOUND_CONNECTION_TBR_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addRemoteOutboundConnection() throws Exception {
        page.addOutboundConnection()
                .name(OUTBOUND_CONNECTION_TBA_ADDRESS.getLastPairValue())
                .uri("new-uri_" + RandomStringUtils.randomAlphanumeric(7))
                .saveAndDismissReloadRequiredWindow();

        Assert.assertTrue(page.getResourceManager().isResourcePresent(OUTBOUND_CONNECTION_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(OUTBOUND_CONNECTION_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeRemoteOutboundConnection() throws Exception {
        page.getResourceManager()
                .removeResource(OUTBOUND_CONNECTION_TBR_ADDRESS.getLastPairValue())
                .confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(page.getResourceManager().isResourcePresent(OUTBOUND_CONNECTION_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(OUTBOUND_CONNECTION_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editURI() throws Exception {
        final String uri = "edited-uri_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(OUTBOUND_CONNECTION_ADDRESS, URI);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, OUTBOUND_CONNECTION_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, URI, uri)
                    .verifyFormSaved()
                    .verifyAttribute(URI, uri);
        } finally {
            operations.writeAttribute(OUTBOUND_CONNECTION_ADDRESS, URI, uri);
        }
    }

}
