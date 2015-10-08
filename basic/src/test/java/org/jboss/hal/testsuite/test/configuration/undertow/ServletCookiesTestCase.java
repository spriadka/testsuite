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
public class ServletCookiesTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private final String COMMENT = "comment";
    private final String DOMAIN = "domain";
    private final String HTTP_ONLY = "http-only";
    private final String MAX_AGE = "max-age";
    private final String NAME = "name";
    private final String SECURE = "secure";

    //attribute names
    private final String COMMENT_ATTR = "comment";
    private final String DOMAIN_ATTR = "domain";
    private final String HTTP_ONLY_ATTR = "http-only";
    private final String MAX_AGE_ATTR = "max-age";
    private final String NAME_ATTR = "name";
    private final String SECURE_ATTR = "secure";

    private static String servletContainer;
    private static ResourceAddress address;

    @BeforeClass
    public static void setUp() {
        servletContainer = operations.createServletContainer();
        address = servletContainerTemplate.append("/setting=session-cookie").resolve(context, servletContainer);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(servletContainer).switchToCookies();
    }

    @AfterClass
    public static void tearDown() {
        operations.removeServletContainer(servletContainer);
    }

    @Test
    public void editComment() throws IOException, InterruptedException {
        editTextAndVerify(address, COMMENT, COMMENT_ATTR);
    }

    @Test
    public void editDomain() throws IOException, InterruptedException {
        editTextAndVerify(address, DOMAIN, DOMAIN_ATTR);
    }

    @Test
    public void setHTTPOnlyToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, HTTP_ONLY, HTTP_ONLY_ATTR, true);
    }

    @Test
    public void setHTTPOnlyToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, HTTP_ONLY, HTTP_ONLY_ATTR, false);
    }

    @Test
    public void editMaxAge() throws IOException, InterruptedException {
        editTextAndVerify(address, MAX_AGE, MAX_AGE_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editMaxAgeInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(MAX_AGE, NUMERIC_INVALID);
    }

    @Test
    public void editName() throws IOException, InterruptedException {
        editTextAndVerify(address, NAME, NAME_ATTR);
    }

    @Test
    public void setSecureToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, SECURE, SECURE_ATTR, true);
    }

    @Test
    public void setSecureToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, SECURE, SECURE_ATTR, false);
    }
}
