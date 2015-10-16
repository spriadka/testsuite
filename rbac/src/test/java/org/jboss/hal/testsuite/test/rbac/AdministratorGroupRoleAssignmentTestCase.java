package org.jboss.hal.testsuite.test.rbac;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.page.admin.RoleAssignmentPage;
import org.jboss.hal.testsuite.util.Authentication;
import org.jboss.hal.testsuite.util.RbacRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;


/**
 * Created by pcyprian on 11.9.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class AdministratorGroupRoleAssignmentTestCase {

    private static final String TYPE = "GROUP";

    private ResourceAddress address1;
    private ResourceAddress address2;
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    CliClient cliClient = CliClientFactory.getClient();

    private String role;
    private String NAME;
    private String REALM;
    private String command = "/core-service=management/access=authorization/role-mapping=";

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;

    @Before
    public void setUp() {
        NAME = RandomStringUtils.randomAlphanumeric(5);
        REALM = RandomStringUtils.randomAlphanumeric(5);
        Library.letsSleep(1000);
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
    }

    @Test
    public void createRoleAssignmentMonitorRole() {
        role = "Monitor";
        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, true)));

        page.createGroup(NAME, REALM, role);
        verifier.verifyAttribute(address1, "type", "GROUP", 100);
        verifier.verifyResource(address1, true, 100);


        page.removeGroup(NAME, REALM);
        verifier.verifyResource(address1, false);
    }

    @Test //should not create
    public void createRoleAssignmentAuditorRole() {
        role = "Auditor";
        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, true)));

        page.createGroup(NAME, REALM, role);
        verifier.verifyResource(address1, false);
    }

    @Test//should not create
    public void createRoleAssignmentSuperUserRole() {
        role = "SuperUser";
        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, true)));

        page.createGroup(NAME, REALM, role);

        verifier.verifyResource(address1, false);
    }


    @Test //should not remove
    public void removeGroupWithMonitorAndSuperUserRole() {
        role = "Monitor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address1, true);

        role = "SuperUser";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address2 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address2, true, 100);

        page.removeGroup(NAME, REALM);

        verifier.verifyResource(address1, true);
        verifier.verifyResource(address2, true, 100);

        cliClient.executeCommand(command + role + "/include=" + NAME + ":remove");
        cliClient.executeCommand(command + "Monitor" + "/include=" + NAME + ":remove");
        verifier.verifyResource(address1, false);
        verifier.verifyResource(address2, false);
    }

    @Test
    public void removeGroupWithMonitorAndOperatorRole() {
        role = "Monitor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address1, true);

        role = "Operator";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address2 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address2, true, 100);

        page.removeGroup(NAME, REALM);

        verifier.verifyResource(address1, false);
        verifier.verifyResource(address2, false);
    }

    @Test //should not remove
    public void removeGroupWithMonitorAndAuditorRole() {
        role = "Monitor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address1, true);

        role = "Auditor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address2 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address2, true);

        page.removeGroup(NAME, REALM);

        verifier.verifyResource(address1, true);
        verifier.verifyResource(address2, true);

        cliClient.executeCommand(command + role + "/include=" + NAME + ":remove");
        cliClient.executeCommand(command + "Monitor" + "/include=" + NAME + ":remove");
        verifier.verifyResource(address1, false);
        verifier.verifyResource(address2, false);
    }

    @Test
    public void addIncludeOpperator() {
        role = "Monitor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address1, true, 100);

        role = "Operator";
        page.addInclude(NAME, REALM, role);

        address2 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address2, true , 200);

        cliClient.executeCommand(command + role + "/include=" + NAME + ":remove");
        cliClient.executeCommand(command + "Monitor" + "/include=" + NAME + ":remove");
        verifier.verifyResource(address1, false);
        verifier.verifyResource(address2, false);
    }

    @Test
    public void addIncludeAuditor() {
        role = "Monitor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address1, true);
        role = "Auditor";
        page.addInclude(NAME, REALM, role);

        address2 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address2, false);

        cliClient.executeCommand(command + "Monitor" + "/include=" + NAME + ":remove");
        verifier.verifyResource(address1, false);
        verifier.verifyResource(address2, false);
    }

    @Test
    public void addIncludeSuperUser() {
        role = "Monitor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address1, true);
        role = "SuperUser";
        page.addInclude(NAME, REALM, role);

        address2 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address2, false);

        cliClient.executeCommand(command + "Monitor" + "/include=" + NAME + ":remove");
        verifier.verifyResource(address1, false);
        verifier.verifyResource(address2, false);
    }

    @Test
    public void removeIncludeOperator() {
        role = "Monitor";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));

        address1 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));

        verifier.verifyResource(address1, true, 100);

        role = "Operator";
        cliClient.executeCommand(command + role + page.prepareAdd(NAME, TYPE, REALM));
        address2 = new ResourceAddress(new ModelNode(page.preparePathGroup(NAME, REALM, role, false)));
        verifier.verifyResource(address2, true, 100);

        page.removeInclude(NAME, REALM, role);

        verifier.verifyResource(address2, false);

        cliClient.executeCommand(command + "Monitor" + "/include=" + NAME + ":remove");
        verifier.verifyResource(address1, false);

    }
}