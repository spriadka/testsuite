package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

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

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static DeploymentsOperations ops = new DeploymentsOperations(client);
    private FinderNavigation navigation;

    @Drone
    WebDriver browser;

    @Page
    DomainDeploymentPage page;

    @AfterClass
    public static void cleanUp() throws IOException {
        try {
            ops.undeploy(NAME);
        } finally {
            client.close();
        }
    }

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        new Administration(client).reloadIfRequired();
    }

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, DomainDeploymentPage.class);
    }

    @Test
    @InSequence(0)
    public void createDeployment() throws Exception {

        navigation.step(FinderNames.BROWSE_BY, "Content Repository").step("All Content");
        navigation.selectColumn().invoke("Add");
        File deployment = new File(FILE_PATH + FILE_NAME);

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.switchToManaged()
                .nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        assertTrue("Deployment wizard should close", result);
        ops.verifyExists(NAME);
    }

    @Test
    @InSequence(1)
    public void assignDeploymentToServerGroup() throws Exception {
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Server Groups")
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT);
        navigation.selectColumn().invoke("Add");

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.switchToRepository()
                .nextFluent()
                .finish();

        assertTrue("Deployment wizard should close", result);
        ops.verifyIsAssignedToServerGroup(MAIN_SERVER_GROUP, NAME);
    }

    @Test
    @InSequence(2)
    public void enableDeployment() throws Exception {
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Server Groups")
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT, NAME);
        navigation.selectRow().invoke("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsEnabledInServerGroup(MAIN_SERVER_GROUP, NAME);
    }

    @Test
    @InSequence(3)
    public void disableDeployment() throws Exception {

        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Server Groups")
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT, NAME);
        navigation.selectRow().invoke("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDisabledInServerGroup(MAIN_SERVER_GROUP, NAME);
    }

    @Test
    @InSequence(4)
    public void removeDeployment() throws Exception {
        Console.withBrowser(browser).waitUntilLoaded();
        Library.letsSleep(1000);
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Server Groups")
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT, NAME);
        navigation.selectRow().invoke("Unassign");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(1000);

        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Unassigned Content")
                .step("Unassigned", NAME);
        navigation.selectRow().invoke("Remove");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyDoesNotExist(NAME);
    }

    @Test
    @InSequence(5)
    public void checkAssingDeploymentName() {
        //create
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Content Repository").step("All Content");
        navigation.selectColumn().invoke("Add");
        File deployment = new File(FILE_PATH + FILE_NAME);
        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.switchToManaged()
                .nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();
        //assing
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Server Groups")
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT);
        navigation.selectColumn().invoke("Add");

        result = wizard.switchToRepository()
                .nextFluent()
                .finish();
        //unassing and remove
        Console.withBrowser(browser).waitUntilLoaded();
        Library.letsSleep(1000);
        Console.withBrowser(browser).refreshAndNavigate(DomainDeploymentPage.class);
        navigation.step(FinderNames.BROWSE_BY, "Server Groups")
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT, NAME);
        navigation.selectRow().invoke("Unassign");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(1000);

        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Unassigned Content")
                .step("Unassigned", NAME);
        navigation.selectRow().invoke("Remove");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(1000);
        //create 2nd
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Content Repository").step("All Content");
        navigation.selectColumn().invoke("Add");
        deployment = new File(FILE_PATH + FILE_NAME);

        result = wizard.switchToManaged()
                .nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name("testNew")
                .runtimeName(RUNTIME_NAME)
                .finish();
        //assing 2nd
        Console.withBrowser(browser).refreshAndNavigate(DomainDeploymentPage.class);
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Unassigned Content")
                .step("Unassigned", "testNew");
        navigation.selectRow().invoke("Assign");

        boolean checkNameResult = page.checkAssingDeploymentNameInAssingContent("testNew");

        assertTrue("Name in asssing content should be name of deployment", checkNameResult);
        //remove2nd
        new FinderNavigation(browser, DomainDeploymentPage.class).step(FinderNames.BROWSE_BY).selectColumn();
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Unassigned Content")
                .step("Unassigned", "testNew");
        navigation.selectRow().invoke("Remove");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        Library.letsSleep(1000);
    }
}
