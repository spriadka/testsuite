package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.fragment.runtime.StandaloneDeploymentsArea;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.ConfigurationPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jcechace
 */
@Location("#standalone-deployments")

public class DeploymentPage extends ConfigurationPage {

    private static final By CONTENT = By.className(PropUtils.get("page.content.gwt-layoutpanel"));
    private static final String CONTENT_TABLE_ROW = "table:contains('Data') tr.form-attribute-row";

    public StandaloneDeploymentsArea getDeploymentContent() {
        WebElement content =  getContentRoot().findElement(CONTENT);

        return Graphene.createPageFragment(StandaloneDeploymentsArea.class, content);
    }

    public String getDeploymentContentTableRow(String rowContains) {
        StringBuffer selector = new StringBuffer(CONTENT_TABLE_ROW);
        if (rowContains != null && !rowContains.isEmpty()) {
            selector.append(":contains('" + rowContains + "')");
        } else {
            return "";
        }

        WebElement content = getContentRoot().findElement(new ByJQuery(selector.toString()));
        return content.getText();
    }

    public List<String> getDeploymentBrowsedContentItems() {
        By selector = ByJQuery.selector("table.browse-content tr");
        List<String> items = new ArrayList<String>();

        List<WebElement> webElements = getContentRoot().findElements(selector);
        for (WebElement webElement : webElements ) {
            items.add(webElement.getText());
        }

        return items;
    }

    /**
     * Opens deployment detail page.
     */
    public void viewDeployment(String deploymentName) {
        FinderNavigation navigation = new FinderNavigation(browser, this.getClass());
        Row row = navigation.step(FinderNames.DEPLOYMENT, deploymentName).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.VIEW);
    }

    public void disableEnabledDeployment(String deploymentName) {
        FinderNavigation navigation = new FinderNavigation(browser, this.getClass());
        Row row = navigation.step(FinderNames.DEPLOYMENT, deploymentName).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.DISABLE);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
    }

    public void enableDisabledDeployment(String deploymentName) {
        FinderNavigation navigation = new FinderNavigation(browser, this.getClass());
        Row row = navigation.step(FinderNames.DEPLOYMENT, deploymentName).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.ENABLE);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
    }

}
