package org.jboss.hal.testsuite.page.home;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.homepage.HomepageModuleFragment;
import org.jboss.hal.testsuite.fragment.homepage.HomepageSideBarFragment;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Optional;

/**
 * @author jcechace
 */
@Location("#home")
public class HomePage extends BasePage implements Navigatable {

    public void navigate() {
        browser.navigate().refresh();
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded().maximizeWindow();
    }

    /**
     * @return null if no matching element is found
     */
    public HomepageModuleFragment getModule(String title) {

        String moduleClass = PropUtils.get("homepage.module.class");
        By anyModuleSelector = By.className(moduleClass);

        String moduleHeaderClass = PropUtils.get("homepage.module.header.class");
        By moduleTitleSelector = ByJQuery.selector("." + moduleHeaderClass + ":contains('" + title + "'):visible");

        Optional<WebElement> module = getContentRoot().findElements(anyModuleSelector).stream().filter(anyModule -> {
            return !anyModule.findElements(moduleTitleSelector).isEmpty();
        }).findFirst();

        if (module.isPresent()) {
            return Graphene.createPageFragment(HomepageModuleFragment.class, module.get());
        }
        return null;
    }

    public HomepageSideBarFragment getSideBar() {
        By selector = By.className(PropUtils.get("homepage.sidebar.class"));
        WebElement sidebarRoot = browser.findElement(selector);

        HomepageSideBarFragment sidebar = Graphene.createPageFragment(HomepageSideBarFragment.class,
                sidebarRoot);

        return sidebar;
    }

}
