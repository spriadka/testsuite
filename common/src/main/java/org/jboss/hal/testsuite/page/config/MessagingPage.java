package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.config.messaging.MessagingConfigArea;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;



/**
 * Created by pcyprian on 2.9.15.
 */
public class MessagingPage extends ConfigPage implements Navigatable {

    protected static final String
        MESSAGING_PROVIDER_LABEL = "Messaging Provider",
        MESSAGING_SUBSYSTEM_LABEL = "Messaging",
        JMS_BRIDGE_LABEL = "JMS Bridge",
        SETTINGS_LABEL = "Settings";
    private FinderNavigation navigation;

    public ConfigFragment getConfigFragment() {
        WebElement editPanel = browser.findElement(ByJQuery.selector(".master_detail-detail:visible"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

    public ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

    public void navigateToMessagingProvider() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .step(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .step(FinderNames.PROFILE, ConfigUtils.getDefaultProfile());
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS);
        }
        navigation.step(FinderNames.SUBSYSTEM, MESSAGING_SUBSYSTEM_LABEL);
        navigation.step(SETTINGS_LABEL, MESSAGING_PROVIDER_LABEL);
    }

    public void selectProvider(String provider) {
        navigateToMessagingProvider();
        navigation.step(MESSAGING_PROVIDER_LABEL, provider);
    }

    public void navigateToMessaging() {
        selectProvider("default");
    }

    public void selectView(String view) {
        Row row = navigation.selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(view);
        Application.waitUntilVisible();
    }

    public void selectConnectionsView() {
        selectView("Connections");
    }

    public void selectQueuesAndTopics() {
        selectView("Queues/Topics");
    }

    public void makeNavigation() {
        navigation.selectRow();
        Library.letsSleep(1000);
    }

    public void createProvider(String name, boolean enabledSecurity, String secirityDomain, String clusterUser, String clusterPassword) {
        WebElement add = browser.findElement(ByJQuery.selector("div.btn,.primary"));
        add.click();
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().checkbox("security-enabled", enabledSecurity);
        getWindowFragment().getEditor().text("security-domain", secirityDomain);
        getWindowFragment().getEditor().text("cluster-user", clusterUser);
        getWindowFragment().getEditor().text("cluster-password", clusterPassword);
        getWindowFragment().clickButton("Save");
    }

    public void removeProvider() {
        navigation.selectRow().invoke("Remove");
        getWindowFragment().clickButton("Confirm");
    }

    public void InvokeProviderSettings() {
        navigation.selectRow().invoke("Provider Settings");
        Library.letsSleep(1000);
    }

    public void switchToSecurityTab() {
        WebElement security = browser.findElement(ByJQuery.selector("div.gwt-Label:contains(Security)"));
        security.click();
    }

    public void switchToJournalTab() {
        WebElement security = browser.findElement(ByJQuery.selector("div.gwt-Label:contains(Journal)"));
        security.click();
    }

    public void switchToConnectionManagementTab() {
        WebElement security = browser.findElement(ByJQuery.selector("div.gwt-Label:contains(Connection Management):visible"));
        security.click();
    }

    public void switchToDiscovery() {
        switchView("Discovery");
    }

    public void switchToConnector() {
        switchView("Connector");
    }

    public void switchToAcceptor() {
        switchView("Acceptor");
    }

    public void switchToConnectorServices() {
        switchView("Connector Services");
    }

    public void switchToConnectionFactories() {
        switchView("Connection Factories");
    }

    public void switchToSecuritySettings() {
        switchView("Security Settings");
    }

    public void switchToAddressSettings() {
        switchView("Address Settings");
    }

    public void switchToConnections() {
        switchView("Connections");
    }

    public void switchToDiverts() {
        switchView("Diverts");
    }

    public void switchToBridges() {
        switchView("Bridges");
    }

    public void switchToJmsQueuesTopics() {
        switchView("JMS Queues/Topics");
    }

    public void selectInTable(String value, int column) {
        getResourceManager().getResourceTable().selectRowByText(column, value);
    }

    public void selectInTable(String text) {
        selectInTable(text, 0);
    }

    public void switchType(String type) {
        Select select = new Select(browser.findElement(ByJQuery.selector(".gwt-ListBox:visible")));
        select.selectByValue("Type: " + type);
    }

    public void switchToGenericType() {
        switchType("Generic");
    }

    public void switchToInVmType() {
        switchType("In-VM");
    }

    public void switchToRemoteType() {
        switchType("Remote");
    }

    public void addBridge(String name, String queue, String address, String connector) {
        clickButton("Add");
        WindowFragment window = Console.withBrowser(browser).openedWindow();
        Editor editor = window.getEditor();
        editor.text("name", name);
        editor.text("queueName", queue);
        editor.text("forwardingAddress", address);
        editor.text("staticConnectors", connector);
        window.clickButton("Save");
    }

    public void addBroadcastGroup(String name, String binding) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("socket-binding", binding);
        getWindowFragment().clickButton("Save");
    }

