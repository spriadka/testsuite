package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.domain.ServersRunningStateBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertEquals;

/**
 * Created by pcyprian on 16.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerManagementTestCase {

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private ServersRunningStateBackup serversRunningStateBackup = new ServersRunningStateBackup();
    private RBACDomainOperations ops = new RBACDomainOperations(client);

    @Before
    public void before() throws CommandFailedException {
        client.apply(serversRunningStateBackup.backup());
    }

    @After
    public void after() throws CommandFailedException {
        client.apply(serversRunningStateBackup.restore());
    }

    @AfterClass
    public static void afterClass() {
        IOUtils.closeQuietly(client);
    }

    @Drone
    private WebDriver browser;


    @Test
    public void monitor() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);

        testNotAbleToSeeChangeServerStateButton("master", "server-one");
        testNotAbleToSeeChangeServerStateButton("slave", "server-one");
    }

    @Test // https://issues.jboss.org/browse/HAL-909
    public void operator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.OPERATOR);

        testCanChangeServerState("master", "server-one");
        testCanChangeServerState("master", "server-three");
        testCanChangeServerState("slave", "server-one");
    }

    @Test // https://issues.jboss.org/browse/HAL-909
    public void mainOperator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAIN_OPERATOR);

        testCanChangeServerState("master", "server-one");
        testNotAbleToSeeServer("master", "server-three");
        testCanChangeServerState("slave", "server-one");
    }

    @Test
    public void hostMasterOperator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_OPERATOR);

        testCanChangeServerState("master", "server-one");
        testCanChangeServerState("master", "server-three");
        testNotAbleToSeeServer("slave", "server-one");
    }

    private void testCanChangeServerState(String host, String server) throws Exception {
        testStopAndStart(host, server, true, true);
    }

    private void testNotAbleToSeeChangeServerStateButton(String host, String server) throws Exception {
       testStopAndStart(host, server, true, false);
    }

    private void testNotAbleToSeeServer(String host, String server) throws Exception {
        testStopAndStart(host, server, false, false);
     }

    private void testStopAndStart(String host, String server, boolean serverShouldBeVisible,
            boolean buttonShouldBeVisible) throws Exception {
        if (ops.isServerRunning(host, server)) {
            changeServerState(host, server, ServerState.STOPPED, serverShouldBeVisible, buttonShouldBeVisible);
            changeServerState(host, server, ServerState.RUNNING, serverShouldBeVisible, buttonShouldBeVisible);
        } else {
            changeServerState(host, server, ServerState.RUNNING, serverShouldBeVisible, buttonShouldBeVisible);
            changeServerState(host, server, ServerState.STOPPED, serverShouldBeVisible, buttonShouldBeVisible);
        }
    }

    private void changeServerState(String host, String server, ServerState serverState, boolean serverShouldBeVisible,
            boolean buttonShouldBeVisible) throws Exception {
        boolean buttonVisibility = true;
        boolean serverVisibility = true;
        FinderNavigation navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class, 100)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, host)
                .addAddress(FinderNames.SERVER, server);

        try {
           navigation.selectRow().invoke(serverState.uiOption);
           Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm().assertClosed();
           Console.withBrowser(browser).waitUntilLoaded();
        } catch (NoSuchElementException ex) {
            buttonVisibility = false;
        } catch (TimeoutException ex) {
            serverVisibility = false;
        }

        assertEquals("Server '" + server + "' of host '" + host + "' visibility was '" + serverVisibility
                + "' but should be '" + serverShouldBeVisible + "'.", serverShouldBeVisible, serverVisibility);

        if (serverShouldBeVisible) {
            assertEquals("Missing " + serverState.uiOption + " option for server '" + server + "' of host '" + host
                    + "'.", buttonShouldBeVisible, buttonVisibility);

            if (buttonShouldBeVisible) {
                Address address = ops.getServerAddress(host, server);
                new ResourceVerifier(address, client, ResourceVerifier.LONG_TIMEOUT)
                    .verifyAttribute(Constants.SERVER_STATE, serverState.dmrAttrValue);
            }
        }

    }

    private enum ServerState {
        RUNNING("Start", "running"), STOPPED("Stop", "STOPPED");

        String uiOption, dmrAttrValue;

        ServerState(String uiOption, String dmrAttrValue) {
            this.uiOption = uiOption;
            this.dmrAttrValue = dmrAttrValue;
        }
    }

}
