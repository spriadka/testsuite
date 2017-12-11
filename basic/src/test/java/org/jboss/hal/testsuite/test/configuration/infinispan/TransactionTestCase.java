package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CacheContainerContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.CacheContext;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.junit.ArquillianParametrized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.util.Collection;

@RunWith(ArquillianParametrized.class)
@RunAsClient
public class TransactionTestCase extends InfinispanTestCaseAbstract {

    private static final String TRANSACTION = "transaction";
    private static final String TRANSACTION_LABEL = "Transaction";
    private static final String COMPONENT = "component";
    private static final String LOCKING = "locking";
    private static final String MODE = "mode";
    private static final String STOP_TIMEOUT = "stop-timeout";

    public CacheContext cacheContext;
    public CacheContainerContext cacheContainerContext;

    public TransactionTestCase(CacheContainerContext cacheContainerContext, CacheContext cacheContext) {
        this.cacheContainerContext = cacheContainerContext;
        this.cacheContext = cacheContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}, Cache type: {1}")
    public static Collection values() {
        return new ParametersFactory(client).containerTypeMatrix();
    }

    @Test
    public void editTransactionTest() throws Exception {
        final LockingType locking = LockingType.OPTIMISTIC;
        final ModeType mode = ModeType.FULL_XA;
        final long stopTimeout = 456789;
        final Address transactionAddress = cacheContext.getCacheAddress().and(COMPONENT, TRANSACTION);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(TRANSACTION_LABEL);
            new ConfigChecker.Builder(client, transactionAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.SELECT, LOCKING, locking.getLockingValue())
                    .edit(ConfigChecker.InputType.SELECT, MODE, mode.getModeValue())
                    .edit(ConfigChecker.InputType.TEXT, STOP_TIMEOUT, String.valueOf(stopTimeout))
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(LOCKING, locking.getLockingValue())
                    .verifyAttribute(MODE, mode.getModeValue())
                    .verifyAttribute(STOP_TIMEOUT, stopTimeout);
        } finally {
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }

}
