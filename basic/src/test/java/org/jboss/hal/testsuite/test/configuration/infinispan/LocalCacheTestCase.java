package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.jboss.hal.testsuite.category.Standalone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class LocalCacheTestCase extends AbstractCacheTestCase {

    @Override
    @Test
    public void createCache() {
        String name = "cn_" + RandomStringUtils.randomAlphanumeric(5);
        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(name)
                .finish();

        Assert.assertTrue("Window should be closed", result);
        verifier.verifyResource(getDmrBase() + name, true);

        page.content().getResourceManager().removeResourceAndConfirm(name);
        verifier.verifyResource(getDmrBase() + name, false);
    }

    @Before
    public void setUp() {
        page.local();
    }

    @Override
    public String getDmrBase() {
        return ABSTRACT_DMR_BASE + "/local-cache=";
    }

    public void addCache() {
        String addCache = CliUtils.buildCommand(cacheDmr, ":add");
        String addTransaction = CliUtils.buildCommand(transactionDmr, ":add");
        String addLocking = CliUtils.buildCommand(lockingDmr, ":add");
        String addStore = CliUtils.buildCommand(storeDmr, ":add", new String[]{"class=clazz"});
        client.executeCommand(addCache);
        client.executeCommand(addTransaction);
        client.executeCommand(addLocking);
        client.executeCommand(addStore);
    }

    public void deleteCache() {
        String cmd = CliUtils.buildCommand(cacheDmr, ":remove");
        client.executeCommand(cmd);
    }
}

