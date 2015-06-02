package org.jboss.hal.testsuite.test.configuration.general;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.cli.DomainCliClient;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.test.category.Shared;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class WebAccessTestCase {

    private static CliClient client = CliClientFactory.getClient();
    @Drone
    public WebDriver browser;

    @AfterClass
    public static void afterClass(){
        toggleWebConsole(true);
    }

    @Test(expected=TimeoutException.class)
    public void disabledAccess(){
        toggleWebConsole(false);
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @Test
    public void enabledAccess(){
        toggleWebConsole(true);
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
    }

    private static void toggleWebConsole(boolean enabled){
        if(client instanceof DomainCliClient){
            client.writeAttribute(CliConstants.DOMAIN_HTTP_INTERFACE_ADDRESS, "console-enabled", Boolean.toString(enabled));
        }else{
            client.writeAttribute(CliConstants.STANDALONE_HTTP_INTERFACE_ADDRESS, "console-enabled", Boolean.toString(enabled));
        }
        client.reload();
    }
}
