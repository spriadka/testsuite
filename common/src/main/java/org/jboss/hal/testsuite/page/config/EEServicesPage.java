package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.config.ee.EEConfigFragment;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 11.9.15.
 */
public class EEServicesPage extends ConfigurationPage implements Navigatable {

    public EEConfigFragment getConfigFragment() {
        WebElement editPanel = browser.findElement(By.className("default-tabpanel"));
        return  Graphene.createPageFragment(EEConfigFragment.class, editPanel);
    }

    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class);
            navigation.addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full")
                    .addAddress(FinderNames.SUBSYSTEM, "EE");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class);
            navigation.addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "EE");
        }
        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        //TODO: remove this after HAL-836 is resolved
        By selector = ByJQuery.selector(".link-bar-first");
        browser.findElement(selector).click();
        Console.withBrowser(browser).waitUntilLoaded();
        view("EE");
        Console.withBrowser(browser).waitUntilLoaded();
        //END OF REMOVE
        switchTab("Services");
    }

}
