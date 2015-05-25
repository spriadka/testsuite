package org.jboss.hal.testsuite.test.configuration.web;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.WebServicesPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.*;


/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class WebServicesTestCase {

    private static final String MODIFY_SOAP_ADDRESS_ID = "modifyAddress";
    private static final String MODIFY_SOAP_ADDRESS_DMR = "modify-wsdl-address";
    private static final String WSDL_HOST_ID = "wsdlHost";
    private static final String WSDL_PORT_ID = "wsdlPort";
    private static final String WSDL_SECURE_PORT_ID = "wsdlSecurePort";

    private static final String PORT_VALUE = "50";
    private static final String PORT_VALUE_NEGATIVE = "50";
    private static final String SIMPLE_IP = "127.0.0.2";

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(WEB_SERVICES_SUBSYSTEM_ADDRESS, client);

    @Drone
    public WebDriver browser;

    @Page
    public WebServicesPage page;

    @Before
    public void before() {
        browser.navigate().refresh();
        Graphene.goTo(WebServicesPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        browser.manage().window().maximize();
    }

    @Test
    public void modifySoapAddress() {
        changeCheckboxAndAssert(page.config(), MODIFY_SOAP_ADDRESS_ID, false, true, MODIFY_SOAP_ADDRESS_DMR);
        changeCheckboxAndAssert(page.config(), MODIFY_SOAP_ADDRESS_ID, true, true, MODIFY_SOAP_ADDRESS_DMR);
    }

    @Test
    public void setWsdlPort() {
        changeTextAndAssert(page.config(), WSDL_PORT_ID, PORT_VALUE, true);
    }

    @Test
    public void setWsdlPortNegative() {
        changeTextAndAssert(page.config(), WSDL_PORT_ID, PORT_VALUE_NEGATIVE, true);
    }

    @Test
    public void setWsdlSecurePort() {
        changeTextAndAssert(page.config(), WSDL_SECURE_PORT_ID, PORT_VALUE, true);
    }

    @Test
    public void setWsdlSecurePortNegative() {
        changeTextAndAssert(page.config(), WSDL_SECURE_PORT_ID, PORT_VALUE_NEGATIVE, true);
    }

    @Test
    public void setWsdlHostSimpleIP() {
        changeTextAndAssert(page.config(), WSDL_HOST_ID, SIMPLE_IP, true);
    }

    private void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expected) {
        changeTextAndAssert(fragment, identifier, value, expected, identifier);
    }

    private void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expected) {
        changeCheckboxAndAssert(fragment, identifier, value, expected, identifier);
    }

    private void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expected, String dmrAttribute) {
        fragment.edit().text(identifier, value);
        fragment.saveAndAssert(expected);
        if (expected != false) {
            verifier.verifyAttribute(dmrAttribute, value);
        }
    }

    private void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expected, String dmrAttribute) {
        fragment.edit().checkbox(identifier, value);
        fragment.saveAndAssert(expected);
        if (expected != false) {
            verifier.verifyAttribute(dmrAttribute, String.valueOf(value));
        }
    }
}
