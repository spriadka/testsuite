package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.runtime.StandaloneDeploymentsArea;
import org.jboss.hal.testsuite.page.runtime.DeploymentPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
        browser.navigate().refresh();
        Graphene.goTo(DeploymentPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Console.withBrowser(browser).maximizeWindow();
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

        boolean result = wizard.uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        assertTrue("Deployment wizard should close", result);
        assertTrue("Deployment should exist", ops.exists(NAME));
    }

    @Ignore
    @Test
    @InSequence(1)
    public void enableDeployment() {
        StandaloneDeploymentsArea content = page.getDeploymentContent();

        content.changeState(NAME);

        assertTrue("Deployment should be enabled", ops.isEnabled(NAME));
    }

    @Ignore
    @Test
    @InSequence(2)
    public void disableDeployment() {
        StandaloneDeploymentsArea content = page.getDeploymentContent();

        content.changeState(NAME);

        assertFalse("Deployment should be disabled", ops.isEnabled(NAME));
    }

    @Test
    @InSequence(3)
    public void removeDeployment() {
        StandaloneDeploymentsArea content = page.getDeploymentContent();
        content.removeAndConfirm(NAME);

        assertFalse("Deployment should not exist", ops.exists(NAME));
    }

}
