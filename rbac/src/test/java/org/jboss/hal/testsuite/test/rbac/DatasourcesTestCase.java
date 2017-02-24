package org.jboss.hal.testsuite.test.rbac;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.config.datasource.DatasourceWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DatasourcesTestCase {

    private String addressName = "ds_" + RandomStringUtils.randomAlphanumeric(5);
    private FinderNavigation navigation;
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final Administration adminOps = new Administration(client);

    @Drone
    public WebDriver browser;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigurationPage.class);
    }

    @After
    public void after() throws IOException, InterruptedException, TimeoutException {
        adminOps.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() {
        IOUtils.closeQuietly(client);
    }

    @Test
    public void administrator() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        checkButtons(true);
        createDatasource(true);
        removeDatasource();

    }

    @Test
    public void maintainer() {
        Authentication.with(browser).authenticate(RbacRole.MAINTAINER);
        checkButtons(true);
        createDatasource(false);
        removeDatasource();
    }

    @Test
    public void monitor() {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);
        checkButtons(false);
    }

    public void checkButtons(boolean visible) {
        navigation.step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "Datasources")
                .step("Type", "Non-XA")
                .step("Datasource").selectColumn();

        By selector = ByJQuery.selector(".btn.primary:contains('Add')");
        try {
            boolean displayed = browser.findElement(selector).isDisplayed();
            Assert.assertEquals("Buttons are visible: " + displayed + ", but expected is " + visible, visible, displayed);
        } catch (NoSuchElementException exc) {
            if (visible) {
                Assert.fail();
            }
        }
    }

    private void createDatasource(boolean fillSensitive) {
        navigation.selectColumn().invoke("Add");
        DatasourceWizard wizard = Console.withBrowser(browser).openedWizard(DatasourceWizard.class);
        Editor editor = wizard.getEditor();
        wizard.next();
        editor.text("name", addressName);
        editor.text("jndiName", "java:/" + addressName);
        wizard.next();
        wizard.switchToDetectedDriverUnchecked();
        wizard.getResourceTable().selectRowByText(0, "h2");
        wizard.next();
        editor.text("connectionUrl", "url");
        Assert.assertEquals(fillSensitive, editor.getText("username").isEnabled());
        Assert.assertEquals(fillSensitive, editor.getText("password").isEnabled());
        wizard.next();
        wizard.next();
        wizard.finish();
    }

    private void removeDatasource() {
        navigation.step("Datasource", addressName).selectRow().invoke("Remove");
        ConfirmationWindow window = Console.withBrowser(browser).openedWindow(ConfirmationWindow.class);
        window.confirmAndDismissReloadRequiredMessage().assertClosed();
    }

}
