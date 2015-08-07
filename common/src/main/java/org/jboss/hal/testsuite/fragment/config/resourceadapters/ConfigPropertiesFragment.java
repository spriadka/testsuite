package org.jboss.hal.testsuite.fragment.config.resourceadapters;

import org.jboss.hal.testsuite.fragment.ConfigFragment;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class ConfigPropertiesFragment extends ConfigFragment{

    public ConfigPropertyWizard addProperty(){
        return getResourceManager().addResource(ConfigPropertyWizard.class);
    }

    public void removeProperty(String key){
        getResourceManager().removeResourceAndConfirm(key);
    }
}
