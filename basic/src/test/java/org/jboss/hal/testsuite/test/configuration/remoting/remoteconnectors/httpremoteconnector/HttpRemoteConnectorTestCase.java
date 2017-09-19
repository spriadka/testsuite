package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors.httpremoteconnector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class HttpRemoteConnectorTestCase extends HttpRemotingConnectorTestCaseAbstract {

    private static final String HAL1331_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1331";

    private static final Address HTTP_CONNECTOR_TBA_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("http-connector", "http-connector_tba" + RandomStringUtils.randomAlphanumeric(7)),
            HTTP_CONNECTOR_TBR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("http-connector", "http-connector_tbr" + RandomStringUtils.randomAlphanumeric(7));


    @BeforeClass
    public static void beforeClass() throws IOException, TimeoutException, InterruptedException {
        createHttpRemotingConnector(HTTP_CONNECTOR_ADDRESS);
        createHttpRemotingConnector(HTTP_CONNECTOR_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        navigateToRemotingHttpConnectorsTab();
        remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(HTTP_CONNECTOR_ADDRESS);
            operations.removeIfExists(HTTP_CONNECTOR_TBR_ADDRESS);
            operations.removeIfExists(HTTP_CONNECTOR_TBA_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void addRemoteHttpConnector() throws Exception {
        remotingSubsystemPage.addHttpConnector()
                .name(HTTP_CONNECTOR_TBA_ADDRESS.getLastPairValue())
                .connectorRef(RandomStringUtils.randomAlphanumeric(7))
                .saveAndDismissReloadRequiredWindow();

        Assert.assertTrue("Connector should be added in table! " + HAL1331_FAIL_MESSAGE,
                remotingSubsystemPage.getResourceManager().isResourcePresent(HTTP_CONNECTOR_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(HTTP_CONNECTOR_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeHttpConnector() throws Exception {
        remotingSubsystemPage.getResourceManager().removeResource(HTTP_CONNECTOR_TBR_ADDRESS.getLastPairValue()).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(remotingSubsystemPage.getResourceManager().isResourcePresent(HTTP_CONNECTOR_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(HTTP_CONNECTOR_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
