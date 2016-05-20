package org.jboss.hal.testsuite.test.runtime;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.WebMetricsPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * Created by pcyprian on 11.8.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class WebMetricsTestCase {

    public static final String NUMBER_OF_REQUESTS = "Request Count";
    public static final String ERRORS = "Error Count";

    private static final Address UNDERTOW_SUBSYSTEM_ADDR = Address.subsystem("undertow");
    private static final Address DEFAULT_HTTP_LISTENER_ADDR = UNDERTOW_SUBSYSTEM_ADDR
            .and("server", "default-server")
            .and("http-listener", "default");

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private static final Administration admin = new Administration(client);
    private FinderNavigation navigation;

    private ModelNode undertowStatsEnabledOrigValue;

    @Drone
    private WebDriver browser;

    @Page
    private WebMetricsPage wmPage;
    @Page
    private StandaloneRuntimeEntryPoint standalonePage;
    @Page
    private DomainRuntimeEntryPoint domainPage;

    @AfterClass
    public static void tearDown() {
        IOUtils.closeQuietly(client);
    }

    @Before
    public void before() throws IOException, TimeoutException, InterruptedException {
        undertowStatsEnabledOrigValue = ops.readAttribute(UNDERTOW_SUBSYSTEM_ADDR, "statistics-enabled", ReadAttributeOption.NOT_INCLUDE_DEFAULTS).value();
        ops.writeAttribute(UNDERTOW_SUBSYSTEM_ADDR, "statistics-enabled", true);

        if (ConfigUtils.isDomain()) {
            admin.reload();
            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .step(FinderNames.HOST, "master")
                    .step(FinderNames.SERVER, "server-one")
                    .step(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .step(FinderNames.SUBSYSTEM, "Undertow");
        } else {
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .step(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                    .step(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .step(FinderNames.SUBSYSTEM, "HTTP");
        }

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
        wmPage.getResourceManager().viewByName("default-server");
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @After
    public void after() throws IOException, TimeoutException, InterruptedException {
        if (undertowStatsEnabledOrigValue != null) {
            ops.writeAttribute(UNDERTOW_SUBSYSTEM_ADDR, "statistics-enabled", undertowStatsEnabledOrigValue);
        }
        admin.reloadIfRequired();
    }

    @Test
    public void requestPerConnectorMetrics() throws IOException {
        long expectedRequests = ops.readAttribute(DEFAULT_HTTP_LISTENER_ADDR, "request-count").longValue();
        long expectedErrors = ops.readAttribute(DEFAULT_HTTP_LISTENER_ADDR, "error-count").longValue();

        Console.withBrowser(browser).waitUntilLoaded();
        MetricsAreaFragment rpcMetricsArea = wmPage.getRequestPerConnectorMetricsArea();
        long errors = (long) rpcMetricsArea.getMetricNumber(ERRORS);
        long numberOfRequests = (long) rpcMetricsArea.getMetricNumber(NUMBER_OF_REQUESTS);
        //double expectedPercentage = rpcMetricsArea.getPercentage(ERRORS, NUMBER_OF_REQUESTS);
        //MetricsFragment metric = rpcMetricsArea.getMetricsFragment(ERRORS);
        //assertEquals(expectedPercentage, metric.getPercentage(), DELTA);

        assertEquals(expectedErrors, errors);
        assertEquals(expectedRequests, numberOfRequests);
    }
}
