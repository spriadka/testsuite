package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheContainerWizard;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheContainersFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#infinispan")
public class CacheContainersPage extends ConfigurationPage implements Navigatable {

    private static final By CONTENT = By.id(PropUtils.get("page.content.id"));

    public CacheContainersFragment content() {
        return Graphene.createPageFragment(CacheContainersFragment.class, getContentRoot().findElement(CONTENT));
    }

    private FinderNavigation createBaseNavigation() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS);
        }
        navigation.addAddress(FinderNames.SUBSYSTEM, "Infinispan");
        return navigation;
    }

    public void navigate() {
        createBaseNavigation().selectColumn();
    }

    public FinderNavigation getNavigationToCacheContainer(String cacheContainer) {
        return createBaseNavigation().addAddress("Cache Container", cacheContainer);
    }

    public void invokeTransportSettings(String cacheContainer) {
        getNavigationToCacheContainer(cacheContainer).selectRow().invoke("Transport Settings");
    }

    public void invokeContainerSettings(String cacheContainer) {
        getNavigationToCacheContainer(cacheContainer).selectRow().invoke("Container Settings");
    }

    public CacheContainerWizard invokeAddCacheContainer() {
        createBaseNavigation().addAddress("Cache Container").selectColumn();
        return getResourceManager().addResource(CacheContainerWizard.class);
    }

    public void removeCacheContainer(String containerName) {
        try {
            getNavigationToCacheContainer(containerName).selectRow().invoke("Remove");
        } catch (TimeoutException ignored) {
        }
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
    }

    public ConfigFragment getSettingsConfig() {
        By selector = ByJQuery.selector("." + PropUtils.get("page.content.rhs.class") + ":visible");
        WebElement formRoot = browser.findElement(selector);
        return Graphene.createPageFragment(ConfigFragment.class, formRoot);
    }
}
