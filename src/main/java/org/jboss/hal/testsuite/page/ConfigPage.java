package org.jboss.hal.testsuite.page;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.AdvancedSelectBox;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableFragment;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableRowFragment;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Created by jcechace on 22/02/14.
 */
public class ConfigPage extends BasePage {
    /**
     * Returns the ConfigArea portion of page as given implementation.
     * Not reliable - you might need to override this method.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends ConfigAreaFragment> T getConfig(Class<T> clazz) {

        WebElement configRoot = null;
        try {
            String cssClass = PropUtils.get("configarea.class");
            By selector = ByJQuery.selector("." + cssClass + ":visible");
            configRoot = getContentRoot().findElement(selector);
        } catch (NoSuchElementException e) { // TODO: this part should be removed once ensured the ID is everywhere
            List<WebElement> elements = getContentRoot().findElements(getConfigSelector());

            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    configRoot = element;
                }
            }
        }

        T config = Graphene.createPageFragment(clazz, configRoot);
        return config;
    }

    private By getConfigSelector() {
        String selectionLabel =
                ".//div[contains(@class, 'content-group-label') and (contains(text(), 'Selection') or contains(text(), 'Details'))]";
        By selector = By.xpath(selectionLabel + "/following::*[contains(@class, 'rhs-content-panel')]");

        return selector;
    }

    /**
     * Returns the default implementation of ConfigArea portion of page.
     * Not reliable - you might need to override this method.
     *
     * @return
     */
    public ConfigAreaFragment getConfig() {
        return getConfig(ConfigAreaFragment.class);
    }

    public <T extends WizardWindow> T addResource(Class<T> clazz, String label) {
        clickButton(label);

        T wizard = Console.withBrowser(browser).openedWizard(clazz);

        return wizard;
    }

    public void pickProfile(String label) {
        AdvancedSelectBox picker = getProfilePicker();
        picker.pickOption(label);

        Console.withBrowser(browser).waitUntilFinished();
    }

    public void pickHost(String label) {
        AdvancedSelectBox picker = getHostPicker();
        picker.pickOption(label);

        Console.withBrowser(browser).waitUntilFinished();
    }

    public AdvancedSelectBox getHostPicker() {
        return getContextPicker(PropUtils.get("navigation.context.host.id"));
    }


    public AdvancedSelectBox getProfilePicker() {
        return getContextPicker(PropUtils.get("navigation.context.profile.id"));
    }

    private AdvancedSelectBox getContextPicker(String label) {
        WebElement pickerRoot = getContextPickerRootByLabel(label);
        AdvancedSelectBox selectBox = Graphene.createPageFragment(AdvancedSelectBox.class,
                pickerRoot);

        return selectBox;

    }

    private WebElement getContextPickerRootByLabel(String label) {
        By selector = By.id(label);
        WebElement element = browser.findElement(selector);
        return element;
    }

    @Deprecated
    public ResourceTableFragment getResourceTable() {
        String cssClass = PropUtils.get("resourcetable.class");
        By selector = ByJQuery.selector("." + cssClass + ":visible");
        WebElement tableRoot = getContentRoot().findElement(selector);
        ResourceTableFragment table = Graphene.createPageFragment(ResourceTableFragment.class, tableRoot);

        return table;
    }

    @Deprecated
    public <T extends WizardWindow> T addResource(Class<T> clazz) {
        String label = PropUtils.get("config.shared.add.label");
        return addResource(clazz, label);
    }

    @Deprecated
    public WizardWindow addResource() {
        return addResource(WizardWindow.class);
    }

    @Deprecated
    public <T extends WindowFragment> T removeResource(String name, Class<T> clazz) {
        selectByName(name);
        String label = PropUtils.get("config.shared.remove.label");
        clickButton(label);

        T window = Console.withBrowser(browser).openedWindow(clazz);

        return window;
    }

    @Deprecated
    public ConfirmationWindow removeResource(String name) {
        return removeResource(name, ConfirmationWindow.class);
    }

    /**
     * Select resource based on its name in firt column of resource table.
     *
     * @param name Name of the resource.
     */
    @Deprecated
    public ResourceTableRowFragment selectByName(String name) {
        return getResourceTable().selectRowByText(0, name);
    }

    /**
     * Select resource based on its name in first column of resource table and then
     * click on view option
     *
     * @param name Name of the resource.
     */
    @Deprecated
    public void viewByName(String name) {
        ResourceTableRowFragment row = selectByName(name);
        row.view();
    }

    public void navigate(String profile) {
        Graphene.goTo(DomainConfigEntryPoint.class);
        Console.withBrowser(browser).waitUntilLoaded();
        pickProfile(profile);
        navigate();
    }
}
