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
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class ServletContainerTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private static final String ALLOW_NON_STANDARD_WRAPPERS = "allow-non-standard-wrappers";
    private static final String DEFAULT_BUFFER_CACHE = "default-buffer-cache";
    private static final String DEFAULT_ENCODING = "default-encoding";
    private static final String DEFAULT_SESSION_TIMEOUT = "default-session-timeout";
    private static final String DIRECTORY_LISTING = "directory-listing";
    private static final String DISABLE_CACHING_FOR_SECURED_PAGES = "disable-caching-for-secured-pages";
    private static final String EAGER_FILTER_INITIALIZATION = "eager-filter-initialization";
    private static final String IGNORE_FLUSH = "ignore-flush";
    private static final String STACK_TRACE_ON_ERROR = "stack-trace-on-error";
    private static final String USE_LISTENER_ENCODING = "use-listener-encoding";

    //values
    private static final String STACK_TRACE_ON_ERROR_VALUE = "all";

    private static final String SERVLET_CONTAINER = "servlet-container_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address SERVLET_CONTAINER_ADDRESS = UNDERTOW_ADDRESS.and("servlet-container", SERVLET_CONTAINER);

    @BeforeClass
    public static void setUp() throws InterruptedException, IOException, TimeoutException {
        operations.add(SERVLET_CONTAINER_ADDRESS);
    }

    @Before
    public void before() {
        page.navigate();
        page.selectServletContainer(SERVLET_CONTAINER);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException, OperationException {
        operations.remove(SERVLET_CONTAINER_ADDRESS);
    }

    @Test
    public void setAllowNonStandardWrappersToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, ALLOW_NON_STANDARD_WRAPPERS, true);
    }

    @Test
    public void setAllowNonStandardWrappersToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, ALLOW_NON_STANDARD_WRAPPERS, false);
    }

    @Test
    public void editDefaultBufferCache() throws Exception {
        editTextAndVerify(SERVLET_CONTAINER_ADDRESS, DEFAULT_BUFFER_CACHE, undertowOps.createBufferCache());
    }

    @Test
    public void editDefaultEncoding() throws Exception {
        editTextAndVerify(SERVLET_CONTAINER_ADDRESS, DEFAULT_ENCODING);
    }

    @Test
    public void editDefaultSessionTimeout() throws Exception {
        editTextAndVerify(SERVLET_CONTAINER_ADDRESS, DEFAULT_SESSION_TIMEOUT, 42);
    }

    @Test
    public void editDefaultSessionTimeoutInvalid() throws Exception {
        verifyIfErrorAppears(DEFAULT_SESSION_TIMEOUT, "54sdfg");
    }

    @Test
    public void setDirectoryListingToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, DIRECTORY_LISTING, true);
    }

    @Test
    public void setDirectoryListingToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, DIRECTORY_LISTING, false);
    }

    @Test
    public void setDisableCachingForSecuredPagesToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, DISABLE_CACHING_FOR_SECURED_PAGES, true);
    }

    @Test
    public void setDisableCachingForSecuredPagesToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, DISABLE_CACHING_FOR_SECURED_PAGES, false);
    }

    @Test
    public void setIgnoreFlushToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, IGNORE_FLUSH, true);
    }

    @Test
    public void setIgnoreFlushToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, IGNORE_FLUSH, false);
    }

    @Test
    public void setEagerFilterInitializationToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, EAGER_FILTER_INITIALIZATION, true);
    }

    @Test
    public void setEagerFilterInitializationToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, EAGER_FILTER_INITIALIZATION, false);
    }

    @Test
    public void selectStackTraceOnError() throws Exception {
        selectOptionAndVerify(SERVLET_CONTAINER_ADDRESS, STACK_TRACE_ON_ERROR, STACK_TRACE_ON_ERROR_VALUE);
    }

    @Test
    public void setUseListenerEncodingToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, USE_LISTENER_ENCODING, true);
    }

    @Test
    public void setUseListenerEncodingToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_CONTAINER_ADDRESS, USE_LISTENER_ENCODING, false);
    }
}
