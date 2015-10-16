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
public class AuthenticationTestCase extends SecurityTestCaseAbstract {

    private static final AddressTemplate AUTHENTICATION_TEMPLATE = SECURITY_DOMAIN_TEMPLATE.append("authentication=classic/login-module=*");
    private static final String JBOSS_EJB_POLICY =  "jboss-ejb-policy";
    private static ResourceAddress EJB_POLICY_ADDRESS;

    @BeforeClass
    public static void setUp() {
        EJB_POLICY_ADDRESS = AUTHENTICATION_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, addLoginModule());
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToAuthentication();
    }

    @Test
    public void addLoginModuleInGUI() {
        String name = "lm_" + RandomStringUtils.randomAlphanumeric(6);
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + name;
        page.addAuthenticationModule(name, code);
        verifier.verifyResource(AUTHENTICATION_TEMPLATE.resolve(context, name));
    }

    @Test
    public void editCode() throws IOException, InterruptedException {
        editTextAndVerify(EJB_POLICY_ADDRESS, CODE, CODE_ATTR);
    }


    @Test
    public void editModule() throws IOException, InterruptedException {
        editTextAndVerify(EJB_POLICY_ADDRESS, MODULE, MODULE_ATTR);
    }

    @Test
    public void editModuleOptions() throws IOException, InterruptedException {
        editTextAndVerify(EJB_POLICY_ADDRESS, MODULE_OPTIONS, MODULE_OPTIONS_ATTR, String.join("\n", MODULE_OPTIONS_VALUE));
    }

    @Test
    public void selectFlag() throws IOException, InterruptedException {
        selectOptionAndVerify(EJB_POLICY_ADDRESS, FLAG, FLAG_ATTR, FLAG_VALUE);
    }

    protected static String addLoginModule() {
        String name = "lm_" + RandomStringUtils.randomAlphanumeric(6);
        dispatcher.execute(new Operation.Builder("add", AUTHENTICATION_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, name)).build());
        return name;
    }

}
