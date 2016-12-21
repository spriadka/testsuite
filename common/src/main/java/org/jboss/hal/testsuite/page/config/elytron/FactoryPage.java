package org.jboss.hal.testsuite.page.config.elytron;

import static org.jboss.hal.testsuite.page.config.elytron.ElytronPageConstants.ELYTRON_SUBSYTEM_LABEL;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FactoryPage extends ConfigPage {

    public FactoryPage navigateToApplication() {
        getSubsystemNavigation(ELYTRON_SUBSYTEM_LABEL).step("Settings", "Factory").openApplication();
        return this;
    }

    public FactoryPage selectFactory(final String factoryLabel) {
        switchSubTab(factoryLabel);
        return this;
    }

    public ConfigFragment switchToConfigAreaTab(final String tabName) {
        return getConfig().switchTo(tabName);
    }

    public boolean resourceIsPresentInMainTable(String resourceName) {
        return getConfigFragment().resourceIsPresent(resourceName);
    }

    public boolean resourceIsPresentInConfigAreaTable(String resourceName) {
        return resourceIsPresentInConfigAreaTable(resourceName, 0);
    }

    public boolean resourceIsPresentInConfigAreaTable2ndColumn(String resourceName) {
        return resourceIsPresentInConfigAreaTable(resourceName, 1);
    }

    private boolean resourceIsPresentInConfigAreaTable(String resourceName, int tableColumnIndex) {
        By configAreaSelector = ByJQuery.selector("." + PropUtils.get("configarea.class") + ":visible");
        WebElement configAreaRoot = getContentRoot().findElement(configAreaSelector);
        ConfigFragment configFragment = Graphene.createPageFragment(ConfigFragment.class, configAreaRoot);
        return configFragment.resourceIsPresent(resourceName, tableColumnIndex);
    }

    public ResourceManager getConfigAreaResourceManager() {
        By configAreaContentSelector = ByJQuery.selector("." + PropUtils.get("configarea.content.class") + ":visible");
        return Graphene.createPageFragment(ResourceManager.class, browser.findElement(configAreaContentSelector));
    }
}
