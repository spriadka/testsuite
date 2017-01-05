package org.jboss.hal.testsuite.fragment.shared.util;

import com.google.common.base.Predicate;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jcechace
 */
public class ResourceManager extends BaseFragment {

    /**
     * Get resource table on page
     * @return {@link ResourceTableFragment} if found
     */
    public ResourceTableFragment getResourceTable() {
        String cssClass = PropUtils.get("resourcetable.class");
        By selector = ByJQuery.selector("table:has('." + cssClass + ":visible')");

        WebElement tableRoot = getRoot().findElement(selector);

        return Graphene.createPageFragment(ResourceTableFragment.class, tableRoot);
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

        return Console.withBrowser(browser).openedWizard(clazz);
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

        return Console.withBrowser(browser).openedWindow(clazz);
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

    /**
     * Checks if given resource is present in resource table
     * @param name name of resource
     * @return true if resource is present, false otherwise
     */
    public boolean isResourcePresent(String name) {
        try {
            Graphene.waitModel(browser)
                    .pollingEvery(100, TimeUnit.MILLISECONDS)
                    .withTimeout(2000, TimeUnit.MILLISECONDS)
                    .until((Predicate<WebDriver>) input -> getResourceTable().getRowByText(0, name) != null);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

}
