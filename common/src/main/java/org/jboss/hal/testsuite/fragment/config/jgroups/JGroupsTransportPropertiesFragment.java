package org.jboss.hal.testsuite.fragment.config.jgroups;

import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 9.10.15.
 */
public class JGroupsTransportPropertiesFragment extends ConfigPropertiesFragment {

    public JGroupsTransportPropertyWizard addProperty() {
        return getResourceManager().addResource(JGroupsTransportPropertyWizard.class);
    }

}
