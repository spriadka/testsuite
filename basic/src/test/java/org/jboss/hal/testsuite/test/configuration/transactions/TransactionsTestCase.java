package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class TransactionsTestCase extends TransactionsTestCaseAbstract {

    private final String DEFAULT_TIMEOUT_ATTR = "default-timeout";
    private final String ENABLE_TSM_STATUS_ATTR = "enable-tsm-status";
    private final String JTS_ATTR = "jts";
    private final String NODE_IDENTIFIER_ATTR = "node-identifier";
    private final String STATISTICS_ENABLED_ATTR = "statistics-enabled";
    private final String SOCKET_BINDING_ATTR = "socket-binding";
    private final String STATUS_SOCKET_BINDING_ATTR = "status-socket-binding";
    private final String RECOVERY_LISTENER_ATTR = "recovery-listener";
    private final String OBJECT_STORE_PATH_ATTR = "object-store-path";
    private final String OBJECT_STORE_RELATIVE_TO_ATTR = "object-store-relative-to";

    @Page
    public TransactionsPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void setStatisticsToTrue() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, STATISTICS_ENABLED_ATTR, true);
    }

    @Test
    public void setStatisticsToFalse() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, STATISTICS_ENABLED_ATTR, true);
    }

    @Test
    public void setJTSToTrue() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JTS_ATTR, true);
    }

    @Test
    public void setJTSToFalse() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, JTS_ATTR, false);
    }

    @Test
    public void setTSMStatusToTrue() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, ENABLE_TSM_STATUS_ATTR, true);
    }

    @Test
    public void setTSMStatusToFalse() throws Exception {
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, ENABLE_TSM_STATUS_ATTR, false);
    }

    @Test
    public void editNodeIdentifier() throws Exception {
        editTextAndVerify(TRANSACTIONS_ADDRESS, NODE_IDENTIFIER_ATTR, "NodeIdentifier");
    }

    @Test
    public void editDefaultTimeout() throws Exception {
        editTextAndVerify(TRANSACTIONS_ADDRESS, DEFAULT_TIMEOUT_ATTR, 500);
    }

    @Test
    public void editDefaultTimeoutNegative() {
        verifyIfErrorAppears(DEFAULT_TIMEOUT_ATTR, "-500");
    }
    @Test
    public void editDefaultTimeoutInvalid() {
        verifyIfErrorAppears(DEFAULT_TIMEOUT_ATTR, "foobar");
    }



    @Test
    public void editSocketBinding() throws Exception {
        String socketBinding = transactionsOps.createSocketBinding();
        ModelNode value = operations.readAttribute(TRANSACTIONS_ADDRESS, SOCKET_BINDING_ATTR).value();
        try {
            page.getConfig().switchTo("Recovery");
            new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOCKET_BINDING_ATTR, socketBinding)
                    .verifyFormSaved()
                    .verifyAttribute(SOCKET_BINDING_ATTR, socketBinding);

        } finally {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, SOCKET_BINDING_ATTR, value);
            client.apply(new RemoveSocketBinding(socketBinding));
        }
    }

    @Test
    public void editStatusSocketBinding() throws Exception {
        String socketBinding = transactionsOps.createSocketBinding();
        ModelNode value = operations.readAttribute(TRANSACTIONS_ADDRESS, STATUS_SOCKET_BINDING_ATTR).value();
        try {
            page.getConfig().switchTo("Recovery");
            new ConfigChecker.Builder(client, TRANSACTIONS_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, STATUS_SOCKET_BINDING_ATTR, socketBinding)
                    .verifyFormSaved()
                    .verifyAttribute(STATUS_SOCKET_BINDING_ATTR, socketBinding);
        } finally {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, STATUS_SOCKET_BINDING_ATTR, value);
            client.apply(new RemoveSocketBinding(socketBinding));
        }
    }

    @Test
    public void setRecoveryListenerToTrue() throws Exception {
        page.getConfig().switchTo("Recovery");
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, RECOVERY_LISTENER_ATTR, true);
    }

    @Test
    public void setRecoveryListenerToFalse() throws Exception {
        page.getConfig().switchTo("Recovery");
        editCheckboxAndVerify(TRANSACTIONS_ADDRESS, RECOVERY_LISTENER_ATTR, false);
    }

    @Test
    public void editObjectStorePath() throws Exception {
        page.getConfig().switchTo("Path");
        ModelNode defValue = operations.readAttribute(TRANSACTIONS_ADDRESS, OBJECT_STORE_PATH_ATTR).value();
        try {
            editTextAndVerify(TRANSACTIONS_ADDRESS, OBJECT_STORE_PATH_ATTR, "test");
        } finally {
            operations.writeAttribute(TRANSACTIONS_ADDRESS, OBJECT_STORE_PATH_ATTR, defValue);
        }
    }

    @Test
    public void editObjectStoreRelativeTo() throws Exception {
        page.getConfig().switchTo("Path");
        editTextAndVerify(TRANSACTIONS_ADDRESS, OBJECT_STORE_RELATIVE_TO_ATTR, "jboss.server.base.dir");
    }

}
