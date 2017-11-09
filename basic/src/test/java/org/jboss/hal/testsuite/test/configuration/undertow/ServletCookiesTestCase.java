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
public class ServletCookiesTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowServletPage page;

    //identifiers
    private static final String COMMENT = "comment";
    private static final String DOMAIN = "domain";
    private static final String HTTP_ONLY = "http-only";
    private static final String MAX_AGE = "max-age";
    private static final String NAME = "name";
    private static final String SECURE = "secure";

    private static final String SERVLET_CONTAINER = "servlet-container_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address SERVLET_CONTAINER_ADDRESS = UNDERTOW_ADDRESS.and("servlet-container", SERVLET_CONTAINER);
    private static final Address SESSION_COOKIE_ADDRESS = SERVLET_CONTAINER_ADDRESS.and("setting", "session-cookie");


    @BeforeClass
    public static void setUp() throws InterruptedException, IOException, TimeoutException {
        operations.add(SERVLET_CONTAINER_ADDRESS);
    }

    @Before
    public void before() {
        page.navigate();
        page.viewServletContainer(SERVLET_CONTAINER).switchToCookies();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException {
        operations.remove(SERVLET_CONTAINER_ADDRESS);
    }

    @Test
    public void editComment() throws Exception {
        editTextAndVerify(SESSION_COOKIE_ADDRESS, COMMENT);
    }

    @Test
    public void editDomain() throws Exception {
        editTextAndVerify(SESSION_COOKIE_ADDRESS, DOMAIN);
    }

    @Test
    public void setHTTPOnlyToTrue() throws Exception {
        editCheckboxAndVerify(SESSION_COOKIE_ADDRESS, HTTP_ONLY, true);
    }

    @Test
    public void setHTTPOnlyToFalse() throws Exception {
        editCheckboxAndVerify(SESSION_COOKIE_ADDRESS, HTTP_ONLY, false);
    }

    @Test
    public void editMaxAge() throws Exception {
        editTextAndVerify(SESSION_COOKIE_ADDRESS, MAX_AGE, NUMERIC_VALID);
    }

    @Test
    public void editMaxAgeInvalid() throws Exception {
        verifyIfErrorAppears(MAX_AGE, NUMERIC_INVALID);
    }

    @Test
    public void editName() throws Exception {
        editTextAndVerify(SESSION_COOKIE_ADDRESS, NAME);
    }

    @Test
    public void setSecureToTrue() throws Exception {
        editCheckboxAndVerify(SESSION_COOKIE_ADDRESS, SECURE, true);
    }

    @Test
    public void setSecureToFalse() throws Exception {
        editCheckboxAndVerify(SESSION_COOKIE_ADDRESS, SECURE, false);
    }
}
