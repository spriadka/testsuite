package org.jboss.hal.testsuite.fragment.config.datasource;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableFragment;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

/**
 * @author jcechace
 */
public class DatasourceWizard extends WizardWindow {

    private static final By DETECTED_DRIVER_BUTTON = By.xpath(".//div[text()='Detected Driver']");

    public TestConnectionWindow testConnection() {
        String label = PropUtils.get("config.datasources.configarea.connection.test.label");
        clickButton(label);
        Console.withBrowser(browser).waitUntilFinished();

        String windowTitle =  PropUtils.get("config.datasources.window.connection.test.head.label");
        TestConnectionWindow window =  Console.withBrowser(browser)
                .openedWindow(windowTitle, TestConnectionWindow.class);

        return window;
    }

    public void switchToDetectedDriver() {
        root.findElement(DETECTED_DRIVER_BUTTON).click();
    }

    public ResourceTableFragment getResourceTable() {
        return Graphene.createPageFragment(ResourceManager.class, root).getResourceTable();
    }

}
