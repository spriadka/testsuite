package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.Row;
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
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>, jstefl <jstefl@redhat.com>
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

    private static final String DEPLOYMENT_ASSIGN_NAME = "an_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_ASSIGN_RUNTIME_NAME = "arn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_ASSIGN_CHECK_NAME = "acn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_ASSIGN_CHECK_RUNTIME_NAME = "acrn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_TBR_NAME = "n-tbr_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_TBR_RUNTIME_NAME = "rn-tbr_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_DISABLED_NAME = "n-disabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_DISABLED_RUNTIME_NAME = "rn-disabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_ENABLED_NAME = "n-enabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_ENABLED_RUNTIME_NAME = "rn-enabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MED_1 = "n-med_dom_1" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MED_1_RUNTIMME_NAME = "rn-med_dom_1" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MED_2 = "n-med_dom_2" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MED_2_RUNTIMME_NAME = "rn-med_dom_2" + RandomStringUtils.randomAlphanumeric(5) + ".war";


    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static Operations operations = new Operations(client);
    private static Administration administration = new Administration(client);
    private static DeploymentsOperations ops = new DeploymentsOperations(client);

    @Drone
    WebDriver browser;

    @Page
    DeploymentsPage page;


    @AfterClass
    public static void cleanUp() throws IOException, CommandFailedException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.undeployIfExists(NAME);
            ops.undeployIfExists(DEPLOYMENT_DISABLED_NAME);
            ops.undeployIfExists(DEPLOYMENT_ENABLED_NAME);
            ops.undeployIfExists(DEPLOYMENT_ASSIGN_NAME);
            ops.undeployIfExists(DEPLOYMENT_ASSIGN_CHECK_NAME);
            ops.undeployIfExists(DEPLOYMENT_TBR_NAME);
            ops.undeployIfExists(DEPLOYMENT_MED_1);
            ops.undeployIfExists(DEPLOYMENT_MED_2);
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException {
        administration.reloadIfRequired();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_TBR_NAME)
                .runtimeName(DEPLOYMENT_TBR_RUNTIME_NAME)
                .particularGroup(MAIN_SERVER_GROUP)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_DISABLED_NAME)
                .runtimeName(DEPLOYMENT_DISABLED_RUNTIME_NAME)
                .particularGroup(MAIN_SERVER_GROUP)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_ENABLED_NAME)
                .runtimeName(DEPLOYMENT_ENABLED_RUNTIME_NAME)
                .particularGroup(MAIN_SERVER_GROUP)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_ASSIGN_NAME)
                .runtimeName(DEPLOYMENT_ASSIGN_RUNTIME_NAME)
                .particularGroup(OTHER_SERVER_GROUP)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MED_1)
                .runtimeName(DEPLOYMENT_MED_1_RUNTIMME_NAME)
                .particularGroup(OTHER_SERVER_GROUP)
                .disabled()
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MED_1)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MED_2)
                .runtimeName(DEPLOYMENT_MED_2_RUNTIMME_NAME)
                .particularGroup(OTHER_SERVER_GROUP)
                .disabled()
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MED_2)).assertSuccess();
    }

    @Test
    public void createDeployment() throws Exception {
        page.navigateToColumnInContentRepository().invoke("Add");
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
        ops.verifyDeploymentExists(NAME);
    }

    @Test
    public void assignDeploymentToServerGroup() throws Exception {
        page.navigateToDeploymentColumnInServerGroup(MAIN_SERVER_GROUP).invoke("Add");

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.switchToRepository()
                .nextFluent()
                .enableDeploymentInGroup(DEPLOYMENT_ASSIGN_NAME)
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

        page.navigateToRowInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_DISABLED_NAME).invoke("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDeploymentEnabledInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_DISABLED_NAME);
    }

    @Test
    public void disableDeployment() throws Exception {
        page.navigateToRowInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_ENABLED_NAME).invoke("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDeploymentDisabledInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_ENABLED_NAME);
    }

    @Test
    public void removeDeployment() throws Exception {
        page.navigateToRowInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_TBR_NAME)
                .invoke("Unassign");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class)
                .confirm();

        page.navigateToRowInUnassignedContent(DEPLOYMENT_TBR_NAME)
                .invoke("Remove");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class)
                .confirm();

        ops.verifyDeploymentDoesNotExist(DEPLOYMENT_TBR_NAME);
    }

    @Test
    public void checkAssignDeploymentName() {
        //create
        page.navigateToColumnInContentRepository().invoke("Add");
        Console.withBrowser(browser).openedWizard(DeploymentWizard.class)
                .switchToManaged()
                .nextFluent()
                .uploadDeployment(new File(FILE_PATH + FILE_NAME))
                .nextFluent()
                .name(DEPLOYMENT_ASSIGN_CHECK_NAME)
                .runtimeName(DEPLOYMENT_ASSIGN_CHECK_RUNTIME_NAME)
                .finish();

        //assign
        page.navigateToDeploymentColumnInServerGroup(MAIN_SERVER_GROUP).invoke("Add");
        Console.withBrowser(browser).openedWizard(DeploymentWizard.class)
                .switchToRepository()
                .nextFluent()
                .enableDeploymentInGroup(DEPLOYMENT_ASSIGN_CHECK_NAME)
                .finish();

        //unassign and remove
        page.navigateToRowInServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_ASSIGN_CHECK_NAME).invoke("Unassign");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        page.navigateToRowInUnassignedContent(DEPLOYMENT_ASSIGN_CHECK_NAME).invoke("Remove");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        //create 2nd
        page.navigateToColumnInContentRepository().invoke("Add");
        Console.withBrowser(browser).openedWizard(DeploymentWizard.class)
                .switchToManaged()
                .nextFluent()
                .uploadDeployment(new File(FILE_PATH + FILE_NAME))
                .nextFluent()
                .name("testNew")
                .runtimeName("testNew")
                .finish();

        //assing 2nd
        page.navigateToRowInUnassignedContent("testNew").invoke("Assign");

        boolean checkNameResult = page.checkAssignDeploymentNameInAssignContent("testNew");
        assertTrue("Name in assign content should be name of deployment", checkNameResult);

        //remove2nd
        page.navigateToRowInUnassignedContent("testNew").invoke("Remove");
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
    }

    @Test
    public void medAssignDeploySuccessExpected() throws Exception {
        page.navigateToDeploymentColumnInServerGroup(MAIN_SERVER_GROUP).invoke("Add");

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.switchToRepository()
                .nextFluent()
                .enableDeploymentInGroup(DEPLOYMENT_MED_1)
                .finish();

        assertTrue("Deployment wizard should close", result);
        ops.verifyIsDeploymentAssignedToServerGroup(MAIN_SERVER_GROUP, DEPLOYMENT_MED_1);
    }

    @Test
    public void medBrowseContentOnUnassignedDeploymentSuccessExpected() throws Exception {
        Row row = page.navigateToRowInUnassignedContent(DEPLOYMENT_MED_2);
        row.invoke(FinderNames.BROWSE_CONTENT);

        List<String> itemsInDeploment = page.getDeploymentBrowsedContentItems();
        ops.verifyDeploymentContentDefault(itemsInDeploment);
    }
}
