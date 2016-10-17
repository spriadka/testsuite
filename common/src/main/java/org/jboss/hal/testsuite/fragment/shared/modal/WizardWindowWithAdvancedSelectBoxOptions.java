package org.jboss.hal.testsuite.fragment.shared.modal;

import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class WizardWindowWithAdvancedSelectBoxOptions extends WizardWindow {

    public WizardWindowWithAdvancedSelectBoxOptions pick(String label) {
        List<WebElement> elements = getOptionElements();

        for (WebElement elem : elements) {
            if (elem.getText().equalsIgnoreCase(label)) {
                elem.click();
                Console.withBrowser(browser).waitUntilFinished();
                return this;
            }
        }
        throw new NoSuchElementException("Unable to find option with value " + label);
    }

    public List<WebElement> getOptionElements() {
        String cssClass = PropUtils.get("components.selectbox.item.class");
        By selector = By.className(cssClass);

        return root.findElements(selector);
    }


    public List<String> getOptions() {
        List<WebElement> elements = getOptionElements();

        return elements.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public WizardWindowWithAdvancedSelectBoxOptions clickContinue() {
        clickButton("Continue");

        Console.withBrowser(browser).waitUntilFinished();
        return this;
    }
}
