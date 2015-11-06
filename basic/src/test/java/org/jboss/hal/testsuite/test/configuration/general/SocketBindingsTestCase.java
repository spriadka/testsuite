package org.jboss.hal.testsuite.test.configuration.general;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.socketbindings.InboundSocketBindingFragment;
import org.jboss.hal.testsuite.fragment.config.socketbindings.InboundSocketBindingWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.StandardSocketBindingsPage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.DEFAULT_SOCKET_BINDING_INBOUND_ADDRESS;
import static org.jboss.hal.testsuite.cli.CliConstants.DEFAULT_SOCKET_BINDING_OUTBOUND_LOCAL_ADDRESS;
import static org.jboss.hal.testsuite.cli.CliConstants.DEFAULT_SOCKET_BINDING_OUTBOUND_REMOTE_ADDRESS;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SocketBindingsTestCase {

    private static final String BINDING_GROUP = "standard-sockets";
    private static final String INBOUND_NAME = "in_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String OUTBOUND_LOCAL_NAME = "oln_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String OUTBOUND_REMOTE_NAME = "orn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PORT = "42150";
    private static final String HOST = "host_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NEW_PORT = "43150";
    private static final String MULTICAST_PORT = "45700";
    private static final String MULTICAST_ADDRESS = "${jboss.default.multicast.address:230.0.0.4}";

    private static final String DMR_INBOUND = DEFAULT_SOCKET_BINDING_INBOUND_ADDRESS + "=" + INBOUND_NAME;
    private static final String DMR_OUTBOUND_LOCAL = DEFAULT_SOCKET_BINDING_OUTBOUND_LOCAL_ADDRESS + "=" + OUTBOUND_LOCAL_NAME;
    private static final String DMR_OUTBOUND_REMOTE = DEFAULT_SOCKET_BINDING_OUTBOUND_REMOTE_ADDRESS + "=" + OUTBOUND_REMOTE_NAME;

    private static CliClient client = CliClientFactory.getClient();
    private static ResourceVerifier verifier = new ResourceVerifier(DMR_INBOUND, client);
    private static ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public StandardSocketBindingsPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @After
    public void after() {
        client.reload(false);
    }

    @AfterClass
    public static void cleanUp() {
        client.removeResource(DMR_INBOUND);
        client.removeResource(DMR_OUTBOUND_REMOTE);
        client.removeResource(DMR_OUTBOUND_LOCAL);
    }

    @Test
    @InSequence(0)
    public void createInboundSocketBinding() {
        InboundSocketBindingFragment fragment = page.switchToInbound();
        InboundSocketBindingWizard wizard = fragment.addSocketBinding();

        boolean result =
                wizard.name(INBOUND_NAME)
                        .port(PORT)
                        .group(BINDING_GROUP)
                        .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Socket Binding should be present in table", fragment.resourceIsPresent(INBOUND_NAME));
        verifier.verifyResource(true);
    }

    @Test
    @InSequence(1)
    public void changeInboundPort() {
        page.switchToInbound();
        checker.editTextAndAssert(page, "port", NEW_PORT).rowName(INBOUND_NAME).invoke();
    }

    @Test
    @InSequence(2)
    public void changeInboundMulticastPort() {
        page.switchToInbound();
        checker.editTextAndAssert(page, "multiCastPort", MULTICAST_PORT)
                .rowName(INBOUND_NAME).disclose("Multicast").dmrAttribute("multicast-port").invoke();
        checker.editTextAndAssert(page, "multiCastAddress", MULTICAST_ADDRESS)
                .rowName(INBOUND_NAME).dmrAttribute("multicast-address").invoke();
    }

    @Test
    @InSequence(3)
    public void removeInboundSocketBinding() {
        InboundSocketBindingFragment fragment = page.switchToInbound();
        fragment.getResourceManager().removeResourceAndConfirm(INBOUND_NAME);

        verifier.verifyResource(false);

    }

    @Test
    public void createOutboundLocalSocketBinding() {
        ConfigFragment fragment = page.switchToOutboundLocal();
        WizardWindow wizard = fragment.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", OUTBOUND_LOCAL_NAME);
        editor.text("socketBinding", "ajp");
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        assertTrue("Socket Binding should be present in table", fragment.resourceIsPresent(OUTBOUND_LOCAL_NAME));
        verifier.verifyResource(DMR_OUTBOUND_LOCAL, true);

        fragment.getResourceManager().removeResourceAndConfirm(OUTBOUND_LOCAL_NAME);
        verifier.verifyResource(DMR_OUTBOUND_LOCAL, false);
    }

    @Test
    public void createOutboundRemoteSocketBinding() {
        ConfigFragment fragment = page.switchToOutboundRemote();
        WizardWindow wizard = fragment.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", OUTBOUND_REMOTE_NAME);
        editor.text("host", HOST);
        editor.text("port", PORT);
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        assertTrue("Socket Binding should be present in table", fragment.resourceIsPresent(OUTBOUND_REMOTE_NAME));
        verifier.verifyResource(DMR_OUTBOUND_REMOTE, true);

        fragment.getResourceManager().removeResourceAndConfirm(OUTBOUND_REMOTE_NAME);

        verifier.verifyResource(DMR_OUTBOUND_REMOTE, false);
    }

}
