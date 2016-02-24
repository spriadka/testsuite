package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Domain;
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
public class DomainUnmanagedDeploymentsTestCase {

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

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, DomainDeploymentPage.class);
    }

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        new Administration(client).reloadIfRequired();
    }

    @AfterClass
    public static void cleanUp() throws IOException {
        try {
            ops.undeploy(NAME);
        } finally {
            client.close();
        }
    }

    @Test
    @InSequence(0)
    public void createDeployment() throws Exception {
        navigation.step(FinderNames.BROWSE_BY, "Content Repository").step("All Content");
        navigation.selectColumn().invoke("Add");
        File deployment = new File(FILE_PATH + FILE_NAME);

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        wizard.switchToUnmanaged()
                .nextFluent()
                .path(deployment.getAbsolutePath())
                .isArchive(true)
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        boolean result = wizard.isClosed();

        assertTrue("Deployment wizard should close", result);
        ops.verifyDeploymentExists(NAME);
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
        ops.verifyIsDeploymentAssignedToServerGroup(MAIN_SERVER_GROUP, NAME);
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

        ops.verifyIsDeploymentEnabledInServerGroup(MAIN_SERVER_GROUP, NAME);
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

        ops.verifyIsDeploymentDisabledInServerGroup(MAIN_SERVER_GROUP, NAME);
    }

    @Test
    @InSequence(4)
    public void removeDeployment() throws Exception {
        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Server Groups")
                .step(FinderNames.SERVER_GROUP, "main-server-group")
                .step(FinderNames.DEPLOYMENT, NAME);
        navigation.selectRow().invoke("Unassign");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        navigation.resetNavigation();
        navigation.step(FinderNames.BROWSE_BY, "Unassigned Content")
                .step("Unassigned", NAME);
        navigation.selectRow().invoke("Remove");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyDeploymentDoesNotExist(NAME);
    }
}
