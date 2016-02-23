package org.jboss.hal.testsuite.test.configuration.JCA;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Created by pcyprian on 21.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class BootstrapContextsTestCase {
    private static final String NAME = "test-BC";

    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=jca/bootstrap-context=" + NAME);
    private ResourceAddress address = new ResourceAddress(path);
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;
    CliClient cliClient = CliClientFactory.getClient();

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
    }

    @Drone
    private WebDriver browser;
    @Page
    private JCAPage page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "JCA");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
        page.switchToBootstrapContextsTab();
    }

    @After()
    public void after() {
        cliClient.reload();
    }

    @Test
    public void addBootstrapContexts() {
        page.clickButton("Add");
        page.getWindowFragment().getEditor().text("name", NAME);
        page.getWindowFragment().getEditor().select("workmanager", "default");
        page.getWindowFragment().clickButton("Save");

        verifier.verifyResource(address, true);

        cliClient.executeCommand("/subsystem=jca/bootstrap-context=" + NAME + ":remove");

        verifier.verifyResource(address, false);
    }

    @Test
    public void removeBootstrapContexts() {
        page.clickButton("Add");
        page.getWindowFragment().getEditor().text("name", NAME);
        page.getWindowFragment().getEditor().select("workmanager", "default");
        page.getWindowFragment().clickButton("Save");
        verifier.verifyResource(address, true);

        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.clickButton("Remove");

        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }

        verifier.verifyResource(address, false);
    }

}
