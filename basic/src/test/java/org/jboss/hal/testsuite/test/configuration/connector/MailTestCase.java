package org.jboss.hal.testsuite.test.configuration.connector;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.Column;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.fragment.config.mail.MailServerFragment;
import org.jboss.hal.testsuite.fragment.config.mail.MailServerWizard;
import org.jboss.hal.testsuite.fragment.config.mail.MailSessionWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.MailSessionsPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class MailTestCase {

    private static final Logger log = LoggerFactory.getLogger(MailTestCase.class);

    private static final String ATTRIBUTES = "Attributes";
    private static final String MAIL_SESSION_LABEL = "Mail Session";
    private static final String MAIL_SESSION_NAME = "java:/mail_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String MAIL_SESSION_NAME_INVALID = "%%%" + RandomStringUtils.randomAlphanumeric(5);
    private static final String MAIL_NAME = "n_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PRE_NAME = "npre_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TBR_NAME = "ntbr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TYPE = "pop3";
    private static final String TYPE_TBR = "imap";
    private static final String USERNAME = "un_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PASSWORD = "pw_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String SOCKET_BINDING = "mailSb_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String FROM = "from_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address MAIL_SESSION_PRE_ADDRESS = Address.subsystem("mail").and("mail-session", PRE_NAME);
    private static final Address MAIL_SESSION_TBR_ADDRESS = Address.subsystem("mail").and("mail-session", TBR_NAME);
    private static final Address MAIL_SESSION_ADDRESS = Address.subsystem("mail").and("mail-session", MAIL_NAME);
    private static final Address MAIL_SESSION_INV_ADDRESS = Address.subsystem("mail").and("mail-session", MAIL_SESSION_NAME_INVALID);
    private static final Address SERVER_TBR_ADDRESS = MAIL_SESSION_PRE_ADDRESS.and("server", TYPE_TBR);
    private static final Address SERVER_ADDRESS = MAIL_SESSION_PRE_ADDRESS.and("server", TYPE);

    private static OnlineManagementClient client;
    private static Administration administration;
    private static Operations operations;

    @Drone
    public WebDriver browser;

    @Page
    public MailSessionsPage page;

    private FinderNavigation navi;
    private static String socketBinding;

    @BeforeClass
    public static void beforeClass() throws Exception {
        client = ManagementClientProvider.createOnlineManagementClient();
        administration = new Administration(client);
        operations = new Operations(client);
        socketBinding = createSocketBinding();
        operations.add(MAIL_SESSION_PRE_ADDRESS, Values.of("jndi-name", "java:/" + PRE_NAME));
        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client).verifyExists();
        operations.add(MAIL_SESSION_TBR_ADDRESS, Values.of("jndi-name", "java:/" + TBR_NAME));
        new ResourceVerifier(MAIL_SESSION_TBR_ADDRESS, client).verifyExists();
        operations.add(SERVER_TBR_ADDRESS, Values.of("outbound-socket-binding-ref", socketBinding));
        new ResourceVerifier(SERVER_TBR_ADDRESS, client).verifyExists();
    }

    @Before
    public void before() {
        navi = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
            .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
            .step(FinderNames.SUBSYSTEM, "Mail");
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException, InterruptedException, TimeoutException, CommandFailedException {
        try {
            client.apply(new RemoveSocketBinding(socketBinding));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING));
            operations.removeIfExists(SERVER_ADDRESS);
            operations.removeIfExists(SERVER_TBR_ADDRESS);
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
    public void createInvalidMailSession() throws Exception {

        invokeOperationAddMailSession();
        MailSessionWizard wizard = Console.withBrowser(browser).openedWizard(MailSessionWizard.class);

        boolean result =
                wizard.jndiName(MAIL_SESSION_NAME_INVALID)
                        .name(MAIL_NAME)
                        .finish();

        assertFalse("Wizard window should not be closed.", result);
        new ResourceVerifier(MAIL_SESSION_INV_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void createMailSession() throws Exception {
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        invokeOperationAddMailSession();
        MailSessionWizard wizard = Console.withBrowser(browser).openedWizard(MailSessionWizard.class);

        boolean result =
                wizard.jndiName(MAIL_SESSION_NAME)
                        .name(MAIL_NAME)
                        .finish();

        assertTrue("Wizard window should be closed.", result);

        new ResourceVerifier(MAIL_SESSION_ADDRESS, client).verifyExists();
    }

    @Test
    public void enableDebugOnMailSession() throws Exception {
        invokeOperationOnMailSession(ATTRIBUTES, PRE_NAME);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).getEditor().checkbox("debug", true);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickButton("Save");

        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client).verifyAttribute("debug", true);
    }

    @Test
    public void disableDebugOnMailSession() throws Exception {
        invokeOperationOnMailSession(ATTRIBUTES, PRE_NAME);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).getEditor().checkbox("debug", false);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickButton("Save");

        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client).verifyAttribute("debug", false);
    }

    @Test
    public void changeDefaultFromOnMailSession() throws Exception {
        invokeOperationOnMailSession(ATTRIBUTES, PRE_NAME);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).getEditor().text("from", FROM);
        Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickButton("Save");

        new ResourceVerifier(MAIL_SESSION_PRE_ADDRESS, client, 5000).verifyAttribute("from", FROM);
    }

    @Test
    public void createMailServer() throws Exception {
        invokeOperationOnMailSession(FinderNames.VIEW, PRE_NAME);
        Application.waitUntilVisible();
        MailServerFragment fragment = page.getSesionsServers();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        MailServerWizard wizard = fragment.addMailServer();

        createSocketBinding(SOCKET_BINDING);

        wizard.socketBinding(SOCKET_BINDING)
                .username(USERNAME)
                .password(PASSWORD)
                .type(TYPE)
                .ssl(true)
                .clickSave();

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        administration.reloadIfRequired();

        assertTrue("Window should be closed", wizard.isClosed());
        assertTrue("Mail server should be present in table", page.getResourceManager().isResourcePresent(TYPE.toUpperCase()));

        new ResourceVerifier(SERVER_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeMailServer() throws Exception {
        invokeOperationOnMailSession(FinderNames.VIEW, PRE_NAME);
        Application.waitUntilVisible();
        MailServerFragment fragment = page.getSesionsServers();
        fragment.removeAndConfirm(TYPE_TBR);

        new ResourceVerifier(SERVER_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void removeMailSession() throws Exception {
        invokeOperationOnMailSession("Remove", TBR_NAME);

        new ResourceVerifier(MAIL_SESSION_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private void invokeOperationOnMailSession(String operationName, String sessionName) {
        Row row = navi.step(MAIL_SESSION_LABEL, sessionName).selectRow();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(operationName);
    }

    private void invokeOperationAddMailSession() {
        Column column = navi.step(MAIL_SESSION_LABEL).selectColumn();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        column.invoke(FinderNames.ADD);
    }

    private static String createSocketBinding() throws IOException, CommandFailedException {
        String socketBinding = "mailTestCaseSb_" + RandomStringUtils.randomAlphanumeric(6);
        return createSocketBinding(socketBinding);
    }

    private static String createSocketBinding(String name) throws IOException, CommandFailedException {
        try (OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient()) {
            int port = AvailablePortFinder.getNextAvailableNonPrivilegedPort();
            log.info("Obtained port for socket binding '" + name + "' is " + port);
            client.apply(new AddSocketBinding.Builder(name)
                    .port(port)
                    .build());
        }
        return name;
    }

}
