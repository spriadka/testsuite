package org.jboss.hal.testsuite.test.runtime.messaging;

import static org.jboss.as.controller.client.helpers.ClientConstants.SUBSYSTEM;
import static org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import static org.jboss.hal.testsuite.page.MetricsPage.MONITORED_SERVER;
import static org.jboss.hal.testsuite.util.ConfigUtils.getDefaultHost;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.KnownIssue;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.page.runtime.MessagingStatisticsPage;
import org.jboss.hal.testsuite.test.runtime.deployments.DeploymentsOperations;
import org.jboss.hal.testsuite.test.runtime.messaging.deployment.MessagingStatisticsBean;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class XaJmsMetricsTestCase {

    private static final Logger log = LoggerFactory.getLogger(XaJmsMetricsTestCase.class);
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration adminOps = new Administration(client);
    private static final Operations ops = new Operations(client);
    private static final DeploymentsOperations deployOps = new DeploymentsOperations(client);
    private static final String
        ARCHIVE_NAME = "xa-jms-txn",
        WAR_FILE_EXTENSION = ".war",
        GENERATE_STATS_URL = "http://" + ConfigUtils.getUrl().getHost() + ":8080/" + ARCHIVE_NAME,
        SUBSYSTEM_NAME = "messaging-activemq",
        CONNECTION_FACTORY_NAME = "activemq-ra",
        SERVER = "server",
        DEFAULT = "default",
        POOLED_CONNECTION_FACTORY = "pooled-connection-factory",
        STATISTICS_ENABLED = "statistics-enabled";
    private static final Address
        defaultJmsServerAddress = Address.subsystem(SUBSYSTEM_NAME).and(SERVER, DEFAULT),
        queueAddress = defaultJmsServerAddress.and("jms-queue", "test"),
        pooledConnectionFactoryAddress = defaultJmsServerAddress.and(POOLED_CONNECTION_FACTORY, CONNECTION_FACTORY_NAME),
        pooledConnectionFactoryStatsAddress;
    private static ModelNodeResult originalFactoryStatsEnabledResult;

    static {
        Address subsystemRuntimeAddress = ConfigUtils.isDomain() ?
                Address.host(getDefaultHost()).and(SERVER, MONITORED_SERVER).and(SUBSYSTEM, SUBSYSTEM_NAME)
                : Address.subsystem(SUBSYSTEM_NAME);
        pooledConnectionFactoryStatsAddress = subsystemRuntimeAddress.and(SERVER, DEFAULT)
                .and(POOLED_CONNECTION_FACTORY, CONNECTION_FACTORY_NAME).and("statistics", "pool");
    }

    @Drone
    private WebDriver browser;

    @Page
    private MessagingStatisticsPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ModelNode entriesNode = new ModelNodeListBuilder(new ModelNode("java:/jms/queue/test")).build();
        ops.add(queueAddress, Values.of("entries", entriesNode)).assertSuccess();
        WebArchive war = ShrinkWrap.create(WebArchive.class, ARCHIVE_NAME + WAR_FILE_EXTENSION);
        war.addPackage(MessagingStatisticsBean.class.getPackage());
        deployOps.deploy(war);

        originalFactoryStatsEnabledResult = ops.readAttribute(pooledConnectionFactoryAddress, STATISTICS_ENABLED);
        originalFactoryStatsEnabledResult.assertSuccess();
        if (!originalFactoryStatsEnabledResult.value().isDefined() || !originalFactoryStatsEnabledResult.booleanValue()) {
            ops.writeAttribute(pooledConnectionFactoryAddress, STATISTICS_ENABLED, true);
            adminOps.reloadIfRequired();
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        try {
            deployOps.undeploy(ARCHIVE_NAME + WAR_FILE_EXTENSION);
            ops.removeIfExists(queueAddress);
            if (!originalFactoryStatsEnabledResult.value().isDefined()
                    || !originalFactoryStatsEnabledResult.booleanValue()) {
                ops.writeAttribute(pooledConnectionFactoryAddress, STATISTICS_ENABLED,
                        originalFactoryStatsEnabledResult.value());
                adminOps.reloadIfRequired();
            }
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    /**
     * @tpTestDetails Generate load on JMS XA pooled connection factory. Open Messaging statistics page in web console
     * and verified displayed pool statistics of the connection factory against actual values from management model.
     * To verify Refresh button functionality generate load on the factory again, click on the Refresh button, select
     * the connection factory and verify against management model that displayed statistics have changed correctly.
     */
    @Test
    @Category(KnownIssue.class)
    public void jmsXaPoolStatisticsTest() throws Exception {
        generateStats();
        page.navigateToDefaultProviderStats().switchToPooledConnectionFactory()
            .selectConnectionFactory(CONNECTION_FACTORY_NAME);
        verifyMetrics();
        generateStats();
        page.refresh().selectConnectionFactory(CONNECTION_FACTORY_NAME);
        verifyMetrics();
    }

    /**
     * Generates statistics by calling deployed application and asserts OK response.<br>Uses {@link CloseableHttpClient}
     * instead of {@link WebDriver} to save resources and avoid taking care of two windows since the original HAL window
     * needs to remain open to test Refresh functionality.
     */
    private void generateStats() throws IOException {
        HttpGet request = new HttpGet(GENERATE_STATS_URL);
        log.info("Sending request '{}'.", request);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            final CloseableHttpResponse response = httpClient.execute(request);
            Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        }
    }

    private void verifyMetrics() throws Exception {
        ResourceVerifier verifier = new ResourceVerifier(pooledConnectionFactoryStatsAddress, client);

        ConfigAreaFragment config = page.getConfig();
        String[] intAttrs = new String[] {"ActiveCount", "AvailableCount", "BlockingFailureCount", "CreatedCount",
                "DestroyedCount", "IdleCount", "InUseCount", "MaxUsedCount", "MaxWaitCount", "TimedOut", "WaitCount"};
        for (String attr : intAttrs) {
            verifier.verifyAttribute(attr, Integer.parseInt(config.getAttributeValue(attr)));
        }
        String[] longAttrs = new String[] {"AverageBlockingTime", "AverageCreationTime", "AverageGetTime",
                "AveragePoolTime", "AverageUsageTime", "MaxCreationTime", "MaxGetTime", "MaxPoolTime", "MaxUsageTime",
                "MaxWaitTime", "TotalBlockingTime", "TotalCreationTime", "TotalGetTime", "TotalPoolTime",
                "TotalUsageTime", "XACommitAverageTime", "XACommitCount", "XACommitMaxTime", "XACommitTotalTime",
                "XAEndAverageTime", "XAEndCount", "XAEndMaxTime", "XAEndTotalTime", "XAForgetAverageTime",
                "XAForgetCount", "XAForgetMaxTime", "XAForgetTotalTime", "XAPrepareAverageTime", "XAPrepareCount",
                "XAPrepareMaxTime", "XAPrepareTotalTime", "XARecoverAverageTime", "XARecoverCount", "XARecoverMaxTime",
                "XARecoverTotalTime", "XARollbackAverageTime", "XARollbackCount", "XARollbackMaxTime",
                "XARollbackTotalTime", "XAStartAverageTime", "XAStartCount", "XAStartMaxTime", "XAStartTotalTime"};
        for (String attr : longAttrs) {
            log.trace("Verifying '{}' attribute of '{}'", attr, pooledConnectionFactoryStatsAddress);
            verifier.verifyAttribute(attr, Long.parseLong(config.getAttributeValue(attr)));
        }
        Assert.assertEquals(true, Boolean.parseBoolean(config.getAttributeValue("statistics-enabled")));
    }

}
