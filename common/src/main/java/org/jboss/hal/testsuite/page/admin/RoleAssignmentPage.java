package org.jboss.hal.testsuite.page.admin;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.util.Console;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author jcechace
 */
@Location("#rbac")
public class RoleAssignmentPage extends BasePage {
    private FinderNavigation navigation;

    public void createGroup(String name, String realm) {
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .addAddress(FinderNames.BROWSE_BY, "Groups")
                .addAddress("Group");
        navigation.selectColumn().invoke("Add");

        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("realm", realm);
        getWindowFragment().clickButton("Save");
    }

    public void removeGroup(String name, String realm) {
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .addAddress(FinderNames.BROWSE_BY, "Groups")
                .addAddress("Group", name + "@" + realm);
        navigation.selectRow().invoke("Remove");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    public void addUser(String name, String realm) {
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .addAddress(FinderNames.BROWSE_BY, "Users")
                .addAddress("User");
        navigation.selectColumn().invoke("Add");

        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("realm", realm);
        getWindowFragment().clickButton("Save");
    }

    public void removeUser(String name, String realm) {
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .addAddress(FinderNames.BROWSE_BY, "Users")
                .addAddress("User", name + "@" + realm);
        navigation.selectRow().invoke("Remove");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    public ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }
}
