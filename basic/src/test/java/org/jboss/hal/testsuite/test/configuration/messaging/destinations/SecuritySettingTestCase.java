package org.jboss.hal.testsuite.test.configuration.messaging.destinations;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
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
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecuritySettingTestCase extends AbstractMessagingTestCase {

    private static final String ROLE = "myrole_" + RandomStringUtils.randomAlphanumeric(3);

    private static final String PATTERN = "test-pattern_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PATTERN_TBA = "test-pattern-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PATTERN_TBR = "test-pattern-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address PATTERN_ADDRESS = DEFAULT_MESSAGING_SERVER.and("security-setting", PATTERN)
            .and("role", ROLE);
    private static final Address PATTERN_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("security-setting", PATTERN_TBA)
            .and("role", ROLE);
    private static final Address PATTERN_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("security-setting", PATTERN_TBR)
            .and("role", ROLE);

    @BeforeClass
    public static void setUp() throws InterruptedException, TimeoutException, IOException {
        addSecuritySettings(PATTERN, ROLE);
        addSecuritySettings(PATTERN_TBR, ROLE);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(PATTERN_ADDRESS);
        operations.removeIfExists(PATTERN_TBA_ADDRESS);
        operations.removeIfExists(PATTERN_TBR_ADDRESS);
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();
        page.selectInTable(PATTERN);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addSecuritySetting() throws Exception {
        page.addSecuritySettings(PATTERN_TBA, ROLE);
        new ResourceVerifier(PATTERN_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateSendSecuritySetting() throws Exception {
        editCheckboxAndVerify(PATTERN_ADDRESS, "send", true);
        editCheckboxAndVerify(PATTERN_ADDRESS, "send", false);
   }

    @Test
    public void updateConsumeSecuritySetting() throws Exception {
        editCheckboxAndVerify(PATTERN_ADDRESS, "consume", true);
        editCheckboxAndVerify(PATTERN_ADDRESS, "consume", false);
    }

    @Test
    public void updateManageSecuritySetting() throws Exception {
        editCheckboxAndVerify(PATTERN_ADDRESS, "manage", true);
        editCheckboxAndVerify(PATTERN_ADDRESS, "manage", false);
    }

    @Test
    public void updateCreateDurableSecuritySetting() throws Exception {
        page.clickAdvanced();
        editCheckboxAndVerify(PATTERN_ADDRESS, "createDurableQueue", "create-durable-queue", true);
    }

    @Test
    public void updateDeleteDurableSecuritySetting() throws Exception {
        page.clickAdvanced();
        editCheckboxAndVerify(PATTERN_ADDRESS, "deleteDurableQueue", "delete-durable-queue", true);
    }

    @Test
    public void updateCreateNonDurableSecuritySetting() throws Exception {
        page.clickAdvanced();
        editCheckboxAndVerify(PATTERN_ADDRESS, "createNonDurableQueue", "create-non-durable-queue", true);
    }

    @Test
    public void updateDeleteNonDurableSecuritySetting() throws Exception {
        page.clickAdvanced();
        editCheckboxAndVerify(PATTERN_ADDRESS, "deleteNonDurableQueue", "delete-non-durable-queue", true);
    }

    @Test
    public void removeSecuritySetting() throws Exception {
        page.remove(PATTERN_TBR);
        new ResourceVerifier(PATTERN_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private static void addSecuritySettings(String pattern, String role) throws IOException {
        Batch batch = new Batch();
        Address address = DEFAULT_MESSAGING_SERVER.and("security-setting", pattern);
        batch.add(address);
        batch.add(address.and("role", role));
        operations.batch(batch);
    }
}
