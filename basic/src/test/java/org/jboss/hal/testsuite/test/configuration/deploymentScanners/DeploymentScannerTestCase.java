package org.jboss.hal.testsuite.test.configuration.deploymentScanners;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.DeploymentScannerPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;

/**
 * Created by pcyprian on 22.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DeploymentScannerTestCase {
    private static final String NAME = "test";
    private static final String PATH = "deployments";

    private ModelNode path = new ModelNode("/subsystem=deployment-scanner/scanner=" + NAME);
    private ResourceAddress address = new ResourceAddress(path);
    static Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    private CliClient cliClient = CliClientFactory.getClient();
    private String add = "/subsystem=deployment-scanner/scanner=" + NAME + ":add(path=" + PATH + ", relative-to=jboss.server.base.dir)";


    @Drone
    private WebDriver browser;

    @Page
    private DeploymentScannerPage page;

    @After
    public void after() {
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        cliClient.reload();
    }

    @AfterClass
    public static void cleanUp() {
        dispatcher.close();
    }

    @Test
    public void addDeploymentScanner() {
        page.navigateToDeploymentScanners();
        page.clickButton("Add");
        page.getWindowFragment().getEditor().text("name", NAME);
        page.getWindowFragment().getEditor().text("path", "${jboss.server.base.dir:null}/" + PATH);
        page.getWindowFragment().clickButton("Save");

        verifier.verifyResource(address, true);

        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");

        verifier.verifyResource(address, false);
    }

    @Test
    public void removeDeploymentScanner() {
        cliClient.executeCommand(add);
        verifier.verifyResource(address, true);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.clickButton("Remove");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
        verifier.verifyResource(address, false);
    }

    @Test
    public void updatePath() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().text("path", "../standalone");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "path", "../standalone", 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateRelativePath() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().text("relative-to", "jboss");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "relative-to", "jboss", 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateRuntimeFailure() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().checkbox("runtime-failure-causes-rollback", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "runtime-failure-causes-rollback", true, 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateAutoDeployExploded() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().checkbox("auto-deploy-exploded", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "auto-deploy-exploded", true, 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateAutoDeployXml() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().checkbox("auto-deploy-xml", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "auto-deploy-xml", false, 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateAutoDeployZipped() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().checkbox("auto-deploy-zipped", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "auto-deploy-zipped", false, 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateScanEnabled() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().checkbox("scan-enabled", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "scan-enabled", false, 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateDeploymentTimeout() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().text("deployment-timeout", "10");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "deployment-timeout", "10", 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateScanInterval() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().text("scan-interval", "1");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "scan-interval", "1", 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

    @Test
    public void updateScanIntervalInvalidValue() {
        cliClient.executeCommand(add);

        page.navigateToDeploymentScanners();
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        page.getMainConfigFragment().edit().text("scan-interval", "-1");
        boolean finished = page.getMainConfigFragment().save();

        assertFalse("Config should not be saved.", finished);
        verifier.verifyAttribute(address, "scan-interval", "0", 500);
        cliClient.executeCommand("/subsystem=deployment-scanner/scanner=" + NAME + ":remove");
        verifier.verifyResource(address, false);
    }

}
