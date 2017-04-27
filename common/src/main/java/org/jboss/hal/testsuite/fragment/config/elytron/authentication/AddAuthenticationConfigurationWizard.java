package org.jboss.hal.testsuite.fragment.config.elytron.authentication;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;

public class AddAuthenticationConfigurationWizard extends WizardWindow {

    public AddAuthenticationConfigurationWizard name(String name) {
        getEditor().text("name", name);
        return this;
    }
}
