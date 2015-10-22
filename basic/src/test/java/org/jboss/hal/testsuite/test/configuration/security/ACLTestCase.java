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
public class ACLTestCase extends SecurityTestCaseAbstract {

    private static final AddressTemplate ACL_TEMPLATE = SECURITY_DOMAIN_TEMPLATE.append("acl=classic/acl-module=*");
    private static ResourceAddress ACL_ADDRESS;
    private static String aclModule;

    @BeforeClass
    public static void setUp() {
        aclModule = addACLModule();
        ACL_ADDRESS = ACL_TEMPLATE.resolve(context, JBOSS_WEB_POLICY, aclModule);
    }

    @Before
    public void before() {
        page.viewJBossWebPolicy();
        page.switchToACL();
        page.selectModule(aclModule);
    }

    @Test
    public void editModuleOptions() throws IOException, InterruptedException {
        editModuleOptionsAndVerify(ACL_ADDRESS, MODULE_OPTIONS, MODULE_OPTIONS_ATTR, MODULE_OPTIONS_VALUE);
    }

    private static String addACLModule() {
        addACL();
        String name = "acl_" + RandomStringUtils.randomAlphanumeric(6);
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + name;
        dispatcher.execute(new Operation.Builder("add", ACL_TEMPLATE.resolve(context, JBOSS_WEB_POLICY, name))
            .param("code", code)
            .param("flag", "optional")
            .build());
        return name;
    }

    private static void addACL() {
        dispatcher.execute(new Operation.Builder("add", SECURITY_DOMAIN_TEMPLATE.append("acl=classic")
                .resolve(context, JBOSS_WEB_POLICY))
                .build());
        reloadIfRequiredAndWaitForRunning();
    }
}
