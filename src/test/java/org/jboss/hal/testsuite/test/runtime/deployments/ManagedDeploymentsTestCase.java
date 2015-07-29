package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.runtime.StandaloneDeploymentsArea;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DeploymentPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ManagedDeploymentsTestCase {

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
        Console.withBrowser(browser).refreshAndNavigate(DeploymentPage.class);
    }

    @BeforeClass
    public static void setUp() throws IOException {
    }

    @AfterClass
    public static void cleanUp() {
        ops.undeploy(NAME);
    }

    @Test
    @InSequence(0)
    public void basicDeployment() throws InterruptedException {
        StandaloneDeploymentsArea content = page.getDeploymentContent();
        File deployment = new File(FILE_PATH + FILE_NAME);


        DeploymentWizard wizard = content.add();

        boolean result = wizard.nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        Library.letsSleep(1000);
        assertTrue("Deployment wizard should close", result);
        assertTrue("Deployment should exist", ops.exists(NAME));
    }

    @Test
    @InSequence(1)
    public void disableDeployment() {
        page.selectMenu(NAME).clickButton("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);
        assertFalse("Deployment should be disabled", ops.isEnabled(NAME));
    }

    @Test
    @InSequence(2)
    public void enableDeployment() {
        page.selectMenu(NAME).clickButton("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);
        assertTrue("Deployment should be enabled", ops.isEnabled(NAME));
    }




    @Test
    @InSequence(3)
    public void removeDeployment() {
        Console.withBrowser(browser).waitUntilLoaded();
        Library.letsSleep(10000);
        page.selectMenu(NAME).remove();

        assertFalse("Deployment should not exist", ops.exists(NAME));
    }

}
