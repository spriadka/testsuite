package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.junit.Assert;
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
public class IdentityTrustTestCase extends SecurityTestCaseAbstract {

    private static final AddressTemplate TRUST_TEMPLATE = SECURITY_DOMAIN_TEMPLATE.append("identity-trust=classic/trust-module=*");
    private static ResourceAddress TRUST_ADDRESS;
    private static String loginModule;

    @BeforeClass
    public static void setUp() {
        loginModule = addTrustModule();
        TRUST_ADDRESS = TRUST_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, loginModule);
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToIdentityTrust();
        page.selectModule(loginModule);
    }

    @Test
    public void editCode() throws IOException, InterruptedException {
        editTextAndVerify(TRUST_ADDRESS, CODE, CODE_ATTR);
    }

    @Test
    public void addTrustModuleInGUI() {
        String name = "tm_" + RandomStringUtils.randomAlphanumeric(6);
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + name;
        page.addTrustModule(name, code);
        Assert.assertTrue("Trust module should be present in table", page.getConfigFragment().resourceIsPresent(name));
        verifier.verifyResource(TRUST_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, name));
    }

    private static String addTrustModule() {
        addIdentityTrust();
        String name = "tm_" + RandomStringUtils.randomAlphanumeric(6);
        dispatcher.execute(new Operation.Builder("add", TRUST_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, name))
                .param("code", RandomStringUtils.randomAlphanumeric(8))
                .param("flag", "optional")
                .build());
        reloadIfRequiredAndWaitForRunning();
        return name;
    }

    private static void addIdentityTrust() {
        dispatcher.execute(new Operation.Builder("add", SECURITY_DOMAIN_TEMPLATE
                .append("identity-trust=classic")
                .resolve(context, JBOSS_EJB_POLICY))
                .build());
        reloadIfRequiredAndWaitForRunning();
    }
}
