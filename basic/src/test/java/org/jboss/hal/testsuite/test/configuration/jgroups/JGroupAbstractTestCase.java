package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.mina.util.AvailablePortFinder;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.config.jgroups.JGroupsProtocolPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.jgroups.JGroupsProtocolPropertyWizard;
import org.jboss.hal.testsuite.fragment.config.jgroups.JGroupsTransportPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.jgroups.JGroupsTransportPropertyWizard;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.JGroupsPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JGroupAbstractTestCase {

    private static final Logger log = LoggerFactory.getLogger(JGroupAbstractTestCase.class);

    protected static Address BASE_ADDRESS;
    protected static Address PROTOCOL_ADDRESS;
    protected static Address TRANSPORT_ADDRESS;

    protected static final String PROPERTY_NAME = "URL";
    protected static final String PROPERTY_VALUE = "url";
    protected static final String PROPERTY_NAME_P = "URL1";
    protected static final String PROPERTY_VALUE_P = "url1";
    protected static final String DEFAULT_PROTOCOL = "UNICAST3";

    protected static final String  PROTOCOL_PROPERTY_TBR = "protocolProperty_" + RandomStringUtils.randomAlphanumeric(3);
    protected static final String  PROTOCOL_PROPERTY_TBR_VALUE = RandomStringUtils.randomAlphanumeric(3);
    protected static final String  TRANSPORT_PROPERTY_TBR = "protocolProperty_" + RandomStringUtils.randomAlphanumeric(3);
    protected static final String  TRANSPORT_PROPERTY_TBR_VALUE = RandomStringUtils.randomAlphanumeric(3);


    protected static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static Administration administration = new Administration(client);
    protected static JGroupsOperations jGroupsOperations = new JGroupsOperations(client);

    protected static final Address JGROUPS_ADDRESS = client.options().isStandalone ? Address.subsystem("jgroups") :
            Address.of("profile", "full-ha").and("subsystem", "jgroups");

    @Drone
    public WebDriver browser;

    @Page
    public JGroupsPage page;

    @Before
    public void before() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full-ha");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS);
        }
        navigation.addAddress(FinderNames.SUBSYSTEM, "JGroups")
                .selectRow(true)
                .invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
    }

    @After
    public void after() {
        Console.withBrowser(browser).refresh();
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        jGroupsOperations.removeProperty(TRANSPORT_ADDRESS, TRANSPORT_PROPERTY_TBR);
        jGroupsOperations.removeProperty(PROTOCOL_ADDRESS, PROTOCOL_PROPERTY_TBR);
        jGroupsOperations.removeProperty(TRANSPORT_ADDRESS, PROPERTY_NAME);
        jGroupsOperations.removeProperty(TRANSPORT_ADDRESS, PROPERTY_NAME);
        jGroupsOperations.removeProperty(TRANSPORT_ADDRESS, PROPERTY_NAME_P);
        jGroupsOperations.removeProperty(PROTOCOL_ADDRESS, PROPERTY_NAME);
        jGroupsOperations.removeProperty(PROTOCOL_ADDRESS, PROPERTY_NAME_P);
    }

    @Test
    public void socketBindingEdit() throws Exception {
        String name = "JGroupsSocketBinding_" + RandomStringUtils.randomAlphanumeric(5);
        try (OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient()) {
            int port = AvailablePortFinder.getNextAvailable();
            log.info("Obtained port for socket binding '" + name + "' is " + port);
            client.apply(new AddSocketBinding.Builder(name)
                    .port(port)
                    .build());
        }
        editTextAndVerify(TRANSPORT_ADDRESS,  "socketBinding", "socket-binding", name);
    }

    @Test
    public void socketBindingEditInvalid() throws Exception {
        verifyValueIsNotSaved(TRANSPORT_ADDRESS, "SocketBinding", "socket-binding", RandomStringUtils.randomAlphabetic(6));
    }

    @Test
    public void diagnosticSocketEdit() throws Exception {
        String name = "jgroups-udp";
        editTextAndVerify(TRANSPORT_ADDRESS, "diagSocketBinding", "diagnostics-socket-binding", name);
    }

    @Test
    public void diagnosticSocketEditInvalid() throws Exception {
        String name = "qwedfsdg"; //non-existing socket binding
        verifyValueIsNotSaved(TRANSPORT_ADDRESS, "diagSocketBinding", "diagnostics-socket-binding", name);
    }

    @Test
    public void machineEdit() throws Exception {
        String name = "JGroupsMachine_" + RandomStringUtils.randomAlphabetic(6);
        editTextAndVerify(TRANSPORT_ADDRESS, "machine", name);
    }

    @Test
    public void sharedStatusEdit() throws Exception {
        page.getConfigFragment().editCheckboxAndSave("shared", true);
        new ResourceVerifier(TRANSPORT_ADDRESS, client).verifyAttribute("shared", true);
    }

    @Test
    public void siteEdit() throws Exception {
        String name = "JGroupsSite_" + RandomStringUtils.randomAlphabetic(6);
        editTextAndVerify(TRANSPORT_ADDRESS, "site", name);
    }

    @Test
    public void rackEdit() throws Exception {
        String name = "JGroupsRack_" + RandomStringUtils.randomAlphabetic(6);
        editTextAndVerify(TRANSPORT_ADDRESS, "rack", name);
    }

    @Test
    public void createTransportProperty() throws IOException {
        JGroupsTransportPropertiesFragment properties = page.getConfig().transportPropertiesConfig();
        JGroupsTransportPropertyWizard wizard = properties.addProperty();
        wizard.key(PROPERTY_NAME).value(PROPERTY_VALUE).clickSave();
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        assertTrue(jGroupsOperations.propertyExists(TRANSPORT_ADDRESS, PROPERTY_NAME, PROPERTY_VALUE));
    }

    @Test
    public void removeTransportProperty() throws IOException {
        JGroupsTransportPropertiesFragment properties = page.getConfig().transportPropertiesConfig();
        properties.removeProperty(TRANSPORT_PROPERTY_TBR);
        assertFalse(jGroupsOperations.propertyExists(TRANSPORT_ADDRESS, TRANSPORT_PROPERTY_TBR, TRANSPORT_PROPERTY_TBR_VALUE));
    }


    @InSequence(2)
    @Test
    public void createProtocolProperty() throws IOException {
        page.switchToProtocol(DEFAULT_PROTOCOL);
        JGroupsProtocolPropertiesFragment properties = page.getConfig().protocolPropertiesConfig();
        JGroupsProtocolPropertyWizard wizard = properties.addProperty();
        wizard.key(PROPERTY_NAME).value(PROPERTY_VALUE).finish();
        assertTrue(jGroupsOperations.propertyExists(PROTOCOL_ADDRESS, PROPERTY_NAME, PROPERTY_VALUE));
    }

    @InSequence(1)
    @Test
    public void removeProtocolProperty() throws IOException {
        page.switchToProtocol(DEFAULT_PROTOCOL);
        JGroupsProtocolPropertiesFragment properties = page.getConfig().protocolPropertiesConfig();
        properties.removeProperty(PROTOCOL_PROPERTY_TBR);
        assertFalse(jGroupsOperations.propertyExists(PROTOCOL_ADDRESS, PROTOCOL_PROPERTY_TBR, PROTOCOL_PROPERTY_TBR_VALUE));
    }

    private void editTextAndVerify(Address address, String identifier, String value) throws Exception {
        editTextAndVerify(address, identifier, identifier, value);
    }

    private void editTextAndVerify(Address address, String identifier, String attribute, String value) throws Exception {
        page.editTextAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(attribute, value);
    }

    private void verifyValueIsNotSaved(Address address, String identifier, String attribute, String value) throws Exception {
        page.editTextAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttributeNotEqual(attribute, new ModelNode(value));
    }

}
