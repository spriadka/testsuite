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
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 17.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ServletContainerTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private final String ALLOW_NON_STANDARD_WRAPPERS = "allow-non-standard-wrappers";
    private final String DEFAULT_BUFFER_CACHE = "default-buffer-cache";
    private final String DEFAULT_ENCODING = "default-encoding";
    private final String DEFAULT_SESSION_TIMEOUT = "default-session-timeout";
    private final String DIRECTORY_LISTING = "directory-listing";
    private final String DISABLE_CACHING_FOR_SECURED_PAGES = "disable-caching-for-secured-pages";
    private final String EAGER_FILTER_INITIALIZATION = "eager-filter-initialization";
    private final String IGNORE_FLUSH = "ignore-flush";
    private final String STACK_TRACE_ON_ERROR = "stack-trace-on-error";
    private final String USE_LISTENER_ENCODING = "use-listener-encoding";

    //attribute names
    private final String ALLOW_NON_STANDARD_WRAPPERS_ATTR = "allow-non-standard-wrappers";
    private final String DEFAULT_BUFFER_CACHE_ATTR = "default-buffer-cache";
    private final String DEFAULT_ENCODING_ATTR = "default-encoding";
    private final String DEFAULT_SESSION_TIMEOUT_ATTR = "default-session-timeout";
    private final String DIRECTORY_LISTING_ATTR = "directory-listing";
    private final String DISABLE_CACHING_FOR_SECURED_PAGES_ATTR = "disable-caching-for-secured-pages";
    private final String EAGER_FILTER_INITIALIZATION_ATTR = "eager-filter-initialization";
    private final String IGNORE_FLUSH_ATTR = "ignore-flush";
    private final String STACK_TRACE_ON_ERROR_ATTR = "stack-trace-on-error";
    private final String USE_LISTENER_ENCODING_ATTR = "use-listener-encoding";

    //values
    private final String STACK_TRACE_ON_ERROR_VALUE = "all";
    protected static String BUFFER_CACHE_VALUE_VALID;

    private static String servletContainer;
    private static ResourceAddress address;

    @BeforeClass
    public static void setUp() throws InterruptedException, IOException, TimeoutException {
        servletContainer = operations.createServletContainer();
        address = servletContainerTemplate.resolve(context, servletContainer);
        BUFFER_CACHE_VALUE_VALID = operations.addBufferCache();
    }

    @Before
    public void before() {
        page.navigate();
        page.selectServletContainer(servletContainer);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException, OperationException {
        operations.removeServletContainer(servletContainer);
        operations.removeBufferCache(BUFFER_CACHE_VALUE_VALID);
    }

    @Test
    public void setAllowNonStandardWrappersToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALLOW_NON_STANDARD_WRAPPERS, ALLOW_NON_STANDARD_WRAPPERS_ATTR, true);
    }

    @Test
    public void setAllowNonStandardWrappersToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALLOW_NON_STANDARD_WRAPPERS, ALLOW_NON_STANDARD_WRAPPERS_ATTR, false);
    }

    @Test
    public void editDefaultBufferCache() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, DEFAULT_BUFFER_CACHE, DEFAULT_BUFFER_CACHE_ATTR, BUFFER_CACHE_VALUE_VALID);
    }

    @Test
    public void editDefaultEncoding() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, DEFAULT_ENCODING, DEFAULT_ENCODING_ATTR);
    }

    @Test
    public void editDefaultSessionTimeout() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, DEFAULT_SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT_ATTR, "42");
    }

    @Test
    public void editDefaultSessionTimeoutInvalid() throws IOException, InterruptedException, TimeoutException {
        verifyIfErrorAppears(DEFAULT_SESSION_TIMEOUT, "54sdfg");
    }

    @Test
    public void setDirectoryListingToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DIRECTORY_LISTING, DIRECTORY_LISTING_ATTR, true);
    }

    @Test
    public void setDirectoryListingToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DIRECTORY_LISTING, DIRECTORY_LISTING_ATTR, false);
    }

    @Test
    public void setDisableCachingForSecuredPagesToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DISABLE_CACHING_FOR_SECURED_PAGES, DISABLE_CACHING_FOR_SECURED_PAGES_ATTR, true);
    }

    @Test
    public void setDisableCachingForSecuredPagesToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DISABLE_CACHING_FOR_SECURED_PAGES, DISABLE_CACHING_FOR_SECURED_PAGES_ATTR, false);
    }

    @Test
    public void setIgnoreFlushToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, IGNORE_FLUSH, IGNORE_FLUSH_ATTR, true);
    }

    @Test
    public void setIgnoreFlushToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, IGNORE_FLUSH, IGNORE_FLUSH_ATTR, false);
    }

    @Test
    public void setEagerFilterInitializationToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, EAGER_FILTER_INITIALIZATION, EAGER_FILTER_INITIALIZATION_ATTR, true);
    }

    @Test
    public void setEagerFilterInitializationToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, EAGER_FILTER_INITIALIZATION, EAGER_FILTER_INITIALIZATION_ATTR, false);
    }

    @Test
    public void selectStackTraceOnError() throws IOException, InterruptedException, TimeoutException {
        selectOptionAndVerify(address, STACK_TRACE_ON_ERROR, STACK_TRACE_ON_ERROR_ATTR, STACK_TRACE_ON_ERROR_VALUE);
    }

    @Test
    public void setUseListenerEncodingToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, USE_LISTENER_ENCODING, USE_LISTENER_ENCODING_ATTR, true);
    }

    @Test
    public void setUseListenerEncodingToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, USE_LISTENER_ENCODING, USE_LISTENER_ENCODING_ATTR, false);
    }
}
