package org.jboss.hal.testsuite.test.transactions;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.runtime.MessagingPreparedTransactionsPage;
import org.jboss.hal.testsuite.util.AssertionErrorCollector;
import org.jboss.hal.testsuite.util.PathOperations;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Test class testing rollback and commit of prepared transactions in Web Console.
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(Standalone.class)
public class TransactionCommitAndRollbackTestCase {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCommitAndRollbackTestCase.class);

    @Drone
    private WebDriver browser;

    @Page
    private MessagingPreparedTransactionsPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    private static final Address TRANSACTIONS_LOGGER_ADDRESS = Address.subsystem("logging").and("logger",
            "org.apache.activemq.artemis.core.transaction.impl.TransactionImpl");

    private static Path serverLog;

    @BeforeClass
    public static void beforeClass() throws IOException {
        operations.add(TRANSACTIONS_LOGGER_ADDRESS, Values.of("level", "TRACE"));
        serverLog = new PathOperations(client).getServerLogFile();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException {
        try {
            operations.removeIfExists(TRANSACTIONS_LOGGER_ADDRESS);
        } finally {
            client.close();
        }
    }

    @Before
    public void before() {
        page.navigate();
    }

    /**
     * Unique identifiers of prepared transactions as they appear in UI. This ID is visible in logs and in Web Console
     * UI. In case of prepared journal update it needs to be updated according to displayed ids in Web Console again.
     */
    private static String[] TRANSACTIONS_XID = new String[]{
            "AAAAAAAAAAAAAP__CigFZuOgfONXxVvAAAASdQAAAAEAAAAAAAAAAAAAAAAAAP__CigFZuOgfONXxVvAAAASdDEHAgIA",
            "AAAAAAAAAAAAAP__CigFZuOgfONXxVvAAAASvwAAAAEAAAAAAAAAAAAAAAAAAP__CigFZuOgfONXxVvAAAASdDEHAgIA",
            "AAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFpgAAAAEAAAAAAAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFSTEHAgIA",
            "AAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFSgAAAAEAAAAAAAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFSTEHAgIA",
            "AAAAAAAAAAAAAP__CigFZuOgfONXxVvAAAASbwAAAAEAAAAAAAAAAAAAAAAAAP__CigFZuOgfONXxVvAAAASbjEHAgIA",
            "AAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFrgAAAAEAAAAAAAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFrTEHAgIA",
            "AAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFsgAAAAEAAAAAAAAAAAAAAAAAAP__CigFZtXWO0pXxVwMAAAFrTEHAgIA"
    };

    private static final Vector<PreparedTransactionTestCell> TRANSACTIONS_TEST_DATA = new Vector<PreparedTransactionTestCell>() {
        {
        add(new PreparedTransactionTestCell(TRANSACTIONS_XID[0], PerformedAction.ROLLBACK));
        add(new PreparedTransactionTestCell(TRANSACTIONS_XID[1], PerformedAction.COMMIT));
        add(new PreparedTransactionTestCell(TRANSACTIONS_XID[2], PerformedAction.COMMIT));
        add(new PreparedTransactionTestCell(TRANSACTIONS_XID[3], PerformedAction.ROLLBACK));
        add(new PreparedTransactionTestCell(TRANSACTIONS_XID[4], PerformedAction.COMMIT));
        add(new PreparedTransactionTestCell(TRANSACTIONS_XID[5], PerformedAction.ROLLBACK));
        add(new PreparedTransactionTestCell(TRANSACTIONS_XID[6], PerformedAction.COMMIT));
        }
    };

    @Test
    public void testTransactionsRollbackAndCommit() throws InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final LogListenerForRollbackAndCommit listener = new LogListenerForRollbackAndCommit();
        final Tailer tailer = new Tailer(serverLog.toFile(), listener, 1);
        Future tailerFuture = null;
        try {
            logger.debug("Starting tailer to monitor logfile!");
            tailerFuture = executor.submit(tailer);
            for (PreparedTransactionTestCell transactionTestCell : TRANSACTIONS_TEST_DATA) {
                page.selectPreparedTransaction(transactionTestCell.getXId());
                page.clickButton(transactionTestCell.getPerformedAction().getButtonText());
            }
        } finally {
            logger.debug("Stopping tailer!");
            tailer.stop();
            if (tailerFuture != null) {
                tailerFuture.get(1, TimeUnit.SECONDS);
            }
            executor.shutdown();
        }

        //check if finished successfully
        AssertionErrorCollector assertionErrorCollector = new AssertionErrorCollector();
        for (PreparedTransactionTestCell transactionTestCell : TRANSACTIONS_TEST_DATA) {
            try {
                Assert.assertTrue(
                        "Transaction " + transactionTestCell.getXId() + " should be " +
                        (transactionTestCell.getPerformedAction().equals(PerformedAction.COMMIT) ? "committed" : "rolled back") +
                         " successfully.", transactionTestCell.getFinishedSuccessfully());
            } catch (AssertionError error) {
                assertionErrorCollector.collect(error);
            }
        }
        assertionErrorCollector.report();
    }

    private static final class LogListenerForRollbackAndCommit extends TailerListenerAdapter {

        @Override
        public void handle(String line) {
            logger.debug("Line handled by tailer: '{}'", line);
            for (PreparedTransactionTestCell transactionTestCell : TRANSACTIONS_TEST_DATA) {
                if (transactionTestCell.matchesLogLinePattern(line)) {
                    transactionTestCell.setFinishedSuccessfully();
                }
            }
        }
    }

    /**
     * Action performed on prepared transaction
     */
    private enum PerformedAction {
        ROLLBACK("TransactionImpl::rollback::TransactionImpl", "Rollback"),
        COMMIT("TransactionImpl::commit::TransactionImpl", "Commit");

        private String logLinePattern;
        private String buttonText;

        PerformedAction(String logLinePattern, String buttonText) {
            this.logLinePattern = logLinePattern;
            this.buttonText = buttonText;
        }

        /**
         * get string which is expected in log matched log line
         * @return expected string in matched log line
         */
        public String getLogLinePattern() {
            return logLinePattern;
        }

        /**
         * Get label of button which triggers action in UI
         * @return label of button
         */
        public String getButtonText() {
            return buttonText;
        }
    }

    private static final class PreparedTransactionTestCell {

        private final AtomicBoolean finishedSuccessfully = new AtomicBoolean(false);
        private final String XId;
        private final PerformedAction performedAction;

        public PreparedTransactionTestCell(String XId, PerformedAction performedAction) {
            this.XId = XId;
            this.performedAction = performedAction;
        }

        /**
         * Find out if given log line matches expected pattern compiled for this test cell
         * @param input log line
         * @return true if line matches this test cell, false otherwise
         */
        public boolean matchesLogLinePattern(String input) {
            Pattern pattern = Pattern.compile(".*" + performedAction.getLogLinePattern() + ".*" + XId + ".*");
            return pattern.matcher(input).matches();
        }

        /**
         * Retrieves {@link PerformedAction} for this test cell
         * @return {@link PerformedAction}
         */
        public PerformedAction getPerformedAction() {
            return performedAction;
        }

        /**
         * Sets true to field
         */
        public void setFinishedSuccessfully() {
            logger.debug("{} made {} as expected!", XId, performedAction.getButtonText());
            this.finishedSuccessfully.set(true);
        }

        /**
         * Return true if finished successfully, false otherwise
         * @return true if finished successfully, false otherwise
         */
        public boolean getFinishedSuccessfully() {
            return finishedSuccessfully.get();
        }

        /**
         * Gets and XID of transaction in this test cell
         * @return XID
         */
        public String getXId() {
            return XId;
        }
    }

}
