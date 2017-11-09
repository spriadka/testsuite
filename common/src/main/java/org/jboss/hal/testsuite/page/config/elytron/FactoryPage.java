package org.jboss.hal.testsuite.page.config.elytron;

import static org.jboss.hal.testsuite.finder.FinderNames.SETTINGS;
import static org.jboss.hal.testsuite.page.config.elytron.ElytronPageConstants.ELYTRON_SUBSYTEM_LABEL;

public class FactoryPage extends AbstractElytronConfigPage<FactoryPage> {

    @Override
    public FactoryPage navigateToApplication() {
        getSubsystemNavigation(ELYTRON_SUBSYTEM_LABEL).step(SETTINGS, "Factory").openApplication();
        return this;
    }

    public FactoryPage selectFactory(final String factoryLabel) {
        switchSubTab(factoryLabel);
        return this;
    }
}
