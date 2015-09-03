package org.jboss.hal.testsuite.fragment.config.messaging;

import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;

/**
 * Created by pcyprian on 3.9.15.
 */
public class MessagingConfigArea extends ConfigAreaFragment {

    public ConfigPropertiesFragment propertiesConfig() {
        return switchTo("Properties", ConfigPropertiesFragment.class);
    }
}
