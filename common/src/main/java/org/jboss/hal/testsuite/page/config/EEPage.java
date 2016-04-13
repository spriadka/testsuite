package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#ee")
public class EEPage extends ConfigurationPage implements Navigatable {

    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class);
            navigation.step(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .step(FinderNames.PROFILE, ConfigUtils.getDefaultProfile())
                    .step(FinderNames.SUBSYSTEM, "EE");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class);
            navigation.step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .step(FinderNames.SUBSYSTEM, "EE");
        }
        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }
}
