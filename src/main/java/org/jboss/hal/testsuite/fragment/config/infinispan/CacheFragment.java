package org.jboss.hal.testsuite.fragment.config.infinispan;

import org.jboss.hal.testsuite.fragment.ConfigFragment;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class CacheFragment extends ConfigFragment{

    public CacheWizard addCache(){
        return getResourceManager().addResource(CacheWizard.class);
    }
}
