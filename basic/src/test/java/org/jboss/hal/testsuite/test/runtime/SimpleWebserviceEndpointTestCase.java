package org.jboss.hal.testsuite.test.runtime;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;
import org.jboss.hal.testsuite.creaper.command.UndeployCommand;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.fragment.MetricsFragment;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.WebServiceEndpointsPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import java.io.File;
import java.io.IOException;
import java.net.URL;


import static junit.framework.TestCase.assertEquals;

/**
 * Created by pcyprian on 11.8.15.
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class SimpleWebserviceEndpointTestCase {
    public static final String RESPONSES = "Responses";
    public static final String NUMBER_OF_REQUEST = "Number of request";
    public static final String FAULTS = "Faults";
    public static final int DELTA = 3;

    private FinderNavigation navigation;

    private static OnlineManagementClient managementClient = ManagementClientProvider.createOnlineManagementClient();
    private ResourceVerifier verifier;
    private Address address = Address.root();

    @Drone
    private WebDriver browser;

    @Page
    private WebServiceEndpointsPage wsePage;

    @Before
    public void before() throws Exception {
        File war = new File("src/test/resources/test.war");
        createDeployment().as(ZipExporter.class).exportTo((war), true);
        if (ConfigUtils.isDomain()) {

            address = address.and("host", "master").and("server", "server-one")
                    .and("deployment", "test.war").and("subsystem", "webservices").and("endpoint", "test%3ATestService");
            managementClient.execute("/profile=full/subsystem=webservices:write-attribute(name=statistics-enabled,value=true)");
            verifier =  new ResourceVerifier(address, managementClient);
            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .addAddress(FinderNames.HOST, "master")
                    .addAddress(FinderNames.SERVER, "server-one")
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Webservices");

            managementClient.apply(new DeployCommand.Builder(war).toAllGroups().name("test.war").build());
        } else {
            address = address.and("deployment", "test.war").and("subsystem", "webservices").and("endpoint", "test%3ATestService");

            verifier =  new ResourceVerifier(address, managementClient);
            managementClient.execute("/subsystem=webservices:write-attribute(name=statistics-enabled,value=true)");
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .addAddress(FinderNames.SERVER, FinderNames.STANDALONE_SERVER)
                    .addAddress(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Webservices");

            managementClient.apply(new DeployCommand.Builder(war).name("test.war").build());
        }
        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    @After
    public void after() throws Exception {
        if (ConfigUtils.isDomain()) {
            managementClient.apply(new UndeployCommand.Builder("test.war").fromAllGroups().build());
        } else {
            managementClient.apply(new UndeployCommand.Builder("test.war").build());
        }
        File war = new File("src/test/resources/test.war");
        war.delete();

        Administration administration = new Administration(managementClient);
        administration.reload();
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

        displayMBuiSubTree("subsystem");
        displayMBuiSubTree("webservices");
        displayMBuiSubTree("endpoint");
        selectItemInMBuiTree("test%3ATestService");

        checkAndAssertMBuiValueOf("Request count", "3");
        checkAndAssertMBuiValueOf("Response count", "3");
        checkAndAssertMBuiValueOf("Fault count", "0");
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

        displayMBuiSubTree("subsystem");
        displayMBuiSubTree("webservices");
        displayMBuiSubTree("endpoint");
        selectItemInMBuiTree("test%3ATestService");

        checkAndAssertMBuiValueOf("Request count", "1");
        checkAndAssertMBuiValueOf("Response count", "0");
        checkAndAssertMBuiValueOf("Fault count", "1");
    }

    @Test
    public void webServiceRequestsMetrics() throws IOException, OperationException {
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
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
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
        Service service = null;
        try {
            service = Service.create(wsdlURL, serviceName);
            final SimpleWebserviceEndpointIface port = service.getPort(SimpleWebserviceEndpointIface.class);
            port.echo("hello");
        } catch (WebServiceException | NullPointerException ex) {
        }
    }



    //Mbui utils
    private void displayMBuiSubTree(String label) {
        WebElement leftMenu = browser.findElement(By.className("split-west"));
        // (ByJQuery.selector("(tr td:contains(subsystem)).siblings(td:has(img))"));
        WebElement tree =  leftMenu.findElement(By.className("gwt-Tree"));
        tree.findElement(By.xpath("//tr[.//td/div[contains(text(),'" + label + "')]]/td/img")).click();
        Library.letsSleep(1000);
    }

    private void selectItemInMBuiTree(String name) {
        browser.findElement(ByJQuery.selector("div.gwt-TreeItem:contains(" + name + ")")).click();
    }

    private void checkAndAssertMBuiValueOf(String label, String expectedValue) {
        WebElement table = browser.findElement(By.className("fill-layout-width"));
        String value = table.findElement(By.xpath("//tr[.//td/div/div[contains(text(), '" + label + "')]]/td/div/span")).getText();
        assertEquals("Value of " + label + "is differrent in CLI and deployment MBUI table.", expectedValue, value);
        Library.letsSleep(1000);
    }
}



