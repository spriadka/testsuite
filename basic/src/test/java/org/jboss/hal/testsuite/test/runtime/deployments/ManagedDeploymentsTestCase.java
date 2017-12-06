package org.jboss.hal.testsuite.test.runtime.deployments;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.KnownIssue;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;

import org.jboss.hal.testsuite.finder.Column;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;

import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.TreeNavigationPage;
import org.jboss.hal.testsuite.page.runtime.DeploymentsPage;
import org.jboss.hal.testsuite.page.runtime.StandaloneDeploymentEntryPoint;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author mkrajcov <mkrajcov@redhat.com>, jstefl <jstefl@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ManagedDeploymentsTestCase {

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

    private static final String DEPLOYMENT_MANAGED_ENABLED_1 = "n-med_1" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_ENABLED_1_RUNTIMME_NAME = "rn-med_1" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_ENABLED_2 = "n-med-2_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_ENABLED_2_RUNTIMME_NAME = "rn-med_2" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3 = "n-med_3" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3_RUNTIMME_NAME = "rn-med_3" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_4 = "n-med_4" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_4_RUNTIMME_NAME = "rn-med_4" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_5 = "n-med_5" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_DISABLED_5_RUNTIMME_NAME = "rn-med_5" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_6 = "n-med_6" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_EXPLODED_DISABLED_6_RUNTIMME_NAME = "rn-med_6" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7 = "n-med_7" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7_RUNTIMME_NAME = "rn-med_7" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_ENABLED_8 = "n-med_8" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_ENABLED_8_RUNTIMME_NAME = "rn-med_8" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9 = "n-med_9" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9_RUNTIMME_NAME = "rn-med_9" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_UNMANAGED_10 = "n-med_10" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_UNMANAGED_10_RUNTIMME_NAME = "rn-med_10" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11 = "n-med_11" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11_RUNTIMME_NAME = "rn-med_11" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12 = "n-med_12" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12_RUNTIMME_NAME = "rn-med_12" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13 = "n-med_13" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13_RUNTIMME_NAME = "rn-med_13" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String MOCK_CSS = "mock.css";


    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final DeploymentsOperations ops = new DeploymentsOperations(client);
    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Page
    private DeploymentsPage page;

    @Page
    private TreeNavigationPage treeNavigationPage;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneDeploymentEntryPoint.class);
    }

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException, CliException {
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

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_ENABLED_1)
                .runtimeName(DEPLOYMENT_MANAGED_ENABLED_1_RUNTIMME_NAME)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_ENABLED_2)
                .runtimeName(DEPLOYMENT_MANAGED_ENABLED_2_RUNTIMME_NAME)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3)
                .disabled()
                .runtimeName(DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3_RUNTIMME_NAME)
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3)).assertSuccess();
        new Operations(client).invoke("deploy", Address.deployment(DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_4)
                .disabled()
                .runtimeName(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_4_RUNTIMME_NAME)
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_4)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_5)
                .runtimeName(DEPLOYMENT_MANAGED_DISABLED_5_RUNTIMME_NAME)
                .disabled()
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_6)
                .disabled()
                .runtimeName(DEPLOYMENT_MANAGED_EXPLODED_DISABLED_6_RUNTIMME_NAME)
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_6)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7)
                .disabled()
                .runtimeName(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7_RUNTIMME_NAME)
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_ENABLED_8)
                .runtimeName(DEPLOYMENT_MANAGED_ENABLED_8_RUNTIMME_NAME)
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9)
                .runtimeName(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9_RUNTIMME_NAME)
                .disabled()
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_UNMANAGED_10)
                .runtimeName(DEPLOYMENT_UNMANAGED_10_RUNTIMME_NAME)
                .unmanaged()
                .build());

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11)
                .runtimeName(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11_RUNTIMME_NAME)
                .disabled()
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12)
                .runtimeName(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12_RUNTIMME_NAME)
                .disabled()
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12)).assertSuccess();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13)
                .runtimeName(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13_RUNTIMME_NAME)
                .disabled()
                .build());
        new Operations(client).invoke("explode", Address.deployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13)).assertSuccess();
        client.executeCli("/deployment=" + DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13 + ":add-content(content=[{target-path => " + MOCK_CSS + ", input-stream-index => " + new File(FILE_PATH + "mock.css").getAbsolutePath() + " }])");
    }

    @AfterClass
    public static void cleanUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException, OperationException {
        try {
            ops.undeploy(NAME);
            ops.undeploy(DEPLOYMENT_DISABLED_NAME);
            ops.undeploy(DEPLOYMENT_ENABLED_NAME);
            ops.undeployIfExists(DEPLOYMENT_TBR_NAME);
            ops.undeploy(DEPLOYMENT_MANAGED_ENABLED_1);
            ops.undeploy(DEPLOYMENT_MANAGED_ENABLED_2);
            ops.undeploy(DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_4);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_5);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_6);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7);
            ops.undeploy(DEPLOYMENT_MANAGED_ENABLED_8);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9);
            ops.undeploy(DEPLOYMENT_UNMANAGED_10);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12);
            ops.undeploy(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13);
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void basicDeployment() throws Exception {
        Column column = navigation.step(FinderNames.DEPLOYMENT).selectColumn();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        column.invoke(FinderNames.ADD);
        File deployment = new File(FILE_PATH + FILE_NAME);

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .finish();

        assertTrue("Deployment wizard should close", result);
        ops.verifyDeploymentExists(NAME);
    }

    @Test
    public void disableDeployment() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_ENABLED_NAME).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.DISABLE);

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyIsDeploymentDisabled(DEPLOYMENT_ENABLED_NAME);
    }

    @Test
    public void enableDeployment() throws Exception {
        page.enableDisabledDeployment(DEPLOYMENT_DISABLED_NAME);
        ops.verifyIsDeploymentEnabled(DEPLOYMENT_DISABLED_NAME);
    }

    @Test
    public void removeDeployment() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_TBR_NAME).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.REMOVE);

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        ops.verifyDeploymentDoesNotExist(DEPLOYMENT_TBR_NAME);
    }

    @Test
    public void unmanagedDeployment() throws Exception {
        page.navigateToDeploymentAndInvokeView(DEPLOYMENT_UNMANAGED_10);

        String content = treeNavigationPage.formItemTable().getValueOf("Managed");
        assertTrue(content.contains("false"));

        content = treeNavigationPage.formItemTable().getValueOf("Status");
        assertTrue("Status of deployment should be 'OK'", content.contains("OK"));
    }

    @Test
    public void medExplodeOnEnabledDeploymentExpectFailure() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_ENABLED_1).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        row.invoke(FinderNames.EXPLODE);

        assertEquals("Cannot explode an enabled deployment", page.getAlertArea().getMessage());
    }

    @Test
    public void medBrowseContentOnUnexplodedEnabledDeploymentExpectSuccess() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_ENABLED_2).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        row.invoke(FinderNames.BROWSE_CONTENT);
        List<String> itemsInDeploment = page.getDeploymentBrowsedContentItems();
        ops.verifyDeploymentContentDefault(itemsInDeploment);
    }

    @Test
    public void medBrowseContentOnExplodedEnabledDeploymentExpectSuccess() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_ENABLED_EXPLODED_3).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        row.invoke(FinderNames.BROWSE_CONTENT);
        List<String> itemsInDeploment = page.getDeploymentBrowsedContentItems();
        ops.verifyDeploymentContentDefault(itemsInDeploment);
    }

    @Test
    public void medBrowseContentOnExplodedDisabledDeploymentExpectSuccess() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_4).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        row.invoke(FinderNames.BROWSE_CONTENT);
        List<String> itemsInDeploment = page.getDeploymentBrowsedContentItems();
        ops.verifyDeploymentContentDefault(itemsInDeploment);
    }

    @Test
    public void medExplodeOnDisabledDeploymentExpectSuccess() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_5).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        row.invoke(FinderNames.EXPLODE);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();

        assertEquals(DEPLOYMENT_MANAGED_DISABLED_5 + " successfully exploded.", page.getAlertArea().getMessage());
    }

    @Test
    public void medExplodeOnExplodedDisabledDeploymentExpectFailure() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_6).selectRow();
        row.invoke(FinderNames.EXPLODE);

        assertEquals("Cannot explode an already exploded deployment", page.getAlertArea().getMessage());
    }

    @Test
    public void medExplodedDeploymentFlagFalseInViewInfoSuccessExpected() {
        navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        page.navigateToDeploymentAndInvokeView(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_7);

        String content = treeNavigationPage.formItemTable().getValueOf("Content");
        assertTrue("Deployment should be exploded but stats states that is not", content.contains("\"archive\" => false"));

        content = treeNavigationPage.formItemTable().getValueOf("Status");
        assertTrue("Status of deployment should be 'STOPPED'", content.contains("STOPPED"));
    }

    @Test
    @Category(KnownIssue.class)
    public void medExplodedDeploymentFlagTrueInViewInfoSuccessExpected() {
        page.navigateToDeploymentAndInvokeView(DEPLOYMENT_MANAGED_ENABLED_8);

        String content = treeNavigationPage.formItemTable().getValueOf("Content");
        assertTrue("Unexploded deployment should be marked as 'archive => true' in deployment detail, reported [HAL-1226]", content.contains("\"archive\" => true"));
    }

    @Test
    public void medManagedTrueIsInViewInformationsSuccessExpected() {
        navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        page.navigateToDeploymentAndInvokeView(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_9);

        String content = treeNavigationPage.formItemTable().getValueOf("Managed");
        assertTrue(content.contains("true"));

        content = treeNavigationPage.formItemTable().getValueOf("Status");
        assertTrue("Status of deployment should be 'STOPPED'", content.contains("STOPPED"));

        content = treeNavigationPage.formItemTable().getValueOf("Enabled");
        assertTrue("Deployment should be disabled", content.contains("false"));
    }

    @Test
    public void medDeploymentTimestampsSuccesssExpected() throws InterruptedException {
        navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        List<String> previewListItems = navigation.getPreview().getDisplayedUnorderedList("deployment was never disabled");
        assertTrue("Deployment has to be never enabled", ops.deploymentNeverEnabled(previewListItems));
        assertTrue("Deployment has to be never disabled", ops.deploymentNeverDisabled(previewListItems));

        page.enableDisabledDeployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11);

        previewListItems = navigation.getPreview().getDisplayedUnorderedList("ast enabled");
        assertTrue("Deployment has to have timestamp when was enabled", ops.deplomentEnabledTimestampExists(previewListItems));
        assertTrue("Deployment has to be never disabled", ops.deploymentNeverDisabled(previewListItems));

        page.disableEnabledDeployment(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_11);

        previewListItems = navigation.getPreview().getDisplayedUnorderedList("disabled at");
        assertTrue("Deployment has to have timestamp when was enabled", ops.deplomentEnabledTimestampExists(previewListItems));
        assertTrue("Deployment has to have timestamp when was disabled", ops.deplomentDisabledTimestampExists(previewListItems));
    }

    @Test
    public void medUpdateExplodedDeploymentSuccessExpected() throws Exception {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        // verify that additional file is not present
        row.invoke(FinderNames.BROWSE_CONTENT);
        List<String> itemsInDeploment = page.getDeploymentBrowsedContentItems();
        assertFalse(ops.itemInListExistsLowerCase(itemsInDeploment, MOCK_CSS.toLowerCase() + " | 566 b"));

        client.executeCli("/deployment=" + DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12 + ":add-content(content=[{" +
                "target-path => " + MOCK_CSS + ", " +
                "input-stream-index => " + new File(FILE_PATH + "mock.css").getAbsolutePath() + " }])");

        row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_12).selectRow();
        row.invoke(FinderNames.BROWSE_CONTENT);
        itemsInDeploment = page.getDeploymentBrowsedContentItems();
        assertTrue(ops.itemInListExistsLowerCase(itemsInDeploment, MOCK_CSS.toLowerCase() + " | 566 b"));
    }

    @Test
    public void medReplaceUpdatedExplodedDeploymentSuccessExpected() throws InterruptedException {
        Row row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        // Verify that additional file is present
        row.invoke(FinderNames.BROWSE_CONTENT);
        List<String> itemsInDeploment = page.getDeploymentBrowsedContentItems();
        assertTrue(ops.itemInListExistsLowerCase(itemsInDeploment, MOCK_CSS.toLowerCase() + " | 566 b"));

        // replace deployment
        row = navigation.step(FinderNames.DEPLOYMENT, DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13).selectRow();
        row.invoke(FinderNames.REPLACE);
        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);
        boolean result = wizard
                .uploadDeployment(new File(FILE_PATH + FILE_NAME))
                .finish();
        assertTrue("Deployment wizard should close", result);
        assertEquals(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13 + " successfully replaced.", page.getAlertArea().getMessage());
        navigation.getPreview().getDisplayedUnorderedList("Archive: Yes");

        // explode new deployment
        row.invoke(FinderNames.EXPLODE);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        assertEquals(DEPLOYMENT_MANAGED_DISABLED_EXPLODED_CONTENT_MODIFIED_13 + " successfully exploded.", page.getAlertArea().getMessage());

        // verify that additional file is not present
        navigation.getPreview().getDisplayedUnorderedList("Archive: No");
        row.invoke(FinderNames.BROWSE_CONTENT);
        itemsInDeploment = page.getDeploymentBrowsedContentItems();
        assertFalse(ops.itemInListExistsLowerCase(itemsInDeploment, MOCK_CSS.toLowerCase() + " | 566 b"));
    }

}
