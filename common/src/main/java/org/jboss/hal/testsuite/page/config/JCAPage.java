package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.hal.testsuite.fragment.config.jca.JCAConfigArea;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Created by pcyprian on 24.8.15.
 */
public class JCAPage extends ConfigurationPage {

    @Drone
    private WebDriver browser;

    public JCAConfigArea getConfig() {
        return getConfig(JCAConfigArea.class);
    }

    public void switchToConnectionDefinitions() {
        WebElement viewPanel = browser.findElement(By.className("paged-view-navigation-container"));
        WebElement editLink = viewPanel.findElement(By.linkText("Connection Definitions"));
        editLink.click();
    }
}
