package org.jboss.hal.testsuite.test.rbac;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;


import java.util.NoSuchElementException;

/**
 * Created by pcyprian on 2.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerAvailabilityTestCase {

    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES);
    }

    @Test
    public void availableServersMainGroup() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAIN_ADMINISTRATOR);
        checkAvailableServer("server-one", "master", true);
        checkAvailableServer("server-two", "master", true);
        checkAvailableServer("server-three", "master", false);
        checkAvailableServer("server-one", "slave", true);
        checkAvailableServer("server-two", "slave", false);
    }

    @Test
    public void availableServersOtherGroup() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.OTHER_ADMINISTRATOR);
        checkAvailableServer("server-one", "master",  false);
        checkAvailableServer("server-two", "master", false);
        checkAvailableServer("server-three", "master", true);
        checkAvailableServer("server-one", "slave", false);
        checkAvailableServer("server-two", "slave", false);
    }

    @Test
    public void availableServersMaster() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_ADMINISTRATOR);
        checkAvailableServer("server-one", "master", true);
        checkAvailableServer("server-two", "master", true);
        checkAvailableServer("server-three", "master", true);
        checkAvailableServer("server-one", "slave", false);
        checkAvailableServer("server-two", "slave", false);
    }

    @Test
    public void availableServersSlave() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.HOST_SLAVE_ADMINISTRATOR);
        checkAvailableServer("server-one", "master", false);
        checkAvailableServer("server-two", "master", false);
        checkAvailableServer("server-three", "master", false);
        checkAvailableServer("server-one", "slave", true);
        checkAvailableServer("server-two", "slave", true);

    }

    @Test
    public void availableServersUnscoped()throws Exception {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        checkAvailableServer("server-one", "master",  true);
        checkAvailableServer("server-two", "master", true);
        checkAvailableServer("server-three", "master", true);
        checkAvailableServer("server-one", "slave", true);
        checkAvailableServer("server-two", "slave", true);
    }

    public void checkAvailableServer(String server, String host, boolean shouldBePresent) {
        navigation.clearNavigation();
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS);
        navigation.addAddress(FinderNames.HOST, host)
                .addAddress(FinderNames.SERVER, server);
        try {
            navigation.selectRow();
        } catch (NoSuchElementException | TimeoutException ex) {
            if (shouldBePresent) Assert.fail();
        }
    }
}
