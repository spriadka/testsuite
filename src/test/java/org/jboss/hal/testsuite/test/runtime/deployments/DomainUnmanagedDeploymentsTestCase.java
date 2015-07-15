package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentContentRepositoryArea;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentPage;
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
@Category(Domain.class)
public class DomainUnmanagedDeploymentsTestCase {

    public static final String MAIN_SERVER_GROUP = "main-server-group";
    public static final String OTHER_SERVER_GROUP = "other-server-group";

    private static final String FILE_PATH = "src/test/resources/";
    private static final String FILE_NAME = "mockWar.war";
    private static final String NAME = "n_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String RUNTIME_NAME = "rn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static CliClient client = CliClientFactory.getDomainClient("full-ha");
    private static DeploymentsOperations ops = new DeploymentsOperations(client);

    @Drone
    WebDriver browser;

    @Page
    DomainDeploymentPage page;

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(DomainDeploymentPage.class);
    }

    @AfterClass
    public static void cleanUp() {
        ops.undeploy(NAME);
    }

    @Test
    @InSequence(0)
    public void createDeployment() throws InterruptedException {
        DeploymentContentRepositoryArea content = page.getDeploymentContent();
        File deployment = new File(FILE_PATH + FILE_NAME);
        page.selectMenu("Content Repository");
        DeploymentWizard wizard = content.add();

        wizard.switchToUnmanaged()
                .nextFluent()
                .path(deployment.getAbsolutePath())
                .isArchive(true)
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        boolean result = wizard.isClosed();

        assertTrue("Deployment wizard should close", result);
        assertTrue("Deployment should exist", ops.exists(NAME));
    }

    @Test
    @InSequence(1)
    public void assignDeploymentToServerGroup() {
        Console.withBrowser(browser).waitUntilLoaded();
        DeploymentContentRepositoryArea content = page.getDeploymentContent();
        page.selectMenu("Server Groups").selectMenu("main-server-group");

        DeploymentWizard wizard = content.add();

        boolean result = wizard.switchToRepository()
                .nextFluent()
                .finish();


        assertTrue("Deployment wizard should close", result);
        assertTrue("Deployment should be assigned to server group.", ops.isAssignedToServerGroup(MAIN_SERVER_GROUP, NAME));
    }

    @Test
    @InSequence(2)
    public void enableDeployment() {

        page.selectMenu("Server Groups").selectMenu(MAIN_SERVER_GROUP).selectMenu(NAME).clickButton("(En/Dis)able");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);
        assertTrue("Deployment should be enabled", ops.isEnabledInServerGroup(MAIN_SERVER_GROUP, NAME));
    }


    @Test
    @InSequence(3)
    public void disableDeployment() {

        page.selectMenu("Server Groups").selectMenu(MAIN_SERVER_GROUP).selectMenu(NAME).clickButton("(En/Dis)able");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);
        assertFalse("Deployment should be enabled", ops.isEnabledInServerGroup(MAIN_SERVER_GROUP, NAME));
    }

    @Test
    @InSequence(4)
    public void removeDeployment() {
        Console.withBrowser(browser).waitUntilLoaded();
        Library.letsSleep(10000);
        page.selectMenu("Server Groups").selectMenu(MAIN_SERVER_GROUP).selectMenu(NAME);
        page.unassign();
        page.selectMenu("Unassigned Content").selectMenu(NAME).remove();

        assertFalse("Deployment should not exist", ops.exists(NAME));
    }
}
