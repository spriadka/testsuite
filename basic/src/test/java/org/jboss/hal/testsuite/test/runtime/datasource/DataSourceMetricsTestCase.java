package org.jboss.hal.testsuite.test.runtime.datasource;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.runtime.DataSourcesMetricsPage;
import org.jboss.hal.testsuite.test.runtime.MetricGraphVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

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

    @Drone
    private WebDriver browser;

    @Page
    private DataSourcesMetricsPage dsPage;

    @Before
    public void before() {
        dsPage.navigate();
    }

    @Test
    public void connectionPoolMetrics() {
        new MetricGraphVerifier(dsPage.getConnectionPoolMetricsArea(), AVAILABLE_CONNECTIONS)
                .verifyRatio(ACTIVE).verifyRatio(MAX_USED);
    }

    @Test
    public void preparedStatementCacheMetrics() {
        new MetricGraphVerifier(dsPage.getPreparedStatementCacheMetricsArea(), ACCESS_COUNT)
                .verifyRatio(HIT_COUNT).verifyRatio(MISS_COUNT);
    }
}
