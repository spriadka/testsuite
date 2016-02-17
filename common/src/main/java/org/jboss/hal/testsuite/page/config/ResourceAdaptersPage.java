package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.Column;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.WindowState;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ResourceAdapterWizard;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ResourceAdaptersConfigArea;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#profile")
public class ResourceAdaptersPage extends ConfigurationPage implements Navigatable {

    private static final String
        RA_SUBSYSTEM_LABEL = "Resource Adapters",
        RA_COLUMN_LABEL = "Resource Adapter";

    public ResourceAdaptersConfigArea getConfigArea() {
        return getConfig(ResourceAdaptersConfigArea.class);
    }

    public ResourceAdaptersPage navigate2ra(String raName) {
        Row row = getSubsystemNavigation(RA_SUBSYSTEM_LABEL).addAddress(RA_COLUMN_LABEL, raName).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        return this;
    }

    public ResourceAdapterWizard addResourceAdapter() {
        Column column = getSubsystemNavigation(RA_SUBSYSTEM_LABEL).addAddress(RA_COLUMN_LABEL).selectColumn();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        column.invoke(FinderNames.ADD);
        return Console.withBrowser(browser).openedWizard(ResourceAdapterWizard.class);
    }

    public WindowState removeRa (String raName) {
        Row row = getSubsystemNavigation(RA_SUBSYSTEM_LABEL).addAddress(RA_COLUMN_LABEL, raName).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.REMOVE);
        return Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
    }

    public ConfigFragment switchConfigAreaTabTo(String tabLabel) {
        return getConfig(ConfigAreaFragment.class).switchTo(tabLabel);
    }

    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, ConfigUtils.getDefaultProfile())
                    .addAddress(FinderNames.SUBSYSTEM, "Resource Adapters");

        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "Resource Adapters");
        }
        navigation.selectColumn();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }
}
