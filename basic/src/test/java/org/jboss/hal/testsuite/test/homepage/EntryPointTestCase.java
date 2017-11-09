package org.jboss.hal.testsuite.test.homepage;

import org.junit.Assert;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.net.URL;

/**
 * @author jcechace
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class EntryPointTestCase {
    @Drone
    private WebDriver browser;

    @Page
    private HomePage homePage;

    @Test
    public void entryPoint() {
        URL consoleURL = ConfigUtils.getUrl();
        browser.navigate().to(consoleURL);
        Console.withBrowser(browser).waitUntilLoaded();

        String currentUrl = browser.getCurrentUrl();

        Assert.assertTrue("Home page  should be an entry point to console",
                currentUrl.endsWith("#home"));
    }
}
