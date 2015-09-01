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
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.WebServiceEndpointsPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by pcyprian on 11.8.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class WebServiceEndpointsTestCase {
    public static final String RESPONSES = "Responses";
    public static final String NUMBER_OF_REQUEST = "Number of request";
    public static final String FAULTS = "Faults";
    public static final int DELTA = 3;

    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private WebServiceEndpointsPage wsePage;
    @Page
    private StandaloneRuntimeEntryPoint standalonePage;
    @Page
    private DomainRuntimeEntryPoint domainPage;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .addAddress(FinderNames.HOST, "master")
                    .addAddress(FinderNames.SERVER,"server-one")
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Webservices");
        }
        else {
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM,"Webservices");
        }
        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    @Test
    public void webServiceRequestsMetrics() {
        MetricsAreaFragment wsrMetricsArea = wsePage.getWebServiceRequestMetricsArea();
        double expectedResponsesPercentage = wsrMetricsArea.getPercentage(RESPONSES, NUMBER_OF_REQUEST);
        double expectedFaultsPercentage = wsrMetricsArea.getPercentage(FAULTS, NUMBER_OF_REQUEST);
        MetricsFragment responsesMetrics = wsrMetricsArea.getMetricsFragment(RESPONSES);
        MetricsFragment faultsMetrics = wsrMetricsArea.getMetricsFragment(FAULTS);

        assertEquals(expectedResponsesPercentage, responsesMetrics.getPercentage(), DELTA);
        assertEquals(expectedFaultsPercentage, faultsMetrics.getPercentage(), DELTA);
    }
}
