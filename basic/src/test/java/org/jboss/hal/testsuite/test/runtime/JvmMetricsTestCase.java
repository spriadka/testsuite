package org.jboss.hal.testsuite.test.runtime;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.JVMPage;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
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
public class JvmMetricsTestCase {

    public static final String USED = "Used";
    public static final String MAX = "Max";
    public static final String COMMITTED = "Committed";
    public static final String DAEMON = "Daemon";
    public static final String LIVE = "Live";

    @Drone
    private WebDriver browser;

    @Page
    private JVMPage jvmPage;
    @Page
    private StandaloneRuntimeEntryPoint standalonePage;
    @Page
    private DomainRuntimeEntryPoint domainPage;

    @Before
    public void before() {
        jvmPage.navigate();
    }

    @Test
    public void heapUsageMetrics() {
        new MetricGraphVerifier(jvmPage.getHeapUsageMetricsArea(), MAX).verifyRatio(USED).verifyRatio(COMMITTED);
    }

    @Test
    public void threadUsageMetrics() {
        new MetricGraphVerifier(jvmPage.getThreadUsageMetricsArea(), LIVE).verifyRatio(DAEMON);
    }
}
