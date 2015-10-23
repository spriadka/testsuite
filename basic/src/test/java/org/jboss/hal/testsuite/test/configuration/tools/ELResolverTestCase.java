package org.jboss.hal.testsuite.test.configuration.tools;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.runtime.DomainRuntimeEntryPoint;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
    private DomainConfigEntryPoint page;

    @Test
    public void resolveSystemPropertyValue() throws Exception {
        FinderNavigation navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, "System Properties");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        addProperty(NAME, VALUE);

        clickExpressionResolver();
        getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        getWindowFragment().clickButton("Resolve");

        String output = getWindowFragment().getEditor().text("output");
        getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=" + VALUE + "\n";
        assertEquals(expected, output);

        removeProperty();
    }

    @Test
    public void resolveServerPropertyWithSameValue() throws Exception {


        goToServerProperties("server-one");
        addProperty(NAME, VALUE);

        goToServerProperties("server-two");
        addProperty(NAME, VALUE);

        clickExpressionResolver();
        getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        getWindowFragment().clickButton("Resolve");

        String output = getWindowFragment().getEditor().text("output");
        getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=" + VALUE + "\n";
        assertEquals(expected, output);

        goToServerProperties("server-one");
        removeProperty();

        goToServerProperties("server-two");
        removeProperty();
    }

    @Test
    public void resolveServerPropertyWithDifferentValue() throws Exception {
        String reverseValue = new StringBuilder(VALUE).reverse().toString();

        goToServerProperties("server-one");
        addProperty(NAME, VALUE);

        goToServerProperties("server-two");
        addProperty(NAME, reverseValue);

        clickExpressionResolver();
        getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        getWindowFragment().clickButton("Resolve");

        String output = getWindowFragment().getEditor().text("output");
        getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=" + reverseValue + "\n";
        assertEquals(expected, output);

        goToServerProperties("server-one");
        removeProperty();

        goToServerProperties("server-two");
        removeProperty();
    }

    @Test
    public void resolveServerPropertyDefinedOnOneServer() throws Exception {
        goToServerProperties("server-one");
        addProperty(NAME, VALUE);

        clickExpressionResolver();
        getWindowFragment().getEditor().text("input", "${" + NAME + ":default_value}");
        getWindowFragment().clickButton("Resolve");

        String output = getWindowFragment().getEditor().text("output");
        getWindowFragment().clickButton("Done");
        String expected = "server-one=" + VALUE + "\nserver-two=default_value\n";
        assertEquals(expected, output);

        goToServerProperties("server-one");
        removeProperty();
    }



    /* UTILS */
    private void goToServerProperties(String server) {
        FinderNavigation navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                .addAddress(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                .addAddress(FinderNames.HOST, "master")
                .addAddress(FinderNames.SERVER, server);

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();

        browser.findElement(ByJQuery.selector("div.gwt-Label:contains(System Properties)")).click();
    }

    private void addProperty(String name, String value) {
        WebElement add = browser.findElement(By.id("gwt-debug-addBtnPropertyEditor"));
        add.click();

        getWindowFragment().getEditor().text("key", name);
        getWindowFragment().getEditor().text("value", value);
        getWindowFragment().save();
    }

    private void removeProperty() {
        page.getResourceManager().getResourceTable().selectRowByText(0, NAME);
        browser.findElement(By.xpath("//button[contains(text(), 'Remove')]")).click();
        try {
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class).confirm();
        } catch (TimeoutException ignored) {
        }
    }

    private void clickExpressionResolver() {
        WebElement tools = browser.findElement(ByJQuery.selector("div.gwt-HTML.footer-link:contains(Tools)"));
        tools.click();
        browser.findElement(By.xpath("//div[@class='popupContent']//a[contains(text(), 'Expression Resolver')]")).click();

    }

    private ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }
}
