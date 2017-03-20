package org.jboss.hal.testsuite.test.configuration.mail;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.MailSessionsPage;
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
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test setting of mail session, its adding and removing
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class MailSessionTestCase {

    private static final String MAIL_SESSION_NAME = "java:/mail_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String MAIL_SESSION_NAME_INVALID = "%%%" + RandomStringUtils.randomAlphanumeric(5);
    private static final String MAIL_NAME = "n_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PRE_NAME = "npre_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TBR_NAME = "ntbr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String FROM = "from_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address MAIL_SESSION_PRE_ADDRESS = Address.subsystem("mail").and("mail-session", PRE_NAME);
    private static final Address MAIL_SESSION_TBR_ADDRESS = Address.subsystem("mail").and("mail-session", TBR_NAME);
    private static final Address MAIL_SESSION_ADDRESS = Address.subsystem("mail").and("mail-session", MAIL_NAME);
    private static final Address MAIL_SESSION_INV_ADDRESS = Address.subsystem("mail").and("mail-session", MAIL_SESSION_NAME_INVALID);

    private static OnlineManagementClient client;
    private static Administration administration;
    private static Operations operations;

    private static String SOCKET_BINDING_1, SOCKET_BINDING_2;

    @Drone
    public WebDriver browser;

    @Page
    public MailSessionsPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        client = ManagementClientProvider.createOnlineManagementClient();
        administration = new Administration(client);
        operations = new Operations(client);
        MailSubsystemOperations mailSubsystemOperations = new MailSubsystemOperations(client);
        SOCKET_BINDING_1 = mailSubsystemOperations.createSocketBinding();
        SOCKET_BINDING_2 = mailSubsystemOperations.createSocketBinding();
        operations.add(MAIL_SESSION_PRE_ADDRESS, Values.of("jndi-name", "java:/" + PRE_NAME));
        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client).verifyExists();
        operations.add(MAIL_SESSION_TBR_ADDRESS, Values.of("jndi-name", "java:/" + TBR_NAME));
        new ResourceVerifier(MAIL_SESSION_TBR_ADDRESS, client).verifyExists();
    }

    @Before
    public void before() {
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException, InterruptedException, TimeoutException, CommandFailedException {
        try {
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_1));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_2));
            operations.removeIfExists(MAIL_SESSION_PRE_ADDRESS);
            operations.removeIfExists(MAIL_SESSION_TBR_ADDRESS);
            operations.removeIfExists(MAIL_SESSION_ADDRESS);
            operations.removeIfExists(MAIL_SESSION_INV_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void createMailSession() throws Exception {
        assertTrue("Wizard window should be closed.", page.addMailSession(MAIL_SESSION_NAME, MAIL_NAME));
        new ResourceVerifier(MAIL_SESSION_ADDRESS, client).verifyExists();
    }

    @Test
    public void createInvalidMailSession() throws Exception {
        assertFalse("Wizard window should NOT be closed.", page.addMailSession(MAIL_SESSION_NAME_INVALID, MAIL_NAME));
        new ResourceVerifier(MAIL_SESSION_INV_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void removeMailSession() throws Exception {
        page.removeMailSession(TBR_NAME);

        new ResourceVerifier(MAIL_SESSION_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void enableDebugOnMailSession() throws Exception {
        page.viewMailSessionAtributesPopUp(PRE_NAME);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).getEditor().checkbox("debug", true);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickSave();

        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client).verifyAttribute("debug", true);
    }

    @Test
    public void disableDebugOnMailSession() throws Exception {
        page.viewMailSessionAtributesPopUp(PRE_NAME);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).getEditor().checkbox("debug", false);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickSave();

        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client).verifyAttribute("debug", false);
    }

    @Test
    public void changeDefaultFromOnMailSession() throws Exception {
        page.viewMailSessionAtributesPopUp(PRE_NAME);

        ConfirmationWindow confirmationWindow = Console.withBrowser(browser).openedWindow(ConfirmationWindow.class);

        confirmationWindow.getEditor().text("from", FROM);
        confirmationWindow.clickSave();

        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client, 5000).verifyAttribute("from", FROM);
    }

}
