package org.jboss.hal.testsuite.test.configuration.messaging.connections;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
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
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 7.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class GenericAcceptorTestCase {
    private static final String NAME = "generic-text-acceptor";
    private static final String BINDING = "socket-binding";
    private static final String FACTORYCLASS = "factoryClass";
    private static final String ADD = "/subsystem=messaging-activemq/server=default/acceptor=" + NAME + ":add(socket-binding=" + BINDING + ",factory-class=" + FACTORYCLASS + ")";
    private static final String DOMAIN = "/profile=full-ha" ;

    private String command;
    private String remove = "/subsystem=messaging-activemq/server=default/acceptor=" + NAME + ":remove";
    private String addProperty = "/subsystem=messaging-activemq/server=default/acceptor=" + NAME + ":write-attribute(name=params.prop,value=test)";
    private ModelNode path = new ModelNode("/subsystem=messaging-activemq/server=default/acceptor=" + NAME);
    private ModelNode domainPath = new ModelNode("/profile=full-ha/subsystem=messaging-activemq/server=default/acceptor=" + NAME);
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
            addProperty = DOMAIN + addProperty;
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
    public void addGenericAcceptor() {
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchType("Type: Generic");
        page.addGenericAcceptor(NAME, BINDING, FACTORYCLASS);

        verifier.verifyResource(address, true);

        cliClient.executeCommand(remove);

        verifier.verifyResource(address, false);
    }

    @Test
    public void updateAcceptorSocketBinding() throws CommandFailedException, IOException {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchType("Type: Generic");
        page.selectInTable(NAME, 0);
        page.edit();

        String socketBindingName = "DiscGroupSocketBinding";

        try (OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient()) {
            client.apply(new AddSocketBinding.Builder(socketBindingName)
                    .port(ThreadLocalRandom.current().nextInt(10000, 19999))
                    .build());
        }

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("socketBinding", socketBindingName);
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "socket-binding", socketBindingName, 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateGenericAcceptorFacotryClass() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchType("Type: Generic");
        page.selectInTable(NAME, 0);
        page.edit();

        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.getEditor().text("factoryClass", "fc");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(address, "factory-class", "fc", 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void updateAcceptorProperties() {
        cliClient.executeCommand(command);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchType("Type: Generic");
        page.selectInTable(NAME, 0);

        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        ConfigPropertyWizard wizard = properties.addProperty();
        boolean result = wizard.name("prop").value("test").finish();

        assertTrue("Property should be added and wizard closed.", result);
        verifier.verifyAttribute(address, "params", "{\"prop\" => \"test\"}", 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeAcceptorProperties() {
        cliClient.executeCommand(command);
        cliClient.executeCommand(addProperty);
        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchType("Type: Generic");
        page.selectInTable(NAME, 0);
        ConfigPropertiesFragment properties = page.getConfig().propertiesConfig();
        properties.removeProperty("prop");

        verifier.verifyAttribute(address, "params", "undefined", 500);

        cliClient.executeCommand(remove);
    }

    @Test
    public void removeGenericAcceptor() {
        cliClient.executeCommand(command);

        page.navigateToMessaging();
        page.selectView("Connections");
        page.switchType("Type: Generic");

        verifier.verifyResource(address, true);

        page.selectInTable(NAME, 0);
        page.remove();

        verifier.verifyResource(address, false);
    }

}
