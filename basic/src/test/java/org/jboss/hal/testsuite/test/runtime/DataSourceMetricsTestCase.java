package org.jboss.hal.testsuite.test.runtime;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.fragment.MetricsFragment;
import org.jboss.hal.testsuite.page.runtime.DataSourcesMetricsPage;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class DataSourceMetricsTestCase {

    public static final String AVAILABLE_CONNECTIONS = "Available Connections";
    public static final String ACTIVE = "Active";
    public static final String MAX_USED = "Max Used";
    public static final String HIT_COUNT = "Hit Count";
    public static final String ACCESS_COUNT = "Access Count";
    public static final String MISS_COUNT = "Miss Count";
    public static final int DELTA = 3;

    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private DataSourcesMetricsPage dsPage;
    @Page
    private StandaloneRuntimeEntryPoint standalonePage;
    @Page
    private DomainRuntimeEntryPoint domainPage;

    @Before
    public void before(){
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .addAddress(FinderNames.HOST, "master")
                    .addAddress(FinderNames.SERVER,"server-one")
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Datasources");
        }
        else{
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM,"Datasources");
        }

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    @Test
    public void connectionPoolMetrics(){
        MetricsAreaFragment metricsArea = dsPage.getConnectionPoolMetricsArea();
        double expectedActivePercentage = metricsArea.getPercentage(ACTIVE, AVAILABLE_CONNECTIONS);
        double expectedMaxUsedPercentage = metricsArea.getPercentage(MAX_USED, AVAILABLE_CONNECTIONS);

        MetricsFragment activeMetrics = metricsArea.getMetricsFragment(ACTIVE);
        MetricsFragment maxUsedMetrics = metricsArea.getMetricsFragment(MAX_USED);

        assertEquals(expectedActivePercentage, activeMetrics.getPercentage(), DELTA);
        assertEquals(expectedMaxUsedPercentage, maxUsedMetrics.getPercentage(), DELTA);
    }

    @Test
    public void preparedStatementCacheMetrics(){
        MetricsAreaFragment metricsArea = dsPage.getPreparedStatementCacheMetricsArea();
        double expectedHitCountPercentage = metricsArea.getPercentage(HIT_COUNT, ACCESS_COUNT);
        double expectedMissCountPercentage = metricsArea.getPercentage(MISS_COUNT, ACCESS_COUNT);

        MetricsFragment hitCountMetrics = metricsArea.getMetricsFragment(HIT_COUNT);
        MetricsFragment missCountMetrics = metricsArea.getMetricsFragment(MISS_COUNT);

        assertEquals(expectedHitCountPercentage, hitCountMetrics.getPercentage(), DELTA);
        assertEquals(expectedMissCountPercentage, missCountMetrics.getPercentage(), DELTA);
    }
}
