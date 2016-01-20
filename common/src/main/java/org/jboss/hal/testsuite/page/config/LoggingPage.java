package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class LoggingPage extends ConfigurationPage implements Navigatable {
    private FinderNavigation navigation;

    public void navigateToLogging() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "default")
                    .addAddress(FinderNames.SUBSYSTEM, "Logging");

            navigation.selectRow().invoke(FinderNames.VIEW);
            Application.waitUntilVisible();

        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Logging");
            Row row = navigation.selectRow();
            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
            row.invoke(FinderNames.VIEW);
            Application.waitUntilVisible();
        }
    }

    public ConfigFragment getConfigFragment() {
        WebElement editPanel = browser.findElement(By.className("default-tabpanel"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

    public ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

    public void remove() {
        clickButton("Remove");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    public void addLogger(String name, String category, String level) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().select("level", level);
        getWindowFragment().getEditor().checkbox("use-parent-handlers", true);
        getWindowFragment().clickButton("Save");
    }

    public void addFormatter(String name) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().clickButton("Save");
    }

    public void addAsyncHandler(String name, String queueLen) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("queue-length", queueLen);
        getWindowFragment().clickButton("Save");
    }

    public void addFileHandler(String name) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().clickButton("Next");
        getWindowFragment().getEditor().text("path", "/logs/");
        getWindowFragment().getEditor().clickButton("Finish");
    }

    public void addSyslogHandler(String name) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().clickButton("Save");
    }

    public void addConsoleHandler(String name, String level) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().select("level", level);
        getWindowFragment().clickButton("Save");
    }

    public void addPeriodicSizeHandler(String name, String suffix) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("suffix", suffix);
        getWindowFragment().clickButton("Next");
        getWindowFragment().getEditor().text("path", "/logs/");
        getWindowFragment().getEditor().clickButton("Finish");
    }

    public void addCustomFormatter(String name, String clazz, String module) {
        clickButton("Add");
        getWindowFragment().getEditor().text("name", name);
        getWindowFragment().getEditor().text("class", clazz);
        getWindowFragment().getEditor().text("module", module);
        getWindowFragment().clickButton("Save");
    }

    public void switchToHandlerTab() {
        String label = PropUtils.get("config.core.logging.handler.tab.label");
        switchTab(label);
    }

    public void switchToCategoriesTab() {
        switchTab("Log Categories");
    }

    public void switchToFormatterTab() {
        switchTab("Formatter");
    }

    public void switchToFile() {
        switchView("File");
    }

    public void switchToPeriodic() {
        switchView("Periodic");
    }

    public void switchToSize() {
        WebElement viewPanel = browser.findElement(By.className("paged-view-navigation-container"));
        WebElement editLink = viewPanel.findElement(By.linkText("Size"));
        editLink.click();
    }

    public void switchToSyslog() {
        switchView("Syslog");
    }

    public void switchToCustomPattern() {
        switchSubTab("Custom");
    }

    public void switchToPeriodicSize() {
        String label = PropUtils.get("config.core.logging.periodic_size.view.label");
        switchView(label);
    }
    public void switchToAsync() {
        WebElement viewPanel = browser.findElement(By.className("paged-view-navigation-container"));
        WebElement editLink = viewPanel.findElement(By.linkText("Async"));
        editLink.click();
    }

    public boolean save() {
        clickButton(PropUtils.get("configarea.save.button.label"));
        try {
            Graphene.waitModel().until().element(getEditButton()).is().visible();
            return true;
        } catch (WebDriverException e) {
            return false;
        }
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

    public void checkbox(String identifier, boolean value) throws InterruptedException {

        String byIdSelector = "input[type='checkbox'][id$='" + identifier + "']";
        String byNameSelector = "input[type='checkbox'][name='" + identifier + "']";
        String byParentTdIdSelector = "td[id*='" + identifier + "'] input[type='checkbox']:visible";
        String selectorString = byIdSelector + ", " + byNameSelector + ", " + byParentTdIdSelector;
        System.out.println(selectorString);
        By selector = ByJQuery.selector(selectorString);

        WebElement input = Console.withBrowser(browser).findElement(selector, getContentRoot());
        boolean current = input.isSelected();

        if (value != current) {
            input.click();
        }

        if (value) {
            Graphene.waitGui().until().element(input).is().selected();
        } else {
            Graphene.waitGui().until().element(input).is().not().selected();
        }
    }

    public void select(String identifier, String value) {
        String byIdSelector = "select[id$='" + identifier + "']";
        String byNameSelector = "select[name='" + identifier + "']";
        String byParentTdIdSelector = "td[id*='" + identifier + "'] select:visible";
        By selector = ByJQuery.selector(byIdSelector + ", " + byNameSelector + ", " + byParentTdIdSelector);

        WebElement selectElement = Console.withBrowser(browser).findElement(selector, getContentRoot());
        Select select = new Select(selectElement);
        select.selectByVisibleText(value);
    }

    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, ConfigUtils.getDefaultProfile())
                    .addAddress(FinderNames.SUBSYSTEM, "Logging");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Logging");
        }
        Row row = navigation.selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }

    public void selectLogger(String name) {
        getResourceManager().getResourceTable().selectRowByText(0, name);
    }

    public void selectFormatter(String name) {
        getResourceManager().getResourceTable().selectRowByText(0, name);
    }

    public void selectHandler(String name) {
        getResourceManager().getResourceTable().selectRowByText(0, name);
    }
}
