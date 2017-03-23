package org.jboss.hal.testsuite.fragment.config.messaging;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;

public class AddDiscoveryGroupWizard extends WizardWindow {

    public AddDiscoveryGroupWizard name(String name) {
        getEditor().text("name", name);
        return this;
    }

    public AddDiscoveryGroupWizard jgroupsChannel(String jgroupsChannel) {
        getEditor().text("jgroups-channel", jgroupsChannel);
        return this;
    }

    public AddDiscoveryGroupWizard socketBinding(String socketBinding) {
        getEditor().text("socket-binding", socketBinding);
        return this;
    }

}
