package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.jgroups.ExecutorTabFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.util.Console;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Created by jkasik on 22.7.15.
 */
@Location("#jgroups")
public class JGroupsPage extends ConfigurationPage {

    public void selectStackByName(String name) {
        getResourceManager().getResourceTable().getRowByText(0, name).view();
    }

    public void openExecutors() {
        By parent = ByJQuery.selector("table.gwt-DisclosurePanel.default-disclosure");
        WebElement root = getContentRoot().findElement(parent);
        ExecutorTabFragment executorTabFragment = Graphene.createPageFragment(ExecutorTabFragment.class, root);
        executorTabFragment.openExecutors();
    }
    
    public void switchToTransport() {
        switchTo("Transport");
    }

    public void switchToProtocols() {
        switchTo("Protocols");
    }

    public void switchTo(String name) {
        switchView(name);
        Console.withBrowser(browser).waitUntilLoaded();
    }

    public ConfigPropertiesFragment getConfigProperties() {
        return Graphene.createPageFragment(ConfigPropertiesFragment.class, getContentRoot());
    }

}
