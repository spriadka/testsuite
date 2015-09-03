package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.messaging.MessagingConfigArea;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Created by pcyprian on 2.9.15.
 */
public class MessagingPage extends ConfigPage {

    private FinderNavigation navigation;

    public ConfigFragment getConfigFragment() {
        WebElement editPanel = browser.findElement(ByJQuery.selector(".master_detail-detail:visible"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

    public ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

    public void navigateToMessaging() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full-ha")
                    .addAddress(FinderNames.SUBSYSTEM, "Messaging")
                    .addAddress("Messaging Provider", "default");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Messaging")
                    .addAddress("Messaging Provider", "default");
        }
    }

    public void selectView(String view) {
        navigation.selectRow().invoke(view);
        Application.waitUntilVisible();
    }

    public void switchToDiscovery() {
        switchView("Discovery");
    }

    public void switchToProperties() {
        WebElement btn = browser.findElement(By.linkText("Properties"));
        btn.click();
    }

    public void switchToConnections() {
        switchView("Connections");
    }

    public void selectInTable(String value, int column) {
        getResourceManager().getResourceTable().selectRowByText(column, value);
    }

    public void switchType(String type) {
        //WebElement listBox = browser.findElement(ByJQuery.selector(type));
        Select select = new Select(browser.findElement(ByJQuery.selector(".gwt-ListBox:visible")));
        select.selectByValue(type);
    }

    public void addBroadcastGroup(String name, String binding) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("socketBinding", binding);
        getWindowFragment().clickButton("Save");
    }

    public void addInVmAcceptor(String name, String server) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("serverId", server);
        getWindowFragment().clickButton("Save");
    }

    public void addGenericAcceptor(String name, String binding, String factoryClass) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("socketBinding", binding);
        getWindowFragment().getEditor().text("factoryClass", factoryClass);
        getWindowFragment().clickButton("Save");
    }


    public void addClusterConnection(String name, String dg, String connectorName, String connectorAddress) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("discoveryGroupName", dg);
        getWindowFragment().getEditor().text("connectorRef", connectorName);
        getWindowFragment().getEditor().text("clusterConnectionAddress", connectorAddress);
        getWindowFragment().clickButton("Save");
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

    public void remove() {
        clickButton("Remove");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }


    public MessagingConfigArea getConfig() {
        return getConfig(MessagingConfigArea.class);
    }
}
