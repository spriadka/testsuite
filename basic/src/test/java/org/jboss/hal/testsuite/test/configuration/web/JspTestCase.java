package org.jboss.hal.testsuite.test.configuration.web;


import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.ServletPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.WEB_SUBSYSTEM_JSP_CONFIGURATION_ADDRESS;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Ignore("This was moved to a different page")
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class JspTestCase {

    private static final String CHECK_INTERVAL_ID = "check-interval";
    private static final String DEVELOPMENT_ID = "development";
    private static final String DISABLED_ID = "disabled";
    private static final String KEEP_GENERATED_ID = "keep-generated";
    private static final String DISPLAY_SOURCE_FRAGMENT_ID = "display-source-fragment";
    private static final String X_POWERED_BY_ID = "x-powered-by";

    private static final String NUMBER_VALUE = "25";
    private static final String ALPHANUMERIC_VALUE = RandomStringUtils.randomAlphabetic(5);
    private static final String ALPHABETIC_VALUE = RandomStringUtils.randomAlphanumeric(5);
    private static final String NUMBER_NEGATIVE_VALUE = "-60";

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(WEB_SUBSYSTEM_JSP_CONFIGURATION_ADDRESS, client);

    @Drone
    public WebDriver browser;

    @Page
    public ServletPage page;

    @Before
    public void before() {
        browser.navigate().refresh();
        Graphene.goTo(ServletPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Console.withBrowser(browser).maximizeWindow();
    }

    @Test
    public void setDisabled() {
        changeCheckboxAndAssert(page.getConfig().jsp(), DISABLED_ID, false, true);
    }

    @Test
    public void setEnabled() {
        changeCheckboxAndAssert(page.getConfig().jsp(), DISABLED_ID, true, true);
    }

    @Test
    public void checkInterval() {
        changeTextAndAssert(page.getConfig().jsp(), CHECK_INTERVAL_ID, NUMBER_VALUE, true);
    }

    @Test
    public void checkIntervalWrongInput_Alphabetic() {
        changeTextAndAssert(page.getConfig().jsp(), CHECK_INTERVAL_ID, ALPHABETIC_VALUE, false);
    }

    @Test
    public void checkIntervalWrongInput_Alphanumeric() {
        changeTextAndAssert(page.getConfig().jsp(), CHECK_INTERVAL_ID, ALPHANUMERIC_VALUE, false);
    }

    @Test
    public void checkIntervalWrongInput_NegativeNumber() {
        changeTextAndAssert(page.getConfig().jsp(), CHECK_INTERVAL_ID, NUMBER_NEGATIVE_VALUE, false);
    }

    private void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expected) {
        changeTextAndAssert(fragment, identifier, value, expected, identifier);
    }

    private void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expected) {
        changeCheckboxAndAssert(fragment, identifier, value, expected, identifier);
    }

    private void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expected, String dmrAttribute) {
        fragment.edit().text(identifier, value);
        fragment.saveAndAssert(expected);
        if (expected != false) {
            verifier.verifyAttribute(dmrAttribute, value);
        }
    }

    private void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expected, String dmrAttribute) {
        fragment.edit().checkbox(identifier, value);
        fragment.saveAndAssert(expected);
        if (expected != false) {
            verifier.verifyAttribute(dmrAttribute, String.valueOf(value));
        }
    }
}
