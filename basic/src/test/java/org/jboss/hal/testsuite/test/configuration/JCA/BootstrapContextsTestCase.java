package org.jboss.hal.testsuite.test.configuration.JCA;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 21.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class BootstrapContextsTestCase {

    private static final String BOOTSTRAP_CONTEXT_TBA = "bootstrap-context-tba_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BOOTSTRAP_CONTEXT_TBR = "bootstrap-context-tbr_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address JCA_ADDRESS = Address.subsystem("jca");
    private static final Address BOOTSTRAP_CONTEXT_TBA_ADDRESS = JCA_ADDRESS.and("bootstrap-context", BOOTSTRAP_CONTEXT_TBA);
    private static final Address BOOTSTRAP_CONTEXT_TBR_ADDRESS = JCA_ADDRESS.and("bootstrap-context", BOOTSTRAP_CONTEXT_TBR);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        operations.add(BOOTSTRAP_CONTEXT_TBR_ADDRESS, Values.of("workmanager", "default"));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, OperationException {
        try {
            operations.removeIfExists(BOOTSTRAP_CONTEXT_TBA_ADDRESS);
            operations.removeIfExists(BOOTSTRAP_CONTEXT_TBR_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Drone
    private WebDriver browser;
    @Page
    private JCAPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToBootstrapContextsTab();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reload();
    }

    @Test
    public void addBootstrapContexts() throws Exception {
        page.clickButton("Add");
        page.getWindowFragment().getEditor().text("name", BOOTSTRAP_CONTEXT_TBA);
        page.getWindowFragment().getEditor().select("workmanager", "default");
        page.getWindowFragment().clickButton("Save");

        new ResourceVerifier(BOOTSTRAP_CONTEXT_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeBootstrapContexts() throws Exception {
        page.getResourceManager().getResourceTable().selectRowByText(0, BOOTSTRAP_CONTEXT_TBR);
        page.clickButton("Remove");

        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(BOOTSTRAP_CONTEXT_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
