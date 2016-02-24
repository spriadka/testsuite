package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
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
@Category(Standalone.class)
public class ManagedDeploymentsTestCase {

    private static final String FILE_PATH = "src/test/resources/";
    private static final String FILE_NAME = "mockWar.war";
    private static final String NAME = "n_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String RUNTIME_NAME = "rn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
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
    public void basicDeployment() throws Exception {
        navigation.step(FinderNames.DEPLOYMENT).selectColumn().invoke("Add");
        File deployment = new File(FILE_PATH + FILE_NAME);

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        Library.letsSleep(1000);
        assertTrue("Deployment wizard should close", result);
        ops.verifyExists(NAME);
    }

    @Test
    @InSequence(1)
    public void disableDeployment() throws Exception {

        navigation.step(FinderNames.DEPLOYMENT, NAME).selectRow().invoke("Disable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDisabled(NAME);
    }

    @Test
    @InSequence(2)
    public void enableDeployment() throws Exception {

        navigation.step(FinderNames.DEPLOYMENT, NAME).selectRow().invoke("Enable");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsEnabled(NAME);
    }


    @Test
    @InSequence(3)
    public void removeDeployment() throws Exception {
        Console.withBrowser(browser).waitUntilLoaded();
        Library.letsSleep(1000);
        navigation.step(FinderNames.DEPLOYMENT, NAME).selectRow().invoke("Remove");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyDoesNotExist(NAME);
    }

}
