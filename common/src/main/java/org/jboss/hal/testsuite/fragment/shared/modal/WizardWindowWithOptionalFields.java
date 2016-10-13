package org.jboss.hal.testsuite.fragment.shared.modal;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class WizardWindowWithOptionalFields extends WizardWindow {

    public void openAdvancedOptionsTab() {
        maximizeWindow();
        makeContentActiveForUserInput();
        final By disclosurePanelSelector = ByJQuery.selector(".form-edit-panel .gwt-DisclosurePanel tbody > tr > td > a");
        final WebElement disclosurePanel = root.findElement(disclosurePanelSelector);
        Graphene.waitGui().until().element(disclosurePanelSelector).is().visible();
        disclosurePanel.click();

        Graphene.waitGui(browser).until()
                .element(ByJQuery.selector(".form-edit-panel .gwt-DisclosurePanel tr:nth-child(2)"))
                .is()
                .visible();
    }

    private void makeContentActiveForUserInput() {
        root.findElement(ByJQuery.selector(".default-window-content")).click();
    }

    private void maximizeWindow() {
        root.findElement(By.ByClassName.className("icon-resize-full")).click();
    }

}
