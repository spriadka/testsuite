package org.jboss.hal.testsuite.test.configuration.security;

import org.junit.Assert;
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
public class AuditTestCase extends SecurityTestCaseAbstract {

    private static final AddressTemplate AUDIT_TEMPLATE = SECURITY_DOMAIN_TEMPLATE.append("audit=classic/provider-module=*");
    private static final String JBOSS_EJB_POLICY =  "jboss-ejb-policy";
    private static ResourceAddress AUDIT_ADDRESS;
    private static String moduleName;

    @BeforeClass
    public static void setUp() {
        moduleName = addProviderModule();
        AUDIT_ADDRESS = AUDIT_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, moduleName);
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToAudit();
        page.selectModule(moduleName);
    }

    @Test
    public void editCode() throws IOException, InterruptedException {
        editTextAndVerify(AUDIT_ADDRESS, CODE, CODE_ATTR);
    }

    @Test
    public void addProviderModuleInGUI() {
        String name = "prm_" + RandomStringUtils.randomAlphanumeric(6);
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + name;
        page.addAuditModule(name, code);
        Assert.assertTrue("Provider module should be present in table", page.getConfigFragment().resourceIsPresent(name));
        verifier.verifyResource(AUDIT_TEMPLATE.resolve(context, name));
    }

    private static String addProviderModule() {
        addAudit();
        String name = "prm_" + RandomStringUtils.randomAlphanumeric(6);
        dispatcher.execute(new Operation.Builder("add", AUDIT_TEMPLATE.resolve(context, JBOSS_EJB_POLICY, name))
                .param("code", RandomStringUtils.randomAlphanumeric(8))
                .build());
        reloadIfRequiredAndWaitForRunning();
        return name;
    }

    private static void addAudit() {
        dispatcher.execute(new Operation.Builder("add", SECURITY_DOMAIN_TEMPLATE
                .append("audit=classic")
                .resolve(context, JBOSS_EJB_POLICY))
            .build());
    }


}
