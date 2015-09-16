package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.runtime.DeploymentPage;
import org.jboss.hal.testsuite.page.runtime.DomainDeploymentPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by pcyprian on 10.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ManagedDeploymentTestCase {
    private static final String FILE_PATH = "src/test/resources/";
    private static final String FILE_NAME = "mockWar.war";
    private static final String NAME = "n_" + RandomStringUtils.randomAlphanumeric(5) + ".war";
    private static final String RUNTIME_NAME = "rn_" + RandomStringUtils.randomAlphanumeric(5) + ".war";

    private FinderNavigation navigation;

    @Drone
    private WebDriver browser;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainDeploymentPage.class);
        } else {
            navigation = new FinderNavigation(browser, DeploymentPage.class);
        }
    }

    @Test
    public void administrator() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        checkButtons(true);
        createDeployment(true);
        enableDeployment();
        removeDeployment();
    }


    @Test
    public void maintainer() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MAINTAINER);
        checkButtons(true);
        createDeployment(true);
        enableDeployment();
        removeDeployment();
    }

    @Test
    public void deployer() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        checkButtons(true);
        createDeployment(true);
        enableDeployment();
        removeDeployment();
    }

    @Test
    public void monitor() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);
        checkButtons(false);
    }



    public void checkButtons(boolean visible) {
        navigation.addAddress("Deployment").selectColumn();

        By selector = ByJQuery.selector(".btn.primary:contains('Add')");
        try {
            boolean displayed = browser.findElement(selector).isDisplayed();
            assertEquals("Buttons are visible: " + displayed + ", but expected is " + visible, visible, displayed);
        } catch (NoSuchElementException exc) {
            if (visible) {
                Assert.fail();
            }
        }
    }

    public void createDeployment(boolean shouldSucceed) {
        navigation.selectColumn().invoke("Add");
        File deployment = new File(FILE_PATH + FILE_NAME);

        DeploymentWizard wizard = Console.withBrowser(browser).openedWizard(DeploymentWizard.class);

        boolean result = wizard.nextFluent()
                .uploadDeployment(deployment)
                .nextFluent()
                .name(NAME)
                .runtimeName(RUNTIME_NAME)
                .enableAfterDeployment(false)
                .finish();

        assertEquals(shouldSucceed, result);
        Graphene.waitGui().until().element(wizard.getRoot()).is().not().present();
    }

    private void removeDeployment() {
        navigation.addAddress("Deployment", NAME).selectRow().invoke("Remove");
        ConfirmationWindow window = Console.withBrowser(browser).openedWindow(ConfirmationWindow.class);
        window.confirm();
        if (!window.isClosed()) Assert.fail();
        Graphene.waitGui().until().element(window.getRoot()).is().not().present();
    }

    private void enableDeployment() {
        navigation.addAddress("Deployment", NAME).selectRow().invoke("Enable");
        ConfirmationWindow window = Console.withBrowser(browser).openedWindow(ConfirmationWindow.class);
        window.confirm();
        Graphene.waitGui().until().element(window.getRoot()).is().not().present();
    }

}

