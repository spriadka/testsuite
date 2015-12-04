package org.jboss.hal.testsuite.test.configuration.messaging.destinations;

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
import org.jboss.hal.testsuite.fragment.ConfigFragment;
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

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 8.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecuritySettingTestCase {
    private static final String PATTERN = "test-p";
    private static final String ROLE = "role";
    private static final String ADD = "/subsystem=messaging-activemq/server==default/security-setting=" + PATTERN + ":add()";
    private static final String DOMAIN = "/profile=full-ha";

    private String command;
    private String addRole = "/subsystem=messaging-activemq/server==default/security-setting=" + PATTERN + "/role=" + ROLE + ":add()";
    private String remove = "/subsystem=messaging-activemq/server=default/security-setting=" + PATTERN + ":remove";
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/security-setting=" + PATTERN + "/role=" + ROLE );
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/security-setting=" + PATTERN + "/role=" + ROLE );
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
            remove = DOMAIN + remove;
            addRole = DOMAIN + addRole;
        } else {
            address = new ResourceAddress(path);
            command = ADD;
        }
    }

    @After
    public void after() {
        cliClient.executeCommand(remove);
    }

    @Test
    public void addSecuritySetting() {
        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.addSecuritySettings(PATTERN, ROLE);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateSendSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.selectInTable(PATTERN, 0);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("send", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "send", true, 500);

        cliClient.executeCommand(remove);
   }

    @Test
         public void updateConsumeSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.selectInTable(PATTERN, 0);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("consume", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "consume", true, 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateManageSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.selectInTable(PATTERN, 0);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("manage", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "manage", true, 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateCreateDurableSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.selectInTable(PATTERN, 0);
        page.clickAdvanced();
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("createDurableQueue", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "create-durable-queue", true, 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDeleteDurableSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.selectInTable(PATTERN, 0);
        page.clickAdvanced();
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("deleteDurableQueue", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "delete-durable-queue", true, 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateCreateNonDurableSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.selectInTable(PATTERN, 0);
        page.clickAdvanced();
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("createNonDurableQueue", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "create-non-durable-queue", true, 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDeleteNonDurableSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        page.selectInTable(PATTERN, 0);
        page.clickAdvanced();
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("deleteNonDurableQueue", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "delete-non-durable-queue", true, 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeSecuritySetting() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addRole);

        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToSecuritySettings();

        verifier.verifyResource(address, true);
        page.selectInTable(PATTERN, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
