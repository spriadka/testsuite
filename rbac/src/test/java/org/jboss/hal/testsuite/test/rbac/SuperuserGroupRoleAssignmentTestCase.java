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
public class SuperuserGroupRoleAssignmentTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final RBACOperations rbacOps = new RBACOperations(client);

    @AfterClass
    public static void afterClass() {
        IOUtils.closeQuietly(client);
    }

    private String groupName;
    private String realmName;

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;

    @Before
    public void setUp() {
        groupName = RandomStringUtils.randomAlphanumeric(5);
        realmName = RandomStringUtils.randomAlphanumeric(5);
        Authentication.with(browser).authenticate(RbacRole.SUPERUSER);
    }

    @Test
    public void createRoleAssignmentMonitorRole() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.getGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client);

        page.createGroup(groupName, realmName, monitorRole);
        groupInMonitorRoleVerifier.verifyAttribute("type", "GROUP");
        groupInMonitorRoleVerifier.verifyExists();

        page.removeGroup(groupName, realmName);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void createRoleAssignmentAuditorRole() throws Exception {

        String auditorRole = "Auditor";
        Address groupInAuditorRoleAddress = rbacOps.getGroupIncludedInRole(groupName, realmName, auditorRole);
        ResourceVerifier groupInAuditorRoleVerifier = new ResourceVerifier(groupInAuditorRoleAddress, client);

        page.createGroup(groupName, realmName, auditorRole);
        groupInAuditorRoleVerifier.verifyExists();

        page.removeGroup(groupName, realmName);
        groupInAuditorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void createRoleAssignmentSuperUserRole() throws Exception {

        String superUserRole = "SuperUser";
        Address groupInSuperUserRoleAddress = rbacOps.getGroupIncludedInRole(groupName, realmName, superUserRole);
        ResourceVerifier groupInSuperUserRoleVerifier = new ResourceVerifier(groupInSuperUserRoleAddress, client);

        page.createGroup(groupName, realmName, superUserRole);
        groupInSuperUserRoleVerifier.verifyExists();

        page.removeGroup(groupName, realmName);
        groupInSuperUserRoleVerifier.verifyDoesNotExist();
    }


    @Test
    public void removeGroupWithMonitorAndSuperUserRole() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client)
                .verifyExists();

        String superUserRole = "SuperUser";
        Address groupInSuperUserRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, superUserRole);
        ResourceVerifier groupInSuperUserRoleVerifier = new ResourceVerifier(groupInSuperUserRoleAddress, client)
                .verifyExists();

        page.removeGroup(groupName, realmName);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
        groupInSuperUserRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void removeGroupWithMonitorAndOperatorRole() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client)
                .verifyExists();

        String operatorRole = "Operator";
        Address groupInOperatorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, operatorRole);
        ResourceVerifier groupInOperatorRoleVerifier = new ResourceVerifier(groupInOperatorRoleAddress, client)
                .verifyExists();

        page.removeGroup(groupName, realmName);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
        groupInOperatorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void removeGroupWithMonitorAndAuditorRole() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client)
                .verifyExists();

        String auditorRole = "Auditor";
        Address groupInAuditorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, auditorRole);
        ResourceVerifier groupInAuditorRoleVerifier = new ResourceVerifier(groupInAuditorRoleAddress, client)
                .verifyExists();

        page.removeGroup(groupName, realmName);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
        groupInAuditorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void addIncludeOpperator() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client)
                .verifyExists();

        String operatorRole = "Operator";
        page.addInclude(groupName, realmName, operatorRole);
        Address groupInOperatorRoleAddress = rbacOps.getPrincipalIncludedInRole(groupName, operatorRole);
        ResourceVerifier groupInOperatorRoleVerifier = new ResourceVerifier(groupInOperatorRoleAddress, client)
                .verifyExists();

        rbacOps.removePrincipalFromRole(groupName, operatorRole);
        rbacOps.removePrincipalFromRole(groupName, monitorRole);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
        groupInOperatorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void addIncludeAuditor() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client)
                .verifyExists();

        String auditorRole = "Auditor";
        page.addInclude(groupName, realmName, auditorRole);
        Address groupInAuditorRoleAddress = rbacOps.getPrincipalIncludedInRole(groupName, auditorRole);
        ResourceVerifier groupInAuditorRoleVerifier = new ResourceVerifier(groupInAuditorRoleAddress, client)
                .verifyExists();

        rbacOps.removePrincipalFromRole(groupName, auditorRole);
        rbacOps.removePrincipalFromRole(groupName, monitorRole);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
        groupInAuditorRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void addIncludeSuperUser() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client)
                .verifyExists();

        String superUserRole = "SuperUser";
        page.addInclude(groupName, realmName, superUserRole);
        Address groupInSuperUserRoleAddress = rbacOps.getPrincipalIncludedInRole(groupName, superUserRole);
        ResourceVerifier groupInSuperUserRoleVerifier = new ResourceVerifier(groupInSuperUserRoleAddress, client)
                .verifyExists();

        rbacOps.removePrincipalFromRole(groupName, superUserRole);
        rbacOps.removePrincipalFromRole(groupName, monitorRole);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
        groupInSuperUserRoleVerifier.verifyDoesNotExist();
    }

    @Test
    public void removeIncludeOperator() throws Exception {

        String monitorRole = "Monitor";
        Address groupInMonitorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, monitorRole);
        ResourceVerifier groupInMonitorRoleVerifier = new ResourceVerifier(groupInMonitorRoleAddress, client)
                .verifyExists();

        String operatorRole = "Operator";
        Address groupInOperatorRoleAddress = rbacOps.addGroupIncludedInRole(groupName, realmName, operatorRole);
        ResourceVerifier groupInOperatorRoleVerifier = new ResourceVerifier(groupInOperatorRoleAddress, client)
                .verifyExists();

        page.removeInclude(groupName, realmName, operatorRole);
        groupInOperatorRoleVerifier.verifyDoesNotExist();

        rbacOps.removePrincipalFromRole(groupName, monitorRole);
        groupInMonitorRoleVerifier.verifyDoesNotExist();
    }
}
