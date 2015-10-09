package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheContainerWizard;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheContainersFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#infinispan")
public class CacheContainersPage extends ConfigurationPage implements Navigatable {

    private static final By CONTENT = By.id(PropUtils.get("page.content.id"));
    private FinderNavigation navigation;

    public CacheContainersFragment content() {
        return Graphene.createPageFragment(CacheContainersFragment.class, getContentRoot().findElement(CONTENT));
    }

    public void navigate() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS);
        }
        navigation.addAddress(FinderNames.SUBSYSTEM, "Infinispan");
        navigation.selectColumn();
    }

    public FinderNavigation getNavigationToCacheContainer(String cacheContainer) {
        navigation.resetNavigation().addAddress("Cache Container", cacheContainer);
        return navigation;
    }

    public void invokeTransportSettings(String cacheContainer) {
        getNavigationToCacheContainer(cacheContainer).selectRow().invoke("Transport Settings");
    }

    public void invokeContainerSettings(String cacheContainer) {
        getNavigationToCacheContainer(cacheContainer).selectRow().invoke("Container Settings");
    }

    public CacheContainerWizard invokeAddCacheContainer() {
        navigation.resetNavigation().addAddress("Cache Container").selectColumn();
        return getResourceManager().addResource(CacheContainerWizard.class);
    }

    public void removeCacheContainer(String containerName) {
        try {
            getNavigationToCacheContainer(containerName).selectRow().invoke("Remove");
        } catch (TimeoutException ignored) {
        }
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
    }
}
