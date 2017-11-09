package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.config.UndertowServletPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ServletJSPSettingsTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private static final String DISABLED = "disabled";
    private static final String DUMP_SMAP = "dump-smap";
    private static final String GENERATE_STRINGS_AS_CHAR_ARRAYS = "generate-strings-as-char-arrays";
    private static final String JAVA_ENCODING = "java-encoding";
    private static final String MAPPED_FILE = "mapped-file";
    private static final String SCRATCH_DIR = "scratch-dir";
    private static final String SMAP_ATTR = "smap";
    private static final String SMAP = "smap";
    private static final String SOURCE_VM = "source-vm";
    private static final String TAG_POOLING = "tag-pooling";
    private static final String TARGET_VM = "target-vm";
    private static final String TRIM_SPACES = "trim-spaces";
    private static final String X_POWERED_BY = "x-powered-by";

    private static final String DEVELOPMENT = "development";
    private static final String KEEP_GENERATED = "keep-generated";
    private static final String MODIFICATION_TEST_INTERVAL = "modification-test-interval";
    private static final String RECOMPILE_ON_FAIL = "recompile-on-fail";
    private static final String ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE = "error-on-use-bean-invalid-class-attribute";
    private static final String DISPLAY_SOURCE_FRAGMENT = "display-source-fragment";
    private static final String CHECK_INTERVAL = "check-interval";

    private static final String SERVLET_CONTAINER = "servlet-container_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address SERVLET_CONTAINER_ADDRESS = UNDERTOW_ADDRESS.and("servlet-container", SERVLET_CONTAINER);
    private static final Address SERVLET_JSP_ADDRESS = SERVLET_CONTAINER_ADDRESS.and("setting", "jsp");

    @BeforeClass
    public static void setUp() throws InterruptedException, IOException, TimeoutException {
        operations.add(SERVLET_CONTAINER_ADDRESS);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(SERVLET_CONTAINER).switchToJSP();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException {
        operations.remove(SERVLET_CONTAINER_ADDRESS);
        administration.restartIfRequired();
        administration.reloadIfRequired();
    }

    @Test
    public void setDisabledToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DISABLED, true);
    }

    @Test
    public void setDisabledToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DISABLED, false);
    }

    @Test
    public void setDumpSmapToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DUMP_SMAP, true);
    }

    @Test
    public void setDumpSmapToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DUMP_SMAP, false);
    }

    @Test
    public void setGenerateStringsAsCharArraysToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, GENERATE_STRINGS_AS_CHAR_ARRAYS, true);
    }

    @Test
    public void setGenerateStringsAsCharArraysToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, GENERATE_STRINGS_AS_CHAR_ARRAYS, false);
    }

    @Test
    public void setJavaEncoding() throws Exception {
        editTextAndVerify(SERVLET_JSP_ADDRESS, JAVA_ENCODING);
    }

    @Test
    public void setMappedFileToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, MAPPED_FILE, true);
    }

    @Test
    public void setMappedFileToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, MAPPED_FILE, false);
    }

    @Test
    public void editScratchDir() throws Exception {
        editTextAndVerify(SERVLET_JSP_ADDRESS, SCRATCH_DIR);
    }

    @Test
    public void setSmapToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, SMAP, SMAP_ATTR, true);
    }

    @Test
    public void setSmapToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, SMAP, SMAP_ATTR, false);
    }

    @Test
    public void editSourceVM() throws Exception {
        editTextAndVerify(SERVLET_JSP_ADDRESS, SOURCE_VM);
    }

    @Test
    public void setTagPoolingToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, TAG_POOLING, true);
    }

    @Test
    public void setTagPoolingToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, TAG_POOLING, false);
    }

    @Test
    public void editTargetVM() throws Exception {
        editTextAndVerify(SERVLET_JSP_ADDRESS, TARGET_VM);
    }

    @Test
    public void setTrimSpacesToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, TRIM_SPACES, true);
    }

    @Test
    public void setTrimSpacesToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, TRIM_SPACES, false);
    }

    @Test
    public void setXPoweredByToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, X_POWERED_BY, true);
    }

    @Test
    public void setXPoweredByToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, X_POWERED_BY, false);
    }

    @Test
    public void setDevelopmentToTrue() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DEVELOPMENT, true);
    }

    @Test
    public void setDevelopmentToFalse() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DEVELOPMENT, false);
    }

    @Test
    public void setKeepGeneratedToTrue() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, KEEP_GENERATED, true);
    }

    @Test
    public void setKeepGeneratedToFalse() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, KEEP_GENERATED, false);
    }

    @Test
    public void editModificationTestInterval() throws Exception {
        page.switchToJSPDevelopment();
        editTextAndVerify(SERVLET_JSP_ADDRESS, MODIFICATION_TEST_INTERVAL, NUMERIC_VALID);
    }

    @Test
    public void editModificationTestIntervalInvalid() throws Exception {
        page.switchToJSPDevelopment();
        verifyIfErrorAppears(MODIFICATION_TEST_INTERVAL, NUMERIC_INVALID);
    }

    @Test
    public void setRecompileOnFailToTrue() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, RECOMPILE_ON_FAIL, true);
    }

    @Test
    public void setRecompileOnFailToFalse() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, RECOMPILE_ON_FAIL, false);
    }

    @Test
    public void setErrorOnUseBeanInvalidToTrue() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE, true);
    }

    @Test
    public void setErrorOnUseBeanInvalidToFalse() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE, false);
    }

    @Test
    public void setDisplaySourceFragmentToTrue() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DISPLAY_SOURCE_FRAGMENT, true);
    }

    @Test
    public void setDisplaySourceFragmentToFalse() throws Exception {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(SERVLET_JSP_ADDRESS, DISPLAY_SOURCE_FRAGMENT, false);
    }

    @Test
    public void editCheckInterval() throws Exception {
        page.switchToJSPDevelopment();
        editTextAndVerify(SERVLET_JSP_ADDRESS, CHECK_INTERVAL, NUMERIC_VALID);
    }

    @Test
    public void editCheckIntervalInvalid() throws Exception {
        page.switchToJSPDevelopment();
        verifyIfErrorAppears(CHECK_INTERVAL, NUMERIC_INVALID);
    }

}
