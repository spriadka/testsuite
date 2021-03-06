package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class ClusterConnectionsTestCase extends AbstractMessagingTestCase {

    private static final String CC_NAME = "test-cluster_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String CC_TBA_NAME = "test-cluster-TBA_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String CC_TBR_NAME = "test-cluster-TBR_" + RandomStringUtils.randomAlphanumeric(6);

    private static final Address CC_ADDRESS = DEFAULT_MESSAGING_SERVER.and("cluster-connection", CC_NAME);
    private static final Address CC_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("cluster-connection", CC_TBA_NAME);
    private static final Address CC_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("cluster-connection", CC_TBR_NAME);

    private static final String
            CLUSTER_CONNECTION_ADDRESS = "cluster-connection-address",
            DISCOVERY_GROUP = "discovery-group",
            CONNECTOR_NAME = "connector-name",
            JMS = "jms";

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(CC_ADDRESS, Values.of(CLUSTER_CONNECTION_ADDRESS, JMS)
                .and(DISCOVERY_GROUP, RandomStringUtils.randomAlphabetic(7))
                .and(CONNECTOR_NAME, "http-connector")).assertSuccess();
        operations.add(CC_TBR_ADDRESS, Values.of(CLUSTER_CONNECTION_ADDRESS, JMS)
                .and(DISCOVERY_GROUP, RandomStringUtils.randomAlphabetic(7))
                .and(CONNECTOR_NAME, "http-connector")).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewClusteringSettings("default");
        page.switchToConnections();
        page.selectInTable(CC_NAME, 0);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(CC_ADDRESS);
        operations.removeIfExists(CC_TBR_ADDRESS);
        operations.removeIfExists(CC_TBA_ADDRESS);
    }

    @Test
    public void addClusterConnection() throws Exception {
        page.addClusterConnection()
                .clusterConnectionAddress(JMS)
                .name(CC_TBA_NAME)
                .connectorName("http-connector")
                .discoveryGroup(RandomStringUtils.randomAlphabetic(7))
                .saveAndDismissReloadRequiredWindow();
        administration.reloadIfRequired();
        new ResourceVerifier(CC_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateClusterConnectionCallTimeout() throws Exception {
        editTextAndVerify(CC_ADDRESS, "call-timeout", "call-timeout", 200L);
    }

    @Test
    public void updateClusterConnectionCheckPeriod() throws Exception {
        editTextAndVerify(CC_ADDRESS, "check-period", "check-period", 9001L);
    }

    @Test
    public void updateClusterConnectionTTLNegativeValue() throws Exception {
        verifyIfErrorAppears("connection-ttl", "-1");
    }

    @Test
    public void setClusterConnectionRetryIntervalToNull() throws Exception {
        //default value will be present if attribute is set to null
        editTextAndVerify(CC_ADDRESS, "retry-interval", "retry-interval", "", 500L);
    }

    @Test
    public void updateClusterConnectionReconnectAttempts() throws Exception {
        editTextAndVerify(CC_ADDRESS, "reconnect-attempts", "reconnect-attempts", 0);
    }

    @Test
    public void removeClusterConnection() throws Exception {
        page.selectInTable(CC_TBR_NAME, 0);
        page.remove();

        new ResourceVerifier(CC_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
