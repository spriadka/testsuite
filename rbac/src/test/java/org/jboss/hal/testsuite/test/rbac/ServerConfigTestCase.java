package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertEquals;

/**
 * Created by pcyprian on 15.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerConfigTestCase {

    private FinderNavigation navigation;

    private ResourceAddress address;
    static Dispatcher dispatcher;
    static ResourceVerifier verifier;

    @BeforeClass
    public static void beforeClass() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void afterClass() {
        dispatcher.close();
    }

    @Drone
    private WebDriver browser;


    @Test
    public void administrator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        addServerConfig("master", "main-server-group", true, "serv_" + RandomStringUtils.randomAlphanumeric(5));
        addServerConfig("master", "other-server-group", true, "serv_" + RandomStringUtils.randomAlphanumeric(5));
        addServerConfig("slave", "main-server-group", true, "serv_" + RandomStringUtils.randomAlphanumeric(5));

        checkStandardButtons(true, "master");
        checkStandardButtons(true, "slave");
        checkRemoveButtonForServer("server-one", "master", true);
        checkRemoveButtonForServer("server-three", "master", true);
        checkRemoveButtonForServer("server-one", "slave", true);
    }


    @Test
    public void monitor() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);
        checkStandardButtons(false, "master");
        checkStandardButtons(false, "slave");

        checkRemoveButtonForServer("server-one", "master", false);
        checkRemoveButtonForServer("server-four", "master", false);
        checkRemoveButtonForServer("server-one", "slave", false);
        checkRemoveButtonForServer("server-two", "slave", false);
        containsHost("slave", true);
    }

    @Test
    public void mainAdministrator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAIN_ADMINISTRATOR);
        addServerConfig("master", "main-server-group", true, "serv_" + RandomStringUtils.randomAlphanumeric(5));
        addServerConfig("master", "other-server-group", false, "serv_" + RandomStringUtils.randomAlphanumeric(5));
        addServerConfig("slave", "main-server-group", true, "serv_" + RandomStringUtils.randomAlphanumeric(5));

        checkStandardButtons(true, "master");
        checkStandardButtons(true, "slave");
        checkRemoveButtonForServer("server-one", "master", true);
        checkRemoveButtonForServer("server-one", "slave", true);
    }

    @Test
    public void mainMonitor() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAIN_MONITOR);
        checkStandardButtons(false, "master");
        checkStandardButtons(false, "slave");
        checkRemoveButtonForServer("server-one", "master", false);
        checkRemoveButtonForServer("server-one", "slave", false);
    }

    @Test
    public void hostMasterAdministrator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_ADMINISTRATOR);
        addServerConfig("master", "main-server-group", true, "serv_" + RandomStringUtils.randomAlphanumeric(5));
        addServerConfig("master", "other-server-group", true, "serv_" + RandomStringUtils.randomAlphanumeric(5));

        checkStandardButtons(true, "master");
        containsHost("slave", false);
        checkRemoveButtonForServer("server-one", "master", true);
        checkRemoveButtonForServer("server-three", "master", true);
    }

    @Test
    public void hostMasterMonitor() {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_MONITOR);
        checkStandardButtons(false, "master");
        containsHost("slave", false);
        checkRemoveButtonForServer("server-one", "master", false);
    }

    // Test utils

    public void checkStandardButtons(boolean visible, String host) {
        boolean unvisible = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, host)
                .addAddress("Server");

        try {
            navigation.selectColumn().invoke("Add");
        } catch (NoSuchElementException ex) {
            unvisible = true;
        }

        assertEquals("Problem with visibility of buttons add and remove.", unvisible, !visible);
    }

    private void checkRemoveButtonForServer(String server, String host, boolean visible) {
        boolean unvisible = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, host)
                .addAddress(FinderNames.SERVER, server);

        try {
            navigation.selectRow().invoke("Remove");
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickButton("Cancel");
        } catch (NoSuchElementException ex) {
            unvisible = true;
        } catch (TimeoutException ignored) {
        }

        assertEquals("Problem with visibility of buttons add and remove. ", unvisible, !visible);
    }

    public void containsHost(String host, boolean visible) {
        boolean unvisible = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, host);

        try {
            navigation.selectRow();
        } catch (org.openqa.selenium.TimeoutException ex) {
            unvisible = true;
        }

        assertEquals("Problem with visibility of host " + host, unvisible, !visible);
    }


    public void  addServerConfig(String host, String serverGroup, boolean shouldSucceed, String name) {
        boolean missingField = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, host)
                .addAddress("Server");

        navigation.selectColumn().invoke("Add");

        try {
            getWindowFragment().getEditor().text("name", name);
            getWindowFragment().getEditor().select("group", serverGroup);
            getWindowFragment().getEditor().text("portOffset", "10");
            getWindowFragment().getEditor().checkbox("autoStart", false);
            getWindowFragment().clickButton("Save");
        }  catch (NoSuchElementException ex) {
            missingField = true;
            getWindowFragment().clickButton("Cancel");
        }

        assertEquals("Missing some field in creation window.", missingField, !shouldSucceed);
        address = new ResourceAddress(new ModelNode("/host=" + host + "/server=" + name));

        if (shouldSucceed) {
            verifier.verifyResource(address, 400);

            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .addAddress(FinderNames.HOST, host)
                    .addAddress("Server", name);

            navigation.selectRow().invoke("Remove");

            try {
                Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
            } catch (TimeoutException ignored) {
            }

            Library.letsSleep(1500);
            verifier.verifyResource(address, false);
        }
    }

    private ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

}
