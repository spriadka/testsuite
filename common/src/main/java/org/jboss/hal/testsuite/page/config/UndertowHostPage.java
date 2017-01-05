package org.jboss.hal.testsuite.page.config;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;

public class UndertowHostPage extends UndertowHTTPPage {

    public ConfigFragment switchToReferenceToFilterSubTab() {
        return getConfig().switchTo("Reference to Filter");
    }

    public void addReferenceToFilter(String filterName) {
        WizardWindow wizardWindow = getConfigFragment().getResourceManager().addResource();
        wizardWindow.getEditor().text("name", filterName);
        wizardWindow.finish();
    }
}
