package org.jboss.hal.testsuite.test.runtime.server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.UndeployCommand;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.deployments.Deploy;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.DomainAdministration;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;


@RunWith(Arquillian.class)
@RunAsClient
@Category(Domain.class)
public class ForceShutdownOperationTestCase {

    private static final String GROUP = "group";
    private static final String SERVER_GROUP = "server-group";
    private static final String SOCKET_BINDING_GROUP = "socket-binding-group";
    private static final String PROFILE = "profile";
    private static final String SERVER = "server";
    private static final String SERVER_CONFIG = "server-config";
    private static final String SERVER_STATE = "server-state";

    private static final String defaultHost = ConfigUtils.getDefaultHost();
    private static final String defaultProfile = ConfigUtils.getDefaultProfile();

    private static final String testServerGroup = "test_group_" + RandomStringUtils.randomAlphanumeric(7);
    private static final String testServerName = "test_server_" + RandomStringUtils.randomAlphanumeric(7);

    private static final Address hostAddress = Address.host(defaultHost);
    private static final Address serverGroupAddress = Address.root().and(SERVER_GROUP, testServerGroup);
    private static final Address serverAddress = hostAddress.and(SERVER, testServerName);
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final DomainAdministration domainAdministration = new DomainAdministration(client);

    private static final String socketBindingGroup = "full-ha-sockets";

    private static final int DEFAULT_PORT = 8080;
    private static final int SERVER_PORT = AvailablePortFinder.getNextAvailablePort(DEFAULT_PORT);

    private static final String FORCE_SHUTDOWN = "Force Shutdown";

    private static final String DEPLOYMENT_NAME = "infinity_servlet_" + RandomStringUtils.randomAlphanumeric(7);
    private static final WebArchive DEPLOYMENT_ARCHIVE = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
            .addClass(TextResponseServlet.class);

    @Drone
    private WebDriver browser;

    private static final Logger LOGGER = Logger.getLogger(ForceShutdownOperationTestCase.class);


    @BeforeClass
    public static void setUp() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        operations.add(serverGroupAddress, Values.of(PROFILE, defaultProfile).and(SOCKET_BINDING_GROUP, socketBindingGroup));
        operations.add(hostAddress.and(SERVER_CONFIG, testServerName), Values.of(GROUP, testServerGroup)
                .and("socket-binding-port-offset", SERVER_PORT - DEFAULT_PORT));
        client.apply(new Deploy.Builder(DEPLOYMENT_ARCHIVE.as(ZipExporter.class)
                .exportAsInputStream(),
                DEPLOYMENT_NAME + ".war",
                true).toServerGroups(testServerGroup).build());
        domainAdministration.reloadIfRequired();
    }

    @Before
    public void before() throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        domainAdministration.startServer(testServerName);
        domainAdministration.waitUntilServersRunning(Collections.singletonList(testServerName));
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException, InterruptedException, IOException, TimeoutException, OperationException {
        try {
            client.apply(new UndeployCommand.Builder(DEPLOYMENT_NAME + ".war").particularGroup(testServerGroup).build());
            domainAdministration.shutdownServer(defaultHost, testServerName);
            domainAdministration.removeServer(defaultHost, testServerName);
            operations.removeIfExists(serverGroupAddress);
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    /**
     * @tpTestDetails Navigate to domain runtime entry point, select test server by server-group
     * and try to force shutdown test server in Web Console.
     * Validate server activity is stopped in the model
     */
    @Test
    @RunAsClient
    public void testForceShutdownByServerGroup() throws Exception {
        try {
            new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                    .step(FinderNames.SERVER_GROUP, testServerGroup)
                    .step(FinderNames.SERVER, testServerName)
                    .selectRow().invoke(FORCE_SHUTDOWN);
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
            Console.withBrowser(browser).waitUntilLoaded();
            new ResourceVerifier(serverAddress, client).verifyAttribute(SERVER_STATE, "STOPPED");
        } finally {
            domainAdministration.reloadIfRequired();
        }
    }


    /**
     * @tpTestDetails Navigate to domain runtime entry point, select test server by host
     * and try to force shutdown test server in Web Console.
     * Validate server activity is stopped in the model
     */
    @Test
    @RunAsClient
    public void testForceShutdownByHost() throws Exception {
        try {
            new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .step(FinderNames.HOST, defaultHost)
                    .step(FinderNames.SERVER, testServerName)
                    .selectRow().invoke(FORCE_SHUTDOWN);
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
            Console.withBrowser(browser).waitUntilLoaded();
            new ResourceVerifier(serverAddress, client).verifyAttribute(SERVER_STATE, "STOPPED");
        } finally {
            domainAdministration.reloadIfRequired();
        }
    }


    /**
     * @tpTestDetails Create deployment associated with test server in the model.
     * Load the server by creating significantly big amount of requests.
     * Navigate to domain runtime entry point, select test server by host
     * and try to force shutdown test server in load via Web Console.
     * Validate server activity is stopped in the model
     */
    @Test
    public void testForceShutdownInLoad() throws Exception {
        Runnable loadServerTask = this::loadServer;
        Thread task = new Thread(loadServerTask);
        try {
            task.start();
            new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .step(FinderNames.HOST, defaultHost)
                    .step(FinderNames.SERVER, testServerName)
                    .selectRow().invoke(FORCE_SHUTDOWN);
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
            Console.withBrowser(browser).waitUntilLoaded();
            new ResourceVerifier(serverAddress, client).verifyAttribute(SERVER_STATE, "STOPPED");
        } finally {
            task.interrupt();
        }
    }


    private void loadServer() {
        final String serverUrl = String.format("http://127.0.0.1:%d/%s%s", SERVER_PORT,
                DEPLOYMENT_NAME, TextResponseServlet.URL_PATTERN);
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(100);
                HttpGet httpGet = new HttpGet(serverUrl);
                CloseableHttpResponse response = client.execute(httpGet);
                LOGGER.debug(response.getStatusLine().getStatusCode());
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.debug(e);
        }
    }
}

