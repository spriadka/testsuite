package org.jboss.hal.testsuite.fragment.config.jgroups;

import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9.10.15.
 */
public class JGroupsProtocolPropertiesFragment extends ConfigPropertiesFragment {

    public JGroupsProtocolPropertyWizard addProperty() {
        return getResourceManager().addResource(JGroupsProtocolPropertyWizard.class);
    }
}
