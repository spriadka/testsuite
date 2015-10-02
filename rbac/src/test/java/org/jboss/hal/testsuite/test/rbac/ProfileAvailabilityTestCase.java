package org.jboss.hal.testsuite.test.rbac;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.util.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

/**
 * Created by pcyprian on 1.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ProfileAvailabilityTestCase {

    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES);
    }

    @Test
    public void availableProfilesMainGroup() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAIN_ADMINISTRATOR);
        pickProfile("default", true);
        pickProfile("full", true);
        pickProfile("ha", false);
        pickProfile("full-ha", false);
    }

    @Test
    public void availableProfilesOtherGroup() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.OTHER_ADMINISTRATOR);
        pickProfile("default", true);
        pickProfile("full-ha", true);
        pickProfile("full", false);
        pickProfile("ha", false);
    }

    @Test
    public void availableProfilesMaster() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_ADMINISTRATOR);
        pickProfile("default", true);
        pickProfile("full", true);
        pickProfile("ha", true);
        pickProfile("full-ha", true);
    }

    @Test
    public void availableProfilesSlave() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.HOST_SLAVE_ADMINISTRATOR);
        pickProfile("default", true);
        pickProfile("full", true);
        pickProfile("ha", true);
        pickProfile("full-ha", true);
    }

    @Test
    public void availableProfilesUnscoped()throws Exception {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        pickProfile("default", true);
        pickProfile("full", true);
        pickProfile("ha", true);
        pickProfile("full-ha", true);
    }

    private void pickProfile(String profile, boolean shouldBePresent) {
        navigation.clearNavigation();
        navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES);
        navigation.addAddress(FinderNames.PROFILE, profile);
        try {
            navigation.selectRow(true);
        } catch (NoSuchElementException | TimeoutException ex) {
            if (shouldBePresent) Assert.fail();
        }
    }
}
