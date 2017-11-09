package org.jboss.hal.testsuite.test.configuration.general;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.ValueExpression;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.socketbindings.InboundSocketBindingFragment;
import org.jboss.hal.testsuite.fragment.config.socketbindings.InboundSocketBindingWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.StandardSocketBindingsPage;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SocketBindingsTestCase {

    private static final String BINDING_GROUP = "standard-sockets";
    private static final String INBOUND_NAME = "in_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INBOUND_TBA_NAME = "in_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INBOUND_TBR_NAME = "in_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String OUTBOUND_LOCAL_NAME = "oln_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String OUTBOUND_REMOTE_NAME = "orn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PORT = "42150";
    private static final String HOST = "host_" + RandomStringUtils.randomAlphanumeric(5);
    private static final int NEW_PORT = 43150;
    private static final int MULTICAST_PORT = 45700;
    private static final String MULTICAST_ADDRESS = "${jboss.default.multicast.address:230.0.0.4}";

    private static final Address DEFAULT_SOCKET_BINDING_GROUP_ADDRESS = Address.of("socket-binding-group", BINDING_GROUP);

    private static final Address DMR_INBOUND = DEFAULT_SOCKET_BINDING_GROUP_ADDRESS.and("socket-binding", INBOUND_NAME);
    private static final Address DMR_INBOUND_TBA = DEFAULT_SOCKET_BINDING_GROUP_ADDRESS.and("socket-binding", INBOUND_TBA_NAME);
    private static final Address DMR_INBOUND_TBR = DEFAULT_SOCKET_BINDING_GROUP_ADDRESS.and("socket-binding", INBOUND_TBR_NAME);
    private static final Address DMR_OUTBOUND_LOCAL = DEFAULT_SOCKET_BINDING_GROUP_ADDRESS
            .and("local-destination-outbound-socket-binding", OUTBOUND_LOCAL_NAME);
    private static final Address DMR_OUTBOUND_REMOTE = DEFAULT_SOCKET_BINDING_GROUP_ADDRESS
            .and("remote-destination-outbound-socket-binding", OUTBOUND_REMOTE_NAME);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);

    @Drone
    public WebDriver browser;

    @Page
    public StandardSocketBindingsPage page;

    @BeforeClass
    public static void beforeClass() throws IOException {
        operations.add(DMR_INBOUND_TBR, Values.of("port", AvailablePortFinder.getNextAvailableNonPrivilegedPort()));
    }

    @Before
    public void before() throws IOException {
        operations.add(DMR_INBOUND, Values.of("port", AvailablePortFinder.getNextAvailableNonPrivilegedPort()));

        page.navigate();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(DMR_INBOUND);

        administration.reloadIfRequired();
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(DMR_INBOUND);
            operations.removeIfExists(DMR_INBOUND_TBA);
            operations.removeIfExists(DMR_INBOUND_TBR);
            operations.removeIfExists(DMR_OUTBOUND_REMOTE);
            operations.removeIfExists(DMR_OUTBOUND_LOCAL);

            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }

    }

    @Test
    public void createInboundSocketBinding() throws Exception {
        InboundSocketBindingFragment fragment = page.switchToInbound();
        InboundSocketBindingWizard wizard = fragment.addSocketBinding();

        boolean result =
                wizard.name(INBOUND_TBA_NAME)
                        .port(PORT)
                        .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Socket Binding should be present in table", page.getResourceManager().isResourcePresent(INBOUND_TBA_NAME));
        new ResourceVerifier(DMR_INBOUND_TBA, client).verifyExists();
    }

    @Test
    public void changeInboundPort() throws Exception {
        InboundSocketBindingFragment fragment = page.switchToInbound();
        page.getResourceManager().selectByName(INBOUND_NAME);

        new ConfigChecker.Builder(client, DMR_INBOUND)
                .configFragment(fragment)
                .editAndSave(ConfigChecker.InputType.TEXT, "port", NEW_PORT)
                .verifyFormSaved()
                .verifyAttribute("port", NEW_PORT);
    }

    @Test
    public void changeInboundMulticastPort() throws Exception {
        InboundSocketBindingFragment fragment = page.switchToInbound();
        page.getResourceManager().selectByName(INBOUND_NAME);

        By disclosure = ByJQuery.selector("a.header:has(td:contains('Multicast'):visible)");
        fragment.getRoot().findElement(disclosure).click();

        Editor editor = fragment.edit();
        editor.text("multiCastPort", String.valueOf(MULTICAST_PORT));
        editor.text("multiCastAddress", MULTICAST_ADDRESS);
        boolean isFormSaved = fragment.save();
        Assert.assertTrue("Form should be in read-only mde now!", isFormSaved);

        ModelNode expectedAddress = new ModelNode(ModelType.EXPRESSION).set(new ValueExpression(MULTICAST_ADDRESS));
        new ResourceVerifier(DMR_INBOUND, client)
                .verifyAttribute("multicast-port", MULTICAST_PORT)
                .verifyAttribute("multicast-address", expectedAddress);
    }

    @Test
    public void removeInboundSocketBinding() throws Exception {
        page.switchToInbound();
        page.getResourceManager().removeResource(INBOUND_TBR_NAME).confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(DMR_INBOUND_TBR, client).verifyDoesNotExist();
    }

    @Test
    public void createOutboundLocalSocketBinding() throws Exception {
        ConfigFragment fragment = page.switchToOutboundLocal();
        WizardWindow wizard = fragment.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", OUTBOUND_LOCAL_NAME);
        editor.text("socketBinding", "https");
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        assertTrue("Socket Binding should be present in table", page.getResourceManager().isResourcePresent(OUTBOUND_LOCAL_NAME));
        new ResourceVerifier(DMR_OUTBOUND_LOCAL, client).verifyExists();
    }

    @Test
    public void createOutboundRemoteSocketBinding() throws Exception {
        ConfigFragment fragment = page.switchToOutboundRemote();
        WizardWindow wizard = fragment.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", OUTBOUND_REMOTE_NAME);
        editor.text("host", HOST);
        editor.text("port", PORT);
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        assertTrue("Socket Binding should be present in table", page.getResourceManager().isResourcePresent(OUTBOUND_REMOTE_NAME));
        new ResourceVerifier(DMR_OUTBOUND_REMOTE, client).verifyExists();
    }

}
