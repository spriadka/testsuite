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
public class MappingTestCase extends SecurityTestCaseAbstract {

    private static final AddressTemplate MAPPING_TEMPLATE = SECURITY_DOMAIN_TEMPLATE.append("mapping=classic/mapping-module=*");
    private static ResourceAddress MAPPING_ADDRESS;
    private static String mappingModule;

    @BeforeClass
    public static void setUp() {
        mappingModule = addMappingModule();
        MAPPING_ADDRESS = MAPPING_TEMPLATE.resolve(context, OTHER, mappingModule);
    }

    @Before
    public void before() {
        page.viewOther();
        page.switchToMapping();
        page.selectModule(mappingModule);
    }

    @Test
    public void editCode() throws IOException, InterruptedException {
        editTextAndVerify(MAPPING_ADDRESS, CODE, CODE_ATTR);
    }

    @Test
    public void addMappingModuleInGUI() {
        String name = "map_" + RandomStringUtils.randomAlphanumeric(6);
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + name;
        String type = "type_" + RandomStringUtils.randomAlphanumeric(6) + "-" + name;
        page.addMappingModule(name, code, type);
        Assert.assertTrue("Mapping module should be present in table", page.getConfigFragment().resourceIsPresent(name));
        verifier.verifyResource(MAPPING_TEMPLATE.resolve(context, OTHER, name));
    }

    private static String addMappingModule() {
        addMapping();
        String name = "map_" + RandomStringUtils.randomAlphanumeric(6);
        dispatcher.execute(new Operation.Builder("add", MAPPING_TEMPLATE.resolve(context, OTHER, name))
                .param("code", RandomStringUtils.randomAlphanumeric(8))
                .param("type", RandomStringUtils.randomAlphanumeric(8))
                .build());
        reloadIfRequiredAndWaitForRunning();
        return name;
    }

    private static void addMapping() {
        dispatcher.execute(new Operation.Builder("add", SECURITY_DOMAIN_TEMPLATE
                .append("mapping=classic")
                .resolve(context, OTHER))
                .build());
    }
}
