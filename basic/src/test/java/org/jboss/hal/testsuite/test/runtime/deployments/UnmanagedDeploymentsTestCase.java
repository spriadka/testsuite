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
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.runtime.StandaloneDeploymentsArea;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DeploymentPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
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
    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private DeploymentPage page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, DeploymentPage.class);
    }

    @AfterClass
    public static void cleanUp() {
        ops.undeploy(NAME);
    }

    @Test
    @InSequence(0)
    public void createDeployment() {
        navigation.addAddress(FinderNames.DEPLOYMENT).selectColumn();
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

        navigation.addAddress(FinderNames.DEPLOYMENT, NAME).selectRow().invoke("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);
        assertTrue("Deployment should be enabled", ops.isEnabled(NAME));
    }

    @Test
    @InSequence(2)
    public void disableDeployment() {

        navigation.addAddress(FinderNames.DEPLOYMENT, NAME).selectRow().invoke("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);
        assertFalse("Deployment should be disabled", ops.isEnabled(NAME));
    }

    @Test
    @InSequence(3)
    public void removeDeployment() {
        Console.withBrowser(browser).waitUntilLoaded();
        Library.letsSleep(1000);
        navigation.addAddress(FinderNames.DEPLOYMENT, NAME).selectRow().invoke("Remove");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(1000);

        assertFalse("Deployment should not exist", ops.exists(NAME));
    }

}
