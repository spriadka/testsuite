package org.jboss.hal.testsuite.test.configuration.web;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.ServletPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertEquals;
import static org.jboss.hal.testsuite.cli.CliConstants.WEB_SUBSYSTEM_ADDRESS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Ignore("This was moved to a different page")
@RunWith(Arquillian.class)
@Category(Shared.class)
public class GlobalSessionTimeoutTestCase {

    private static final String DEFAULT_SESSION_TIMEOUT_CONSOLE_ID = "default-session-timeout";
    private static final String DEFAULT_SESSION_TIMEOUT_CLI_ATTR_NAME = DEFAULT_SESSION_TIMEOUT_CONSOLE_ID;
    private static final int DEFAULT_SESSION_TIMEOUT = 30;
    private static final int CHANGED_SESSION_TIMEOUT = 7;

    private String origValue = null;

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(WEB_SUBSYSTEM_ADDRESS, client);

    @Drone
    private WebDriver browser;

    @Page
    private ServletPage servletPage;

    @Before
    public void setup() throws IOException {
        browser.navigate().refresh();
        Graphene.goTo(ServletPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        origValue = client.readAttribute(CliConstants.WEB_SUBSYSTEM_ADDRESS, DEFAULT_SESSION_TIMEOUT_CONSOLE_ID);
    }

    @After
    public void cleanUp() throws IOException, TimeoutException, InterruptedException {
        if(origValue != null) {
            client.writeAttribute(CliConstants.WEB_SUBSYSTEM_ADDRESS, DEFAULT_SESSION_TIMEOUT_CONSOLE_ID, origValue);
            client.reload(false);
        }
    }

    @Test
    public void changeSessionTimeout() throws InterruptedException, TimeoutException, IOException {
        ConfigFragment globalConfig = servletPage.getConfig().global();
        Editor editor = globalConfig.edit();

        assertEquals(String.valueOf(DEFAULT_SESSION_TIMEOUT), editor.text(DEFAULT_SESSION_TIMEOUT_CONSOLE_ID));

        editor.text(DEFAULT_SESSION_TIMEOUT_CONSOLE_ID, "-20");
        assertFalse(globalConfig.save());

        editor.text(DEFAULT_SESSION_TIMEOUT_CONSOLE_ID, String.valueOf(CHANGED_SESSION_TIMEOUT));
        assertTrue(globalConfig.save());

        client.reload(false);
        verifier.verifyAttribute(DEFAULT_SESSION_TIMEOUT_CONSOLE_ID, String.valueOf(CHANGED_SESSION_TIMEOUT));
    }
}
