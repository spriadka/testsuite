package org.jboss.hal.testsuite.fragment.config.messaging;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;

public class AddClusterConnectionWizard extends WizardWindow {

    public AddClusterConnectionWizard name(String name) {
        getEditor().text("name", name);
        return this;
    }

    public AddClusterConnectionWizard clusterConnectionAddress(String clusterConnectionAddress) {
        getEditor().text("cluster-connection-address", clusterConnectionAddress);
        return this;
    }

    public AddClusterConnectionWizard connectorName(String connectorName) {
        getEditor().text("connector-name", connectorName);
        return this;
    }

}
