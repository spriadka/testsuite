package org.jboss.hal.testsuite.test.runtime.transaction;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.runtime.TransactionsMetricsPage;
import org.jboss.hal.testsuite.test.runtime.MetricGraphVerifier;
import org.jboss.hal.testsuite.test.runtime.deployments.DeploymentsOperations;
import org.jboss.hal.testsuite.test.runtime.transaction.beans.StatisticsBean;
import org.jboss.hal.testsuite.test.runtime.transaction.beans.StatisticsRemote;
import org.jboss.hal.testsuite.util.EJBUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.jboss.hal.testsuite.page.MetricsPage.MONITORED_SERVER;
import static org.jboss.hal.testsuite.util.ConfigUtils.*;
import static org.junit.Assert.*;
import static org.wildfly.extras.creaper.core.online.Constants.*;
import static org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption.NOT_INCLUDE_DEFAULTS;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.ejb.EJBTransactionRolledbackException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class TransactionsMetricsTestCase {

    private static final Logger log = LoggerFactory.getLogger(TransactionsMetricsTestCase.class);

    private static final String
        AVERAGE_COMMIT_TIME_LABEL = "Average Commit Time",
        AVERAGE_COMMIT_TIME = "average-commit-time",
        NUMBER_OF_ABORTED_TRANSACTIONS_LABEL = "Aborted",
        NUMBER_OF_ABORTED_TRANSACTIONS = "number-of-aborted-transactions",
        NUMBER_OF_APPLICATION_ROLLBACKS_LABEL = "Application Failures",
        NUMBER_OF_APPLICATION_ROLLBACKS = "number-of-application-rollbacks",
        NUMBER_OF_COMMITTED_TRANSACTIONS_LABEL = "Committed",
        NUMBER_OF_COMMITTED_TRANSACTIONS = "number-of-committed-transactions",
        NUMBER_OF_HEURISTICS_LABEL = "Heuristics",
        NUMBER_OF_HEURISTICS = "number-of-heuristics",
        NUMBER_OF_INFLIGHT_TRANSACTIONS_LABEL = "Inflight Transactions",
        NUMBER_OF_INFLIGHT_TRANSACTIONS = "number-of-inflight-transactions",
        NUMBER_OF_NESTED_TRANSACTIONS_LABEL = "Nested Transactions",
        NUMBER_OF_NESTED_TRANSACTIONS = "number-of-nested-transactions",
        NUMBER_OF_RESOURCE_ROLLBACKS_LABEL = "Resource Failures",
        NUMBER_OF_RESOURCE_ROLLBACKS = "number-of-resource-rollbacks",
        NUMBER_OF_SYSTEM_ROLLBACKS_LABEL = "System Failures",
        NUMBER_OF_SYSTEM_ROLLBACKS = "number-of-system-rollbacks",
        NUMBER_OF_TIMED_OUT_TRANSACTIONS_LABEL = "Timed Out",
        NUMBER_OF_TIMED_OUT_TRANSACTIONS = "number-of-timed-out-transactions",
        NUMBER_OF_TRANSACTIONS_LABEL = "Number of Transactions",
        NUMBER_OF_TRANSACTIONS = "number-of-transactions",
        TOTAL_FAILURES_LABEL = "Total Failures",
        ARCHIVE_NAME = TransactionsMetricsTestCase.class.getSimpleName(),
        STATISTICS_ENABLED = "statistics-enabled", TRANSACTIONS = "transactions";
    private static final Address TXN_ADDRESS = Address.subsystem(TRANSACTIONS),
            TXN_RUNTIME_ADDRESS = isDomain() ?
                    Address.host(getDefaultHost()).and(SERVER, MONITORED_SERVER).and(SUBSYSTEM, TRANSACTIONS)
                    : TXN_ADDRESS;
    public static final int DELTA = 3;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private static final DeploymentsOperations deploymentOps = new DeploymentsOperations(client);
    private static final Administration adminOps = new Administration(client);

    private static ModelNode originallyStatisticEnabled;

    @Drone
    private WebDriver browser;

    @Page
    private TransactionsMetricsPage tmPage;

    @BeforeClass
    public static void deployTxnApplication() throws CommandFailedException, IOException, InterruptedException,
            TimeoutException {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME + ".jar")
                .addPackage("org.jboss.hal.testsuite.test.runtime.transaction.beans")
                .addClass(TxnRollbackException.class)
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.jts\n"), "MANIFEST.MF");
        deploymentOps.deploy(jar);
        originallyStatisticEnabled = ops.readAttribute(TXN_ADDRESS, STATISTICS_ENABLED, NOT_INCLUDE_DEFAULTS).value();
        ops.writeAttribute(TXN_ADDRESS, STATISTICS_ENABLED, true);
    }

    @AfterClass
    public static void undeployTxnApplication() throws IOException, OperationException, CommandFailedException,
            InterruptedException, TimeoutException {
        try {
            if (originallyStatisticEnabled != null) {
                ops.writeAttribute(TXN_ADDRESS, STATISTICS_ENABLED, originallyStatisticEnabled);
            }
            deploymentOps.undeployIfExists(ARCHIVE_NAME + ".jar");
            adminOps.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void metricsTest() throws Exception {
        callEjbs();
        tmPage.navigate();
        verifyAllMetrics();
        callEjbs();
        tmPage.refreshStats();
        verifyAllMetrics();
    }

    private void verifyAllMetrics() throws Exception {
        ResourceVerifier verifier = new ResourceVerifier(TXN_RUNTIME_ADDRESS, client);
        MetricsAreaFragment generalStatisticsArea = tmPage.getGeneralStatisticsMetricsArea(),
                successRatioArea = tmPage.getSuccessRationMetricsArea(),
                failureOriginArea = tmPage.getFailureOriginMetricsArea();

        verifier.verifyAttribute(AVERAGE_COMMIT_TIME,
                (long) generalStatisticsArea.getMetricNumber(AVERAGE_COMMIT_TIME_LABEL));
        verifier.verifyAttribute(NUMBER_OF_INFLIGHT_TRANSACTIONS,
                (long) generalStatisticsArea.getMetricNumber(NUMBER_OF_INFLIGHT_TRANSACTIONS_LABEL));
        verifier.verifyAttribute(NUMBER_OF_NESTED_TRANSACTIONS,
                (long) generalStatisticsArea.getMetricNumber(NUMBER_OF_NESTED_TRANSACTIONS_LABEL));

        verifier.verifyAttribute(NUMBER_OF_TRANSACTIONS,
                (long) successRatioArea.getMetricNumber(NUMBER_OF_TRANSACTIONS_LABEL));
        verifier.verifyAttribute(NUMBER_OF_COMMITTED_TRANSACTIONS,
                (long) successRatioArea.getMetricNumber(NUMBER_OF_COMMITTED_TRANSACTIONS_LABEL));
        verifier.verifyAttribute(NUMBER_OF_ABORTED_TRANSACTIONS,
                (long) successRatioArea.getMetricNumber(NUMBER_OF_ABORTED_TRANSACTIONS_LABEL));
        verifier.verifyAttribute(NUMBER_OF_TIMED_OUT_TRANSACTIONS,
                (long) successRatioArea.getMetricNumber(NUMBER_OF_TIMED_OUT_TRANSACTIONS_LABEL));
        verifier.verifyAttribute(NUMBER_OF_HEURISTICS,
                (long) successRatioArea.getMetricNumber(NUMBER_OF_HEURISTICS_LABEL));
        new MetricGraphVerifier(successRatioArea, NUMBER_OF_TRANSACTIONS_LABEL)
                .verifyRatio(NUMBER_OF_COMMITTED_TRANSACTIONS_LABEL)
                .verifyRatio(NUMBER_OF_ABORTED_TRANSACTIONS_LABEL)
                .verifyRatio(NUMBER_OF_TIMED_OUT_TRANSACTIONS_LABEL)
                .verifyRatio(NUMBER_OF_HEURISTICS_LABEL);

        assertEquals(numberOf(NUMBER_OF_SYSTEM_ROLLBACKS)
                + numberOf(NUMBER_OF_APPLICATION_ROLLBACKS)
                + numberOf(NUMBER_OF_RESOURCE_ROLLBACKS),
                (long) failureOriginArea.getMetricNumber(TOTAL_FAILURES_LABEL));
        verifier.verifyAttribute(NUMBER_OF_SYSTEM_ROLLBACKS,
                (long) failureOriginArea.getMetricNumber(NUMBER_OF_SYSTEM_ROLLBACKS_LABEL));
        verifier.verifyAttribute(NUMBER_OF_APPLICATION_ROLLBACKS,
                (long) failureOriginArea.getMetricNumber(NUMBER_OF_APPLICATION_ROLLBACKS_LABEL));
        verifier.verifyAttribute(NUMBER_OF_RESOURCE_ROLLBACKS,
                (long) failureOriginArea.getMetricNumber(NUMBER_OF_RESOURCE_ROLLBACKS_LABEL));
        new MetricGraphVerifier(failureOriginArea, TOTAL_FAILURES_LABEL)
                .verifyRatio(NUMBER_OF_SYSTEM_ROLLBACKS_LABEL)
                .verifyRatio(NUMBER_OF_APPLICATION_ROLLBACKS_LABEL)
                .verifyRatio(NUMBER_OF_RESOURCE_ROLLBACKS_LABEL);
    }

    private void callEjbs() throws Exception {
        StatisticsRemote bean = EJBUtils.lookup(StatisticsRemote.class, StatisticsBean.class, ARCHIVE_NAME, false);
        bean.testTx();
        try {
            bean.testTxRollback();
            fail("Transaction rollback exception has to be thrown");
        } catch (TxnRollbackException e) {
            log.info("Transaction rollback exception was thrown as expected");
        }
        try {
            bean.testTxTimeout();
            fail("Transaction rollback exception has to be thrown");
        } catch (EJBTransactionRolledbackException e) {
            log.info("Transaction rollback exception was thrown as expected");
        }
        log.info("Full statistics are: " + statisticsToString());
    }

    private long numberOf(String transactionAttributeName) throws IOException {
        return ops.readAttribute(TXN_RUNTIME_ADDRESS, transactionAttributeName).longValue();
    }

    private String statisticsToString() throws IOException {
        return AVERAGE_COMMIT_TIME + ":" + numberOf(AVERAGE_COMMIT_TIME) + ", "
                    + NUMBER_OF_ABORTED_TRANSACTIONS + ":" + numberOf(NUMBER_OF_ABORTED_TRANSACTIONS) + ", "
                    + NUMBER_OF_APPLICATION_ROLLBACKS + ":" + numberOf(NUMBER_OF_APPLICATION_ROLLBACKS) + ", "
                    + NUMBER_OF_COMMITTED_TRANSACTIONS + ":" + numberOf(NUMBER_OF_COMMITTED_TRANSACTIONS) + ", "
                    + NUMBER_OF_HEURISTICS + ":" + numberOf(NUMBER_OF_HEURISTICS) + ", "
                    + NUMBER_OF_INFLIGHT_TRANSACTIONS + ":" + numberOf(NUMBER_OF_INFLIGHT_TRANSACTIONS) + ", "
                    + NUMBER_OF_NESTED_TRANSACTIONS + ":" + numberOf(NUMBER_OF_NESTED_TRANSACTIONS) + ", "
                    + NUMBER_OF_RESOURCE_ROLLBACKS + ":" + numberOf(NUMBER_OF_RESOURCE_ROLLBACKS) + ", "
                    + NUMBER_OF_SYSTEM_ROLLBACKS + ":" + numberOf(NUMBER_OF_SYSTEM_ROLLBACKS) + ", "
                    + NUMBER_OF_TIMED_OUT_TRANSACTIONS + ":" + numberOf(NUMBER_OF_TIMED_OUT_TRANSACTIONS) + ", "
                    + NUMBER_OF_TRANSACTIONS + ":" + numberOf(NUMBER_OF_TRANSACTIONS);
    }
}
