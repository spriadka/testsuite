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

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 17.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ServletWebSocketsTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private final String BUFFER_POOL = "buffer-pool";
    private final String DISPATCH_TO_WORKER = "dispatch-to-worker";
    private final String WORKER = "worker";

    //attribute names
    private final String BUFFER_POOL_ATTR = "buffer-pool";
    private final String DISPATCH_TO_WORKER_ATTR = "dispatch-to-worker";
    private final String WORKER_ATTR = "worker";

    private static String servletContainer;
    private static ResourceAddress address;

    @BeforeClass
    public static void setUp() {
        servletContainer = operations.createServletContainer();
        address = servletContainerTemplate.append("/setting=websockets").resolve(context, servletContainer);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(servletContainer).switchToWebSockets();
    }

    @AfterClass
    public static void tearDown() {
        operations.removeServletContainer(servletContainer);
    }

    @Test
    public void editBufferPool() throws IOException, InterruptedException {
        editTextAndVerify(address, BUFFER_POOL, BUFFER_POOL_ATTR, BUFFER_POOL_VALUE_VALID);
    }

    @Test
    public void setDispatchToWorkerToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, DISPATCH_TO_WORKER, DISPATCH_TO_WORKER_ATTR, true);
    }

    @Test
    public void setDispatchToWorkerToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, DISPATCH_TO_WORKER, DISPATCH_TO_WORKER_ATTR, false);
    }

    @Test
    public void editWorker() throws IOException, InterruptedException {
        editTextAndVerify(address, WORKER, WORKER_ATTR, WORKER_VALUE_VALID);
    }
}
