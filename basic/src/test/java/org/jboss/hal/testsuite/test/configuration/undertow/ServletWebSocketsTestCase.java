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
public class ServletWebSocketsTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private static final String BUFFER_POOL = "buffer-pool";
    private static final String DISPATCH_TO_WORKER = "dispatch-to-worker";
    private static final String WORKER = "worker";

    private static final String SERVLET_CONTAINER = "servlet-container_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address SERVLET_CONTAINER_ADDRESS = UNDERTOW_ADDRESS.and("servlet-container", SERVLET_CONTAINER);
    private static final Address SERVLET_WEBSOCKETS_SETTING = SERVLET_CONTAINER_ADDRESS.and("setting", "websockets");

    @BeforeClass
    public static void setUp() throws InterruptedException, IOException, TimeoutException {
        operations.add(SERVLET_CONTAINER_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(SERVLET_CONTAINER).switchToWebSockets();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException {
        operations.remove(SERVLET_CONTAINER_ADDRESS);
        administration.restartIfRequired();
        administration.reloadIfRequired();
    }

    @Test
    public void editBufferPool() throws Exception {
        editTextAndVerify(SERVLET_WEBSOCKETS_SETTING, BUFFER_POOL, undertowOps.createBufferPool());
    }

    @Test
    public void setDispatchToWorkerToTrue() throws Exception {
        editCheckboxAndVerify(SERVLET_WEBSOCKETS_SETTING, DISPATCH_TO_WORKER, true);
    }

    @Test
    public void setDispatchToWorkerToFalse() throws Exception {
        editCheckboxAndVerify(SERVLET_WEBSOCKETS_SETTING, DISPATCH_TO_WORKER, false);
    }

    @Test
    public void editWorker() throws Exception {
        editTextAndVerify(SERVLET_WEBSOCKETS_SETTING, WORKER, undertowOps.createWorker());
    }
}
