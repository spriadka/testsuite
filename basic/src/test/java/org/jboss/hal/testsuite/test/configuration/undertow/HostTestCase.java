package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.UndertowHTTPPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class HostTestCase extends UndertowTestCaseAbstract {

    @Page
    UndertowHTTPPage page;

    private final String ALIAS = "alias";
    private final String DEFAULT_RESPONSE_CODE = "default-response-code";
    private final String DEFAULT_WEB_MODULE = "default-web-module";
    private final String DISABLE_CONSOLE_REDIRECT = "disable-console-redirect";

    private final String ALIAS_ATTR = "alias";
    private final String DEFAULT_RESPONSE_CODE_ATTR = "default-response-code";
    private final String DEFAULT_WEB_MODULE_ATTR = "default-web-module";
    private final String DISABLE_CONSOLE_REDIRECT_ATTR = "disable-console-redirect";

    //values
    private final String[] ALIAS_VALUES = new String[]{"localhost", "test", "example"};
    private final String DEFAULT_RESPONSE_CODE_VALUE = "500";

    private static AddressTemplate hostTemplate = httpServerTemplate.append("/host=*");
    private static String httpServer;
    private static String httpServerHost;
    private static String httpServerHostToBeRemoved;
    private static ResourceAddress address;

    @BeforeClass
    public static void setUp() {
        httpServer = operations.createHTTPServer();
        httpServerHost = operations.createHTTPServerHost(httpServer);
        httpServerHostToBeRemoved = operations.createHTTPServerHost(httpServer);
        address = hostTemplate.resolve(context, httpServer, httpServerHost);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewHTTPServer(httpServer).switchToHosts().selectItemInTableByText(httpServerHost);
    }

    @AfterClass
    public static void tearDown() {
        operations.removeHTTPServerHostIfExists(httpServer, httpServerHost);
        operations.removeHTTPServer(httpServer);
    }

    @Test
    public void editAliases() throws IOException, InterruptedException {
        editTextAreaAndVerify(address, ALIAS, ALIAS_ATTR, ALIAS_VALUES);
    }

    @Test
    public void editDefaultResponseCode() throws IOException, InterruptedException {
        editTextAndVerify(address, DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_ATTR, DEFAULT_RESPONSE_CODE_VALUE);
    }

    @Test
    public void editDefaultWebModule() throws IOException, InterruptedException {
        editTextAndVerify(address, DEFAULT_WEB_MODULE, DEFAULT_WEB_MODULE_ATTR);
    }

    @Test
    public void setDisableConsoleRedirectToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, DISABLE_CONSOLE_REDIRECT, DISABLE_CONSOLE_REDIRECT_ATTR, true);
    }

    @Test
    public void setDisableConsoleRedirectToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, DISABLE_CONSOLE_REDIRECT, DISABLE_CONSOLE_REDIRECT_ATTR, false);
    }

    @Test
    public void addHTTPServerHostInGUI() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        String webModule = RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text(ALIAS, String.join("\n", ALIAS_VALUES));
        editor.text(DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_VALUE);
        editor.text(DEFAULT_WEB_MODULE, webModule);
        editor.checkbox(DISABLE_CONSOLE_REDIRECT, true);
        boolean result = wizard.finish();

        Assert.assertTrue("Window should be closed", result);
        Assert.assertTrue("HTTP server host should be present in table", config.resourceIsPresent(name));
        ResourceAddress address = hostTemplate.resolve(context, httpServer, name);
        verifier.verifyResource(address, true);
        verifier.verifyAttribute(address, ALIAS_ATTR, ALIAS_VALUES);
        verifier.verifyAttribute(address, DEFAULT_RESPONSE_CODE_ATTR, DEFAULT_RESPONSE_CODE_VALUE);
        verifier.verifyAttribute(address, DEFAULT_WEB_MODULE_ATTR, webModule);
        verifier.verifyAttribute(address, DISABLE_CONSOLE_REDIRECT, true);
    }

    @Test
    public void removeHTTPServerHostInGUI() {
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager()
                .removeResource(httpServerHostToBeRemoved)
                .confirm();

        ResourceAddress address = hostTemplate.resolve(context, httpServer, httpServerHostToBeRemoved);
        Assert.assertFalse("Host server host should not be present in table", config.resourceIsPresent(httpServerHostToBeRemoved));
        verifier.verifyResource(address, false); //HTTP server host should not be present on the server
    }
}
