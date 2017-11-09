package org.jboss.hal.testsuite.test.rbac;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertFalse;

/**
 * Created by pcyprian on 2.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ScopedRoleTest {
    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;

    @Test
    public void typeIsReadOnlyInEditMode() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .step(FinderNames.BROWSE_BY, "Roles")
                .step("Role", "main-Monitor");

        navigation.selectRow().invoke("Edit");

        WebElement type = browser.findElement(ByJQuery.selector("#form-gwt-uid-31_type"));

        assertFalse("Scope type is expected to be read-only.", type.isEnabled());

        page.getWindowFragment().clickButton("Cancel");
    }

}


