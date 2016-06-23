package org.jboss.hal.testsuite.page.domain;

import org.jboss.hal.testsuite.page.ConfigPage;

/**
 * Represents the page with server group details (Attributes, JVM Configuration and System Properties).
 *
 * @author msmerek
 */
public class ServerGroupDetailPage extends ConfigPage {

    public void switchToSystemProperties() {
        getConfig().switchTo("System Properties");
    }

    public void switchToJVMConfiguration() {
        getConfig().switchTo("JVM Configuration");
    }

}
