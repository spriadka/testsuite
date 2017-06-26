package org.jboss.hal.testsuite.page.config.elytron;

import static org.jboss.hal.testsuite.finder.FinderNames.SETTINGS;
import static org.jboss.hal.testsuite.page.config.elytron.ElytronPageConstants.ELYTRON_SUBSYTEM_LABEL;

public class ElytronOtherOtherPage extends AbstractElytronConfigPage<ElytronOtherOtherPage> {

    @Override
    public ElytronOtherOtherPage navigateToApplication() {
        getSubsystemNavigation(ELYTRON_SUBSYTEM_LABEL).step(SETTINGS, "Other").openApplication();
        switchTab("Other");
        return this;
    }
}
