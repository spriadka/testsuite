package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
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
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class HostTestCase extends UndertowTestCaseAbstract {

    @Page
    UndertowHTTPPage page;

    private static final String ALIAS = "alias";
    private static final String DEFAULT_RESPONSE_CODE = "default-response-code";
    private static final String DEFAULT_WEB_MODULE = "default-web-module";
    private static final String DISABLE_CONSOLE_REDIRECT = "disable-console-redirect";

    //values
    private static final String[] ALIAS_VALUES = new String[]{"localhost", "test", "example"};
    private static final int DEFAULT_RESPONSE_CODE_VALUE = 500;

    private static final String HTTP_SERVER = "undertow-http-server-host_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String HOST = "host_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String HOST_TBR = "host-btr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String HOST_TBA = "host-tba_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address HTTP_SERVER_ADDRESS = UNDERTOW_ADDRESS.and("server", HTTP_SERVER);

    private static final Address HOST_ADDRESS = HTTP_SERVER_ADDRESS.and("host", HOST);
    private static final Address HOST_TBR_ADDRESS = HTTP_SERVER_ADDRESS.and("host", HOST_TBR);
    private static final Address HOST_TBA_ADDRESS = HTTP_SERVER_ADDRESS.and("host", HOST_TBA);


    @BeforeClass
    public static void setUp() throws InterruptedException, TimeoutException, IOException {
        final String defaultWebModule = "default-web-module";
        operations.add(HTTP_SERVER_ADDRESS).assertSuccess();
        operations.add(HOST_ADDRESS, Values.of(defaultWebModule, "defaultWebModule-" + RandomStringUtils
                .randomAlphanumeric(6))).assertSuccess();
        operations.add(HOST_TBR_ADDRESS, Values.of(defaultWebModule, "defaultWebModule-tbr-" + RandomStringUtils
                .randomAlphanumeric(6))).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.viewHTTPServer(HTTP_SERVER)
                .switchToHosts()
                .selectItemInTableByText(HOST);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException, OperationException {
        operations.removeIfExists(HOST_ADDRESS);
        operations.removeIfExists(HOST_TBA_ADDRESS);
        operations.removeIfExists(HOST_TBR_ADDRESS);
        operations.removeIfExists(HTTP_SERVER_ADDRESS);
    }

    @Test
    public void editAliases() throws Exception {
        editTextAreaAndVerify(HOST_ADDRESS, ALIAS, ALIAS_VALUES);
    }

    @Test
    public void editDefaultResponseCode() throws Exception {
        editTextAndVerify(HOST_ADDRESS, DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_VALUE);
    }

    @Test
    public void editDefaultWebModule() throws Exception {
        editTextAndVerify(HOST_ADDRESS, DEFAULT_WEB_MODULE);
    }

    @Test
    public void setDisableConsoleRedirectToTrue() throws Exception {
        editCheckboxAndVerify(HOST_ADDRESS, DISABLE_CONSOLE_REDIRECT, true);
    }

    @Test
    public void setDisableConsoleRedirectToFalse() throws Exception {
        editCheckboxAndVerify(HOST_ADDRESS, DISABLE_CONSOLE_REDIRECT, false);
    }

    @Test
    public void addHTTPServerHostInGUI() throws Exception {
        String webModule = "webModule_" + RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", HOST_TBA);
        editor.text(ALIAS, String.join("\n", ALIAS_VALUES));
        editor.text(DEFAULT_RESPONSE_CODE, String.valueOf(DEFAULT_RESPONSE_CODE_VALUE));
        editor.text(DEFAULT_WEB_MODULE, webModule);
        editor.checkbox(DISABLE_CONSOLE_REDIRECT, true);
        boolean result = wizard.finish();

        Assert.assertTrue("Window should be closed", result);
        Assert.assertTrue("HTTP server host should be present in table", config.resourceIsPresent(HOST_TBA));
        ResourceVerifier verifier = new ResourceVerifier(HOST_TBA_ADDRESS, client);
        verifier.verifyExists();
        verifier.verifyAttribute(DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_VALUE);
        verifier.verifyAttribute(DEFAULT_WEB_MODULE, webModule);
        verifier.verifyAttribute(DISABLE_CONSOLE_REDIRECT, true);
    }

    @Test
    public void removeHTTPServerHostInGUI() throws Exception {
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager()
                .removeResource(HOST_TBR)
                .confirm();

        Assert.assertFalse("Host server host should not be present in table", config.resourceIsPresent(HOST_TBR));
        new ResourceVerifier(HOST_TBR_ADDRESS, client).verifyDoesNotExist(); //HTTP server host should not be present on the server
    }
}
