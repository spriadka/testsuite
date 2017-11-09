package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

/**
 * Created by pcyprian on 16.9.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SuperuserUserRoleAssignmentTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final RBACOperations rbacOps = new RBACOperations(client);

    private String userName;
    private String realmName;

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;

    @AfterClass
    public static void afterClass() {
        IOUtils.closeQuietly(client);
    }

    @Before
    public void setUp() {
        userName = RandomStringUtils.randomAlphanumeric(5);
        realmName = RandomStringUtils.randomAlphanumeric(5);
        Authentication.with(browser).authenticate(RbacRole.SUPERUSER);
    }

    @Test
    public void createRoleAssignmentMonitorRole() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.getUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client);

        page.addUser(userName, realmName, monitorRole);
        userInMonitorRoleVerifier.verifyAttribute("type", "USER");
        userInMonitorRoleVerifier.verifyExists();

        page.removeUser(userName, realmName);
        userInMonitorRoleVerifier.verifyDoesNotExist();
    }

    @Test //should not create
    public void createRoleAssignmentAuditorRole() throws Exception {

        String auditorRole = "Auditor";
        Address userInAuditorRoleAddress = rbacOps.getUserIncludedInRole(userName, realmName, auditorRole);
        ResourceVerifier userInAuditorRoleVerifier = new ResourceVerifier(userInAuditorRoleAddress, client);

        page.addUser(userName, realmName, auditorRole);
        userInAuditorRoleVerifier.verifyExists();

        page.removeUser(userName, realmName);
        userInAuditorRoleVerifier.verifyDoesNotExist();
    }

    @Test//should not create
    public void createRoleAssignmentSuperUserRole() throws Exception {

        String superUserRole = "SuperUser";
        Address userInSuperUserRoleAddress = rbacOps.getUserIncludedInRole(userName, realmName, superUserRole);
        ResourceVerifier userInSuperUserRoleVerifier = new ResourceVerifier(userInSuperUserRoleAddress, client);

        page.addUser(userName, realmName, superUserRole);
        userInSuperUserRoleVerifier.verifyExists();

        page.removeUser(userName, realmName);
        userInSuperUserRoleVerifier.verifyDoesNotExist();
    }


    @Test
    public void removeUserWithMonitorAndSuperUserRole() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client)
                .verifyExists();

        String superUserRole = "SuperUser";
        Address userInSuperUserRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, superUserRole);
        ResourceVerifier userInSuperUserRoleVerifier = new ResourceVerifier(userInSuperUserRoleAddress, client)
                .verifyExists();

        page.removeUser(userName, realmName);
        userInMonitorRoleVerifier.verifyDoesNotExist();
        userInSuperUserRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void removeUserWithMonitorAndOperatorRole() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client)
                .verifyExists();

        String operatorRole = "Operator";
        Address userInOperatorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, operatorRole);
        ResourceVerifier userInOperatorRoleVerifier = new ResourceVerifier(userInOperatorRoleAddress, client)
                .verifyExists();

        page.removeUser(userName, realmName);
        userInMonitorRoleVerifier.verifyDoesNotExist();
        userInOperatorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void removeUserWithMonitorAndAuditorRole() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client)
                .verifyExists();

        String auditorRole = "Auditor";
        Address userInAuditorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, auditorRole);
        ResourceVerifier userInAuditorRoleVerifier = new ResourceVerifier(userInAuditorRoleAddress, client)
                .verifyExists();

        page.removeUser(userName, realmName);
        userInMonitorRoleVerifier.verifyDoesNotExist();
        userInAuditorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void addIncludeOpperator() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client)
                .verifyExists();

        String operatorRole = "Operator";
        page.addInclude(userName, realmName, operatorRole);
        Address userInOperatorRoleAddress = rbacOps.getPrincipalIncludedInRole(userName, operatorRole);
        ResourceVerifier userInOperatorRoleVerifier = new ResourceVerifier(userInOperatorRoleAddress, client)
                .verifyExists();

        rbacOps.removePrincipalFromRole(userName, operatorRole);
        rbacOps.removePrincipalFromRole(userName, monitorRole);
        userInMonitorRoleVerifier.verifyDoesNotExist();
        userInOperatorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void addIncludeAuditor() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client)
                .verifyExists();

        String auditorRole = "Auditor";
        page.addInclude(userName, realmName, auditorRole);
        Address userInAuditorRoleAddress = rbacOps.getPrincipalIncludedInRole(userName, auditorRole);
        ResourceVerifier userInAuditorRoleVerifier = new ResourceVerifier(userInAuditorRoleAddress, client)
                .verifyExists();

        rbacOps.removePrincipalFromRole(userName, auditorRole);
        rbacOps.removePrincipalFromRole(userName, monitorRole);
        userInMonitorRoleVerifier.verifyDoesNotExist();
        userInAuditorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void addIncludeSuperUser() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client)
                .verifyExists();

        String superUserRole = "SuperUser";
        page.addInclude(userName, realmName, superUserRole);
        Address userInSuperUserRoleAddress = rbacOps.getPrincipalIncludedInRole(userName, superUserRole);
        ResourceVerifier userInSuperUserRoleVerifier = new ResourceVerifier(userInSuperUserRoleAddress, client)
                .verifyExists();

        rbacOps.removePrincipalFromRole(userName, superUserRole);
        rbacOps.removePrincipalFromRole(userName, monitorRole);
        userInMonitorRoleVerifier.verifyDoesNotExist();
        userInSuperUserRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void removeIncludeOperator() throws Exception {

        String monitorRole = "Monitor";
        Address userInMonitorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, monitorRole);
        ResourceVerifier userInMonitorRoleVerifier = new ResourceVerifier(userInMonitorRoleAddress, client)
                .verifyExists();

        String operatorRole = "Operator";
        Address userInOperatorRoleAddress = rbacOps.addUserIncludedInRole(userName, realmName, operatorRole);
        ResourceVerifier userInOperatorRoleVerifier = new ResourceVerifier(userInOperatorRoleAddress, client)
                .verifyExists();

        page.removeInclude(userName, realmName, operatorRole);
        userInOperatorRoleVerifier.verifyDoesNotExist();

        rbacOps.removePrincipalFromRole(userName, monitorRole);
        userInMonitorRoleVerifier.verifyDoesNotExist();

    }
}
