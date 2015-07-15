package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.runtime.StandaloneDeploymentsArea;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.page.runtime.DeploymentPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class UnmanagedDeploymentsTestCase {

    private static final String FILE_PATH = "src/test/resources/";
    private static final String FILE_NAME = "mockWar.war";
    private static final String NAME = "n_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String RUNTIME_NAME = "rn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static CliClient client = CliClientFactory.getClient();
    private static DeploymentsOperations ops = new DeploymentsOperations(client);

    @Drone
    private WebDriver browser;

    @Page
    private DeploymentPage page;

    @Before
    public void before() {
        browser.navigate().refresh();
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Graphene.goTo(DeploymentPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @AfterClass
    public static void cleanUp() {
        ops.undeploy(NAME);
    }

    @Test
    @InSequence(0)
    public void createDeployment() {
        StandaloneDeploymentsArea content = page.getDeploymentContent();
        File deployment = new File(FILE_PATH + FILE_NAME);

        DeploymentWizard wizard = content.add();

        wizard.switchToUnmanaged()
                .nextFluent()
                .path(deployment.getAbsolutePath())
                .isArchive(true)
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .enable(false)
                .finish();

        boolean result = wizard.isClosed();

        assertTrue("Deployment wizard should close", result);
        assertTrue("Deployment should exist", ops.exists(NAME));
    }

    @Test
    @InSequence(1)
    public void enableDeployment() {

        page.select(NAME).clickButton("Enable");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }

        assertTrue("Deployment should be enabled", ops.isEnabled(NAME));
    }

    @Test
    @InSequence(2)
    public void disableDeployment() {

        page.select(NAME).clickButton("Disable");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }

        assertFalse("Deployment should be disabled", ops.isEnabled(NAME));
    }

    @Test
    @InSequence(3)
    public void removeDeployment() {

        StandaloneDeploymentsArea content = page.getDeploymentContent();
        page.select(NAME).remove();

        assertFalse("Deployment should not exist", ops.exists(NAME));
    }

}
