package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Created by pcyprian on 26.10.15.
 */
public class ServerConfigurationPage extends  ConfigurationPage {
    /* UTILS */
    public void goToServerProperties(String server) {
        FinderNavigation navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .step(FinderNames.HOST, ConfigUtils.getDefaultHost())
                .step(FinderNames.SERVER, server);

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        browser.findElement(ByJQuery.selector("div.gwt-Label:contains(System Properties)")).click();
    }

    public void addProperty(String name, String value) {
        WebElement add = browser.findElement(By.id("gwt-debug-addBtnPropertyEditor"));
        add.click();

        getWindowFragment().getEditor().text("key", name);
        getWindowFragment().getEditor().text("value", value);
        getWindowFragment().save();
    }

    public void removeProperty(String name) {
        getResourceManager().getResourceTable().selectRowByText(0, name);
        browser.findElement(By.xpath("//button[contains(text(), 'Remove')]")).click();
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    public void clickExpressionResolver() {
        WebElement tools = browser.findElement(ByJQuery.selector("div.gwt-HTML.footer-link:contains(Tools)"));
        tools.click();
        browser.findElement(By.xpath("//div[@class='popupContent']//a[contains(text(), 'Expression Resolver')]")).click();

    }

    public ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }
}
