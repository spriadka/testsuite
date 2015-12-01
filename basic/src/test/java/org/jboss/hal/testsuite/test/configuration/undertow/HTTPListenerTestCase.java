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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class HTTPListenerTestCase extends UndertowTestCaseAbstract {

    private static final Logger log = LoggerFactory.getLogger(HTTPListenerTestCase.class);

    @Page
    private UndertowHTTPPage page;

    //identifiers
    private final String ALLOW_ENCODED_SLASH = "allow-encoded-slash";
    private final String ALLOW_EQUALS_IN_COOKIE_VALUE = "allow-equals-in-cookie-value";
    private final String ALWAYS_SET_KEEP_ALIVE = "always-set-keep-alive";
    private final String BUFFER_PIPELINED_DATA = "buffer-pipelined-data";
    private final String BUFFER_POOL = "buffer-pool";
    private final String CERTIFICATE_FORWARDING = "certificate-forwarding";
    private final String DECODE_URL = "decode-url";
    private final String ENABLE_HTTP2 = "enable-http2";
    private final String ENABLED = "enabled";
    private final String MAX_BUFFERED_REQUEST_SIZE = "max-buffered-request-size";
    private final String MAX_CONNECTIONS = "max-connections";
    private final String MAX_COOKIES = "max-cookies";
    private final String MAX_HEADER_SIZE = "max-header-size";
    private final String MAX_HEADERS = "max-headers";
    private final String MAX_PARAMETERS = "max-parameters";
    private final String MAX_POST_SIZE = "max-post-size";
    private final String NO_REQUEST_TIMEOUT = "no-request-timeout";
    private final String PROXY_ADDRESS_FORWARDING = "proxy-address-forwarding";
    private final String READ_TIMEOUT = "read-timeout";
    private final String RECEIVE_BUFFER = "receive-buffer";
    private final String RECORD_REQUEST_START_TIME = "record-request-start-time";
    private final String REDIRECT_SOCKET = "redirect-socket";
    private final String REQUEST_PARSE_TIMEOUT = "request-parse-timeout";
    private final String RESOLVE_PEER_ADDRESS = "resolve-peer-address";
    private final String SEND_BUFFER = "send-buffer";
    private final String SOCKET_BINDING = "socket-binding";
    private final String TCP_BACKLOG = "tcp-backlog";
    private final String TCP_KEEP_ALIVE = "tcp-keep-alive";
    private final String URL_CHARSET = "url-charset";
    private final String WORKER = "worker";
    private final String WRITE_TIMEOUT = "write-timeout";

    //attribute names
    private final String ALLOW_ENCODED_SLASH_ATTR = "allow-encoded-slash";
    private final String ALLOW_EQUALS_IN_COOKIE_VALUE_ATTR = "allow-equals-in-cookie-value";
    private final String ALWAYS_SET_KEEP_ALIVE_ATTR = "always-set-keep-alive";
    private final String BUFFER_PIPELINED_DATA_ATTR = "buffer-pipelined-data";
    private final String CERTIFICATE_FORWARDING_ATTR = "certificate-forwarding";
    private final String BUFFER_POOL_ATTR = "buffer-pool";
    private final String DECODE_URL_ATTR = "decode-url";
    private final String ENABLE_HTTP2_ATTR = "enable-http2";
    private final String ENABLED_ATTR = "enabled";
    private final String MAX_BUFFERED_REQUEST_SIZE_ATTR = "max-buffered-request-size";
    private final String MAX_CONNECTIONS_ATTR = "max-connections";
    private final String MAX_COOKIES_ATTR = "max-cookies";
    private final String MAX_HEADER_SIZE_ATTR = "max-header-size";
    private final String MAX_HEADERS_ATTR = "max-headers";
    private final String MAX_PARAMETERS_ATTR = "max-parameters";
    private final String MAX_POST_SIZE_ATTR = "max-post-size";
    private final String NO_REQUEST_TIMEOUT_ATTR = "no-request-timeout";
    private final String PROXY_ADDRESS_FORWARDING_ATTR = "proxy-address-forwarding";
    private final String READ_TIMEOUT_ATTR = "read-timeout";
    private final String RECEIVE_BUFFER_ATTR = "receive-buffer";
    private final String RECORD_REQUEST_START_TIME_ATTR = "record-request-start-time";
    private final String REDIRECT_SOCKET_ATTR = "redirect-socket";
    private final String REQUEST_PARSE_TIMEOUT_ATTR = "request-parse-timeout";
    private final String RESOLVE_PEER_ADDRESS_ATTR = "resolve-peer-address";
    private final String SEND_BUFFER_ATTR = "send-buffer";
    private final String SOCKET_BINDING_ATTR = "socket-binding";
    private final String TCP_BACKLOG_ATTR = "tcp-backlog";
    private final String TCP_KEEP_ALIVE_ATTR = "tcp-keep-alive";
    private final String URL_CHARSET_ATTR = "url-charset";
    private final String WORKER_ATTR = "worker";
    private final String WRITE_TIMEOUT_ATTR = "write-timeout";

    //values
    private final String NUMERIC_VALID = "25";
    private final String NUMERIC_INVALID = "25fazf";

    private static AddressTemplate httpListenerTemplate = httpServerTemplate.append("/http-listener=*");
    private static String httpServer;
    private static String httpListener;
    private static String httpListenerToBeRemoved;
    private static ResourceAddress address;

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        httpServer = operations.createHTTPServer();
        httpListenerToBeRemoved = operations.createHTTPListener(httpServer);
        httpListener = operations.createHTTPListener(httpServer);
        address = httpListenerTemplate.resolve(context, httpServer, httpListener);
        log.info("Address is " + address);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewHTTPServer(httpServer).switchToHTTPListeners().selectItemInTableByText(httpListener);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, CommandFailedException, TimeoutException, IOException {
        operations.removeHTTPListener(httpServer, httpListener);
        operations.removeHTTPServer(httpServer);
    }

    @Test
    public void setAllowEncodedSlashToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALLOW_ENCODED_SLASH, ALLOW_ENCODED_SLASH_ATTR, true);
    }

    @Test
    public void setAllowEncodedSlashToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALLOW_ENCODED_SLASH, ALLOW_ENCODED_SLASH_ATTR, false);
    }

    @Test
    public void setAllowEqualsInCookieValueToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALLOW_EQUALS_IN_COOKIE_VALUE, ALLOW_EQUALS_IN_COOKIE_VALUE_ATTR, true);
    }

    @Test
    public void setAllowEqualsInCookieValueToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALLOW_EQUALS_IN_COOKIE_VALUE, ALLOW_EQUALS_IN_COOKIE_VALUE_ATTR, false);
    }

    @Test
    public void setAlwaysSetKeepAliveToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALWAYS_SET_KEEP_ALIVE, ALWAYS_SET_KEEP_ALIVE_ATTR, true);
    }

    @Test
    public void setAlwaysSetKeepAliveToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ALWAYS_SET_KEEP_ALIVE, ALWAYS_SET_KEEP_ALIVE_ATTR, false);
    }

    @Test
    public void setBufferPipelinedDataToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, BUFFER_PIPELINED_DATA, BUFFER_PIPELINED_DATA_ATTR, true);
    }

    @Test
    public void setBufferPipelinedDataToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, BUFFER_PIPELINED_DATA, BUFFER_PIPELINED_DATA_ATTR, false);
    }

    @Test
    public void editBufferPool() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, BUFFER_POOL, BUFFER_POOL_ATTR, BUFFER_POOL_VALUE_VALID);
    }

    @Test
    public void setCertificateForwardingToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, CERTIFICATE_FORWARDING, CERTIFICATE_FORWARDING_ATTR, true);
    }

    @Test
    public void setCertificateForwardingToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, CERTIFICATE_FORWARDING, CERTIFICATE_FORWARDING_ATTR, false);
    }

    @Test
    public void setDecodeURLToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DECODE_URL, DECODE_URL_ATTR, true);
    }

    @Test
    public void setDecodeURLToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, DECODE_URL, DECODE_URL_ATTR, false);
    }

    //TODO:DISALLOWED METHODS

    @Test
    public void setEnableHTTP2ToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ENABLE_HTTP2, ENABLE_HTTP2_ATTR, true);
    }

    @Test
    public void setEnableHTTP2ToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ENABLE_HTTP2, ENABLE_HTTP2_ATTR, false);
    }

    @Test
    public void setEnabledToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ENABLED, ENABLED_ATTR, true);
    }

    @Test
    public void setEnabledToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, ENABLED, ENABLED_ATTR, false);
    }

    @Test
    public void editMaxBufferedRequestSize() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, MAX_BUFFERED_REQUEST_SIZE, MAX_BUFFERED_REQUEST_SIZE_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxBufferedRequestSizeInvalid() {
        verifyIfErrorAppears(MAX_BUFFERED_REQUEST_SIZE, NUMERIC_INVALID);
    }

    @Test
    public void editMaxConnections() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, MAX_CONNECTIONS, MAX_CONNECTIONS_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxConnectionsInvalid() {
        verifyIfErrorAppears(MAX_CONNECTIONS, NUMERIC_INVALID);
    }

    @Test
    public void editMaxCookies() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, MAX_COOKIES, MAX_COOKIES_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxCookiesInvalid() {
        verifyIfErrorAppears(MAX_COOKIES, NUMERIC_INVALID);
    }

    @Test
    public void editMaxHeaderSize() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, MAX_HEADER_SIZE, MAX_HEADER_SIZE_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxHeaderSizeInvalid() {
        verifyIfErrorAppears(MAX_HEADER_SIZE, NUMERIC_INVALID);
    }

    @Test
    public void editMaxHeaders() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, MAX_HEADERS, MAX_HEADERS_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxHeadersInvalid() {
        verifyIfErrorAppears(MAX_HEADERS, NUMERIC_INVALID);
    }

    @Test
    public void editMaxParameters() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, MAX_PARAMETERS, MAX_PARAMETERS_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxParametersInvalid() {
        verifyIfErrorAppears(MAX_PARAMETERS, NUMERIC_INVALID);
    }

    @Test
    public void editMaxPostSize() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, MAX_POST_SIZE, MAX_POST_SIZE_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxPostSizeInvalid() {
        verifyIfErrorAppears(MAX_POST_SIZE, NUMERIC_INVALID);
    }

    @Test
    public void editNoRequestTimeout() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, NO_REQUEST_TIMEOUT, NO_REQUEST_TIMEOUT_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editNoRequestInvalid() {
        verifyIfErrorAppears(NO_REQUEST_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void setProxyAddressForwardingToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, PROXY_ADDRESS_FORWARDING, PROXY_ADDRESS_FORWARDING_ATTR, true);
    }

    @Test
    public void setProxyAddressForwardingToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, PROXY_ADDRESS_FORWARDING, PROXY_ADDRESS_FORWARDING_ATTR, false);
    }

    @Test
    public void editReadTimeout() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, READ_TIMEOUT, READ_TIMEOUT_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editReadTimeoutInvalid() throws IOException, InterruptedException, TimeoutException {
        verifyIfErrorAppears(READ_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void editReceiveBuffer() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, RECEIVE_BUFFER, RECEIVE_BUFFER_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editReceiveBufferInvalid() {
        verifyIfErrorAppears(RECEIVE_BUFFER, NUMERIC_INVALID);
    }

    @Test
    public void setRecordRequestStartTimeToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, RECORD_REQUEST_START_TIME, RECORD_REQUEST_START_TIME_ATTR, true);
    }

    @Test
    public void setRecordRequestStartTimeToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, RECORD_REQUEST_START_TIME, RECORD_REQUEST_START_TIME_ATTR, false);
    }

    @Test
    public void editRedirectSocket() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, REDIRECT_SOCKET, REDIRECT_SOCKET_ATTR, SOCKET_BINDING_VALUE_VALID);
    }

    @Test
    public void editRequestParseTimeout() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, REQUEST_PARSE_TIMEOUT, REQUEST_PARSE_TIMEOUT_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editRequestParseTimeoutInvalid() throws IOException, InterruptedException, TimeoutException {
        verifyIfErrorAppears(REQUEST_PARSE_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void setResolvePeerAddressToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, RESOLVE_PEER_ADDRESS, RESOLVE_PEER_ADDRESS_ATTR, true);
    }

    @Test
    public void setResolvePeerAddressToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, RESOLVE_PEER_ADDRESS, RESOLVE_PEER_ADDRESS_ATTR, false);
    }

    @Test
    public void editSendBuffer() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, SEND_BUFFER, SEND_BUFFER_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editSendBufferInvalid() throws IOException, InterruptedException, TimeoutException {
        verifyIfErrorAppears(SEND_BUFFER, NUMERIC_INVALID);
    }

    @Test
    public void editSocketBinding() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, SOCKET_BINDING, SOCKET_BINDING_ATTR, SOCKET_BINDING_VALUE_VALID);
    }

    @Test
    public void editTCPBacklog() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, TCP_BACKLOG, TCP_BACKLOG_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editTCPBacklogInvalid() {
        verifyIfErrorAppears(TCP_BACKLOG, NUMERIC_INVALID);
    }

    @Test
    public void setTCPKeepAliveToTrue() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, TCP_KEEP_ALIVE, TCP_KEEP_ALIVE_ATTR, true);
    }

    @Test
    public void setTCPKeepAliveToFalse() throws IOException, InterruptedException, TimeoutException {
        editCheckboxAndVerify(address, TCP_KEEP_ALIVE, TCP_KEEP_ALIVE_ATTR, false);
    }

    @Test
    public void editURLCharset() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, URL_CHARSET, URL_CHARSET_ATTR);
    }

    @Test
    public void editWorker() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, WORKER, WORKER_ATTR, WORKER_VALUE_VALID);
    }

    @Test
    public void editWriteTimeout() throws IOException, InterruptedException, TimeoutException {
        editTextAndVerify(address, WRITE_TIMEOUT, WRITE_TIMEOUT_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editWriteTimeoutInvalid() throws IOException, InterruptedException, TimeoutException {
        verifyIfErrorAppears(WRITE_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void addHTTPListenerInGUI() throws InterruptedException, CommandFailedException, TimeoutException, IOException {
        String name = "httpListener_" + RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text(SOCKET_BINDING, SOCKET_BINDING_VALUE_VALID);
        boolean result = wizard.finish();

        Assert.assertTrue("Window should be closed", result);
        Assert.assertTrue("HTTP listener should be present in table", config.resourceIsPresent(name));
        ResourceAddress address = httpListenerTemplate.resolve(context, httpServer, name);
        verifier.verifyResource(address, true);
        verifier.verifyAttribute(address, SOCKET_BINDING, SOCKET_BINDING_VALUE_VALID);
        operations.removeHTTPListener(httpServer, name);
    }

    @Test
    public void removeHTTPListenerInGUI() {
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager()
                .removeResource(httpListenerToBeRemoved)
                .confirm();

        ResourceAddress address = httpListenerTemplate.resolve(context, httpServer, httpListenerToBeRemoved);
        Assert.assertFalse("HTTP listener host should not be present in table", config.resourceIsPresent(httpListenerToBeRemoved));
        verifier.verifyResource(address, false); //HTTP server host should not be present on the server
    }

}
