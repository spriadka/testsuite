package org.jboss.hal.testsuite.test.configuration.JCA;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Created by pcyprian on 21.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class JCASubsystemTestCase {

    private FinderNavigation navigation;


    private ModelNode path1 = new ModelNode("/subsystem=jca/cached-connection-manager=cached-connection-manager");
    private ModelNode pathArchive = new ModelNode("/subsystem=jca/archive-validation=archive-validation");
    private ModelNode pathBean = new ModelNode("/subsystem=jca/bean-validation=bean-validation");
    private ResourceAddress address = new ResourceAddress(path1);
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;

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
    private JCAPage page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "JCA");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    @Test
    public void updateDebug() {
        page.edit().checkbox("debug", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "debug", true, 500);

        page.edit().checkbox("debug", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "debug", false, 500);
    }

    @Test
    public void updateError() {
        page.edit().checkbox("error", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "error", true, 500);

        page.edit().checkbox("error", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "error", false, 500);
    }

    @Test
    public void updateIgnoreUnknownConnections() {
        page.edit().checkbox("ignore-unknown-connections", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "ignore-unknown-connections", true, 500);

        page.edit().checkbox("ignore-unknown-connections", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "ignore-unknown-connections", false, 500);
    }

    @Test
    public void updateArchiveValidationEnabled() {
        address = new ResourceAddress(pathArchive);

        page.switchToArchiveValidation();
        page.edit().checkbox("enabled", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "enabled", false, 500);

        page.edit().checkbox("enabled", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "enabled", true, 500);
    }

    @Test
    public void updateArchiveValidationFailOnError() {
        address = new ResourceAddress(pathArchive);

        page.switchToArchiveValidation();
        page.edit().checkbox("fail-on-error", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "fail-on-error", false, 500);

        page.edit().checkbox("fail-on-error", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "fail-on-error", true, 500);
    }

    @Test
    public void updateArchiveValidationFailOnWarn() {
        address = new ResourceAddress(pathArchive);

        page.switchToArchiveValidation();
        page.edit().checkbox("fail-on-warn", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "fail-on-warn", true, 500);

        page.edit().checkbox("fail-on-warn", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "fail-on-warn", false, 500);
    }

    @Test
    public void updateBeanValidationEnabled() {
        address = new ResourceAddress(pathBean);

        page.switchToBeanValidation();
        page.edit().checkbox("enabled", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "enabled", false, 500);

        page.edit().checkbox("enabled", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "enabled", true, 500);
    }
}
