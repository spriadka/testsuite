package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 16.10.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class AuthorizationTestCase extends SecurityTestCaseAbstract {

    private static final AddressTemplate AUTHORIZATION_TEMPLATE = SECURITY_DOMAIN_TEMPLATE.append("authorization=classic/policy-module=*");
    private static final String JBOSS_EJB_POLICY =  "jboss-ejb-policy";
    private static ResourceAddress AUTHORIZATION_ADDRESS;
    private static String moduleName;
    private static String toBeRemoved;

    @BeforeClass
    public static void setUp() {
        moduleName = addPolicyModule();
        toBeRemoved = addPolicyModule();
        AUTHORIZATION_ADDRESS = AUTHORIZATION_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, moduleName);
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToAuthorization();
        page.selectModule(moduleName);
    }

    @Test
    public void selectFlag() throws IOException, InterruptedException {
        selectOptionAndVerify(AUTHORIZATION_ADDRESS, FLAG, FLAG_ATTR, FLAG_VALUE);
    }

    @Test
    public void removeModuleInGUI() {
        page.getResourceManager().removeResource(toBeRemoved).confirm();
    }

    @Test
    public void editModuleOptions() throws IOException, InterruptedException {
        editModuleOptionsAndVerify(AUTHORIZATION_ADDRESS, MODULE_OPTIONS, MODULE_OPTIONS_ATTR, MODULE_OPTIONS_VALUE);
    }

    protected static String addPolicyModule() {
        String name = "pm_" + RandomStringUtils.randomAlphanumeric(6);
        dispatcher.execute(new Operation.Builder("add", AUTHORIZATION_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, name))
                .param("code", RandomStringUtils.randomAlphanumeric(8))
                .param("flag", "optional")
                .build());
        return name;
    }

}
