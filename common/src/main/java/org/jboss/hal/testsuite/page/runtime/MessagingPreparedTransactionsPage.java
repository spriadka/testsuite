package org.jboss.hal.testsuite.page.runtime;

import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.page.config.ConfigurationPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;

public class MessagingPreparedTransactionsPage extends ConfigurationPage implements Navigatable {

    @Override
    public void navigate() {
        new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "Messaging - ActiveMQ")
                .step("Settings", "Messaging Provider")
                .step("Messaging Provider", "default")
                .selectRow()
                .invoke("Prepared Transactions");
        Application.waitUntilVisible();
    }

    public void selectPreparedTransaction(String xId) {
        getResourceManager().selectByName(xId);
    }
}
