package org.jboss.hal.testsuite.test.runtime;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;
import org.jboss.hal.testsuite.creaper.command.UndeployCommand;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.fragment.MetricsFragment;
import org.jboss.hal.testsuite.mbui.MBUITreeNavigation;
import org.jboss.hal.testsuite.mbui.MbuiNavigation;
import org.jboss.hal.testsuite.page.runtime.WebServiceEndpointsPage;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by pcyprian on 11.8.15.
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class SimpleWebserviceEndpointTestCase {
    private static final String RESPONSES = "Responses";
    private static final String NUMBER_OF_REQUEST = "Number of request";
    private static final String FAULTS = "Faults";
    private static final int DELTA = 3;

    private MbuiNavigation mbuiNavigation;
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);
    private static ResourceVerifier verifier;
    private static Address address = Address.root();

    private static final Address WEBSERVICES_ADDRESS = Address.subsystem("webservices");
    private static final String DEPLOYMENT_FILE_NAME = "test.war";
    private static final File DEPLOYMENT_FILE = new File("src/test/resources/" + DEPLOYMENT_FILE_NAME);

    private static final String STATISTICS_ENABLED = "statistics-enabled";
    private static ModelNode originalStatisticsEnabledValue;

    @Drone
    private WebDriver browser;

    @Page
    private WebServiceEndpointsPage wsePage;

    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException {
        originalStatisticsEnabledValue = operations.readAttribute(WEBSERVICES_ADDRESS, STATISTICS_ENABLED,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS).value();
        operations.writeAttribute(WEBSERVICES_ADDRESS, STATISTICS_ENABLED, true);
        if (client.options().isDomain) {
            address = Address.host(client.options().defaultHost)
                    .and("server", "server-one");
        }
        address = address.and("deployment", DEPLOYMENT_FILE_NAME)
                .and("subsystem", "webservices")
                .and("endpoint", "test%3ATestService");
        verifier =  new ResourceVerifier(address, client);
    }

    @Before
    public void before() throws IOException, CommandFailedException {
        createDeployment().as(ZipExporter.class).exportTo((DEPLOYMENT_FILE), true);

        DeployCommand.Builder deployBuilder = new DeployCommand.Builder(DEPLOYMENT_FILE);
        if (client.options().isDomain) {
            deployBuilder.toAllGroups();
        }
        client.apply(deployBuilder.name(DEPLOYMENT_FILE_NAME).build());

        mbuiNavigation = new MbuiNavigation(browser);
        wsePage.navigate();
    }

    @After
    public void after() throws Exception {
        UndeployCommand.Builder undeployBuilder = new UndeployCommand.Builder(DEPLOYMENT_FILE_NAME);
        if (client.options().isDomain) {
            undeployBuilder.fromAllGroups();
        }
        client.apply(undeployBuilder.build());

        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, TimeoutException, InterruptedException {
        try {
            DEPLOYMENT_FILE.delete();
            if (originalStatisticsEnabledValue != null) {
                operations.writeAttribute(WEBSERVICES_ADDRESS, STATISTICS_ENABLED, originalStatisticsEnabledValue);
            }
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void callServiceAndGetMetrics() throws Exception {
        verifier.verifyExists();

        MetricsAreaFragment wsrMetricsArea = wsePage.getWebServiceRequestMetricsArea();
        double requests = wsrMetricsArea.getMetricNumber(NUMBER_OF_REQUEST);

        assertEquals(0.0, requests);
        verifier.verifyAttribute("request-count", 0L);

        callWebservice(false);
        wsePage.refreshStats();

        wsrMetricsArea = wsePage.getWebServiceRequestMetricsArea();

        requests = wsrMetricsArea.getMetricNumber(NUMBER_OF_REQUEST);
        assertEquals(3.0, requests);
        verifier.verifyAttribute("request-count", 3L);

        double responses = wsrMetricsArea.getMetricNumber(RESPONSES);
        assertEquals(3.0, responses);
        verifier.verifyAttribute("response-count", 3L);

        double faults = wsrMetricsArea.getMetricNumber(FAULTS);
        assertEquals(0.0, faults);
        verifier.verifyAttribute("fault-count", 0L);

        wsePage.navigateInDeploymentsMenu();

        Graphene.createPageFragment(MBUITreeNavigation.class, wsePage.getContentRoot())
                .step("subsystem")
                .step("webservices")
                .step("endpoint")
                .step("test:TestService")
                .navigateToTreeItem()
                .clickLabel();

        mbuiNavigation.checkAndAssertMBuiValueOf("Request count", "3");
        mbuiNavigation.checkAndAssertMBuiValueOf("Response count", "3");
        mbuiNavigation.checkAndAssertMBuiValueOf("Fault count", "0");
    }

    @Test
    public void callServiceWithFaultRequestAndGetMetrics() throws Exception {
        verifier.verifyExists();

        MetricsAreaFragment wsrMetricsArea = wsePage.getWebServiceRequestMetricsArea();
        double requests = wsrMetricsArea.getMetricNumber(NUMBER_OF_REQUEST);
        assertEquals(0.0, requests);

        callWebservice(true);
        wsePage.refreshStats();
        wsrMetricsArea = wsePage.getWebServiceRequestMetricsArea();

        requests = wsrMetricsArea.getMetricNumber(NUMBER_OF_REQUEST);
        assertEquals(1.0, requests);
        verifier.verifyAttribute("request-count", 1L);

        double responses = wsrMetricsArea.getMetricNumber(RESPONSES);
        assertEquals(0.0, responses);
        verifier.verifyAttribute("response-count", 0L);

        double faults = wsrMetricsArea.getMetricNumber(FAULTS);
        assertEquals(1.0, faults);
        verifier.verifyAttribute("fault-count", 1L);

        wsePage.navigateInDeploymentsMenu();

        mbuiNavigation.displayMBuiSubTree("subsystem");
        mbuiNavigation.displayMBuiSubTree("webservices");
        mbuiNavigation.displayMBuiSubTree("endpoint");
        mbuiNavigation.selectItemInMBuiTree("test:TestService");

        mbuiNavigation.checkAndAssertMBuiValueOf("Request count", "1");
        mbuiNavigation.checkAndAssertMBuiValueOf("Response count", "0");
        mbuiNavigation.checkAndAssertMBuiValueOf("Fault count", "1");
    }

    @Test
    public void webServiceRequestsMetrics() throws Exception {
        verifier.verifyExists();

        MetricsAreaFragment wsrMetricsArea = wsePage.getWebServiceRequestMetricsArea();
        double expectedResponsesPercentage = wsrMetricsArea.getPercentage(RESPONSES, NUMBER_OF_REQUEST);
        double expectedFaultsPercentage = wsrMetricsArea.getPercentage(FAULTS, NUMBER_OF_REQUEST);
        MetricsFragment responsesMetrics = wsrMetricsArea.getMetricsFragment(RESPONSES);
        MetricsFragment faultsMetrics = wsrMetricsArea.getMetricsFragment(FAULTS);

        assertEquals(expectedResponsesPercentage, responsesMetrics.getPercentage(), DELTA);
        assertEquals(expectedFaultsPercentage, faultsMetrics.getPercentage(), DELTA);
    }

    //Utils
    private WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_FILE_NAME);
        war.addPackage(SimpleWebserviceEndpointImpl.class.getPackage());
        war.addClass(SimpleWebserviceEndpointImpl.class);
        war.addAsWebInfResource("web.xml", "web.xml");
        return war;
    }

    private void callWebservice(boolean wrongUrl) throws Exception {
        final QName serviceName = new QName("org.jboss.hal.testsuite.test.runtime", "SimpleService");
        URL wsdlURL = null;

        if (wrongUrl)
            wsdlURL = new URL("http", "localhost", 8080, "/test/SimpleService?wsdl/string");
        else
            wsdlURL = new URL("http", "localhost", 8080, "/test/SimpleService?wsdl");
        Service service;
        try {
            service = Service.create(wsdlURL, serviceName);
            final SimpleWebserviceEndpointIface port = service.getPort(SimpleWebserviceEndpointIface.class);
            port.echo("hello");
        } catch (WebServiceException | NullPointerException ignored) {
        }
    }


}



