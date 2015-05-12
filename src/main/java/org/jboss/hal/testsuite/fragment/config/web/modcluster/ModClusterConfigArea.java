package org.jboss.hal.testsuite.fragment.config.web.modcluster;

import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.ConfigFragment;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class ModClusterConfigArea extends ConfigAreaFragment {

    private static final String ADVERTISING = "Advertising";
    private static final String SESSIONS = "Sessions";
    private static final String WEB_CONTEXTS = "Web Contexts";
    private static final String PROXIES = "Proxies";
    private static final String SSL = "SSL";
    private static final String NETWORKING = "Networking";

    public ConfigFragment advertising(){
        return switchTo(ADVERTISING);
    }

    public ConfigFragment sessions(){
        return switchTo(SESSIONS);
    }

    public ConfigFragment webContexts(){
        return switchTo(WEB_CONTEXTS);
    }

    public ConfigFragment proxies(){
        return switchTo(PROXIES);
    }

    public ConfigFragment ssl(){
        return switchTo(SSL);
    }

    public ConfigFragment networking(){
        return switchTo(NETWORKING);
    }
}
