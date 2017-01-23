package org.jboss.hal.testsuite.test.runtime.datasource;

import static org.jboss.as.controller.client.helpers.ClientConstants.SERVER;
import static org.jboss.as.controller.client.helpers.ClientConstants.SUBSYSTEM;
import static org.jboss.hal.testsuite.page.MetricsPage.MONITORED_SERVER;
import static org.jboss.hal.testsuite.util.ConfigUtils.getDefaultHost;
import static org.jboss.hal.testsuite.util.ConfigUtils.isDomain;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb.client.EJBClientTransactionContext;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.runtime.DataSourcesMetricsPage;
import org.jboss.hal.testsuite.test.runtime.datasource.beans.DsStatisticsBean;
import org.jboss.hal.testsuite.test.runtime.datasource.beans.DsStatisticsRemote;
import org.jboss.hal.testsuite.test.runtime.deployments.DeploymentsOperations;
import org.jboss.hal.testsuite.util.EJBUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.datasources.AddXADataSource;
import org.wildfly.extras.creaper.commands.datasources.RemoveXADataSource;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class XaDatasourceMetricsTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration adminOps = new Administration(client);
    private static final DeploymentsOperations deployOps = new DeploymentsOperations(client);
    private static final String XA_DATASOURCE_NAME = "ExampleXADS",
            XA_DATASOURCE_JNDI_NAME = "java:jboss/datasources/" + XA_DATASOURCE_NAME,
            APP_NAME = XaDatasourceMetricsTestCase.class.getSimpleName(),
            ARCHIVE_NAME = "xa-txn",
            PERSISTENCE_XML = "persistence.xml",
            AVAILABLE_CONNECTIONS_LABEL = "Available Connections",
            AVAILABLE_COUNT = "AvailableCount",
            ACTIVE_LABEL = "Active",
            ACTIVE_COUNT = "ActiveCount",
            MAX_USED_LABEL = "Max Used",
            MAX_USED_COUNT = "MaxUsedCount";
    private static final Address DS_RUNTIME_ADDRESS;
    static {
        String datasources = "datasources";
        Address dsSubsytemRuntimeAddress = isDomain() ?
                Address.host(getDefaultHost()).and(SERVER, MONITORED_SERVER).and(SUBSYSTEM, datasources)
                : Address.subsystem(datasources);
        DS_RUNTIME_ADDRESS = dsSubsytemRuntimeAddress.and("xa-data-source", XA_DATASOURCE_NAME)
                .and("statistics", "pool");
    }

    @Drone
    private WebDriver browser;

    @Page
    private DataSourcesMetricsPage page;

    @BeforeClass
    public static void beforeClass() throws CommandFailedException {
        client.apply(new AddXADataSource.Builder<>(XA_DATASOURCE_NAME).jndiName(XA_DATASOURCE_JNDI_NAME)
                .driverName("h2").usernameAndPassword("sa", "sa").enableAfterCreate().statisticsEnabled(true)
                .addXaDatasourceProperty("URL", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE").build());
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, APP_NAME + ".ear");
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME + ".jar");
        jar.addPackage(DsStatisticsRemote.class.getPackage());
        jar.addClasses(TestEntity.class);
        jar.addAsManifestResource(XaDatasourceMetricsTestCase.class.getPackage(), PERSISTENCE_XML, PERSISTENCE_XML);
        ear.addAsModule(jar);
        ear.addAsManifestResource(new StringAsset("Dependencies: com.h2database.h2\n"), "MANIFEST.MF");
        deployOps.deploy(ear);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        try {
            deployOps.undeploy(APP_NAME + ".ear");
            client.apply(new RemoveXADataSource(XA_DATASOURCE_NAME));
            adminOps.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void poolStatisticsTest() throws Exception {
        EJBClientTransactionContext.setGlobalContext(EJBClientTransactionContext.createLocal());
        callEjb();
        page.navigateToXADataSources();
        verifyMetrics();
        callEjb();
        page.switchToStatistics().refreshStats();
        verifyMetrics();
    }

    private void callEjb() throws Exception {
        DsStatisticsRemote bean =
                EJBUtils.lookup(DsStatisticsRemote.class, DsStatisticsBean.class, APP_NAME, ARCHIVE_NAME, false);
        bean.commit();
        bean.rollback();
    }

    private void verifyMetrics() throws Exception {
        ResourceVerifier verifier = new ResourceVerifier(DS_RUNTIME_ADDRESS, client);
        page.switchToStatistics();
        MetricsAreaFragment connectionPoolMetricsArea = page.getConnectionPoolMetricsArea();
        verifier.verifyAttribute(AVAILABLE_COUNT,
                (int) connectionPoolMetricsArea.getMetricNumber(AVAILABLE_CONNECTIONS_LABEL));
        verifier.verifyAttribute(ACTIVE_COUNT,
                (int) connectionPoolMetricsArea.getMetricNumber(ACTIVE_LABEL));
        verifier.verifyAttribute(MAX_USED_COUNT,
                (int) connectionPoolMetricsArea.getMetricNumber(MAX_USED_LABEL));

        page.switchToPoolStatistics();
        ConfigAreaFragment config = page.getConfig();
        String[] intAttrs = new String[] {AVAILABLE_COUNT, ACTIVE_COUNT, MAX_USED_COUNT, "CreatedCount", "IdleCount"};
        for (String attr : intAttrs) {
            verifier.verifyAttribute(attr, Integer.parseInt(config.getAttributeValue(attr)));
        }
        String[] longAttrs = new String[] {"AverageGetTime", "AveragePoolTime", "AverageUsageTime", "MaxGetTime",
                "MaxPoolTime", "MaxUsageTime", "TotalGetTime", "TotalPoolTime", "TotalUsageTime", "XACommitCount",
                "XAEndCount", "XARollbackCount", "XAStartCount", "XAStartMaxTime", "XAStartTotalTime"};
        for (String attr : longAttrs) {
            verifier.verifyAttribute(attr, Long.parseLong(config.getAttributeValue(attr)));
        }
    }
}
