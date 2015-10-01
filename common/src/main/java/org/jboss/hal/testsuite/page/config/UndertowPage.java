package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.socketbindings.InboundSocketBindingFragment;
import org.jboss.hal.testsuite.fragment.config.undertow.UndertowFragment;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
public class UndertowPage extends ConfigurationPage implements Navigatable {

    private static final By CONTENT_ROOT = ByJQuery.selector("." + PropUtils.get("page.content.rhs.class")
            + ":visible:has(." + PropUtils.get("configarea.content.class")+ ":visible)");

    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS);
        }
        navigation.addAddress(FinderNames.SUBSYSTEM, "Undertow").selectRow();
        Application.waitUntilVisible();
    }

    public UndertowFragment getConfigFragment() {
        WebElement editPanel = browser.findElement(CONTENT_ROOT);
        return Graphene.createPageFragment(UndertowFragment.class, editPanel);
    }

    @Override
    public void switchSubTab(String identifier) {
        super.switchSubTab(identifier);
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @Override
    public ConfigAreaFragment getConfig() {
        By selector = CONTENT_ROOT;
        WebElement root = browser.findElement(selector);
        return Graphene.createPageFragment(ConfigAreaFragment.class, root);
    }

}
