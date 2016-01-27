package org.jboss.hal.testsuite.test.configuration.messaging;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.mina.util.AvailablePortFinder;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 18.12.15.
 */
public abstract class AbstractMessagingTestCase {

    protected static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static Administration administration;
    protected static Operations operations;

    protected static final Address MESSAGING_SUBSYSTEM = client.options().isDomain ?
            Address.of("profile", client.options().defaultProfile)
                    .and("subsystem", "messaging-activemq") :
            Address.subsystem("messaging-activemq");

    protected static final Address DEFAULT_MESSAGING_SERVER = MESSAGING_SUBSYSTEM.and("server", "default");

    private static List<String> socketBindings;

    @Page
    protected MessagingPage page;

    @Drone
    private WebDriver browser;

    @BeforeClass
    public static void beforeClass_() {
        administration = new Administration(client);
        operations = new Operations(client);
    }

    @AfterClass
    public static void afterClass_() throws IOException, CommandFailedException {
        try {
            removeAccumulatedSocketBindings();
        } finally {
            client.close();
        }
    }

    protected void editTextAndVerify(Address address, String name, String value) throws Exception {
        editTextAndVerify(address, name, name, value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, String value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String name, Integer value) throws Exception {
        editTextAndVerify(address, name, name, value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, Integer value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, value.toString());
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String name, Long value) throws Exception {
       editTextAndVerify(address, name, name, value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, Long value) throws Exception {
        editTextAndVerify(address, name, attributeName, value.toString(), value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, String formValue, Long value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, formValue);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(attributeName, value);
    }

    protected void editCheckboxAndVerify(Address address, String name, boolean value) throws Exception {
        boolean finished = page.getConfigFragment().editCheckboxAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(name, value);
    }

    protected  void selectOptionAndVerify(Address address, String name, String value) throws Exception {
        boolean finished = page.getConfigFragment().selectOptionAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(name, value);
    }

    protected void editTextAreaAndVerify(Address address, String name, String[] values) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, String.join("\n", values));

        assertTrue("Config should be saved and closed.", finished);

        ModelNode modelNode = new ModelNode();

        for (String value : values) {
            modelNode.add(value);
        }

        new ResourceVerifier(address, client, 500).verifyAttribute(name, modelNode);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShownInForm());
        config.cancel();
    }

    protected void undefineAndVerify(Address address, String identifier, String attributeName) throws Exception {
        page.getConfigFragment().editTextAndSave(identifier, "");
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttributeIsUndefined(attributeName);
    }

    protected void undefineAndVerify(Address address, String identifier) throws Exception {
        undefineAndVerify(address, identifier, identifier);
    }

    protected static String createSocketBinding(String name) throws CommandFailedException {
        client.apply(new AddSocketBinding.Builder(name)
                .port(AvailablePortFinder.getNextAvailable())
                .build());
        addSocketBindingToList(name);
        return name;
    }

    protected static String createSocketBinding() throws CommandFailedException {
        String name = "messaging_" + RandomStringUtils.randomAlphanumeric(6);
        return createSocketBinding(name);
    }


    private static void addSocketBindingToList(String name) {
        if (socketBindings == null) {
            socketBindings = new LinkedList<>();
        }
        socketBindings.add(name);
    }

    private static void removeAccumulatedSocketBindings() throws CommandFailedException {
        if (socketBindings == null) {
            return;
        }
        for (String socketBinding : socketBindings) {
            client.apply(new RemoveSocketBinding(socketBinding));
        }
        socketBindings.clear();
    }



}
