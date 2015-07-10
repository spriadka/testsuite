package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class ConfigurationPage extends ConfigPage {

    public ConfigurationPage select(String label) {
        String cellClass = PropUtils.get("table.cell.class");
        String cellSelectedClass = PropUtils.get("table.cell.selected.class");
        By selector = ByJQuery.selector("." + cellClass + ":contains('" + label + "')");
        getContentRoot().findElement(selector).click();
        Graphene.waitModel().until().element(selector).attribute("class").contains(cellSelectedClass);
        Library.letsSleep(1000);
        return this;
    }

    public <T extends WizardWindow> T add(Class<T> clazz) {
        return getResourceManager().addResource(clazz);
    }

    public void remove() {
        option("Remove");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    public void option(String optionLabel) {
        String menuClass = PropUtils.get("options.dropdown.class");
        String menuItemClass = PropUtils.get("options.menu.item.class");
        By menuSelector = ByJQuery.selector("button." + menuClass + ":visible");
        By optionSelector = ByJQuery.selector("td." + menuItemClass + ":contains('" + optionLabel + "')");
        getContentRoot().findElement(menuSelector).click();
        Graphene.waitModel().until().element(optionSelector).is().present();
        browser.findElement(optionSelector).click();
    }

    public ConfigurationPage view(String label) {
        select(label);
        clickButton("View");
        return this;
    }

    @Override
    public ConfigAreaFragment getConfig() {
        String layoutClass = PropUtils.get("configarea.layout.class");
        String viewPanelClass = PropUtils.get("configarea.view.panel.class");
        By selector = ByJQuery.selector("." + layoutClass + ":visible:has(table." + viewPanelClass + ")");
        WebElement root = browser.findElement(selector);
        return Graphene.createPageFragment(ConfigAreaFragment.class, root);
    }
}
