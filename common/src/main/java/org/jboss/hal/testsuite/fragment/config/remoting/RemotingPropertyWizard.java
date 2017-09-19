package org.jboss.hal.testsuite.fragment.config.remoting;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;

public class RemotingPropertyWizard extends WizardWindow {
    private static final String NAME = "name";
    private static final String VALUE = "value";

    public RemotingPropertyWizard name(String value) {
        getEditor().text(NAME, value);
        return this;
    }

    public RemotingPropertyWizard value(String value) {
        getEditor().text(VALUE, value);
        return this;
    }
}
