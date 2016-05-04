package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;
import org.jboss.hal.testsuite.finder.Column;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
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
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

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

    private static final String DEPLOYMENT_TBR_NAME = "n-tbr_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_TBR_RUNTIME_NAME = "rn-tbr_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_DISABLED_NAME = "n-disabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_DISABLED_RUNTIME_NAME = "rn-disabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_ENABLED_NAME = "n-enabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_ENABLED_RUNTIME_NAME = "rn-enabled_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final DeploymentsOperations ops = new DeploymentsOperations(client);
    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private DeploymentPage page;

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException {
        new Administration(client).reloadIfRequired();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_TBR_NAME)
                .runtimeName(DEPLOYMENT_TBR_RUNTIME_NAME)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_DISABLED_NAME)
                .runtimeName(DEPLOYMENT_DISABLED_RUNTIME_NAME)
                .disabled()
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_ENABLED_NAME)
                .runtimeName(DEPLOYMENT_ENABLED_RUNTIME_NAME)
                .build());
    }

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, DeploymentPage.class);
    }

    @AfterClass
    public static void cleanUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException, OperationException {
        try {
            ops.undeploy(NAME);
            ops.undeploy(DEPLOYMENT_DISABLED_NAME);
            ops.undeploy(DEPLOYMENT_ENABLED_NAME);
            ops.undeployIfExists(DEPLOYMENT_TBR_NAME);
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void createDeployment() throws Exception {
        Column column = navigation.step(FinderNames.DEPLOYMENT).selectColumn();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        column.invoke("Add");
        File deployment = new File(FILE_PATH + FILE_NAME);

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

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
        ops.verifyDeploymentExists(NAME);
    }

    @Test
    public void enableDeployment() throws Exception {

        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_DISABLED_NAME).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDeploymentEnabled(DEPLOYMENT_DISABLED_NAME);
    }

    @Test
    public void disableDeployment() throws Exception {

        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_ENABLED_NAME).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDeploymentDisabled(DEPLOYMENT_ENABLED_NAME);
    }

    @Test
    public void removeDeployment() throws Exception {

        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_TBR_NAME).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke("Remove");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyDeploymentDoesNotExist(NAME);
    }

}
