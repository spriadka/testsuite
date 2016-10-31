package org.jboss.hal.testsuite.test.configuration.securitymanager;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithAdvancedSelectBoxOptions;
import org.jboss.hal.testsuite.page.config.SecurityManagerPage;
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
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecurityManagerTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private SecurityManagerPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);

    private static final String
            DEFAULT = "default",
            SECURITY_MANAGER = "security-manager",
            DEPLOYMENT_PERMISSIONS = "deployment-permissions";

    private static final Address DEFAULT_PERMISSIONS_ADDRESS = Address.subsystem(SECURITY_MANAGER).and(DEPLOYMENT_PERMISSIONS, DEFAULT);

    private static BackupAndRestoreAttributes backup = new BackupAndRestoreAttributes.Builder(DEFAULT_PERMISSIONS_ADDRESS).build();


    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException {
        client.apply(backup.backup());
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, CommandFailedException, TimeoutException, InterruptedException {
        try {
            if (!operations.exists(DEFAULT_PERMISSIONS_ADDRESS)) {
                operations.add(DEFAULT_PERMISSIONS_ADDRESS);
            }
            client.apply(backup.restore());
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Before
    public void before() {
        page.navigate();
    }

    @InSequence(0)
    @Test
    public void removeDefaultInstance() throws Exception {
        page.treeNavigation()
                .step(DEPLOYMENT_PERMISSIONS)
                .navigateToTreeItem()
                .clickLabel();

        page.getResourceManager()
                .removeResource(DEFAULT)
                .confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(DEFAULT_PERMISSIONS_ADDRESS, client)
                .verifyDoesNotExist();
    }

    @InSequence(1)
    @Test
    public void addDefaultInstance() throws Exception {
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

        page.treeNavigation()
                .step(DEPLOYMENT_PERMISSIONS)
                .navigateToTreeItem()
                .clickLabel();

        WizardWindowWithAdvancedSelectBoxOptions window = page.getResourceManager()
                .addResource(WizardWindowWithAdvancedSelectBoxOptions.class);

        window.pick(DEFAULT)
                .clickContinue()
                .finish();

        new ResourceVerifier(DEFAULT_PERMISSIONS_ADDRESS, client)
                .verifyExists();
    }


}
