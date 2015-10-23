package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class JournalTestCase {
    private static final String NAME = "test-provider";
    private static final String ADD = "/subsystem=messaging-activemq/server=" + NAME + ":add()";
    private static final String DOMAIN = "/profile=full-ha";
    private static final String REMOVE = "/subsystem=messaging-activemq/server=" + NAME + ":remove";

    private String command;
    private String removeCmd;
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=" + NAME);
    private ResourceAddress address;
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;
    CliClient cliClient = CliClientFactory.getClient();

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
    }

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            address = new ResourceAddress(domainPath);
            command = DOMAIN + ADD;
            removeCmd = DOMAIN + REMOVE;
        } else {
            address = new ResourceAddress(path);
            command = ADD;
            removeCmd = REMOVE;
        }
        cliClient.executeCommand(command);
    }

    @After
    public void after() {
        cliClient.executeCommand(removeCmd);
        cliClient.reload();
    }

    @Test
    public void updateJournalType() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().select("journal-type", "NIO");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-type", "NIO");
    }

    @Test
    public void updateCreateJournalDir() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("create-journal-dir", false);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "create-journal-dir", false);
    }

    @Test
    public void updateJournalSyncNonTransactional() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("journal-sync-non-transactional", false);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-sync-non-transactional", false);
    }

    @Test
    public void updateJournalSyncTransactional() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().checkbox("journal-sync-transactional", false);
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-sync-transactional", false);
    }

    @Test //https://issues.jboss.org/browse/HAL-839 https://issues.jboss.org/browse/HAL-840
    public void updateJournalMinFiles() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("journal-min-files", "0");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-min-files", "2");
    }

    @Test
    public void updateJournalMaxFilesWrongValue() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("journal-max-io", "-1");
        boolean finished =  page.getWindowFragment().save();

        assertFalse("Config should not be saved and closed.Invalid value.", finished);
        verifier.verifyAttribute(address, "journal-max-io", "undefined");
    }

    @Test //https://issues.jboss.org/browse/HAL-839
    public void updateJournalBufferSize() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("journal-buffer-size", "1024");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should not be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-buffer-size", "1024");

        page.getWindowFragment().edit();
        page.getWindowFragment().getEditor().text("journal-buffer-size", "");
        finished =  page.getWindowFragment().save();

        assertTrue("Config should not be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-buffer-size", "undefined");
    }

    @Test
    public void updateJournalBufferTimeout() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("journal-buffer-timeout", "1");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should not be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-buffer-timeout", "1");
    }

    @Test
    public void updateJournalCompactMinFiles() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("journal-compact-min-files", "1");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should not be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-compact-min-files", "1");
    }

    @Test
    public void updateJournalCompactPercentage() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("journal-compact-percentage", "110");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should not be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-compact-percentage", "110");
    }

    @Test
    public void updateJournalFileSize() {
        page.selectProvider(NAME);
        page.InvokeProviderSettings();
        page.switchToJournalTab();
        page.getWindowFragment().edit();

        page.getWindowFragment().getEditor().text("journal-file-size", "110");
        boolean finished =  page.getWindowFragment().save();

        assertTrue("Config should not be saved and closed.", finished);
        verifier.verifyAttribute(address, "journal-file-size", "110");
    }
}