    public void addRemoteAcceptor(String name, String socketBinding) {
        clickButton(FinderNames.ADD);
        ConfigFragment windowFragment = getWindowFragment();
        Editor editor = windowFragment.getEditor();
        editor.text("name", name);
        editor.text("socketBinding", socketBinding);
        windowFragment.save();
    }

    public void addRemoteConnector(String name, String socketBinding) {
        addRemoteAcceptor(name, socketBinding);
    }

    public void addDiscoveryGroup(String name, String binding) {
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

    public void addDiverts(String name, String divert, String forward) {
        clickButton("Add");
        WindowFragment window = Console.withBrowser(browser).openedWindow();
        Editor editor = window.getEditor();
        editor.text("routingName", name);
        editor.text("divertAddress", divert);
        editor.text("forwardingAddress", forward);
        window.clickButton("Save");
    }

    public void addConnetorServices(String name, String factoryClass) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("factoryClass", factoryClass);
        getWindowFragment().clickButton("Save");
    }

    public void addSecuritySettings(String pattern, String role) {
        clickButton("Add");
        getWindowFragment().getEditor().text("pattern", pattern);
        getWindowFragment().getEditor().text("role", role);
        getWindowFragment().clickButton("Save");
    }

    public void addAddressSettings(String pattern) {
        clickButton("Add");
        getWindowFragment().getEditor().text("pattern", pattern);
        getWindowFragment().clickButton("Save");
    }

    public void addQueue(String name, String jndiName) {
        clickButton("Add");
        ConfigFragment configFragment = getWindowFragment();
        Editor editor = configFragment.getEditor();
        editor.text("name", name);
        editor.text("entries", jndiName);
        configFragment.clickButton("Save");
    }

    public void addTopic(String name, String jndiName) {
        addQueue(name, jndiName);
    }

    public void addFactory(String name, String jndiName, String connector) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("entries", jndiName);
        getWindowFragment().getEditor().text("connectors", connector);
        getWindowFragment().clickButton("Save");
    }


    public void addClusterConnection(String name, String dg, String connectorName, String connectorAddress) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("discoveryGroup", dg);
        getWindowFragment().getEditor().text("connectorName", connectorName);
        getWindowFragment().getEditor().text("clusterConnectionAddress", connectorAddress);
        getWindowFragment().clickButton("Save");
    }

    public void clickAdvanced() {
        WebElement advanced = browser.findElement(By.linkText("Advanced"));
        advanced.click();
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
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirmAndDismissReloadRequiredMessage();
        } catch (TimeoutException ignored) {
        }
    }

    public void remove(String text) {
        selectInTable(text);
        remove();
    }


    public MessagingConfigArea getConfig() {
        return getConfig(MessagingConfigArea.class);
    }

    public boolean addProperty(String key, String value) {
        ConfigPropertiesFragment properties = getConfig().propertiesConfig();
        ConfigPropertyWizard wizard = properties.addProperty();
        wizard.name(key).value(value).clickSave();

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        return wizard.isClosed();
    }

    public void removeProperty(String key) {
        ConfigPropertiesFragment config = getConfig().propertiesConfig();
        ResourceManager properties = config.getResourceManager();
        try {
            properties.removeResource(key).confirmAndDismissReloadRequiredMessage();
        } catch (TimeoutException ignored) {

        }
    }
}
