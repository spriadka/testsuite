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
import org.jboss.hal.testsuite.cli.CliClient;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

/**
 * @author jcechace
 */
public class ResourceManager extends BaseFragment {

    private String dmrPath;
    private CliClient cliClient;

    public void setDmrPath(String dmrPath) {
        this.dmrPath = dmrPath;
    }

    public void setCliClient(CliClient cliClient) {
        this.cliClient = cliClient;
    }

    public ResourceTableFragment getResourceTable() {
        String cssClass = PropUtils.get("resourcetable.class");
        By selector = ByJQuery.selector("." + cssClass + ":visible");
        WebElement tableRoot = getRoot().findElement(selector);
        ResourceTableFragment table = Graphene.createPageFragment(ResourceTableFragment.class, tableRoot);

        return table;
    }

    /**
     * Select resource based on its name in firt column of resource table.
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
     * Verifies that resource on given path exists in model
     *
     * @param dmrPath dmr address of the resource
     * @param expected <code>true</code> if resource is expected to exists, false otherwise
     */
    public void verifyResource(String dmrPath, boolean expected) {
        boolean exists = cliClient.executeForSuccess(dmrPath + ":read-resource()");
        if (exists) {
            Assert.assertTrue("Resource " + dmrPath + " should exist", exists);
        } else {
            Assert.assertFalse("Resource " + dmrPath + " should not exist", exists);
        }
    }

    /**
     * Verifies that resource on set path path exists in model
     *
     * @param expected <code>true</code> if resource is expected to exists, false otherwise
     */
    public void verifyResource(boolean expected) {
        if (this.dmrPath == null) {
            throw new IllegalStateException("DMR path not set");
        }
        verifyResource(this.dmrPath, expected);
    }

    /**
     * Verifies the value of attribute in model.
     *
     * @param name name of the attribute. If the name is camelCase it will be converted to camel-case.
     * @param expectedValue expected value
     * @param cliClient cli client to use
     */
    public void verifyAttribute(String name, String expectedValue, CliClient cliClient) {
        if (this.dmrPath == null) {
            throw new IllegalStateException("DMR path not set");
        }

        String dmrName = camelToDash(name);
        String actualValue = cliClient.readAttribute(dmrPath, dmrName);

        Assert.assertEquals("Attribute value is different in model.", expectedValue, actualValue);
    }

    /**
     * Verifies the value of attribute in model using previously set Cli Client.
     *
     * @param name name of the attribute. If the name is camelCase it will be converted to camel-case.
     * @param expectedValue expected value
     */
    public void verifyAttribute(String name, String expectedValue) {
        if (this.cliClient == null) {
            throw new IllegalStateException("Cli Client not set");
        }

        verifyAttribute(name, expectedValue, this.cliClient);
    }

    /**
     * Verify the value of attributes against model
     *
     * @param pairs Key-Value map of attribute names and values. If name is camelCase it will be coverted to camel-case
     * @param cliClient cli client to use
     */
    public void verifyAttributes(Map<String, String> pairs, CliClient cliClient) {
        for (Map.Entry<String, String> p : pairs.entrySet()) {
            verifyAttribute(p.getKey(), p.getValue(), cliClient);
        }
    }

    /**
     * Verify the value of attributes against model using previously set Cli Client.
     *
     * @param pairs Key-Value map of attribute names and values. If name is camelCase it will be coverted to camel-case
     */
    public void verifyAttributes(Map<String, String> pairs) {
        if (this.cliClient == null) {
            throw new IllegalStateException("Cli Client not set");
        }

        verifyAttributes(pairs, this.cliClient);
    }

    /**
     * Converts a camelCaseString to camel-case-string
     *
     * @param input
     * @return
     */
    private static String camelToDash(String input) {
        return input.replaceAll("\\B([A-Z])", "-$1" ).toLowerCase();
    }
}
