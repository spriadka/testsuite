package org.jboss.hal.testsuite.test.configuration.connector;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.fragment.config.mail.MailServerFragment;
import org.jboss.hal.testsuite.fragment.config.mail.MailServerWizard;
import org.jboss.hal.testsuite.fragment.config.mail.MailSessionWizard;
import org.jboss.hal.testsuite.fragment.config.mail.MailSessionsFragment;
import org.jboss.hal.testsuite.page.config.MailSessionsPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.MAIL_SESSION_SUBSYSTEM_ADDRESS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class MailTestCase {

    private static final String MAIL_SESSION = "java:/mail_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String MAIL_SESSION_INVALID = "%%%" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TYPE = "pop3";
    private static final String USERNAME = "un_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PASSWORD = "pw_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String SOCKET_BINDING = "sb_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String FROM = "from_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String DMR_SESSION = MAIL_SESSION_SUBSYSTEM_ADDRESS + "=\"" + MAIL_SESSION + "\"";
    private static final String DMR_SESSION_INVALID = MAIL_SESSION_SUBSYSTEM_ADDRESS + "=\"" + MAIL_SESSION_INVALID + "\"";
    private static final String DMR_SERVER = DMR_SESSION + "/server=" + TYPE;

    private static CliClient client = new CliClient();
    private static ResourceVerifier verifier = new ResourceVerifier(DMR_SESSION, client);

    @Drone
    public WebDriver browser;

    @Page
    public MailSessionsPage page;

    @Before
    public void before() {
        browser.navigate().refresh();
        Graphene.goTo(MailSessionsPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        browser.manage().window().maximize();
    }

    @AfterClass
    public static void cleanUp() {
        client.removeResource(DMR_SERVER);
        client.removeResource(DMR_SESSION);
        client.removeResource(DMR_SESSION_INVALID);
    }

    @Test
    public void createInvalidMailSession() {
        MailSessionWizard wizard = page.getMailSessions().addMailSession();

        boolean result =
                wizard.jndiName(MAIL_SESSION_INVALID)
                        .finish();

        assertFalse("Wizard window should not be closed.", result);
        verifier.verifyResource(DMR_SESSION_INVALID, false);
    }

    @Test
    @InSequence(0)
    public void createMailSession() {
        MailSessionsFragment fragment = page.getMailSessions();
        MailSessionWizard wizard = fragment.addMailSession();

        boolean result =
                wizard.jndiName(MAIL_SESSION)
                        .finish();

        assertTrue("Wizard window should be closed.", result);
        assertTrue("Mail session should be present in table", fragment.resourceIsPresent(MAIL_SESSION));
        verifier.verifyResource(DMR_SESSION, true);
    }

    @Test
    @InSequence(1)
    public void enableDebugOnMailSession() {
        MailSessionsFragment fragment = page.getMailSessions();
        fragment.editDebugAndSave(MAIL_SESSION, true);

        verifier.verifyAttribute("debug", "true");
    }

    @Test
    @InSequence(2)
    public void disableDebugOnMailSession() {
        MailSessionsFragment fragment = page.getMailSessions();
        fragment.editDebugAndSave(MAIL_SESSION, false);

        verifier.verifyAttribute("debug", "false");
    }

    @Test
    @InSequence(3)
    public void changeDefaultFromOnMailSession() {
        MailSessionsFragment fragment = page.getMailSessions();
        fragment.editFromAndSave(MAIL_SESSION, FROM);

        verifier.verifyAttribute("from", FROM);
    }

    @Test
    @InSequence(4)
    public void createMailServer() {
        MailServerFragment fragment = page.getMailServers(MAIL_SESSION);
        MailServerWizard wizard = fragment.addMailServer();

        boolean result =
                wizard.socketBinding(SOCKET_BINDING)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .type(TYPE)
                        .ssl(true)
                        .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Mail server should be present in table", fragment.resourceIsPresent(TYPE.toUpperCase()));
        verifier.verifyResource(DMR_SERVER, true);
    }

    @Test
    @InSequence(5)
    public void removeMailServer() {
        MailServerFragment fragment = page.getMailServers(MAIL_SESSION);
        fragment.removeAndConfirm(TYPE);

        verifier.verifyResource(DMR_SERVER, false);
    }

    @Test
    @InSequence(6)
    public void removeMailSession() {
        MailSessionsFragment fragment = page.getMailSessions();
        fragment.removeAndConfirm(MAIL_SESSION);

        verifier.verifyResource(DMR_SESSION, false);
    }


}
