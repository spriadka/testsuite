package org.jboss.hal.testsuite.fragment.config.socketbindings;

import org.jboss.hal.testsuite.fragment.ConfigFragment;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class InboundSocketBindingFragment extends ConfigFragment {
    public InboundSocketBindingWizard addSocketBinding() {
        return getResourceManager().addResource(InboundSocketBindingWizard.class);
    }

    public void selectSocketBinding(String name) {
        getResourceManager().selectByName(name);
    }

    public void removeSocketBinding(String name) {
        getResourceManager().removeResource(name).confirmAndDismissReloadRequiredMessage();
    }
}
