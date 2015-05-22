package org.jboss.hal.testsuite.test.configuration.web;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.config.web.servlet.VirtualServersFragment;
import org.jboss.hal.testsuite.fragment.config.web.servlet.VirtualServersWizard;
import org.jboss.hal.testsuite.page.config.ServletPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.VIRTUAL_SERVER_SUBSYSTEM_ADDRESS;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class VirtualServersTestCase {

    private static final String NAME = "vs_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ALIAS = "alias_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String MODULE = "module_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String DMR_PATH = VIRTUAL_SERVER_SUBSYSTEM_ADDRESS + "=" + NAME;

    private static CliClient client = CliClientFactory.getClient();
    private static ResourceVerifier verifier = new ResourceVerifier(DMR_PATH, client);


    @Drone
    public WebDriver browser;

    @Page
    public ServletPage page;

    @AfterClass
    public static void cleanUp(){
        client.removeResource(DMR_PATH);
        client.reload(false);
    }

    @Before
    public void before(){
        browser.navigate().refresh();
        Graphene.goTo(ServletPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        browser.manage().window().maximize();
    }

    @Test
    @InSequence(0)
    public void createVirtualServer(){
        VirtualServersFragment fragment = page.getConfig().virtualServers();
        VirtualServersWizard wizard = fragment.addVirtualServer();

        boolean result =
                wizard.name(NAME)
                .alias(ALIAS)
                .defaultModule(MODULE)
                .finish();

        assertTrue("Wizard window should be closed", result);
        verifier.verifyResource(true);
    }

    @Test
    @InSequence(1)
    public void removeVirtualServer(){
        VirtualServersFragment fragment = page.getConfig().virtualServers();
        fragment.removeVirtualServer(NAME);
        verifier.verifyResource(false);
    }

}
