package org.jboss.hal.testsuite.test.configuration.mail;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.config.mail.MailServerWizard;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Standalone.class)
public class IMAPMailServerTestCase extends MailServerTestCaseAbstract {

    private static final String IMAP = "imap";

    private static final Address IMAP_SERVER_ADDRESS = MAIL_SESSION_ADDRESS.and("server", IMAP);

    @BeforeClass
    public static void beforeClass() throws Exception {
        SOCKET_BINDING = mailOperations.createSocketBinding();
        operations.add(MAIL_SESSION_ADDRESS, Values.of("jndi-name", "java:/" + MAIL_SESSION_NAME)).assertSuccess();
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException, InterruptedException, TimeoutException, CommandFailedException {
        try {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
            client.apply(new RemoveSocketBinding(SOCKET_BINDING));
            operations.removeIfExists(MAIL_SESSION_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    /**
     * @tpTestDetails Try to create IMAP mail server in Web Console's mail subsystem configuration.
     * Validate newly created mail server is visible in the Mail Server table.
     * Validate newly created mail server is present in model.
     * Validate attributes associated with newly created mail server in model
     */
    @Test
    public void createMailServer() throws Exception {
        final String userName = "username_" + RandomStringUtils.randomAlphanumeric(7);
        final String password = "password_" + RandomStringUtils.randomAlphanumeric(7);
        try {
            page.viewMailSession(MAIL_SESSION_NAME);
            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
            MailServerWizard wizard = page.getResourceManager().addResource(MailServerWizard.class);
            wizard.socketBinding(SOCKET_BINDING)
                    .username(userName)
                    .password(password)
                    .type(IMAP)
                    .ssl(true)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            administration.reloadIfRequired();
            assertTrue("Mail server should be present in table", page.getResourceManager().isResourcePresent(IMAP.toUpperCase()));
            new ResourceVerifier(IMAP_SERVER_ADDRESS, client)
                    .verifyExists()
                    .verifyAttribute(USERNAME_ATTRIBUTE, userName)
                    .verifyAttribute(PASSWORD_ATTRIBUTE, password);
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
            administration.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to remove it in Web Console's mail subsystem configuration
     * Validate removed server is not visible anymore in Mail Server table
     * Validate removed server is not present in model.
     */
    @Test
    public void removeMailServer() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().removeResource(IMAP.toUpperCase()).confirmAndDismissReloadRequiredMessage();
            new ResourceVerifier(IMAP_SERVER_ADDRESS, client).verifyDoesNotExist();
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to set its credential reference attribute
     * to clear text in Web Console's mail subsystem configuration
     * Validate attribute modification in model.
     */
    @Category(Elytron.class)
    @Test
    public void setCredentialReferenceToClearText() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            operations.undefineAttribute(IMAP_SERVER_ADDRESS, PASSWORD_ATTRIBUTE);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            page.switchToCredentialReferenceTab();
            new ElytronIntegrationChecker.Builder(client)
                    .configFragment(page.getConfigFragment())
                    .address(IMAP_SERVER_ADDRESS)
                    .build()
                    .setClearTextCredentialReferenceAndVerify();
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to set its credential reference attribute
     * to credential store in Web Console's mail subsystem configuration
     * Validate attribute modification in model.
     */
    @Category(Elytron.class)
    @Test
    public void setCredentialReferenceToCredentialStore() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            operations.undefineAttribute(IMAP_SERVER_ADDRESS, PASSWORD_ATTRIBUTE);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            page.switchToCredentialReferenceTab();
            new ElytronIntegrationChecker.Builder(client)
                    .configFragment(page.getConfigFragment())
                    .address(IMAP_SERVER_ADDRESS)
                    .build()
                    .setCredentialStoreCredentialReferenceAndVerify();
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to set its credential reference attribute
     * to invalid combination in Web Console's mail subsystem configuration
     * Validate attribute cannot be set with invalid values
     */
    @Category(Elytron.class)
    @Test
    public void setInvalidCombinationToCredentialStore() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            operations.undefineAttribute(IMAP_SERVER_ADDRESS, PASSWORD_ATTRIBUTE);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            page.switchToCredentialReferenceTab();
            new ElytronIntegrationChecker.Builder(client)
                    .configFragment(page.getConfigFragment())
                    .address(IMAP_SERVER_ADDRESS)
                    .build()
                    .testIllegalCombinationCredentialReferenceAttributes();
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to set its username attribute
     * in Web Console's mail subsystem configuration
     * Validate attribute in model.
     */
    @Test
    public void editUsername() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            final String value = "FooUsername_" + RandomStringUtils.randomAlphanumeric(5);
            new ConfigChecker.Builder(client, IMAP_SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, USERNAME_ATTRIBUTE, value)
                    .verifyFormSaved()
                    .verifyAttribute(USERNAME_ATTRIBUTE, value);
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to set its password attribute
     * in Web Console's mail subsystem configuration
     * Validate attribute in model.
     */
    @Test
    public void editPassword() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            final String value = "FooPassword_" + RandomStringUtils.randomAlphanumeric(5);
            new ConfigChecker.Builder(client, IMAP_SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PASSWORD_ATTRIBUTE, value)
                    .verifyFormSaved()
                    .verifyAttribute(PASSWORD_ATTRIBUTE, value);
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to toggle its ssl attribute
     * in Web Console's mail subsystem configuration
     * Validate attribute in model.
     */
    @Test
    public void toggleUseSSL() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            final String attributeName = "ssl";
            final ModelNodeResult nodeResult = operations.readAttribute(IMAP_SERVER_ADDRESS, attributeName);
            nodeResult.assertSuccess();
            final boolean sslValue = nodeResult.booleanValue();
            new ConfigChecker.Builder(client, IMAP_SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, !sslValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, !sslValue);

            new ConfigChecker.Builder(client, IMAP_SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, sslValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, sslValue);
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to toggle its tls attribute
     * in Web Console's mail subsystem configuration
     * Validate attribute in model.
     */
    @Test
    public void toggleUseTLS() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            final String attributeName = "tls";
            final ModelNodeResult nodeResult = operations.readAttribute(IMAP_SERVER_ADDRESS, attributeName);
            nodeResult.assertSuccess();
            final boolean tlsValue = nodeResult.booleanValue();

            new ConfigChecker.Builder(client, IMAP_SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, !tlsValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, !tlsValue);

            new ConfigChecker.Builder(client, IMAP_SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, tlsValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, tlsValue);
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to edit its socked binding attribute
     * in Web Console's mail subsystem configuration
     * Validate attribute in model.
     */
    @Test
    public void editSocketBinding() throws Exception {
        final String value = mailOperations.createSocketBinding();
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            new ConfigChecker.Builder(client, IMAP_SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, OUTBOUND_SOCKET_BINDING_REFERENCE, value)
                    .verifyFormSaved()
                    .verifyAttribute(OUTBOUND_SOCKET_BINDING_REFERENCE, value, "Probably fails because of https://issues.jboss.org/browse/HAL-1349");
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
            client.apply(new RemoveSocketBinding(value));
        }
    }

    /**
     * @tpTestDetails Create IMAP mail server instance in model and try to create another one
     * in Web Console's mail subsystem configuration
     * Validate form error regarding duplicate mail server is shown in UI form.
     */
    @Test
    public void createDuplicateMailServerShowsError() throws Exception {
        try {
            createMailServerInstanceInModel(IMAP_SERVER_ADDRESS, SOCKET_BINDING);
            page.viewMailSession(MAIL_SESSION_NAME);
            page.getResourceManager().selectByName(IMAP);
            page.getResourceManager().addResource(MailServerWizard.class)
                    .type(IMAP)
                    .socketBinding(SOCKET_BINDING)
                    .saveWithState()
                    .assertWindowOpen();
            assertTrue("Validation error regarding existing mail server type should be shown"
                    , page.getWindowFragment().isErrorShownInForm());
        } finally {
            operations.removeIfExists(IMAP_SERVER_ADDRESS);
            administration.reloadIfRequired();
        }
    }


}
