package org.jboss.hal.testsuite.fragment.shared.util;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableFragment;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableRowFragment;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author jcechace
 */
public class ResourceManager extends BaseFragment {

    /**
     * Get resource table on page
     * @return {@link ResourceTableFragment} if found
     */
    public ResourceTableFragment getResourceTable() {
        return getResourceTableInElement((WebElement) null);
    }

    /**
     * Get resource table in element
     * @param element element containing resource table
     * @return {@link ResourceTableFragment} if found
     */
    public ResourceTableFragment getResourceTableInElement(WebElement element) {
        String cssClass = PropUtils.get("resourcetable.class");
        By selector = ByJQuery.selector("table:has('." + cssClass + ":visible')");

        WebElement tableRoot;

        if (element == null) {
            tableRoot = getRoot().findElement(selector);
        } else {
            tableRoot = element.findElement(selector);
        }

        return Graphene.createPageFragment(ResourceTableFragment.class, tableRoot);
    }

    /**
     * Get resource table in element defined by {@link By} selector
     * @param elementSelector selector describing element containing resource table
     * @return {@link ResourceTableFragment} if found
     */
    public ResourceTableFragment getResourceTableInElement(By elementSelector) {
        return getResourceTableInElement(getRoot().findElement(elementSelector));
    }

    /**
     * Select resource based on its name in first column of resource table.
     *
     * @param name Name of the resource.
     */
    public ResourceTableRowFragment selectByName(String name) {
        return getResourceTable().selectRowByText(0, name);
    }

    public <T extends WizardWindow> T addResource(Class<T> clazz, String label) {
        clickButton(label);

        T wizard = Console.withBrowser(browser).openedWizard(clazz);

        return wizard;
    }

    public <T extends WizardWindow> T addResource(Class<T> clazz) {
        String label = PropUtils.get("config.shared.add.label");
        return addResource(clazz, label);
    }

    public WizardWindow addResource() {
        return addResource(WizardWindow.class);
    }

    public <T extends WindowFragment> T removeResource(String name, Class<T> clazz) {
        selectByName(name);
        String label = PropUtils.get("config.shared.remove.label");
        clickButton(label);

        T window = Console.withBrowser(browser).openedWindow(clazz);

        return window;
    }

    public ConfirmationWindow removeResource(String name) {
        return removeResource(name, ConfirmationWindow.class);
    }

    /**
     * When 'Reload required' window is expected use {@link ResourceManager#removeResource(String)}
     * .{@link ConfirmationWindow#confirmAndDismissReloadRequiredMessage()} instead!
     */
    public void removeResourceAndConfirm(String name) {
        ConfirmationWindow confirmationWindow = removeResource(name, ConfirmationWindow.class);
        confirmationWindow.confirm();
    }

    /**
     * Select resource based on its name in first column of resource table and then
     * click on view option
     *
     * @param name Name of the resource.
     */
    public void viewByName(String name) {
        ResourceTableRowFragment row = selectByName(name);
        row.view();
    }

    public List<String> listResources() {
        return getResourceTable().getTextInColumn(0);
    }

}
