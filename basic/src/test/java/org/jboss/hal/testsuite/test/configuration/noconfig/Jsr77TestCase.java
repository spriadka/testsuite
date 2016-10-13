package org.jboss.hal.testsuite.test.configuration.noconfig;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class Jsr77TestCase {

    @Drone
    private WebDriver browser;

    @Test
    public void verifyJsr77SubsystemIsPresent() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .step(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .step(FinderNames.PROFILE, ConfigUtils.getDefaultProfile());
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS);
        }
        navigation.step(FinderNames.SUBSYSTEM, "Jsr77");
        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }

}
