package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Created by pcyprian on 15.9.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class AdministratorUserRoleAssignmentTestCase {
    private static final String NAME = RandomStringUtils.randomAlphanumeric(5);
    private static final String REALM = RandomStringUtils.randomAlphanumeric(5);
    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;

    @Before
    public void setUp() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class);
    }

    @Test
    public void createRoleAssignment() {
        page.addUser(NAME, REALM);

        navigation.addAddress(FinderNames.BROWSE_BY, "Groups")
                .addAddress("User", NAME + "@" + REALM);
        //page.removeUser(NAME, REALM);
    }

}
