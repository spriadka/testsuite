package org.jboss.hal.testsuite.test.configuration.general;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.interfaces.NetworkInterfaceContentFragment;
import org.jboss.hal.testsuite.fragment.config.interfaces.NetworkInterfaceWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.NetworkInterfacesPage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class NetworkInterfacesTestCase {

    private static final String INET_ADDRESS = "127.0.0.1";
    private static final String WILDCARD_ANY_ADDRESS = "Any Address";
    private static final String WILDCARD_ANY_IPV4 = "Any IP4";
    private static final String WILDCARD_ANY_IPV6 = "Any IP6";
    private static final String INTERFACE_TBA_NAME = "in-tba_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_TBR_NAME = "in-tbr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_NAME = "in_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_ANY_NAME = "ininet_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_ANY_IP4_NAME = "inip4_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_ANY_IP6_NAME = "inip6_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_NO_ADDRESS_NAME = "inno_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NEW_NIC = "nic_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NEW_NIC_MATCH = "nic_match_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NEW_LOOPBACK_ADDRESS = "loopback_address_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address DMR_INTERFACE_TBA = Address.root().and("interface", INTERFACE_TBA_NAME);
    private static final Address DMR_INTERFACE_TBR = Address.root().and("interface", INTERFACE_TBR_NAME);
    private static final Address DMR_INTERFACE = Address.root().and("interface", INTERFACE_NAME);
    private static final Address DMR_INTERFACE_ANY = Address.root().and("interface", INTERFACE_ANY_NAME);
    private static final Address DMR_INTERFACE_NO_ADDRESS = Address.root().and("interface", INTERFACE_NO_ADDRESS_NAME);
    private static final Address DMR_INTERFACE_ANY_IP4 = Address.root().and("interface", INTERFACE_ANY_IP4_NAME);
    private static final Address DMR_INTERFACE_ANY_IP6 = Address.root().and("interface", INTERFACE_ANY_IP6_NAME);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    @Drone
    public WebDriver browser;

    @Page
    public NetworkInterfacesPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        operations.add(DMR_INTERFACE_TBR, Values.of("any-address", true));

        new ResourceVerifier(DMR_INTERFACE_TBR, client, 500).verifyExists();
    }

    @Before
    public void before() throws Exception {
        operations.add(DMR_INTERFACE, Values.of("inet-address", "127.0.0.1"));
        new ResourceVerifier(DMR_INTERFACE, client, 500).verifyExists();

        page.navigate();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(DMR_INTERFACE);

        administration.reloadIfRequired();
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(DMR_INTERFACE_TBA);
        operations.removeIfExists(DMR_INTERFACE_TBR);
        operations.removeIfExists(DMR_INTERFACE);
        operations.removeIfExists(DMR_INTERFACE_ANY);
        operations.removeIfExists(DMR_INTERFACE_NO_ADDRESS);
        operations.removeIfExists(DMR_INTERFACE_ANY_IP4);
        operations.removeIfExists(DMR_INTERFACE_ANY_IP6);

        administration.restartIfRequired();
        administration.reloadIfRequired();
    }

    @Test
    public void createInterfaceSpecificInetAddress() throws Exception {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_TBA_NAME)
                .inetAddress(INET_ADDRESS)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_TBA_NAME));
        new ResourceVerifier(DMR_INTERFACE_TBA, client).verifyExists();
    }

    @Test
    public void changeNicAttribute() throws Exception {
        NetworkInterfaceContentFragment fragment = page.getContent();
        fragment.selectInterface(INTERFACE_NAME);

        Editor editor = fragment.edit();
        editor.text("inetAddress", "");
        editor.text("nic", NEW_NIC);
        boolean isInReadMode = fragment.save();

        Assert.assertTrue("Form should be in read-only mode now!", isInReadMode);
        new ResourceVerifier(DMR_INTERFACE, client).verifyAttribute("nic", NEW_NIC);
    }

    @Test
    public void changeNicMatchAttribute() throws Exception {
        NetworkInterfaceContentFragment fragment = page.getContent();
        fragment.selectInterface(INTERFACE_NAME);

        Editor editor = fragment.edit();
        editor.text("inetAddress", "");
        editor.text("nicMatch", NEW_NIC_MATCH);
        boolean isInReadMode = fragment.save();

        Assert.assertTrue("Form should be in read-only mode now!", isInReadMode);
        new ResourceVerifier(DMR_INTERFACE, client).verifyAttribute("nic-match", NEW_NIC_MATCH);
    }

    @Test
    public void enableLoopBackAddress() throws Exception {
        NetworkInterfaceContentFragment fragment = page.getContent();
        fragment.selectInterface(INTERFACE_NAME);

        Editor editor = fragment.edit();
        editor.text("inetAddress", "");
        editor.text("loopbackAddress", NEW_LOOPBACK_ADDRESS);
        boolean isInReadMode = fragment.save();

        Assert.assertTrue("Form should be in read-only mode now!", isInReadMode);
        new ResourceVerifier(DMR_INTERFACE, client).verifyAttribute("loopback-address", NEW_LOOPBACK_ADDRESS);
    }

    @Test
    public void removeInterface() throws Exception {
        NetworkInterfaceContentFragment area = page.getContent();

        area.removeInterface(INTERFACE_TBR_NAME);
        new ResourceVerifier(DMR_INTERFACE_TBR, client).verifyDoesNotExist();
    }

    @Test
    public void createInterfaceAnyInetAddress() throws Exception {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_ANY_NAME)
                .addressWildcard(WILDCARD_ANY_ADDRESS)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_ANY_NAME));
        new ResourceVerifier(DMR_INTERFACE_ANY, client).verifyExists();
    }

    @Ignore("Missing IPV4 option")
    @Test
    public void createInterfaceAnyIPv4Address() throws Exception {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_ANY_IP4_NAME)
                .addressWildcard(WILDCARD_ANY_IPV4)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_ANY_IP4_NAME));
        new ResourceVerifier(DMR_INTERFACE_ANY_IP4, client).verifyExists();
    }

    @Ignore("Missing IPV6 option")
    @Test
    public void createInterfaceAnyIPv6Address() throws Exception {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_ANY_IP6_NAME)
                .addressWildcard(WILDCARD_ANY_IPV6)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_ANY_IP6_NAME));
        new ResourceVerifier(DMR_INTERFACE_ANY_IP6, client).verifyExists();
    }

    @Test
    public void createInterfaceWithoutAddress() throws Exception {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_NO_ADDRESS_NAME)
                .finish();

        assertFalse("Window should not be closed", result);
        new ResourceVerifier(DMR_INTERFACE_NO_ADDRESS, client).verifyDoesNotExist();
    }
}
