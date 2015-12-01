package org.jboss.hal.testsuite.test.configuration.undertow;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.page.config.UndertowServletPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 17.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ServletJSPSettingsTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private final String DISABLED = "disabled";
    private final String DUMP_SMAP = "dump-smap";
    private final String GENERATE_STRINGS_AS_CHAR_ARRAYS = "generate-strings-as-char-arrays";
    private final String JAVA_ENCODING = "java-encoding";
    private final String MAPPED_FILE = "mapped-file";
    private final String SCRATCH_DIR = "scratch-dir";
    private final String SMAP = "smap";
    private final String SOURCE_VM = "source-vm";
    private final String TAG_POOLING = "tag-pooling";
    private final String TARGET_VM = "target-vm";
    private final String TRIM_SPACES = "trim-spaces";
    private final String X_POWERED_BY = "x-powered-by";

    private final String DEVELOPMENT = "development";
    private final String KEEP_GENERATED = "keep-generated";
    private final String MODIFICATION_TEST_INTERVAL = "modification-test-interval";
    private final String RECOMPILE_ON_FAIL = "recompile-on-fail";
    private final String ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE = "error-on-use-bean-invalid-class-attribute";
    private final String DISPLAY_SOURCE_FRAGMENT = "display-source-fragment";
    private final String CHECK_INTERVAL = "check-interval";

    //attribute names
    private final String DISABLED_ATTR = "disabled";
    private final String DUMP_SMAP_ATTR = "dump-smap";
    private final String GENERATE_STRINGS_AS_CHAR_ARRAYS_ATTR = "generate-strings-as-char-arrays";
    private final String JAVA_ENCODING_ATTR = "java-encoding";
    private final String MAPPED_FILE_ATTR = "mapped-file";
    private final String SCRATCH_DIR_ATTR = "scratch-dir";
    private final String SMAP_ATTR = "smap";
    private final String SOURCE_VM_ATTR = "source-vm";
    private final String TAG_POOLING_ATTR = "tag-pooling";
    private final String TARGET_VM_ATTR = "target-vm";
    private final String TRIM_SPACES_ATTR = "trim-spaces";
    private final String X_POWERED_BY_ATTR = "x-powered-by";

    private final String DEVELOPMENT_ATTR = "development";
    private final String KEEP_GENERATED_ATTR = "keep-generated";
    private final String MODIFICATION_TEST_INTERVAL_ATTR = "modification-test-interval";
    private final String RECOMPILE_ON_FAIL_ATTR = "recompile-on-fail";
    private final String ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE_ATTR = "error-on-use-bean-invalid-class-attribute";
    private final String DISPLAY_SOURCE_FRAGMENT_ATTR = "display-source-fragment";
    private final String CHECK_INTERVAL_ATTR = "check-interval";

    //values
    private final String NUMERIC_VALID = "24";
    private final String NUMERIC_INVALID = "24d";

    private static String servletContainer;
    private static ResourceAddress address;

    @BeforeClass
    public static void setUp() throws InterruptedException, IOException, TimeoutException {
        servletContainer = operations.createServletContainer();
        address = servletContainerTemplate.append("/setting=jsp").resolve(context, servletContainer);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(servletContainer).switchToJSP();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException {
        operations.removeServletContainer(servletContainer);
    }

    @Test
    public void setDisabledToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DISABLED, DISABLED_ATTR, true);
    }

    @Test
    public void setDisabledToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DISABLED, DISABLED_ATTR, false);
    }

    @Test
    public void setDumpSmapToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DUMP_SMAP, DUMP_SMAP_ATTR, true);
    }

    @Test
    public void setDumpSmapToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DUMP_SMAP, DUMP_SMAP_ATTR, false);
    }

    @Test
    public void setGenerateStringsAsCharArraysToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, GENERATE_STRINGS_AS_CHAR_ARRAYS, GENERATE_STRINGS_AS_CHAR_ARRAYS_ATTR, true);
    }

    @Test
    public void setGenerateStringsAsCharArraysToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, GENERATE_STRINGS_AS_CHAR_ARRAYS, GENERATE_STRINGS_AS_CHAR_ARRAYS_ATTR, false);
    }

    @Test
    public void setJavaEncoding() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, JAVA_ENCODING, JAVA_ENCODING_ATTR);
    }

    @Test
    public void setMappedFileToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, MAPPED_FILE, MAPPED_FILE_ATTR, true);
    }

    @Test
    public void setMappedFileToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, MAPPED_FILE, MAPPED_FILE_ATTR, false);
    }

    @Test
    public void editScratchDir() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, SCRATCH_DIR, SCRATCH_DIR_ATTR);
    }

    @Test
    public void setSmapToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, SMAP, SMAP_ATTR, true);
    }

    @Test
    public void setSmapToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, SMAP, SMAP_ATTR, false);
    }

    @Test
    public void editSourceVM() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, SOURCE_VM, SOURCE_VM_ATTR);
    }

    @Test
    public void setTagPoolingToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, TAG_POOLING, TAG_POOLING_ATTR, true);
    }

    @Test
    public void setTagPoolingToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, TAG_POOLING, TAG_POOLING_ATTR, false);
    }

    @Test
    public void editTargetVM() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, TARGET_VM, TARGET_VM_ATTR);
    }

    @Test
    public void setTrimSpacesToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, TRIM_SPACES, TRIM_SPACES_ATTR, true);
    }

    @Test
    public void setTrimSpacesToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, TRIM_SPACES, TRIM_SPACES_ATTR, false);
    }

    @Test
    public void setXPoweredByToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, X_POWERED_BY, X_POWERED_BY_ATTR, true);
    }

    @Test
    public void setXPoweredByToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, X_POWERED_BY, X_POWERED_BY_ATTR, false);
    }

    @Test
    public void setDevelopmentToTrue() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, DEVELOPMENT, DEVELOPMENT_ATTR, true);
    }

    @Test
    public void setDevelopmentToFalse() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, DEVELOPMENT, DEVELOPMENT_ATTR, false);
    }

    @Test
    public void setKeepGeneratedToTrue() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, KEEP_GENERATED, KEEP_GENERATED_ATTR, true);
    }

    @Test
    public void setKeepGeneratedToFalse() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, KEEP_GENERATED, KEEP_GENERATED_ATTR, false);
    }

    @Test
    public void editModificationTestInterval() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editTextAndVerify(address, MODIFICATION_TEST_INTERVAL, MODIFICATION_TEST_INTERVAL_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editModificationTestIntervalInvalid() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        verifyIfErrorAppears(MODIFICATION_TEST_INTERVAL, NUMERIC_INVALID);
    }

    @Test
    public void setRecompileOnFailToTrue() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, RECOMPILE_ON_FAIL, RECOMPILE_ON_FAIL_ATTR, true);
    }

    @Test
    public void setRecompileOnFailToFalse() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, RECOMPILE_ON_FAIL, RECOMPILE_ON_FAIL_ATTR, false);
    }

    @Test
    public void setErrorOnUseBeanInvalidToTrue() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE, ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE_ATTR, true);
    }

    @Test
    public void setErrorOnUseBeanInvalidToFalse() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE, ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE_ATTR, false);
    }

    @Test
    public void setDisplaySourceFragmentToTrue() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, DISPLAY_SOURCE_FRAGMENT, DISPLAY_SOURCE_FRAGMENT_ATTR, true);
    }

    @Test
    public void setDisplaySourceFragmentToFalse() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editCheckboxAndVerify(address, DISPLAY_SOURCE_FRAGMENT, DISPLAY_SOURCE_FRAGMENT_ATTR, false);
    }

    @Test
    public void editCheckInterval() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        editTextAndVerify(address, CHECK_INTERVAL, CHECK_INTERVAL_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editCheckIntervalInvalid() throws IOException, InterruptedException, TimeoutException {
        page.switchToJSPDevelopment();
        verifyIfErrorAppears(CHECK_INTERVAL, NUMERIC_INVALID);
    }

}
