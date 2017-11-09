package org.jboss.hal.testsuite.test.rbac;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.util.RbacRole.ADMINISTRATOR;
import static org.jboss.hal.testsuite.util.RbacRole.AUDITOR;
import static org.jboss.hal.testsuite.util.RbacRole.DEPLOYER;
import static org.jboss.hal.testsuite.util.RbacRole.MAINTAINER;
import static org.jboss.hal.testsuite.util.RbacRole.MONITOR;
import static org.jboss.hal.testsuite.util.RbacRole.OPERATOR;
import static org.jboss.hal.testsuite.util.RbacRole.SUPERUSER;
import static org.junit.Assert.fail;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class RunAsTestCase {

    @Drone
    public WebDriver browser;

    @Page
    public HomePage page;

    @Test
    public void testAsOperator() {
        runAsAvailable(OPERATOR, false);
    }

    @Test
    public void testAsMonitor() {
        runAsAvailable(MONITOR, false);
    }

    @Test
    public void testAsMaintainer() {
        runAsAvailable(MAINTAINER, false);
    }

    @Test
    public void testAsAdministrator() {
        runAsAvailable(ADMINISTRATOR, false);
    }

    @Test
    public void testAsAuditor() {
        runAsAvailable(AUDITOR, false);
    }

    @Test
    public void testAsDeployer() {
        runAsAvailable(DEPLOYER, false);
    }

    @Test
    public void testAsSuperuser() {
        runAsAvailable(SUPERUSER, true);
    }

    public void runAsAvailable(RbacRole role, boolean available) {
        Authentication.with(browser).authenticate(role);
        Console.withBrowser(browser).refreshAndNavigate(HomePage.class);
        try {
            Console.withBrowser(browser).getUserFragment().openMenu().clickMenuItem("Run as");
            if (!available) {
                fail("\"Run as\" tools should not be available for role " + role);
            }
            String title = Console.withBrowser(browser).openedWindow().getHeadTitle();
            Assert.assertTrue("\"Run as\" popup windows expected", title.contains("Run as"));
        } catch (NoSuchElementException exc) {
            if (available) {
                fail("\"Run as\" tools should be available " + role);
            }
        } finally {
            Console.withBrowser(browser).refresh();
        }
    }
}
