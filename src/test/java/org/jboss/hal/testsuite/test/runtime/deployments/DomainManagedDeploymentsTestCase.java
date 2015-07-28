package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentContentRepositoryArea;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentPage;
import org.jboss.hal.testsuite.test.category.Domain;
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
public class DomainManagedDeploymentsTestCase {

    public static final String MAIN_SERVER_GROUP = "main-server-group";
    public static final String OTHER_SERVER_GROUP = "other-server-group";

    private static final String FILE_PATH = "src/test/resources/";
    private static final String FILE_NAME = "mockWar.war";
    private static final String NAME = "n_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String RUNTIME_NAME = "rn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static CliClient client = CliClientFactory.getClient();
    private static DeploymentsOperations ops = new DeploymentsOperations(client);

    @Drone
    WebDriver browser;

    @Page
    DomainDeploymentPage page;

    @AfterClass
    public static void cleanUp() {
        ops.undeploy(NAME);
    }

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(DomainDeploymentPage.class);
    }

    @Test
    @InSequence(0)
    public void createDeployment() throws InterruptedException {

        DeploymentContentRepositoryArea content = page.getDeploymentContent();
        File deployment = new File(FILE_PATH + FILE_NAME);
        page.select("Content Repository");
        DeploymentWizard wizard = content.add();

        boolean result = wizard.switchToManaged()
                .nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        assertTrue("Deployment wizard should close", result);
        assertTrue("Deployment should exist", ops.exists(NAME));
    }

    @Test
    @InSequence(1)
    public void assignDeploymentToServerGroup() {

        DeploymentContentRepositoryArea content = page.getDeploymentContent();
        page.select("Server Groups").select("main-server-group");

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

        page.select("Server Groups").select(MAIN_SERVER_GROUP).select(NAME).clickButton("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);

        assertTrue("Deployment should be enabled", ops.isEnabledInServerGroup(MAIN_SERVER_GROUP, NAME));
    }

    @Test
    @InSequence(3)
    public void disableDeployment() {

        page.select("Server Groups").select(MAIN_SERVER_GROUP).select(NAME).clickButton("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(10000);

        assertFalse("Deployment should be disabled", ops.isEnabledInServerGroup(MAIN_SERVER_GROUP, NAME));
    }

    @Test
    @InSequence(4)
    public void removeDeployment() {
        Console.withBrowser(browser).waitUntilLoaded();
        page.select("Server Groups").select(MAIN_SERVER_GROUP).select(NAME);
        page.unassign();
        page.select("Unassigned Content").select(NAME).remove();

        assertFalse("Deployment should not exist", ops.exists(NAME));
    }
}
