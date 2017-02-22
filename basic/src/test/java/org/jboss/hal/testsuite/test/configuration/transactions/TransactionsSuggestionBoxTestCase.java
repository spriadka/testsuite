package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.util.SuggestionChecker;
import org.jboss.hal.testsuite.util.SuggestionResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 8/22/16.
 *
 *  This test case covers suggestion boxes in Transactions configuration.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class TransactionsSuggestionBoxTestCase extends TransactionsTestCaseAbstract {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsSuggestionBoxTestCase.class);

    private final String PROCESS_ID_SOCKET_BINDING = "process-id-socket-binding";
    private final String RECOVERY_SOCKET_BINDING = "socket-binding";

    private static SuggestionResource socketBindingSuggestionResource;

    private SuggestionChecker processIDSuggestionChecker;
    private SuggestionChecker recoverySocketBindingSuggestionChecker;

    @BeforeClass
    public static void setUpSuggestionResources() {
        SuggestionResource.Builder suggestionResourceBuilder = new SuggestionResource.Builder()
                .withClient(client)
                .template(Address.of("socket-binding-group", "standard-sockets").and("socket-binding", "*"));

        if (client.options().isDomain) {
            suggestionResourceBuilder.template(Address.of("socket-binding-group", "ha-sockets").and("socket-binding", "*"))
                    .template(Address.of("socket-binding-group", "full-ha-sockets").and("socket-binding", "*"))
                    .template(Address.of("socket-binding-group", "full-sockets").and("socket-binding", "*"))
                    .template(Address.of("socket-binding-group", "load-balancer-sockets").and("socket-binding", "*"));
        }

        socketBindingSuggestionResource = suggestionResourceBuilder.build();
    }

    @Before
    public void before() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
        page.navigate();
    }

    private SuggestionChecker getProcessIDSuggestionChecker() {
        if (processIDSuggestionChecker == null) {
            processIDSuggestionChecker = new SuggestionChecker.Builder()
                    .browser(browser)
                    .suggestionResource(socketBindingSuggestionResource)
                    .configFragment(page.getConfigFragment())
                    .suggestBoxInputLabel(PROCESS_ID_SOCKET_BINDING)
                    .build();
        }
        return processIDSuggestionChecker;
    }

    private SuggestionChecker getRecoverySocketBindingSuggestionChecker() {
        if (recoverySocketBindingSuggestionChecker == null) {
            recoverySocketBindingSuggestionChecker = new SuggestionChecker.Builder()
                    .browser(browser)
                    .suggestionResource(socketBindingSuggestionResource)
                    .configFragment(page.getConfigFragment())
                    .suggestBoxInputLabel(RECOVERY_SOCKET_BINDING)
                    .build();
        }
        return recoverySocketBindingSuggestionChecker;
    }

    @Test
    public void processIDSocketBindingFilter_http() throws IOException {
        page.switchToProcessID();
        getProcessIDSuggestionChecker()
                .filterSuggestions("http")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void processIDSocketBindingFilter_https() throws IOException {
        page.switchToProcessID();
        getProcessIDSuggestionChecker()
                .filterSuggestions("https")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void processIDSocketBindingFilter_EMPTY() throws IOException {
        page.switchToProcessID();
        getProcessIDSuggestionChecker()
                .filterSuggestions("")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void processIDSocketBindingFilterCommonForAllOptions() throws IOException {
        page.switchToProcessID();
        getProcessIDSuggestionChecker()
                .filterSuggestions("s")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void processIDSocketBindingFilter_http_Append_STAR() throws IOException {
        page.switchToProcessID();
        getProcessIDSuggestionChecker()
                .filterSuggestions("http")
                .verifyOnlyRelevantSuggestionWereSuggested();

        getProcessIDSuggestionChecker()
                .appendSymbolToInputField('*')
                .verifyNoneSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilter_http() throws IOException {
        page.switchToRecovery();
        getRecoverySocketBindingSuggestionChecker()
                .filterSuggestions("http")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilter_https() throws IOException {
        page.switchToRecovery();
        getRecoverySocketBindingSuggestionChecker()
                .filterSuggestions("https")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilter_EMPTY() throws IOException {
        page.switchToRecovery();
        getRecoverySocketBindingSuggestionChecker()
                .filterSuggestions("")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilterCommonForAllOptions() throws IOException {
        page.switchToRecovery();
        getRecoverySocketBindingSuggestionChecker()
                .filterSuggestions("s")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilter_http_Append_STAR() throws IOException {
        page.switchToRecovery();
        getRecoverySocketBindingSuggestionChecker()
                .filterSuggestions("http")
                .verifyOnlyRelevantSuggestionWereSuggested();

        getRecoverySocketBindingSuggestionChecker()
                .appendSymbolToInputField('*')
                .verifyNoneSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilter_txn() throws IOException {
        page.switchToRecovery();
        getRecoverySocketBindingSuggestionChecker()
                .filterSuggestions("txn")
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilter_txn_RemoveSymbol() throws IOException {
        page.switchToRecovery();
        getRecoverySocketBindingSuggestionChecker()
                .filterSuggestions("txn")
                .verifyOnlyRelevantSuggestionWereSuggested();

        getRecoverySocketBindingSuggestionChecker()
                .removeSymbolFromInputField()
                .verifyOnlyRelevantSuggestionWereSuggested();
    }

    @Test
    public void recoverySocketBindingFilterSymbolBySymbol_https() throws IOException {
        page.switchToRecovery();
        final String https = "https";
        for (int i = 0; i < https.length(); i++) {
            getRecoverySocketBindingSuggestionChecker()
                    .appendSymbolToInputField(https.charAt(i))
                    .verifyOnlyRelevantSuggestionWereSuggested();
        }
    }
}
