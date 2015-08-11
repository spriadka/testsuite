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
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.JVMPage;
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
public class JvmMetricsTestCase {

    public static final String USED = "Used";
    public static final String MAX = "Max";
    public static final String COMMITTED = "Committed";
    public static final String DAEMON = "Daemon";
    public static final String LIVE = "Live";
    public static final int DELTA = 3;

    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private JVMPage jvmPage;
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
                    .addAddress(FinderNames.MONITOR, "JVM");
        }
        else{
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                    .addAddress(FinderNames.MONITOR, "JVM");
        }

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    @Test
    public void heapUsageMetrics(){
        MetricsAreaFragment metricsArea = jvmPage.getHeapUsageMetricsArea();
        double expectedUsedPercentage = metricsArea.getPercentage(USED, MAX);
        double expectedCommittedPercentage = metricsArea.getPercentage(COMMITTED, MAX);
        MetricsFragment usedMetrics = metricsArea.getMetricsFragment(USED);
        MetricsFragment committedMetrics = metricsArea.getMetricsFragment(COMMITTED);

        assertEquals(expectedUsedPercentage, usedMetrics.getPercentage(), DELTA);
        assertEquals(expectedCommittedPercentage, committedMetrics.getPercentage(), DELTA);
    }

    @Test
    public void threadUsageMetrics(){
        MetricsAreaFragment metricsArea = jvmPage.getThreadUsageMetricsArea();
        double expectedUsagePercentage = metricsArea.getPercentage(DAEMON, LIVE);
        MetricsFragment threadUsageMetrics = metricsArea.getMetricsFragment(DAEMON);

        assertEquals(expectedUsagePercentage, threadUsageMetrics.getPercentage(), DELTA);
    }
}
