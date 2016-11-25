package org.jboss.hal.testsuite.test.configuration.general;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class WebAccessTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private static final Administration adminOps = new Administration(client);

    @Drone
    public WebDriver browser;

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        try {
            toggleWebConsole(true);
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test(expected = org.openqa.selenium.TimeoutException.class)
    @InSequence(0)
    public void disabledAccess() throws IOException, InterruptedException, TimeoutException {
        toggleWebConsole(false);
        Graphene.goTo(HomePage.class);
        browser.navigate().refresh();
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @Test
    @InSequence(1)
    public void enabledAccess() throws IOException, InterruptedException, TimeoutException {
        toggleWebConsole(true);
        Graphene.goTo(HomePage.class);
        browser.navigate().refresh();
        Console.withBrowser(browser).waitUntilLoaded();
    }

    private static void toggleWebConsole(boolean enabled) throws IOException, InterruptedException, TimeoutException {
        ops.writeAttribute(Address.coreService("management").and("management-interface", "http-interface"),
                "console-enabled", enabled);
        adminOps.reloadIfRequired();
    }
}
