package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.openqa.selenium.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by pcyprian on 15.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerGroupTestCase {

    private FinderNavigation navigation;

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();

    @AfterClass
    public static void afterClass() {
        IOUtils.closeQuietly(client);
    }

    @Drone
    private WebDriver browser;

    @Test
    public void administrator() throws Exception {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        checkStandardButtons(true);
        checkRemoveButtonForGroup("main-server-group", true);
        checkRemoveButtonForGroup("mainmaster-server-group", true);
        checkRemoveButtonForGroup("other-server-group", true);
        addServerGroup();
    }

    @Test
    public void monitor() {
        Authentication.with(browser).authenticate(RbacRole.MONITOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
        checkRemoveButtonForGroup("mainmaster-server-group", false);
        checkRemoveButtonForGroup("other-server-group", false);
    }

    @Test // https://issues.jboss.org/browse/HAL-908
    public void mainAdministrator() {
        Authentication.with(browser).authenticate(RbacRole.MAIN_ADMINISTRATOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
    }


    @Test
    public void mainMonitor() {
        Authentication.with(browser).authenticate(RbacRole.MAIN_MONITOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
    }

    @Test
    public void hostMasterAdministrator() {
        Authentication.with(browser).authenticate(RbacRole.HOST_MASTER_ADMINISTRATOR);
        checkStandardButtons(false);
        checkRemoveButtonForGroup("main-server-group", false);
        checkRemoveButtonForGroup("mainmaster-server-group", false);
        checkRemoveButtonForGroup("other-server-group", false);

    }



    // test utils

    public void checkStandardButtons(boolean visible) {
        boolean unvisible = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP);

        try {
            navigation.selectColumn().invoke("Add");
        } catch (NoSuchElementException ex) {
            unvisible = true;
        }
        assertEquals("Problem with visibility of buttons add and remove.", unvisible, !visible);
    }

    private void checkRemoveButtonForGroup(String group, boolean visible) {
        boolean unvisible = false;
        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP, group);

        try {
            navigation.selectRow().invoke("Remove");
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).clickButton("Cancel");
        } catch (NoSuchElementException ex) {
             unvisible = true;
        } catch (TimeoutException ignored) {
        }

        assertEquals("Problem with visibility of buttons add and remove. ", unvisible, !visible);
    }

    public void addServerGroup() throws Exception {
        Address serverGroupAddress = Address.of("server-group", "my-server-group");
        ResourceVerifier serverGroupVerifier = new ResourceVerifier(serverGroupAddress, client);

        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP);

        navigation.selectColumn().invoke("Add");

        WizardWindow wizard = Console.withBrowser(browser).openedWizard();
        Editor editor = wizard.getEditor();
        editor.text("name", "my-server-group");
        editor.select("profileName", "default");
        editor.select("socketBinding", "standard-sockets");

        wizard.assertFinish(true);
        serverGroupVerifier.verifyExists();

        navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.SERVER_GROUPS)
                .step(FinderNames.SERVER_GROUP, "my-server-group");

        navigation.selectRow().invoke("Remove");

        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }

        serverGroupVerifier.verifyDoesNotExist();
    }

}



