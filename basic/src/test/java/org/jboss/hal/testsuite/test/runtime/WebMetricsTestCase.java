package org.jboss.hal.testsuite.test.runtime;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.runtime.UndertowServerMetricsPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.deployments.Deploy;
import org.wildfly.extras.creaper.commands.deployments.Undeploy;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * Test case for testing metrics of web (undertow) connectors.
 *
 * Real values are generated to see whether they are really updated in the web console.
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class WebMetricsTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebMetricsTestCase.class);

    private static final String DEPLOYMENT_NAME = WebMetricsTestCase.class.getSimpleName()+"_servlets";

    public static final String NUMBER_OF_REQUESTS = "Request Count";
    public static final String ERRORS = "Error Count";
    public static final String BYTES_SEND = "Bytes Send";
    public static final String BYTES_RECEIVED = "Bytes Received";

    private static final Address UNDERTOW_SUBSYSTEM_ADDR = Address.subsystem("undertow");
    private Address measuredHttpListenerAddr;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private static final Administration admin = new Administration(client);

    private ModelNode undertowStatsEnabledOrigValue;

    @Drone
    private WebDriver browser;

    @Page
    private UndertowServerMetricsPage undertowMetricsPage;

    @AfterClass
    public static void tearDown() {
        IOUtils.closeQuietly(client);
    }

    @Before
    public void before() throws IOException, TimeoutException, InterruptedException, CommandFailedException {
        if (ConfigUtils.isDomain()) {
            measuredHttpListenerAddr = Address.host(ConfigUtils.getDefaultHost())
                    .and("server", UndertowServerMetricsPage.MONITORED_SERVER);
        } else {
            measuredHttpListenerAddr = Address.root();
        }
        measuredHttpListenerAddr = measuredHttpListenerAddr.and("subsystem", "undertow")
                .and("server", "default-server")
                .and("http-listener", "default");
        WebArchive deployment = createDeployment();
        client.apply(new Deploy.Builder(deployment.as(ZipExporter.class).exportAsInputStream(), deployment.getName(), true).build());
        undertowStatsEnabledOrigValue = ops.readAttribute(UNDERTOW_SUBSYSTEM_ADDR, "statistics-enabled",
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS).value();
        ops.writeAttribute(UNDERTOW_SUBSYSTEM_ADDR, "statistics-enabled", true);
        admin.reloadIfRequired();
        undertowMetricsPage.navigate();
    }

    @After
    public void after() throws IOException, TimeoutException, InterruptedException, CommandFailedException {
        if (undertowStatsEnabledOrigValue != null) {
            ops.writeAttribute(UNDERTOW_SUBSYSTEM_ADDR, "statistics-enabled", undertowStatsEnabledOrigValue);
        }
        admin.reloadIfRequired();
        client.apply(new Undeploy.Builder(DEPLOYMENT_NAME + ".war").build());
    }

    private WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addClasses(UploadServlet.class, DataSenderServlet.class);
        return war;
    }

    @Test
    public void requestPerConnectorMetrics() throws IOException {

        // generate requests
        generateErrorRequests(10);
        generateValidRequests(20);


        undertowMetricsPage.refreshStats();

        // read statistics data from management model and from console
        long expectedRequestsCount = ops.readAttribute(measuredHttpListenerAddr, "request-count").longValue();
        long expectedErrorsCount = ops.readAttribute(measuredHttpListenerAddr, "error-count").longValue();

        MetricsAreaFragment rpcMetricsArea = undertowMetricsPage.getRequestPerConnectorMetricsArea();
        long errors = (long) rpcMetricsArea.getMetricNumber(ERRORS);
        long numberOfRequests = (long) rpcMetricsArea.getMetricNumber(NUMBER_OF_REQUESTS);

        assertEquals("Errors count should match tha value in model" + failedInDomainDueJbeap4688(), expectedErrorsCount, errors);
        assertEquals("Number of requests should match tha value in model" + failedInDomainDueJbeap4688(), expectedRequestsCount, numberOfRequests);
    }

    @Test
    public void dataSendReceivedPerConnectorMetrics() throws IOException, URISyntaxException {
        long amountOfDataToSend = 2L * 1024 * 1024;
        long amountOfDataToReceive = 4L * 1024 * 1024;

        // do some load
        generateBytesSentBySending(amountOfDataToSend);
        generageBytesReceivedBySending(amountOfDataToReceive);

        // refresh results
        undertowMetricsPage.refreshStats();

        // read statistics data from management model and from console
        long expectedBytesReceived = ops.readAttribute(measuredHttpListenerAddr, "bytes-received").longValue();
        long expectedBytesSent = ops.readAttribute(measuredHttpListenerAddr, "bytes-sent").longValue();

        MetricsAreaFragment rpcMetricsArea = undertowMetricsPage.getRequestPerConnectorMetricsArea();
        long bytesReceived = (long) rpcMetricsArea.getMetricNumber(BYTES_RECEIVED);
        long bytesSent = (long) rpcMetricsArea.getMetricNumber(BYTES_SEND);

        // verify that the values are the same as in model
        assertEquals("Bytes received should match tha value in model" + failedInDomainDueJbeap4688(), expectedBytesReceived, bytesReceived);
        assertEquals("Bytes send should match the value in model" + failedInDomainDueJbeap4688(), expectedBytesSent, bytesSent);
    }

    private String failedInDomainDueJbeap4688() {
        if (ConfigUtils.isDomain()) {
            return "; failed probably due https://issues.jboss.org/browse/JBEAP-4688";
        } else {
            return "";
        }
    }


    private URL deploymentBaseUrl() throws MalformedURLException {
        return new URL("http://127.0.0.1:8080/" + DEPLOYMENT_NAME);
    }


    private void generateBytesSentBySending(long numberOfBytes) throws IOException, URISyntaxException {
        HttpGet request = new HttpGet(deploymentBaseUrl().toExternalForm() + DataSenderServlet.URL_PATTERN + "?"
                + DataSenderServlet.AMOUNT_OF_DATA_IN_MB_ATTR + "=" + (numberOfBytes));
        log.info("Sending request to produce {} B: GET {}", numberOfBytes, request);

        try (final CloseableHttpClient client = HttpClientBuilder.create().build()) {
            final CloseableHttpResponse response = client.execute(request);
            Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        }
    }

    private void generageBytesReceivedBySending(long numberOfBytes) throws IOException {
        URL uploadUrl = new URL(deploymentBaseUrl() + UploadServlet.URL_PATTERN);
        sendNumOfMegaBytesToAsMultipart(uploadUrl, (numberOfBytes / (1024 * 1024)));
    }

    private void sendNumOfMegaBytesToAsMultipart(URL uploadUrl, long numOfMBsToSend) throws IOException {
        log.info("Sending {} MBs of data to upload servlet", numOfMBsToSend);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        byte[] dataFile = new byte[1024 * 1024]; // 1 MB file

        multipartEntityBuilder.addBinaryBody("images", dataFile, ContentType.create("application/octet-stream"),
                "big-file");

        final HttpEntity entity = multipartEntityBuilder.build();

        HttpPost request = new HttpPost(uploadUrl.toExternalForm());
        request.setEntity(entity);

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            for (int i = 0; i < numOfMBsToSend; i++) {
                if (numOfMBsToSend % 10 == 0) {
                    log.debug("Already send {} MB", i);
                }
                HttpResponse response = client.execute(request);
                Assert.assertEquals("Upload file should end successfully",
                        HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    private void generateErrorRequests(int amountOfRequests) throws IOException {
        HttpGet errorRequest = new HttpGet(deploymentBaseUrl().toExternalForm()
                + DataSenderServlet.URL_PATTERN + "?" + DataSenderServlet.AMOUNT_OF_DATA_IN_MB_ATTR + "=aaa"); // expected number, string will cause servlet failure => error request
        generateRequests(errorRequest, amountOfRequests);

    }

    private void generateValidRequests(int amountOfRequests) throws IOException {
        HttpGet validRequest = new HttpGet(deploymentBaseUrl().toExternalForm()
                + DataSenderServlet.URL_PATTERN + "?" + DataSenderServlet.AMOUNT_OF_DATA_IN_MB_ATTR + "=0");
        generateRequests(validRequest, amountOfRequests);
    }

    private void generateRequests(HttpGet request, int amount) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            for (int i = 0; i < amount; i++) {
                HttpResponse response = client.execute(request);
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }
}
