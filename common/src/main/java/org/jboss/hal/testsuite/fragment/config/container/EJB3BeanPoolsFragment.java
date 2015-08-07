package org.jboss.hal.testsuite.fragment.config.container;

import org.jboss.hal.testsuite.fragment.ConfigFragment;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class EJB3BeanPoolsFragment extends ConfigFragment {

    public EJB3BeanPoolWizard addBeanPool(){
        return getResourceManager().addResource(EJB3BeanPoolWizard.class);
    }

    public void removeBeanPool(String name){
        getResourceManager().removeResourceAndConfirm(name);
    }
}
