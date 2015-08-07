package org.jboss.hal.testsuite.test.homepage;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.fragment.homepage.HomepageTaskFragment;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.page.runtime.DeploymentPage;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentPage;
import org.jboss.hal.testsuite.page.runtime.HostsPage;
import org.jboss.hal.testsuite.page.runtime.PatchManagementPage;
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
public class TaskBoxTestCase {
    @Drone
    private WebDriver browser;

    @Page
    private HomePage homePage;

    @Before
    public void setup() {
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded().maximizeWindow();
    }

    //
    // Shared tests
    //

    @Test @Category(Shared.class)
    public void datasourceTask() {
        String id = PropUtils.get("homepage.task.datasource.id");

        assertTaskIsPresent(id);
        assertLinkInTask(id, "datasource", ConfigUtils.isDomain() ? DomainConfigurationPage.class : StandaloneConfigurationPage.class);
    }

    @Test @Category(Shared.class)
    public void patchingTask() {
        String id = PropUtils.get("homepage.task.patch.id");

        assertTaskIsPresent(id);
        assertLinkInTask(id, "patch", PatchManagementPage.class);
    }

    @Test @Category(Shared.class)
    public void adminTask() {
        String id = PropUtils.get("homepage.task.admin.id");

        assertTaskIsPresent(id);
        assertLinkInTask(id, "admin", RoleAssignmentPage.class);
    }

    @Test @Category(Shared.class)
    public void deploymentTask() {
        Class<? extends BasePage> page =
                ConfigUtils.isDomain() ? DomainDeploymentPage.class : DeploymentPage.class;

        String id = PropUtils.get("homepage.task.deployment.id");

        assertTaskIsPresent(id);
        assertLinkInTask(id, "deployment", page);
    }

    //
    // Domain only tests
    //

    @Test @Category(Domain.class)
    public void topologyTask() {
        String id = PropUtils.get("homepage.task.topology.id");

        assertTaskIsPresent(id);
        assertLinkInTask(id, "topology", HostsPage.class);
    }

    @Test @Category(Domain.class)
    public void serverGroupTask() {
        String id = PropUtils.get("homepage.task.server-group.id");

        assertTaskIsPresent(id);
        assertLinkInTask(id, "server-group", HostsPage.class);
    }

    //
    // Util methods
    //
    private void assertTaskIsPresent(String id) {
        try {
            homePage.getTaskBox(id);
        } catch (NoSuchElementException e) {
            Assert.fail("Unable to find task box " + id);
        }
    }

    private void assertLinkInTask(String id, String sectionKey, Class<? extends BasePage> page) {
        String key = "homepage.task." + sectionKey + ".link.label";
        String label = PropUtils.get(key);

        HomepageTaskFragment task = homePage.getTaskBox(id);
        task.open();

        String link = ConfigUtils.getPageLocation(page);
        try {
            task.getLink(label, link);
        } catch (NoSuchElementException e) {
            Assert.fail("Unable to find link " + label + "[" + link + "] in section " + id);
        }
    }

}
