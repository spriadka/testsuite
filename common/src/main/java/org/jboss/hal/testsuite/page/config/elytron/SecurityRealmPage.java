package org.jboss.hal.testsuite.page.config.elytron;

import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.Console;

public class SecurityRealmPage extends ConfigPage {

    public void navigate() {
        getSubsystemNavigation("Security - Elytron")
                .step(FinderNames.SETTINGS, "Security Realm / Authentication")
                .selectRow()
                .invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }

    public SecurityRealmPage switchToFilesystemRealms() {
        switchSubTab("Filesystem Realm");
        return this;
    }
}
