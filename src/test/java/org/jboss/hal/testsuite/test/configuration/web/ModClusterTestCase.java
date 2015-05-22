package org.jboss.hal.testsuite.test.configuration.web;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.ModClusterPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.MOD_CLUSTER_CONFIG_ADDRESS;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ModClusterTestCase {

    private static final String LOAD_BALANCING_GROUP_ID = "loadBalancingGroup";
    private static final String BALANCER_ID = "balancer";
    private static final String ADVERTISE_SOCKET_ID = "advertiseSocket";
    private static final String ADVERTISE_KEY_ID = "advertiseKey";
    private static final String ADVERTISE_ID = "advertise";
    private static final String STICKY_SESSION_ID = "stickySession";
    private static final String STICKY_SESSION_FORCE_ID = "stickySessionForce";
    private static final String STICKY_SESSION_REMOVE_ID = "stickySessionRemove";
    private static final String AUTO_ENABLE_CONTEXTS_ID = "autoEnableContexts";
    private static final String EXCLUDED_CONTEXTS_ID = "excludedContexts";
    private static final String PROXY_URL_ID = "proxyUrl";
    private static final String PROXY_LIST_ID = "proxyList";
    private static final String NODE_TIMEOUT_ID = "nodeTimeout";
    private static final String SOCKET_TIMEOUT_ID = "socketTimeout";
    private static final String STOP_CONTEXT_TIMEOUT_ID = "stopContextTimeout";
    private static final String MAX_ATTEMPTS_ID = "maxAttemps";
    private static final String FLUSH_PACKETS_ID = "flushPackets";
    private static final String FLUSH_WAIT_ID = "flushWait";
    private static final String PING_ID = "ping";
    private static final String TTL_ID = "ttl";
    private static final String WORKER_TIMEOUT_ID = "workerTimeout";

    private static final String EXCLUDED_CONTEXTS_VALUE = "ROOT";
    private static final String LOAD_BALANCING_GROUP_VALUE = "lbg_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BALANCER_VALUE = "b_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADVERTISE_SOCKET_VALUE = "lbg_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADVERTISE_KEY_VALUE = "lbg_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROXY_URL_VALUE = "/dd";
    private static final String PROXY_LIST_VALUE = "dd";
    private static final String NODE_TIMEOUT_VALUE = "50";
    private static final String SOCKET_TIMEOUT_VALUE = "52";
    private static final String STOP_CONTEXT_TIMEOUT_VALUE = "51";
    private static final String MAX_ATTEMPTS_VALUE = "58";
    private static final String FLUSH_WAIT_VALUE = "44";
    private static final String PING_VALUE = "46";
    private static final String TTL_VALUE = "42";
    private static final String WORKER_TIMEOUT_VALUE = "41";
    private static final String NUMERIC_VALUE_NEGATIVE = "-50";
    private static final String NUMERIC_VALUE_INVALID = "50" + RandomStringUtils.randomAlphabetic(3);


    private CliClient client = new CliClient();
    private ResourceVerifier verifier = new ResourceVerifier(MOD_CLUSTER_CONFIG_ADDRESS, client);

    @Drone
    public WebDriver browser;

    @Page
    public ModClusterPage page;

    @Before
    public void before(){
        browser.navigate().refresh();
        Graphene.goTo(ModClusterPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        browser.manage().window().maximize();
    }


    /*
     * ADVERTISING
     */
    @Test
    public void loadBalancingGroup(){
        changeTextAndAssert(page.getConfig().advertising(), LOAD_BALANCING_GROUP_ID, LOAD_BALANCING_GROUP_VALUE, true);
    }

    @Test
    public void balancer(){
        changeTextAndAssert(page.getConfig().advertising(), BALANCER_ID, BALANCER_VALUE, true);
    }

    @Test
    public void advertiseSocket(){
        changeTextAndAssert(page.getConfig().advertising(), ADVERTISE_SOCKET_ID, ADVERTISE_SOCKET_VALUE, true);
    }

    @Test
    public void advertiseKey(){
        changeTextAndAssert(page.getConfig().advertising(), ADVERTISE_KEY_ID, ADVERTISE_KEY_VALUE, true, "advertise-security-key");
    }

    @Test
    public void advertise(){
        changeCheckboxAndAssert(page.getConfig().advertising(), ADVERTISE_ID, false, true);

        changeCheckboxAndAssert(page.getConfig().advertising(), ADVERTISE_ID, true, true);
    }

    /*
     * SESSIONS
     */
    @Test
    public void stickySession(){
        changeCheckboxAndAssert(page.getConfig().sessions(), STICKY_SESSION_ID, false, true);
        changeCheckboxAndAssert(page.getConfig().sessions(), STICKY_SESSION_ID, true, true);
    }

    @Test
    public void stickySessionRemove(){
        changeCheckboxAndAssert(page.getConfig().sessions(), STICKY_SESSION_REMOVE_ID, false, true);
        changeCheckboxAndAssert(page.getConfig().sessions(), STICKY_SESSION_REMOVE_ID, true, true);
    }

    @Test
    public void stickySessionForce(){
        changeCheckboxAndAssert(page.getConfig().sessions(), STICKY_SESSION_FORCE_ID, false, true);
        changeCheckboxAndAssert(page.getConfig().sessions(), STICKY_SESSION_FORCE_ID, true, true);
    }

    /*
     * WEB CONTEXTS
     */
    @Test
    public void autoEnableContexts(){
        changeCheckboxAndAssert(page.getConfig().webContexts(), AUTO_ENABLE_CONTEXTS_ID, false, true);
        changeCheckboxAndAssert(page.getConfig().webContexts(), AUTO_ENABLE_CONTEXTS_ID, true, true);
    }

    @Test
    public void excludedContexts(){
        changeTextAndAssert(page.getConfig().webContexts(), EXCLUDED_CONTEXTS_ID, EXCLUDED_CONTEXTS_VALUE, true);
    }

    /*
     * PROXIES
     */
    @Test
    public void proxyUrl(){
        changeTextAndAssert(page.getConfig().proxies(), PROXY_URL_ID, PROXY_URL_VALUE, true);
    }

    @Test
    public void proxyList(){
        changeTextAndAssert(page.getConfig().proxies(), PROXY_LIST_ID, PROXY_LIST_VALUE, true);
    }

    /*
     * NETWORKING
     */
    @Test
    public void nodeTimeout(){
        changeTextAndAssert(page.getConfig().networking(), NODE_TIMEOUT_ID, NODE_TIMEOUT_VALUE, true);
    }

    @Test
    public void nodeTimeoutNegative(){
        changeTextAndAssert(page.getConfig().networking(), NODE_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void nodeTimeoutInvalid(){
        changeTextAndAssert(page.getConfig().networking(), NODE_TIMEOUT_ID, NUMERIC_VALUE_INVALID, false);
    }

    @Test
    public void socketTimeout(){
        changeTextAndAssert(page.getConfig().networking(), SOCKET_TIMEOUT_ID, SOCKET_TIMEOUT_VALUE, true);
    }

    @Test
    public void socketTimeoutNegative(){
        changeTextAndAssert(page.getConfig().networking(), SOCKET_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void socketTimeoutInvalid(){
        changeTextAndAssert(page.getConfig().networking(), SOCKET_TIMEOUT_ID, NUMERIC_VALUE_INVALID, false);
    }

    @Test
    public void stopContextTimeout(){
        changeTextAndAssert(page.getConfig().networking(), STOP_CONTEXT_TIMEOUT_ID, STOP_CONTEXT_TIMEOUT_VALUE, true);
    }

    @Test
    public void stopContextTimeoutNegative(){
        changeTextAndAssert(page.getConfig().networking(), STOP_CONTEXT_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void stopContextTimeoutInvalid(){
        changeTextAndAssert(page.getConfig().networking(), STOP_CONTEXT_TIMEOUT_ID, NUMERIC_VALUE_INVALID, false);
    }

    @Test
    public void maxAttempts(){
        changeTextAndAssert(page.getConfig().networking(), MAX_ATTEMPTS_ID, MAX_ATTEMPTS_VALUE, true, "max-attempts");
    }

    @Test
    public void maxAttemptsNegative(){
        changeTextAndAssert(page.getConfig().networking(), MAX_ATTEMPTS_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void maxAttemptsInvalid(){
        changeTextAndAssert(page.getConfig().networking(), MAX_ATTEMPTS_ID, NUMERIC_VALUE_INVALID, false);
    }

    @Test
    public void flushPackets(){
        changeCheckboxAndAssert(page.getConfig().networking(), FLUSH_PACKETS_ID, false, true);
        changeCheckboxAndAssert(page.getConfig().networking(), FLUSH_PACKETS_ID, true, true);
    }

    @Test
    public void flushWait(){
        changeTextAndAssert(page.getConfig().networking(), FLUSH_WAIT_ID, FLUSH_WAIT_VALUE, true);
    }

    @Test
    public void flushWaitNegative(){
        changeTextAndAssert(page.getConfig().networking(), FLUSH_WAIT_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void flushWaitInvalid(){
        changeTextAndAssert(page.getConfig().networking(), FLUSH_WAIT_ID, NUMERIC_VALUE_INVALID, false);
    }

    @Test
    public void ping(){
        changeTextAndAssert(page.getConfig().networking(), PING_ID, PING_VALUE, true);
    }

    @Test
    public void pingNegative(){
        changeTextAndAssert(page.getConfig().networking(), PING_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void pingInvalid(){
        changeTextAndAssert(page.getConfig().networking(), PING_ID, NUMERIC_VALUE_INVALID, false);
    }

    @Test
    public void ttl(){
        changeTextAndAssert(page.getConfig().networking(), TTL_ID, TTL_VALUE, true);
    }

    @Test
    public void ttlNegative(){
        changeTextAndAssert(page.getConfig().networking(), TTL_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void ttlInvalid(){
        changeTextAndAssert(page.getConfig().networking(), TTL_ID, NUMERIC_VALUE_INVALID, false);
    }

    @Test
    public void workerTimeout(){
        changeTextAndAssert(page.getConfig().networking(), WORKER_TIMEOUT_ID, WORKER_TIMEOUT_VALUE, true);
    }

    @Test
    public void workerTimeoutNegative(){
        changeTextAndAssert(page.getConfig().networking(), WORKER_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE, false);
    }

    @Test
    public void workerTimeoutInvalid(){
        changeTextAndAssert(page.getConfig().networking(), WORKER_TIMEOUT_ID, NUMERIC_VALUE_INVALID, false);
    }

    private void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expected) {
        changeTextAndAssert(fragment, identifier, value, expected, identifier);
    }

    private void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expected) {
        changeCheckboxAndAssert(fragment, identifier, value, expected, identifier);
    }

    private void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expected, String dmrAttribute) {
        fragment.edit().text(identifier, value);
        fragment.saveAndAssert(expected);
        if (expected != false) {
            verifier.verifyAttribute(dmrAttribute, value);
        }
    }

    private void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expected, String dmrAttribute) {
        fragment.edit().checkbox(identifier, value);
        fragment.saveAndAssert(expected);
        if (expected != false) {
            verifier.verifyAttribute(dmrAttribute, String.valueOf(value));
        }
    }
}
