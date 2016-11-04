package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class JournalTestCase extends AbstractMessagingTestCase {
    private static final String SERVER_NAME = "test-provider_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and("server", SERVER_NAME);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(SERVER_ADDRESS);
        Address journalPathAddress = SERVER_ADDRESS.and("path", "journal-directory");
        operations.add(journalPathAddress, Values.of("path", "test-journal").and("relative-to", "jboss.server.data.dir"));
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, InterruptedException, TimeoutException {
        operations.removeIfExists(SERVER_ADDRESS);
        administration.restart();
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.selectProvider(SERVER_NAME);
        page.invokeProviderSettings();
        page.switchToJournalTab();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void updateJournalType() throws Exception {
        selectOptionAndVerify(SERVER_ADDRESS, "journal-type", "NIO");
    }

    @Test
    public void updateCreateJournalDir() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "create-journal-dir", false);
    }

    @Test
    public void updateJournalSyncNonTransactional() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "journal-sync-non-transactional", false);
    }

    @Test
    public void updateJournalSyncTransactional() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "journal-sync-transactional", false);
    }

    @Test
    public void updateJournalMinFilesInvalid() throws Exception {
        verifyIfErrorAppears("journal-min-files", "0");
    }

    @Test
    public void updateJournalMaxFilesWrongValue() {
        verifyIfErrorAppears("journal-max-io", "-1");
    }

    @Test
    public void updateJournalBufferSize() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-buffer-size", 1024L);
        undefineAndVerify(SERVER_ADDRESS, "journal-buffer-size");
    }

    @Test
    public void updateJournalBufferTimeout() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-buffer-timeout", 1L);
    }

    @Test
    public void updateJournalCompactMinFiles() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-compact-min-files", 1);
    }

    @Test
    public void updateJournalCompactPercentage() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-compact-percentage", 99);
    }

    @Test
    public void updateJournalFileSize() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "journal-file-size", 4096L);
    }
}
