package org.jboss.hal.testsuite.page.config;

import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.util.ConfigUtils;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
public class UndertowHTTPPage extends UndertowPage implements Navigatable {

    @Override
    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS);
        }
        navigation.addAddress(FinderNames.SUBSYSTEM, "Undertow")
                .addAddress("Settings", "HTTP")
                .selectRow();
        Application.waitUntilVisible();
    }

    public UndertowHTTPPage selectHTTPServer(String serverName) {
        getResourceManager().getResourceTable().selectRowByText(0, serverName).view();
        return this;
    }

    public void switchToAJPListeners() {
        switchSubTab("AJP Listener");
    }

    public void switchToHTTPListeners() {
        switchSubTab("HTTP Listener");
    }

    public void switchToHTTPSListeners() {
        switchSubTab("HTTPS Listener");
    }

    public void switchToHosts() {
        switchSubTab("Hosts");
    }

}
