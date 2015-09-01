package org.jboss.hal.testsuite.test.configuration.general;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.config.interfaces.NetworkInterfaceContentFragment;
import org.jboss.hal.testsuite.fragment.config.interfaces.NetworkInterfaceWizard;
import org.jboss.hal.testsuite.page.config.NetworkInterfacesPage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.INTERFACE_ADDRESS;
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
    private static final String INTERFACE_NAME = "in_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_ANY_NAME = "ininet_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_ANY_IP4_NAME = "inip4_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_ANY_IP6_NAME = "inip6_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String INTERFACE_NO_ADDRESS_NAME = "inno_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NEW_NIC = "nic_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NEW_NIC_MATCH = "nic_match_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NEW_LOOPBACK_ADDRESS = "loopback_address_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String DMR_INTERFACE = INTERFACE_ADDRESS + "=" + INTERFACE_NAME;
    private static final String DMR_INTERFACE_ANY = INTERFACE_ADDRESS + "=" + INTERFACE_ANY_NAME;
    private static final String DMR_INTERFACE_NO_ADDRESS = INTERFACE_ADDRESS + "=" + INTERFACE_NO_ADDRESS_NAME;
    private static final String DMR_INTERFACE_ANY_IP4 = INTERFACE_ADDRESS + "=" + INTERFACE_ANY_IP4_NAME;
    private static final String DMR_INTERFACE_ANY_IP6 = INTERFACE_ADDRESS + "=" + INTERFACE_ANY_IP6_NAME;

    private static CliClient client = CliClientFactory.getClient();
    private static ResourceVerifier verifier = new ResourceVerifier(DMR_INTERFACE,client);
    private static ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public NetworkInterfacesPage page;

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(NetworkInterfacesPage.class);
    }

    @After
    public void after() {
        client.reload(false);
    }

    @AfterClass
    public static void cleanUp() {
        client.removeResource(DMR_INTERFACE);
        client.removeResource(DMR_INTERFACE_ANY);
        client.removeResource(DMR_INTERFACE_NO_ADDRESS);
        client.removeResource(DMR_INTERFACE_ANY_IP4);
        client.removeResource(DMR_INTERFACE_ANY_IP6);
    }

    @Test
    @InSequence(0)
    public void createInterfaceSpecificInetAddress() {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_NAME)
                .inetAddress(INET_ADDRESS)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_NAME));
        verifier.verifyResource(DMR_INTERFACE, true);
    }



    @Test
    @InSequence(1)
    public void changeNicAttribute() {
        checker.editTextAndAssert(page, "nic", NEW_NIC).clear("inetAddress").rowName(INTERFACE_NAME).invoke();
    }

    @Test
    @InSequence(2)
    public void changeNicMatchAttribute() {
        checker.editTextAndAssert(page, "nicMatch", NEW_NIC_MATCH).clear("nic").rowName(INTERFACE_NAME).invoke();
    }

    @Test
    @InSequence(3)
    public void enableLoopBackAddress() {
        checker.editTextAndAssert(page, "loopbackAddress", NEW_LOOPBACK_ADDRESS).clear("nicMatch").rowName(INTERFACE_NAME).invoke();
    }

    @Test
    @InSequence(4)
    public void removeInterface() {
        NetworkInterfaceContentFragment area = page.getContent();

        area.removeInterface(INTERFACE_NAME);
        verifier.verifyResource(DMR_INTERFACE, false);
    }

    @Test
    public void createInterfaceAnyInetAddress() {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_ANY_NAME)
                .addressWildcard(WILDCARD_ANY_ADDRESS)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_ANY_NAME));
        verifier.verifyResource(DMR_INTERFACE_ANY, true);

        area.removeInterface(INTERFACE_ANY_NAME);
        verifier.verifyResource(DMR_INTERFACE_ANY, false);
    }

    @Ignore("Missing IPV4 option")
    @Test
    public void createInterfaceAnyIPv4Address() {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_ANY_IP4_NAME)
                .addressWildcard(WILDCARD_ANY_IPV4)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_ANY_IP4_NAME));
        verifier.verifyResource(DMR_INTERFACE_ANY_IP4, true);

        area.removeInterface(INTERFACE_ANY_IP4_NAME);
        verifier.verifyResource(DMR_INTERFACE_ANY_IP4, false);
    }

    @Ignore("Missing IPV6 option")
    @Test
    public void createInterfaceAnyIPv6Address() {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_ANY_IP6_NAME)
                .addressWildcard(WILDCARD_ANY_IPV6)
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue("Interface should be present in table", area.resourceIsPresent(INTERFACE_ANY_IP6_NAME));
        verifier.verifyResource(DMR_INTERFACE_ANY_IP6, true);

        area.removeInterface(INTERFACE_ANY_IP6_NAME);
        verifier.verifyResource(DMR_INTERFACE_ANY_IP6, false);
    }

    @Test
    public void createInterfaceWithoutAddress() {
        NetworkInterfaceContentFragment area = page.getContent();
        NetworkInterfaceWizard wizard = area.addInterface();

        boolean result = wizard
                .name(INTERFACE_NO_ADDRESS_NAME)
                .finish();

        assertFalse("Window should not be closed", result);
        verifier.verifyResource(DMR_INTERFACE_NO_ADDRESS, false);
    }
}
