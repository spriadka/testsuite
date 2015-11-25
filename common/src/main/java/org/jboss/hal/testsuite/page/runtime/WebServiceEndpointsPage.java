package org.jboss.hal.testsuite.page.runtime;

import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.MetricsPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Created by mkrajcov on 4/10/15.
 */
public class WebServiceEndpointsPage extends MetricsPage {
    public MetricsAreaFragment getWebServiceRequestMetricsArea() {
        return getMetricsArea("Web Service Requests");
    }

    public void refreshStats() {
        WebElement viewPanel = browser.findElement(By.className("rhs-content-panel"));
        WebElement refreshLink = viewPanel.findElement(By.className("html-link"));
        refreshLink.click();
        Library.letsSleep(500);
    }

    public void navigateInDeploymentsMenu() {
        if (ConfigUtils.isDomain()) {
            FinderNavigation deployNavigation = new FinderNavigation(browser, DomainDeploymentPage.class)
                    .addAddress(FinderNames.BROWSE_BY, FinderNames.SERVER_GROUPS)
                    .addAddress(FinderNames.SERVER_GROUP, "main-server-group")
                    .addAddress(FinderNames.DEPLOYMENT, "test.war");
            deployNavigation.selectRow().invoke(FinderNames.VIEW);
            Application.waitUntilVisible();

        } else {
            FinderNavigation deployNavigation = new FinderNavigation(browser, DeploymentPage.class)
                    .addAddress(FinderNames.DEPLOYMENT, "test.war");
            deployNavigation.selectRow().invoke(FinderNames.VIEW);
            Application.waitUntilVisible();
        }
    }
}
