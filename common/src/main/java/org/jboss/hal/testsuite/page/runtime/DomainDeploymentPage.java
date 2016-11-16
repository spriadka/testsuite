package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.Column;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentContentRepositoryArea;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentServerGroupArea;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.ConfigurationPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jcechace
 */
@Location("#domain-deployments")
public class DomainDeploymentPage extends ConfigurationPage {

    private static final By BACK_BUTTON = By.xpath(".//a[text()='Back']");
    private static final By CONTENT = By.className(PropUtils.get("page.content.gwt-layoutpanel"));

    public DeploymentContentRepositoryArea switchToContentRepository() {
        switchTab("Content Repository");
        return getDeploymentContent(DeploymentContentRepositoryArea.class);
    }

    public DeploymentServerGroupArea switchToServerGroup(String serverGroup) {
        switchTab("Server Groups");
        WebElement backAnchor = getContentRoot().findElement(BACK_BUTTON);
        if (backAnchor.isDisplayed()) {
            backAnchor.click();
        }
        getResourceManager().viewByName(serverGroup);
        return getDeploymentContent(DeploymentServerGroupArea.class);
    }

    public <T extends BaseFragment> T getDeploymentContent(Class<T> clazz) {
        WebElement content = getContentRoot().findElement(CONTENT);
        return Graphene.createPageFragment(clazz, content);
    }

    public DeploymentContentRepositoryArea  getDeploymentContent() {
        WebElement content = getContentRoot().findElement(CONTENT);
        return Graphene.createPageFragment(DeploymentContentRepositoryArea.class, content);
    }

    public void unassign() {
        option("Unassign");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    public boolean checkAssignDeploymentNameInAssignContent(String assignName) {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        String contentText = editPanel.findElement(By.className("gwt-Label")).getText();
        editPanel.findElement(ByJQuery.selector("button:contains(Cancel)")).click();
        return contentText.contains(assignName);
    }

    public Row navigateToRowInServerGroup(String groupName, String deploymentName) {
        Row row = new FinderNavigation(browser, this.getClass()).step(FinderNames.BROWSE_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP, groupName)
                .step(FinderNames.DEPLOYMENT, deploymentName)
                .selectRow();
        Console.withBrowser(browser).waitUntilLoaded();
        return row;
    }

    public Row navigateToRowInUnassignedContent(String deploymentName) {
        Row row = new FinderNavigation(browser, this.getClass())
                .step(FinderNames.BROWSE_BY, "Unassigned Content")
                .step("Unassigned", deploymentName)
                .selectRow();
        Console.withBrowser(browser).waitUntilLoaded();
        return row;
    }

    public Column navigateToColumnInContentRepository() {
        Column column = new FinderNavigation(browser, this.getClass())
                .step(FinderNames.BROWSE_BY, "Content Repository").step("All Content")
                .selectColumn();
        Console.withBrowser(browser).waitUntilLoaded();
        return column;
    }

    public Column navigateToDeploymentColumnInServerGroup(String groupName) {
        Column column = new FinderNavigation(browser, this.getClass())
                .step(FinderNames.BROWSE_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP, groupName)
                .step(FinderNames.DEPLOYMENT)
                .selectColumn();
        Console.withBrowser(browser).waitUntilLoaded();
        return column;
    }

    public List<String> getDeploymentBrowsedContentItems() {
        By selector = ByJQuery.selector("table.browse-content tr");
        List<String> items = new ArrayList<>();

        List<WebElement> webElements = getContentRoot().findElements(selector);
        for (WebElement webElement : webElements ) {
            items.add(webElement.getText());
        }

        return items;
    }
}
