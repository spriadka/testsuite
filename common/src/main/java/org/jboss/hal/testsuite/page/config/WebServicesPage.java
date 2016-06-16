package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#webservices")
public class WebServicesPage extends ConfigPage implements Navigatable {

    private static final By CONFIG_CONTENT = By.className(PropUtils.get("page.content.rhs.class"));

    @Override
    public ConfigAreaFragment getConfig() {
        return Graphene.createPageFragment(ConfigAreaFragment.class, getContentRoot().findElement(CONFIG_CONTENT));
    }

    public void navigate() {
        new FinderNavigation(browser, StandaloneConfigurationPage.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "Web Services")
                .selectRow()
                .invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }
}
