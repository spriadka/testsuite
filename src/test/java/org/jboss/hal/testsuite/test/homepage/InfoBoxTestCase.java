package org.jboss.hal.testsuite.test.homepage;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.homepage.HomepageInfoFragment;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.admin.AdminEntryPoint;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.page.domain.DomainEntryPoint;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
import org.jboss.hal.testsuite.test.category.Domain;
import org.jboss.hal.testsuite.test.category.Shared;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

/**
 * @author jcechace
 */
@RunWith(Arquillian.class)
public class InfoBoxTestCase {
    @Drone
    private WebDriver browser;

    @Page
    private HomePage homePage;

    @Before
    public void setup() {
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
    }

    //
    // Shared tests
    //

    @Test @Category(Shared.class)
    public void adminInfo() {
        String id = PropUtils.get("homepage.info.admin.id");
        assertInfoIsPresent(id);
        assertLinkInInfo(id, "admin", AdminEntryPoint.class);
    }

    @Test @Category(Shared.class)
    public void configInfo() {
        Class<? extends BasePage> page = ConfigUtils.isDomain() ? DomainConfigEntryPoint.class
                                                                : StandaloneConfigEntryPoint.class;

        String id = ConfigUtils.isDomain() ? PropUtils.get("homepage.info.domain.config.id")
                                           : PropUtils.get("homepage.info.config.id");

        assertInfoIsPresent(id);
        assertLinkInInfo(id, "config", page);
    }

    @Test @Category(Shared.class)
    public void runtimeInfo() {
        Class<? extends BasePage> page = ConfigUtils.isDomain() ? DomainRuntimeEntryPoint.class
                                                                : StandaloneRuntimeEntryPoint.class;

        String id = ConfigUtils.isDomain()  ? PropUtils.get("homepage.info.domain.runtime.id")
                                            : PropUtils.get("homepage.info.runtime.id");

        assertInfoIsPresent(id);
        assertLinkInInfo(id, "runtime", page);
    }

    //
    // Domain only test
    //
    @Test @Category(Domain.class)
    public void domainInfo() {
        String id = PropUtils.get("homepage.info.domain.id");

        assertInfoIsPresent(id);
        assertLinkInInfo(id, "domain", DomainEntryPoint.class);
    }

    //
    // Util methods
    //

    private void assertInfoIsPresent(String id) {
        try {
            homePage.getInfoBox(id);
        } catch (NoSuchElementException e) {
            Assert.fail("Unable to find info box " + id);
        }
    }


    private void assertLinkInInfo(String id, String sectionKey, Class<? extends BasePage> page) {
        String key = "homepage.info." + sectionKey + ".link.label";
        String label = PropUtils.get(key);

        HomepageInfoFragment task = homePage.getInfoBox(id);

        String link = ConfigUtils.getPageLocation(page);
        try {
            task.getLink(label, link);
        } catch (NoSuchElementException e) {
            Assert.fail("Unable to find link " + label + "[" + link + "] in info box " + id);
        }
    }

}
