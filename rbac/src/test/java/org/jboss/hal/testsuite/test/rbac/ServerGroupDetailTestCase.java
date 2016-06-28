package org.jboss.hal.testsuite.test.rbac;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.domain.ServerGroupDetailPage;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Tests behaviour of server group detail page when accessed with a role with several different
 * privileges to multiple server groups.
 *
 * @author msmerek
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerGroupDetailTestCase {

    private FinderNavigation runtimePageNavigation;

    @Drone
    private WebDriver browser;

    @Page
    private ServerGroupDetailPage serverGroupDetailPage;

    @Test
    public void buttonsVisibilityIsChangedForDifferentPrivileges() {
        Authentication.with(browser).authenticate(RbacRole.MAIN_ADMINISTRATOR_OTHER_MONITOR);

        startOnRuntimePage();
        goToServerGroupDetailPage("other-server-group");

        checkVisibilityOfButtons(false);

        goBackToRuntimePage();
        goToServerGroupDetailPage("main-server-group");

        checkVisibilityOfButtons(true);
    }

    // test utils

    private void startOnRuntimePage() {
        runtimePageNavigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class);
    }

    private void goToServerGroupDetailPage(String group) {
        runtimePageNavigation.step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP, group);

        runtimePageNavigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    private void goBackToRuntimePage() {
        serverGroupDetailPage.clickOnBackLink();
        runtimePageNavigation.resetNavigation();
    }

    private void checkVisibilityOfButtons(boolean expectVisible) {
        serverGroupDetailPage.switchToSystemProperties();
        checkButtonVisibility("Add", expectVisible);
        checkButtonVisibility("Remove", expectVisible);

        serverGroupDetailPage.switchToJVMConfiguration();
        checkButtonVisibility("Clear", expectVisible);
    }

    private void checkButtonVisibility(String buttonLabel, boolean expectVisible) {
        boolean actuallyVisible = serverGroupDetailPage.isButtonVisible(buttonLabel);
        assertThat("Incorrectly set visibility for button \'" + buttonLabel + "\'.",
                actuallyVisible,
                is(expectVisible));
    }

}



