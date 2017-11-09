package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DeploymentsPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
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

    private static final String DEPLOYMENT_ASSIGN_NAME = "an_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_ASSIGN_RUNTIME_NAME = "arn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_TBR_NAME = "n-tbr_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_TBR_RUNTIME_NAME = "rn-tbr_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_DISABLED_NAME = "n-disabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_DISABLED_RUNTIME_NAME = "rn-disabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_ENABLED_NAME = "n-enabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_ENABLED_RUNTIME_NAME = "rn-enabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static Operations operations = new Operations(client);
    private static Administration administration = new Administration(client);
    private static DeploymentsOperations ops = new DeploymentsOperations(client);

    @Drone
    WebDriver browser;

    @Page
    DeploymentsPage page;

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException {
        administration.reloadIfRequired();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_TBR_NAME)
                .runtimeName(DEPLOYMENT_TBR_RUNTIME_NAME)
                .particularGroup(MAIN_SERVER_GROUP)
                .unmanaged()
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_DISABLED_NAME)
                .runtimeName(DEPLOYMENT_DISABLED_RUNTIME_NAME)
                .particularGroup(MAIN_SERVER_GROUP)
                .unmanaged()
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_ENABLED_NAME)
                .runtimeName(DEPLOYMENT_ENABLED_RUNTIME_NAME)
                .particularGroup(MAIN_SERVER_GROUP)
                .unmanaged()
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_ASSIGN_NAME)
                .runtimeName(DEPLOYMENT_ASSIGN_RUNTIME_NAME)
                .particularGroup(OTHER_SERVER_GROUP)
                .unmanaged()
                .build());
    }

    @AfterClass
    public static void cleanUp() throws IOException, CommandFailedException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.undeployIfExists(NAME);
            ops.undeployIfExists(DEPLOYMENT_DISABLED_NAME);
            ops.undeployIfExists(DEPLOYMENT_ENABLED_NAME);
            ops.undeployIfExists(DEPLOYMENT_ASSIGN_NAME);
            ops.undeployIfExists(DEPLOYMENT_TBR_NAME);
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void createDeployment() throws Exception {
        page.navigateToColumnInContentRepository().invoke("Add");
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
    public void assignDeploymentToServerGroup() throws Exception {
        page.navigateToDeploymentColumnInServerGroup(MAIN_SERVER_GROUP)
                .invoke("Add");
        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.switchToRepository()
                .nextFluent()
                .finish();
        assertTrue("Deployment wizard should close", result);

        ops.verifyIsDeploymentAssignedToServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_ASSIGN_NAME);
    }

    @Test
    public void enableDeployment() throws Exception {
        //disable deployment first in server group, so it can be enabled in test
        operations.invoke("undeploy", Address.of("server-group", MAIN_SERVER_GROUP)
                .and("deployment", DEPLOYMENT_DISABLED_NAME))
                .assertSuccess();

        page.navigateToRowInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_DISABLED_NAME)
                .invoke("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDeploymentEnabledInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_DISABLED_NAME);
    }


    @Test
    public void disableDeployment() throws Exception {
        page.navigateToRowInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_ENABLED_NAME)
                .invoke("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDeploymentDisabledInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_ENABLED_NAME);
    }

    @Test
    public void removeDeployment() throws Exception {
        page.navigateToRowInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_TBR_NAME)
                .invoke("Unassign");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        page.navigateToRowInUnassignedContent(DEPLOYMENT_TBR_NAME)
                .invoke("Remove");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyDeploymentDoesNotExist(DEPLOYMENT_TBR_NAME);
    }
}
