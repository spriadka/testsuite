package org.jboss.hal.testsuite.test.rbac;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Domain;
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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by pcyprian on 15.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerGroupTestCase {

    private FinderNavigation navigation;

    private ResourceAddress address;
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);

    @Drone
    private WebDriver browser;

    @Test
    public void administrator() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        checkStandardButtons(true);
        checkRemoveButtonForGroup("main-server-group", true);
        checkRemoveButtonForGroup("mainmaster-server-group", true);
        checkRemoveButtonForGroup("other-server-group", true);
        addServerGroup(true);
    }

    @Test
    public void monitor() {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
        checkRemoveButtonForGroup("mainmaster-server-group", false);
        checkRemoveButtonForGroup("other-server-group", false);
    }

    @Test // https://issues.jboss.org/browse/HAL-908
    public void mainAdministrator() {
        Authentication.with(browser).authenticate(RbacRole.MAIN_ADMINISTRATOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
    }


    @Test
    public void mainMonitor() {
        Authentication.with(browser).authenticate(RbacRole.MAIN_MONITOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
    }

    @Test
    public void hostMasterAdministrator() {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_ADMINISTRATOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
        checkRemoveButtonForGroup("mainmaster-server-group", false);
        checkRemoveButtonForGroup("other-server-group", false);

    }



    // test utils

    public void checkStandardButtons(boolean visible) {
        boolean unvisible = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .addAddress(FinderNames.SERVER_GROUP);

        try {
            navigation.selectColumn().invoke("Add");
        } catch (NoSuchElementException ex) {
            unvisible = true;
        }
        assertEquals("Problem with visibility of buttons add and remove.", unvisible, !visible);
    }

    private void checkRemoveButtonForGroup(String group, boolean visible) {
        boolean unvisible = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .addAddress(FinderNames.SERVER_GROUP, group);

        try {
            navigation.selectRow().invoke("Remove");
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickButton("Cancel");
        } catch (NoSuchElementException ex) {
             unvisible = true;
        } catch (TimeoutException ignored) {
        }

        assertEquals("Problem with visibility of buttons add and remove. ", unvisible, !visible);
    }

    public void addServerGroup(boolean shouldSucceed) {
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .addAddress(FinderNames.SERVER_GROUP);

        navigation.selectColumn().invoke("Add");

        getWindowFragment().getEditor().text("name", "my-server-group");
        getWindowFragment().getEditor().select("profileName", "default");
        getWindowFragment().getEditor().select("socketBinding", "standard-sockets");
        getWindowFragment().clickButton("Save");

        address = new ResourceAddress(new ModelNode("/server-group=my-server-group"));

        verifier.verifyResource(address, shouldSucceed);

        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .addAddress(FinderNames.SERVER_GROUP, "my-server-group");

        navigation.selectRow().invoke("Remove");

        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }

        verifier.verifyResource(address, false);
    }

    private ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }
}



