package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors;

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
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Testing settings of HTTP Remote Connectors
 * TODO: Expand tests
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class HttpRemoteConnectorTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private RemotingSubsystemPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String CONNECTOR_REF = "connector-ref";

    private static final Address
            REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting"),
            HTTP_CONNECTOR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("http-connector", "http-connector" + RandomStringUtils.randomAlphanumeric(7)),
            HTTP_CONNECTOR_TBA_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("http-connector", "http-connector_tba" + RandomStringUtils.randomAlphanumeric(7)),
            HTTP_CONNECTOR_TBR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("http-connector", "http-connector_tbr" + RandomStringUtils.randomAlphanumeric(7));


    @BeforeClass
    public static void beforeClass() throws IOException, TimeoutException, InterruptedException {
        operations.add(HTTP_CONNECTOR_ADDRESS, Values.of(CONNECTOR_REF, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        operations.add(HTTP_CONNECTOR_TBR_ADDRESS, Values.of(CONNECTOR_REF, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.switchToRemoteConnectorsTab();
        page.switchSubTab("HTTP Connectors");
        page.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(HTTP_CONNECTOR_ADDRESS);
            operations.removeIfExists(HTTP_CONNECTOR_TBR_ADDRESS);
            operations.removeIfExists(HTTP_CONNECTOR_TBA_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addRemoteHttpConnector() throws Exception {
        page.addHttpConnector()
                .name(HTTP_CONNECTOR_TBA_ADDRESS.getLastPairValue())
                .connectorRef(RandomStringUtils.randomAlphanumeric(7))
                .saveAndDismissReloadRequiredWindow();

        Assert.assertTrue(page.getResourceManager().isResourcePresent(HTTP_CONNECTOR_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(HTTP_CONNECTOR_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeHttpConnector() throws Exception {
        page.getResourceManager().removeResource(HTTP_CONNECTOR_TBR_ADDRESS.getLastPairValue()).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(page.getResourceManager().isResourcePresent(HTTP_CONNECTOR_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(HTTP_CONNECTOR_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editConnectionRef() throws Exception {
        final String value = "new_connector_ref_" + RandomStringUtils.randomAlphanumeric(5);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(HTTP_CONNECTOR_ADDRESS, CONNECTOR_REF);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, HTTP_CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CONNECTOR_REF, value)
                    .verifyFormSaved()
                    .verifyAttribute(CONNECTOR_REF, value);
        } finally {
            operations.writeAttribute(HTTP_CONNECTOR_ADDRESS, CONNECTOR_REF, originalModelNodeResult.value());
        }
    }

}
