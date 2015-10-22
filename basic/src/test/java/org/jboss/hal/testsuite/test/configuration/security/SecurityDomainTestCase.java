package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.fragment.config.security.SecurityDomainAddWizard;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 16.10.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecurityDomainTestCase extends SecurityTestCaseAbstract {

    @Test
    public void addSecurityDomainInGUI() {
        String name = "sd_" + RandomStringUtils.randomAlphanumeric(6);
        SecurityDomainAddWizard wizard = page.addSecurityDomain();
        wizard.name(name)
            .cacheType("default")
            .finish();
        Assert.assertTrue("Security domain should be visible", page.isDomainPresent(name));
        verifier.verifyResource(SECURITY_DOMAIN_TEMPLATE.resolve(context, name));
    }

    @Test
    public void removeSecurityDomainInGUI() {
        String name = createSecurityDomain();
        page.navigate();
        page.removeSecurityDomain(name);
        verifier.verifyResource(SECURITY_DOMAIN_TEMPLATE.resolve(context, name), false);
    }

    private String createSecurityDomain() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        dispatcher.execute(new Operation.Builder("add", SECURITY_DOMAIN_TEMPLATE.resolve(context, name)).build());
        return name;
    }
}
