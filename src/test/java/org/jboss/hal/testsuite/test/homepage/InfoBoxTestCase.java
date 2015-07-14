package org.jboss.hal.testsuite.test.homepage;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.homepage.HomepageInfoFragment;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.page.runtime.DeploymentPage;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentPage;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.page.runtime.StandaloneRuntimeEntryPoint;
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
@Category(Shared.class)
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

    @Test
    public void accessControl() {
        String id = PropUtils.get("homepage.info.access.control.id");
        assertInfoIsPresent(id);
        assertLinkInInfo(id, "access.control", RoleAssignmentPage.class);
    }

    @Test
    public void configInfo() {
        Class<? extends BasePage> page = ConfigUtils.isDomain() ? DomainConfigEntryPoint.class
                                                                : StandaloneConfigEntryPoint.class;

        String id = ConfigUtils.isDomain() ? PropUtils.get("homepage.info.domain.config.id")
                                           : PropUtils.get("homepage.info.config.id");

        assertInfoIsPresent(id);
        assertLinkInInfo(id, "config", page);
    }

    @Test
    public void runtimeInfo() {
        Class<? extends BasePage> page = ConfigUtils.isDomain() ? DomainRuntimeEntryPoint.class
                                                                : StandaloneRuntimeEntryPoint.class;

        String id = ConfigUtils.isDomain()  ? PropUtils.get("homepage.info.domain.runtime.id")
                                            : PropUtils.get("homepage.info.runtime.id");

        assertInfoIsPresent(id);
        assertLinkInInfo(id, "runtime", page);
    }

    @Test
    public void deploymentsInfo(){
        Class <? extends BasePage> page = ConfigUtils.isDomain() ? DomainDeploymentPage.class
                                                                 : DeploymentPage.class;

        String id = ConfigUtils.isDomain() ? PropUtils.get("homepage.info.domain.deployments.id")
                                           : PropUtils.get("homepage.info.deployments.id");

        assertInfoIsPresent(id);
        assertLinkInInfo(id, "deployments", page);
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
