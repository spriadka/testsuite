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
public class ServletSessionsTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private static final String PATH = "path";
    private static final String RELATIVE_TO = "relative-to";

    private static final String SERVLET_CONTAINER = "servlet-container_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address SERVLET_CONTAINER_ADDRESS = UNDERTOW_ADDRESS.and("servlet-container", SERVLET_CONTAINER);
    private static final Address SERVLET_SESSIONS_ADDRESS = SERVLET_CONTAINER_ADDRESS.and("setting", "persistent-sessions");


    @BeforeClass
    public static void setUp() throws InterruptedException, IOException, TimeoutException {
        operations.add(SERVLET_CONTAINER_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(SERVLET_CONTAINER).switchToSessions();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException {
        operations.remove(SERVLET_SESSIONS_ADDRESS);
    }

    @Test
    public void editPath() throws Exception {
        editTextAndVerify(SERVLET_SESSIONS_ADDRESS, PATH);
    }

    @Test
    public void editRelativeTo() throws Exception {
        editTextAndVerify(SERVLET_SESSIONS_ADDRESS, RELATIVE_TO, "jboss.server.base.dir");
    }

}
