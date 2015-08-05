package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertyWizard;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.page.config.JGroupsPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jkasik <jkasik@redhat.com>
 */
public class JGroupAbstractTestCase {

    private static boolean initialized = false;

    protected static final String PROPERTY_NAME = "URL";
    protected static final String PROPERTY_VALUE = "url";
    protected static final String PROPERTY_NAME_P = "URL1";
    protected static final String PROPERTY_VALUE_P = "url1";

    private static CliClient client = CliClientFactory.getClient();
    protected static ResourceVerifier verifier = new ResourceVerifier("", client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);
    protected static JGroupsOperations jGroupsOperations = new JGroupsOperations(client);

    @Drone
    public WebDriver browser;

    @Page
    public JGroupsPage page;


    public void onlyOnce() {
        Console.withBrowser(browser).maximizeWindow();
    }

    public void navigateInDomain() {
        Graphene.goTo(DomainConfigurationPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        page.select("Profiles").select("full-ha").view("JGroups");
        Console.withBrowser(browser).waitUntilLoaded();
    }

    private void navigateInStandalone() {
        Graphene.goTo(StandaloneConfigurationPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        page.select("Subsystems").view("JGroups");
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @Before
    public void before() {
        if (!initialized) { onlyOnce(); initialized = true;}
        Graphene.goTo(HomePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        if (ConfigUtils.isDomain()) {
            navigateInDomain();
        } else {
            navigateInStandalone();
        }
    }

    @After
    public void after() {
        Console.withBrowser(browser).refresh();
    }

    @AfterClass
    public static void tearDown() {
        jGroupsOperations.removeTransportProperty(PROPERTY_NAME, PROPERTY_VALUE);
        jGroupsOperations.removeTransportProperty(PROPERTY_NAME_P, PROPERTY_VALUE_P);
        jGroupsOperations.removeProtocolProperty(PROPERTY_NAME);
        jGroupsOperations.removeProtocolProperty(PROPERTY_NAME_P);
    }

    @Test
    public void socketBindingEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        checker.editTextAndAssert(page, "socketBinding", name).dmrAttribute("socket-binding").invoke();
    }

    @Test
    public void diagnosticSocketEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        checker.editTextAndAssert(page, "diagSocketBinding", name)
                .dmrAttribute("diagnostics-socket-binding").invoke();
    }

    @Test
    public void machineEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        checker.editTextAndAssert(page, "machine", name).dmrAttribute("machine").invoke();
    }

    @Test
    public void sharedStatusEdit() {
        checker.editCheckboxAndAssert(page, "shared", true).dmrAttribute("shared").invoke();
    }

    @Test
    public void siteEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        checker.editTextAndAssert(page, "site", name).invoke();
    }

    @Test
    public void rackEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        checker.editTextAndAssert(page, "rack", name).invoke();
    }

    @Test
    public void threadFactoryEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        page.openExecutors();
        checker.editTextAndAssert(page, "threadFactory", name).dmrAttribute("thread-factory").invoke();
    }

    @Test
    public void defaultExecutorEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        page.openExecutors();
        checker.editTextAndAssert(page, "defaultExecutor", name).dmrAttribute("default-executor").invoke();
    }

    @Test
    public void oobExecutorEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        page.openExecutors();
        checker.editTextAndAssert(page, "oobExecutor", name).dmrAttribute("oob-executor").invoke();
    }

    @Test
    public void timerExecutorEdit() {
        String name = RandomStringUtils.randomAlphabetic(6);
        page.openExecutors();
        checker.editTextAndAssert(page, "timerExecutor", name).dmrAttribute("timer-executor").invoke();
    }

    @Test
    public void createTransportProperty() {
        ConfigPropertiesFragment properties = page.getConfigProperties();
        ConfigPropertyWizard wizard = properties.addProperty();
        wizard.name(PROPERTY_NAME).value(PROPERTY_VALUE).finish();
        assertTrue(jGroupsOperations.verifyTransportProperty(PROPERTY_NAME, PROPERTY_VALUE));
    }

    @Test
    public void removeTransportProperty() {
        ConfigPropertiesFragment properties = page.getConfigProperties();
        properties.removeProperty(PROPERTY_NAME_P);
        assertFalse(jGroupsOperations.verifyTransportProperty(PROPERTY_NAME_P, PROPERTY_VALUE_P));
    }

    @Test
    public void createProtocolProperty() {//TODO
        page.switchToProtocols();
        ConfigPropertiesFragment properties = page.getConfigProperties();
        ConfigPropertyWizard wizard = properties.addProperty();
        wizard.name(PROPERTY_NAME).value(PROPERTY_VALUE).finish();
        assertTrue(jGroupsOperations.verifyProtocolProperty(PROPERTY_NAME, PROPERTY_VALUE));
    }

    @Test
    public void removeProtocolProperty() {//TODO
        page.switchToProtocols();
        ConfigPropertiesFragment properties = page.getConfigProperties();
        properties.removeProperty(PROPERTY_NAME_P);
        assertFalse(jGroupsOperations.verifyProtocolProperty(PROPERTY_NAME_P, PROPERTY_VALUE_P));
    }
}
