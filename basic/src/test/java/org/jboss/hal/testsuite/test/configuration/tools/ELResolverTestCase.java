package org.jboss.hal.testsuite.test.configuration.tools;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.ServerConfigurationPage;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;


/**
 * Created by pcyprian on 23.10.15.
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ELResolverTestCase {
    private static final String NAME = "pr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String VALUE = "val_" + RandomStringUtils.randomAlphanumeric(5);

    @Drone
    private WebDriver browser;

    @Page
    private ServerConfigurationPage page;

    @Test
    public void resolveSystemPropertyValue() throws Exception {
        FinderNavigation navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, "System Properties");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        page.addProperty(NAME, VALUE);

        page.clickExpressionResolver();
        page.getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        page.getWindowFragment().clickButton("Resolve");

        String output = page.getWindowFragment().getEditor().text("output");
        page.getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=" + VALUE + "\n";
        assertEquals(expected, output);

        page.removeProperty(NAME);
    }

    @Test
    public void resolveServerPropertyWithSameValue() throws Exception {
        page.goToServerProperties("server-one");
        page.addProperty(NAME, VALUE);

        page.goToServerProperties("server-two");
        page.addProperty(NAME, VALUE);

        page.clickExpressionResolver();
        page.getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        page.getWindowFragment().clickButton("Resolve");

        String output = page.getWindowFragment().getEditor().text("output");
        page.getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=" + VALUE + "\n";
        assertEquals(expected, output);

        page.goToServerProperties("server-one");
        page.removeProperty(NAME);

        page.goToServerProperties("server-two");
        page.removeProperty(NAME);
    }

    @Test
    public void resolveServerPropertyWithDifferentValue() throws Exception {
        String reverseValue = new StringBuilder(VALUE).reverse().toString();

        page.goToServerProperties("server-one");
        page.addProperty(NAME, VALUE);

        page.goToServerProperties("server-two");
        page.addProperty(NAME, reverseValue);

        page.clickExpressionResolver();
        page.getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        page.getWindowFragment().clickButton("Resolve");

        String output = page.getWindowFragment().getEditor().text("output");
        page.getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=" + reverseValue + "\n";
        assertEquals(expected, output);

        page.goToServerProperties("server-one");
        page.removeProperty(NAME);

        page.goToServerProperties("server-two");
        page.removeProperty(NAME);
    }

    @Test
    public void resolveServerPropertyDefinedOnOneServer() throws Exception {
        page.goToServerProperties("server-one");
        page.addProperty(NAME, VALUE);

        page.clickExpressionResolver();
        page.getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        page.getWindowFragment().clickButton("Resolve");

        String output = page.getWindowFragment().getEditor().text("output");
        page.getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=default_value\n";
        assertEquals(expected, output);

        page.goToServerProperties("server-one");
        page.removeProperty(NAME);
    }
}
