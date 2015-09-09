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

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class DivertsTestCase {
    private static final String NAME = "test-divert";
    private static final String DIVERTADDRESS = "divert";
    private static final String FORWATDADDRESS = "forward";
    private static final String ADD = "/subsystem=messaging-activemq/server==default/divert=" + NAME + ":add(divert-address="
            + DIVERTADDRESS + ",forwarding-address=" + FORWATDADDRESS + ",routing-name=" + NAME + ")";
    private static final String DOMAIN = "/profile=full-ha";

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/divert=" + NAME + ":remove";
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/divert=" + NAME );
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/divert=" + NAME);
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
    public void addDiverts() {
        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToDiverts();

        page.addDiverts(NAME, DIVERTADDRESS, FORWATDADDRESS);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateDivertsDivertAddress() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToDiverts();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("divertAddress", "divAdd");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "divert-address", "divAdd");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDivertsForwardAddress() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToDiverts();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("forwardingAddress", "fowAdd");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "forwarding-address", "fowAdd");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDivertExclusive() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToDiverts();
        page.selectInTable(NAME, 0);
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().checkbox("exclusive", true);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "exclusive", true);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDivertsFilter() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToDiverts();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("filter", "myFilter");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "filter", "myFilter");

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateDivertsTransformerClass() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToDiverts();
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("transformerClass", "clazz");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "transformer-class-name", "clazz");

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeDiverts() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Destinations");
        page.switchToDiverts();

        verifier.verifyResource(address, true);
        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }
}
