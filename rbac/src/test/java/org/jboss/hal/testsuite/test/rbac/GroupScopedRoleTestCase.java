package org.jboss.hal.testsuite.test.rbac;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

/**
 * Created by pcyprian on 9.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class GroupScopedRoleTestCase {
    private String NAME;
    private FinderNavigation navigation;
    String command = "/core-service=management/access=authorization/server-group-scoped-role=";

    private ResourceAddress address;
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final RBACDomainAdminOperations adminOps = new RBACDomainAdminOperations(client);

    @BeforeClass
    public static void beforeClass() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
    }

    @AfterClass
    public static void afterClass() {
        dispatcher.close();
        IOUtils.closeQuietly(client);
    }

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;


    @Before
    public void before() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
        NAME = "scoped_" + RandomStringUtils.randomAlphanumeric(5);
    }

    @Test
    public void addRole() {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", false);
        verifier.verifyResource(address, true, 300);
        refresh();
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }

    @Test
    public void addRoleWithIncludeAll() {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", true);
        verifier.verifyResource(address, true, 300);
        refresh();
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }

    @Test
    public void removeRoleMonitor() {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", false);
        verifier.verifyResource(address, true, 300);
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }

    @Test
    public void removeRoleWithIncludeAll() {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", true);
        verifier.verifyResource(address, true, 300);
        refresh();
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }

    @Test
    public void removeRoleWithAssignment() throws Exception {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", false);
        verifier.verifyResource(address, true, 300);

        page.addInclude("monitor", "ManagementRealm", NAME);
        Library.letsSleep(1000);
        removeRole();
        verifier.verifyResource(address, true);

        page.removeInclude("monitor", "ManagementRealm", NAME);
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }
    @Test
    public void modifyScope() {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", false);
        verifier.verifyResource(address, true, 300);

        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .step(FinderNames.BROWSE_BY, "Roles")
                .step("Role", NAME);
        navigation.selectRow().invoke("Edit");
        page.getWindowFragment().getEditor().text("scope", "slave");
        page.getWindowFragment().clickButton("Save");
        Library.letsSleep(1000);
        verifier.verifyAttribute(address, "server-groups", "[\"slave\"]");
        refresh();
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }
    @Test
    public void modifyIncludeAll() {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", false);
        verifier.verifyResource(address, true, 300);

        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .step(FinderNames.BROWSE_BY, "Roles")
                .step("Role", NAME);
        navigation.selectRow().invoke("Edit");
        page.getWindowFragment().getEditor().checkbox("includeAll", true);
        page.getWindowFragment().clickButton("Save");

        Library.letsSleep(1000);
        ResourceAddress add = new ResourceAddress(new ModelNode("/core-service=management/access=authorization/role-mapping=" + NAME));
        verifier.verifyAttribute(add, "include-all", true);

        refresh();
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }

    @Test
    public void modifyBaseRole() throws IOException, InterruptedException, java.util.concurrent.TimeoutException {
        address = new ResourceAddress(new ModelNode(command + NAME));
        addRole("Monitor", "master", false);
        verifier.verifyResource(address, true, 300);

        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .step(FinderNames.BROWSE_BY, "Roles")
                .step("Role", NAME);
        navigation.selectRow().invoke("Edit");
        page.getWindowFragment().getEditor().select("baseRole", "Maintainer");
        page.getWindowFragment().clickButton("Save");
        Library.letsSleep(1000);
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        adminOps.reloadAllHostsIfRequired();
        verifier.verifyAttribute(address, "base-role", "Maintainer");
        refresh();
        removeRole();
        Library.letsSleep(1500);
        verifier.verifyResource(address, false);
    }


    public void removeRole() {
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .step(FinderNames.BROWSE_BY, "Roles")
                .step("Role", NAME);
        navigation.selectRow().invoke("Remove");
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    public void addRole(String baseRole, String scope, boolean includeAll) {
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .step(FinderNames.BROWSE_BY, "Roles")
                .step("Role");
        navigation.selectColumn().invoke("Add");

        page.getWindowFragment().getEditor().text("name", NAME);
        page.getWindowFragment().getEditor().select("baseRole", baseRole);
        page.getWindowFragment().getEditor().select("type", "Server Group");
        page.getWindowFragment().getEditor().text("scope", scope);
        page.getWindowFragment().getEditor().checkbox("includeAll", includeAll);
        page.getWindowFragment().clickButton("Save");
    }

    public void refresh() {
        navigation = new FinderNavigation(browser, RoleAssignmentPage.class)
                .step(FinderNames.BROWSE_BY);
        navigation.selectColumn().invoke("Refresh");
    }

}
