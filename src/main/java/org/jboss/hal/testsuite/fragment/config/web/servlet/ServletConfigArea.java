package org.jboss.hal.testsuite.fragment.config.web.servlet;

import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.util.PropUtils;

public class ServletConfigArea extends ConfigAreaFragment {

    public ConfigFragment global() {
        String label = PropUtils.get("config.web.servlet.configarea.global.tab.label");
        return switchTo(label);
    }
}
