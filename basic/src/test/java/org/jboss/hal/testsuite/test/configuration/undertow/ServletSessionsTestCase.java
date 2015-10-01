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
public class ServletSessionsTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private final String PATH = "path";
    private final String RELATIVE_TO = "relative-to";

    //attribute names
    private final String PATH_ATTR = "path";
    private final String RELATIVE_TO_ATTR = "relative-to";


    private static String servletContainer;
    private static ResourceAddress address;

    @BeforeClass
    public static void setUp() {
        servletContainer = operations.createServletContainer();
        address = servletContainerTemplate.append("/setting=persistent-sessions").resolve(context, servletContainer);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(servletContainer).switchToSessions();
    }

    @AfterClass
    public static void tearDown() {
        operations.removeServletContainer(servletContainer);
    }

    @Test
    public void editPath() throws IOException, InterruptedException {
        editTextAndVerify(address, PATH, PATH_ATTR);
    }

    @Test
    public void editRelativeTo() throws IOException, InterruptedException {
        editTextAndVerify(address, RELATIVE_TO, RELATIVE_TO_ATTR);
    }

}
