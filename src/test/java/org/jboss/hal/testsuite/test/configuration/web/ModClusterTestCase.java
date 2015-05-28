package org.jboss.hal.testsuite.test.configuration.web;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.page.config.ModClusterPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.test.util.ConfigAreaUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Before;
import org.junit.Ignore;
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

    private static final String SESSIONS = "Sessions";
    private static final String WEB_CONTEXTS = "Web Contexts";
    private static final String PROXIES = "Proxies";
    private static final String NETWORKING = "Networking";
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

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(MOD_CLUSTER_CONFIG_ADDRESS, client);
    private ConfigAreaUtils utils = new ConfigAreaUtils(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public ModClusterPage page;

    @Before
    public void before() {
        browser.navigate().refresh();
        Graphene.goTo(ModClusterPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Console.withBrowser(browser).maximizeWindow();
    }

    /*
     * ADVERTISING
     */
    @Test
    public void loadBalancingGroup() {
        utils.editTextAndAssert(page, LOAD_BALANCING_GROUP_ID, LOAD_BALANCING_GROUP_VALUE).invoke();
    }

    @Test
    public void balancer() {
        utils.editTextAndAssert(page, BALANCER_ID, BALANCER_VALUE).invoke();
    }

    @Test
    public void advertiseSocket() {
        utils.editTextAndAssert(page, ADVERTISE_SOCKET_ID, ADVERTISE_SOCKET_VALUE).invoke();
    }

    @Test
    public void advertiseKey() {
        utils.editTextAndAssert(page, ADVERTISE_KEY_ID, ADVERTISE_KEY_VALUE).dmrAttribute("advertise-security-key").invoke();
    }

    @Test
    public void advertise() {
        utils.editCheckboxAndAssert(page, ADVERTISE_ID, false).invoke();
        utils.editCheckboxAndAssert(page, ADVERTISE_ID, true).invoke();
    }

    /*
     * SESSIONS
     */
    @Test
    public void stickySession() {
        utils.editCheckboxAndAssert(page, STICKY_SESSION_ID, false).tab(SESSIONS).invoke();
        utils.editCheckboxAndAssert(page, STICKY_SESSION_ID, true).tab(SESSIONS).invoke();
    }

    @Test
    public void stickySessionRemove() {
        utils.editCheckboxAndAssert(page, STICKY_SESSION_REMOVE_ID, false).tab(SESSIONS).invoke();
        utils.editCheckboxAndAssert(page, STICKY_SESSION_REMOVE_ID, true).tab(SESSIONS).invoke();
    }

    @Test
    public void stickySessionForce() {
        utils.editCheckboxAndAssert(page, STICKY_SESSION_FORCE_ID, false).tab(SESSIONS).invoke();
        utils.editCheckboxAndAssert(page, STICKY_SESSION_FORCE_ID, true).tab(SESSIONS).invoke();
    }

    /*
     * WEB CONTEXTS
     */
    @Test
    public void autoEnableContexts() {
        utils.editCheckboxAndAssert(page, AUTO_ENABLE_CONTEXTS_ID, false).tab(WEB_CONTEXTS).invoke();
        utils.editCheckboxAndAssert(page, AUTO_ENABLE_CONTEXTS_ID, true).tab(WEB_CONTEXTS).invoke();
    }

    @Test
    public void excludedContexts() {
        utils.editTextAndAssert(page, EXCLUDED_CONTEXTS_ID, EXCLUDED_CONTEXTS_VALUE).tab(WEB_CONTEXTS).invoke();
    }

    /*
     * PROXIES
     */
    @Test
    public void proxyUrl() {
        utils.editTextAndAssert(page, PROXY_URL_ID, PROXY_URL_VALUE).tab(PROXIES).invoke();
    }

    @Test
    @Ignore
    public void proxyList() {
        utils.editTextAndAssert(page, PROXY_LIST_ID, PROXY_LIST_VALUE).tab(PROXIES).invoke();
    }

    /*
     * NETWORKING
     */
    @Test
    public void nodeTimeout() {
        utils.editTextAndAssert(page, NODE_TIMEOUT_ID, NODE_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void nodeTimeoutNegative() {
        utils.editTextAndAssert(page, NODE_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void nodeTimeoutInvalid() {
        utils.editTextAndAssert(page, NODE_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void socketTimeout() {
        utils.editTextAndAssert(page, SOCKET_TIMEOUT_ID, SOCKET_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void socketTimeoutNegative() {
        utils.editTextAndAssert(page, SOCKET_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void socketTimeoutInvalid() {
        utils.editTextAndAssert(page, SOCKET_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void stopContextTimeout() {
        utils.editTextAndAssert(page, STOP_CONTEXT_TIMEOUT_ID, STOP_CONTEXT_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void stopContextTimeoutNegative() {
        utils.editTextAndAssert(page, STOP_CONTEXT_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void stopContextTimeoutInvalid() {
        utils.editTextAndAssert(page, STOP_CONTEXT_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void maxAttempts() {
        utils.editTextAndAssert(page, MAX_ATTEMPTS_ID, MAX_ATTEMPTS_VALUE).tab(NETWORKING).dmrAttribute("max-attempts").invoke();
    }

    @Test
    public void maxAttemptsNegative() {
        utils.editTextAndAssert(page, MAX_ATTEMPTS_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void maxAttemptsInvalid() {
        utils.editTextAndAssert(page, MAX_ATTEMPTS_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void flushPackets() {
        utils.editCheckboxAndAssert(page, FLUSH_PACKETS_ID, false).tab(NETWORKING).invoke();
        utils.editCheckboxAndAssert(page, FLUSH_PACKETS_ID, true).tab(NETWORKING).invoke();
    }

    @Test
    public void flushWait() {
        utils.editTextAndAssert(page, FLUSH_WAIT_ID, FLUSH_WAIT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void flushWaitNegative() {
        utils.editTextAndAssert(page, FLUSH_WAIT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void flushWaitInvalid() {
        utils.editTextAndAssert(page, FLUSH_WAIT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void ping() {
        utils.editTextAndAssert(page, PING_ID, PING_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void pingNegative() {
        utils.editTextAndAssert(page, PING_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void pingInvalid() {
        utils.editTextAndAssert(page, PING_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void ttl() {
        utils.editTextAndAssert(page, TTL_ID, TTL_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void ttlNegative() {
        utils.editTextAndAssert(page, TTL_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void ttlInvalid() {
        utils.editTextAndAssert(page, TTL_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void workerTimeout() {
        utils.editTextAndAssert(page, WORKER_TIMEOUT_ID, WORKER_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void workerTimeoutNegative() {
        utils.editTextAndAssert(page, WORKER_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void workerTimeoutInvalid() {
        utils.editTextAndAssert(page, WORKER_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }
}
