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
import org.junit.Before;
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
public class AddressSettingsTestCase {
    private static final String PATTERN = "test-p";
    private static final String ADD = "/subsystem=messaging-activemq/server==default/address-setting=" + PATTERN + ":add()";
    private static final String DOMAIN = "/profile=full-ha";

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/address-setting=" + PATTERN + ":remove";
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/address-setting=" + PATTERN );
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/address-setting=" + PATTERN);
    private ResourceAddress address;
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    CliClient cliClient = CliClientFactory.getClient();

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
    public void addAddressSetting() {
        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToAddressSettings();

        page.addAddressSettings(PATTERN);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateDeadLetterAddressSetting() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToAddressSettings();
        page.selectInTable(PATTERN, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("deadLetterQueue", "jms.queue.ExpiryQueue");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "dead-letter-address", "jms.queue.ExpiryQueue");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateExpiryAddressSetting() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToAddressSettings();
        page.selectInTable(PATTERN, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("expiryQueue", "jms.queue.DLQ");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "expiry-address", "jms.queue.DLQ");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateRedeliveryDelayAddressSetting() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToAddressSettings();
        page.selectInTable(PATTERN, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("redeliveryDelay", "10");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "redelivery-delay", "10");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateRedeliveryDelayAddressSettingWrongValue() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToAddressSettings();
        page.selectInTable(PATTERN, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("redeliveryDelay", "-1");
        boolean finished = editPanelFragment.save();

        assertFalse("Config should not be saved and closed.Value was invalid.", finished);
        verifier.verifyAttribute(address, "redelivery-delay", "0");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateMaxDeliveryAttemptsAddressSetting() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToAddressSettings();
        page.selectInTable(PATTERN, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("maxDelivery", "0");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "max-delivery-attempts", "0");

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeAddressSetting() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToAddressSettings();

        verifier.verifyResource(address, true);
        page.selectInTable(PATTERN, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
