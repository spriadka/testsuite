package org.jboss.hal.testsuite.test.configuration.logging;

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
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 1.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class CustomFormatterTestCase {
    private static final String NAME = "customFormatter";
    private static final String CLASS = "java.util.logging.XMLFormatter";
    private static final String MODULE = "org.jboss.logmanager";
    private static final String ADD = "/subsystem=logging/custom-formatter=" + NAME + ":add(class=" + CLASS + ",module=" + MODULE + ")";
    private static final String DOMAIN = "/profile=default" ;

    private String command;
    private String remove = "/subsystem=logging/custom-formatter=" + NAME + ":remove";

    private ModelNode path = new ModelNode("/subsystem=logging/custom-formatter=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=default/subsystem=logging/custom-formatter=" + NAME);
    private ResourceAddress address;
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    CliClient cliClient = CliClientFactory.getClient();

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            address = new ResourceAddress(domainPath);
            command = DOMAIN + ADD;
            remove = DOMAIN + "/subsystem=logging/custom-formatter=" + NAME + ":remove";
        } else {
            address = new ResourceAddress(path);
            command = ADD;
        }
    }

    @Test
    public void addCustomFormatter() {
        page.navigateToLogging();
        page.switchToFormatterTab();
        page.switchToCustomPattern();
        page.addCustomFormatter(NAME, CLASS, MODULE);

        verifier.verifyResource(address, true);
        cliClient.executeCommand(remove);
    }

    @Test // https://issues.jboss.org/browse/HAL-821
    public void updateCustomFormatterAttributes() {
        boolean finished = cliClient.executeForSuccess(command);
        assertTrue("Custom formatter should be added by CLI.", finished);

        page.navigateToLogging();
        page.switchToFormatterTab();
        page.switchToCustomPattern();
        page.edit();
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("class", "org.jboss.logmanager.formatters.PatternFormatter");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "class", "org.jboss.logmanager.formatters.PatternFormatter");

        verifier.verifyAttribute(address, "module", "org.jboss.logmanager");

        page.edit();
        editPanelFragment.getEditor().text("properties", "pattern=%s%E%n");
        finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "properties", "[(\"pattern\" => \"%s%E%n\")]");

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeCustomFormatter() {
        boolean finished = cliClient.executeForSuccess(command);
        assertTrue("Custom formatter should be added by CLI.", finished);

        page.navigateToLogging();
        page.switchToFormatterTab();
        page.switchToCustomPattern();
        page.remove();

        verifier.verifyResource(address, false);
    }
}
