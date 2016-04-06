package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Created by pcyprian on 26.10.15.
 */
public class ELResolverPage extends HomePage {

    public void openExpressionResolver() {
        WebElement tools = browser.findElement(ByJQuery.selector("div.gwt-HTML.footer-link:contains(Tools)"));
        tools.click();
        browser.findElement(By.xpath("//div[@class='popupContent']//a[contains(text(), 'Expression Resolver')]")).click();
    }

    public ConfigFragment getWindowFragment() {
        WebElement editPanel = browser.findElement(By.className("default-window-content"));
        return  Graphene.createPageFragment(ConfigFragment.class, editPanel);
    }

    public String resolveSystemProperty(String name) {
        ConfigFragment configFragment = getWindowFragment();
        configFragment.getEditor().text("input", "${" + name + ":default_value}");
        configFragment.clickButton("Resolve");

        return configFragment.getEditor().text("output");
    }
}
