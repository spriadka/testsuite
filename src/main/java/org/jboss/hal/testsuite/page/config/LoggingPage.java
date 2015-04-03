package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@Location("#logging")
public class LoggingPage extends ConfigPage {

    public void switchToHandlerTab() {
        String label = PropUtils.get("config.core.logging.handler.tab.label");
        switchTab(label);
    }

    public void switchToPeriodicSize() {
        String label = PropUtils.get("config.core.logging.periodic_size.view.label");
        switchView(label);
    }

    public boolean save() {
        clickButton(PropUtils.get("configarea.save.button.label"));
        try {
            Graphene.waitModel().until().element(getEditButton()).is().visible();
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    public Editor edit() {
        WebElement button = getEditButton();
        button.click();
        Graphene.waitGui().until().element(button).is().not().visible();
        return getConfig().getEditor();

    }

    private WebElement getEditButton() {
        By selector = ByJQuery.selector("." + PropUtils.get("configarea.edit.button.class") + ":visible");
        return getContentRoot().findElement(selector);
    }

    public void checkbox(String identifier, boolean value) throws InterruptedException {

        String byIdSelector = "input[type='checkbox'][id$='" + identifier + "']";
        String byNameSelector = "input[type='checkbox'][name='" + identifier + "']";
        String byParentTdIdSelector = "td[id*='" + identifier + "'] input[type='checkbox']:visible";
        String selectorString = byIdSelector + ", " + byNameSelector + ", " + byParentTdIdSelector;
        System.out.println(selectorString);
        By selector = ByJQuery.selector(selectorString);

        WebElement input = Console.withBrowser(browser).findElement(selector, getContentRoot());
        boolean current = input.isSelected();

        if (value != current) {
            input.click();
        }

        if (value) {
            Graphene.waitGui().until().element(input).is().selected();
        } else {
            Graphene.waitGui().until().element(input).is().not().selected();
        }
    }

    public void select(String identifier, String value) {
        String byIdSelector = "select[id$='" + identifier + "']";
        String byNameSelector = "select[name='" + identifier + "']";
        String byParentTdIdSelector = "td[id*='" + identifier + "'] select:visible";
        By selector = ByJQuery.selector(byIdSelector + ", " + byNameSelector + ", " + byParentTdIdSelector);

        WebElement selectElement = Console.withBrowser(browser).findElement(selector, getContentRoot());
        Select select = new Select(selectElement);
        select.selectByVisibleText(value);
    }
}
