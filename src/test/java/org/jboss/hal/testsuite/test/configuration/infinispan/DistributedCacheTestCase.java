package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DistributedCacheTestCase extends AbstractCacheTestCase {

    @Override
    public String getDmrBase() {
        return ABSTRACT_DMR_BASE + "/distributed-cache=";
    }

    @Before
    public void setUp() {
        page.distributed();
    }

    public void addCache() {
        String addCache = CliUtils.buildCommand(cacheDmr, ":add", new String[]{"mode=SYNC"});
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

