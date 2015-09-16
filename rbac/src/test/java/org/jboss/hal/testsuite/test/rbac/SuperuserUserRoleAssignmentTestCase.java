package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Created by pcyprian on 16.9.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SuperuserUserRoleAssignmentTestCase {
    private static final String NAME = RandomStringUtils.randomAlphanumeric(5);
    private static final String REALM = RandomStringUtils.randomAlphanumeric(5);
    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;

    @Before
    public void setUp() {
        Authentication.with(browser).authenticate(RbacRole.SUPERUSER);
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class);
    }
}
