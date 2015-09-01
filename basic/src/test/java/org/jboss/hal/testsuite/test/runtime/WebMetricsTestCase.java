package org.jboss.hal.testsuite.test.runtime;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

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
    public static final int DELTA = 3;

    static final AddressTemplate ADDRESS_TEMPLATE = AddressTemplate
            .of("{default.profile}/subsystem=undertow/server=default-server/http-listener=default");
    static final AddressTemplate ADDRESS_TEMPLATE_STATISTICS = AddressTemplate
            .of("{default.profile}/subsystem=undertow");

    private CliClient cliClient = CliClientFactory.getClient();
    private FinderNavigation navigation;
    private DefaultContext statementContext;
    private static Dispatcher dispatcher;

    @Drone
    private WebDriver browser;

    @Page
    private WebMetricsPage wmPage;
    @Page
    private StandaloneRuntimeEntryPoint standalonePage;
    @Page
    private DomainRuntimeEntryPoint domainPage;

    @Before
    public void before() throws IOException,TimeoutException,InterruptedException {
        dispatcher = new Dispatcher();
        statementContext = new DefaultContext();
        ResourceAddress addressStats = ADDRESS_TEMPLATE_STATISTICS.resolve(statementContext);
        dispatcher.execute(new Operation.Builder(ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION, addressStats).param("name", "statistics-enabled").param("value", "true").build());
        cliClient.reload();

        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .addAddress(FinderNames.HOST, "master")
                    .addAddress(FinderNames.SERVER,"server-one")
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Undertow");
        }
        else {
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM,"HTTP");
        }

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
        wmPage.getResourceManager().viewByName("default-server");
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @After
    public void after() throws IOException,TimeoutException,InterruptedException {
        ResourceAddress addressStats = ADDRESS_TEMPLATE_STATISTICS.resolve(statementContext);
        dispatcher.execute(new Operation.Builder(ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION, addressStats).param("name", "statistics-enabled").param("value", "false").build());
        cliClient.reload();
    }

    @Test
    public void requestPerConnectorMetrics() {
        ResourceAddress address = ADDRESS_TEMPLATE.resolve(statementContext);
        long expectedRequests = dispatcher.execute(new Operation.Builder(ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION, address).param("name", "request-count").build()).payload().asLong();
        long expectedErrors = dispatcher.execute(new Operation.Builder(ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION, address).param("name", "error-count").build()).payload().asLong();

        Console.withBrowser(browser).waitUntilLoaded();
        MetricsAreaFragment rpcMetricsArea = wmPage.getRequestPerConnectorMetricsArea();
        long errors = (long) rpcMetricsArea.getMetricNumber(ERRORS);
        long numberOfRequests = (long) rpcMetricsArea.getMetricNumber(NUMBER_OF_REQUESTS);
        //double expectedPercentage = rpcMetricsArea.getPercentage(ERRORS, NUMBER_OF_REQUESTS);
        //MetricsFragment metric = rpcMetricsArea.getMetricsFragment(ERRORS);
        //assertEquals(expectedPercentage, metric.getPercentage(), DELTA);

        assertEquals(expectedErrors,errors);
        assertEquals(expectedRequests,numberOfRequests);
    }
}
