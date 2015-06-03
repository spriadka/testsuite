package org.jboss.hal.testsuite.fragment.config.interfaces;

import org.jboss.hal.testsuite.fragment.ConfigFragment;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class NetworkInterfaceContentFragment extends ConfigFragment {

    public static final String NIC = "nic";
    private static final String NIC_MATCH = "nicMatch";
    private static final String LOOPBACK_ADDRESS = "loopbackAddress";

    public NetworkInterfaceWizard addInterface(){
        return getResourceManager().addResource(NetworkInterfaceWizard.class);
    }

    public void removeInterface(String interfaceName){
        getResourceManager().removeResourceAndConfirm(interfaceName);
    }

}
