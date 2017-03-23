package org.jboss.hal.testsuite.test.configuration.mail;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.config.mail.MailServerWizard;
import org.jboss.hal.testsuite.page.config.MailSessionsPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * Tests setting of mail servers (POP3, IMAP, SMTP), their adding and removing
 * TODO extend test by testing each server type (POP3, IMAP, SMTP). Preferably with TestNG or jUnit 5 when the possibilities are investigated
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class MailServerTestCase {

    private static final String MAIL_SESSION_NAME = "java:/mail_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String POP3 = "pop3";
    private static final String IMAP = "imap";
    private static final String SMTP = "smtp";
    private static final String USERNAME = "un_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PASSWORD = "pw_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address MAIL_SESSION_ADDRESS = Address.subsystem("mail").and("mail-session", MAIL_SESSION_NAME);

    private static final Address SERVER_ADDRESS = MAIL_SESSION_ADDRESS.and("server", SMTP);
    private static final Address SERVER_TBA_ADDRESS = MAIL_SESSION_ADDRESS.and("server", POP3);
    private static final Address SERVER_TBR_ADDRESS = MAIL_SESSION_ADDRESS.and("server", IMAP);

    private static OnlineManagementClient client;
    private static Administration administration;
    private static Operations operations;

    private static MailSubsystemOperations mailOperations;

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
        mailOperations = new MailSubsystemOperations(client);
        SOCKET_BINDING_1 = mailOperations.createSocketBinding();
        SOCKET_BINDING_2 = mailOperations.createSocketBinding();

        //Prepare mail sessions
        operations.add(MAIL_SESSION_ADDRESS, Values.of("jndi-name", "java:/" + MAIL_SESSION_NAME)).assertSuccess();

        //Prepare servers on default session
        operations.add(SERVER_ADDRESS, Values.of("outbound-socket-binding-ref", SOCKET_BINDING_1)).assertSuccess();
        operations.add(SERVER_TBR_ADDRESS, Values.of("outbound-socket-binding-ref", SOCKET_BINDING_1)).assertSuccess();
    }

    @Before
    public void before() {
        page.viewMailSession(MAIL_SESSION_NAME);
        page.getResourceManager().selectByName(SMTP.toUpperCase());
        page.getSesionsServers().selectServer(SMTP);
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException, InterruptedException, TimeoutException, CommandFailedException {
        try {
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_1));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_2));
            operations.removeIfExists(SERVER_TBA_ADDRESS);
            operations.removeIfExists(SERVER_TBR_ADDRESS);
            operations.removeIfExists(MAIL_SESSION_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void createMailServer() throws Exception {
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        MailServerWizard wizard = page.getResourceManager().addResource(MailServerWizard.class);

        wizard.socketBinding(SOCKET_BINDING_1)
                .username(USERNAME)
                .password(PASSWORD)
                .type(POP3)
                .ssl(true)
                .clickSave();

        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        administration.reloadIfRequired();

        assertTrue("Window should be closed", wizard.isClosed());
        assertTrue("Mail server should be present in table", page.getResourceManager().isResourcePresent(POP3.toUpperCase()));
        new ResourceVerifier(SERVER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeMailServer() throws Exception {
        page.getResourceManager().removeResource(IMAP.toUpperCase()).confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(SERVER_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void setCredentialReferenceToClearText() throws Exception {
        operations.undefineAttribute(SERVER_ADDRESS, "password");
        page.switchToCredentialReferenceTab();
        new ElytronIntegrationChecker.Builder(client)
                .configFragment(page.getConfigFragment())
                .address(SERVER_ADDRESS)
                .build()
                .setClearTextCredentialReferenceAndVerify();
    }

    @Test
    public void setCredentialReferenceToCredentialStore() throws Exception {
        operations.undefineAttribute(SERVER_ADDRESS, "password");
        page.switchToCredentialReferenceTab();
        new ElytronIntegrationChecker.Builder(client)
                .configFragment(page.getConfigFragment())
                .address(SERVER_ADDRESS)
                .build()
                .setCredentialStoreCredentialReferenceAndVerify();
    }

    @Test
    public void setInvalidCombinationToCredentialStore() throws Exception {
        page.switchToCredentialReferenceTab();
        new ElytronIntegrationChecker.Builder(client)
                .configFragment(page.getConfigFragment())
                .address(SERVER_ADDRESS)
                .build()
                .testIllegalCombinationCredentialReferenceAttributes();
    }

    @Test
    public void editUsername() throws Exception {
        final String value = "FooUsername_" + RandomStringUtils.randomAlphanumeric(5),
                attributeName = "username";
        new ConfigChecker.Builder(client, SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, attributeName, value)
                .verifyFormSaved()
                .verifyAttribute(attributeName, value);
    }

    @Test
    public void editPassword() throws Exception {
        final String value = "FooPassword_" + RandomStringUtils.randomAlphanumeric(5),
                attributeName = "password";
        new ConfigChecker.Builder(client, SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, attributeName, value)
                .verifyFormSaved()
                .verifyAttribute(attributeName, value);
    }

    @Test
    public void toggleUseSSL() throws Exception {
        final String attributeName = "ssl";
        final ModelNodeResult nodeResult = operations.readAttribute(SERVER_ADDRESS, attributeName);
        nodeResult.assertSuccess();
        final boolean sslValue = nodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, !sslValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, !sslValue);

            new ConfigChecker.Builder(client, SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, sslValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, sslValue);
        } finally {
            operations.writeAttribute(SERVER_ADDRESS, attributeName, nodeResult.value()).assertSuccess();
        }
    }

    @Test
    public void editSocketBinding() throws Exception {
        final String value = mailOperations.createSocketBinding(),
                attributeName = "outbound-socket-binding-ref";
        try {
            new ConfigChecker.Builder(client, SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, "socketBinding", value)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, value);
        } finally {
            client.apply(new RemoveSocketBinding(value));
        }
    }

}
