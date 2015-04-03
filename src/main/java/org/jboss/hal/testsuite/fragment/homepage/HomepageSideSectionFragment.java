package org.jboss.hal.testsuite.fragment.homepage;

import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jcechace
 */
public class HomepageSideSectionFragment extends BaseFragment {

    public WebElement getHeader() {
        By selector = By.className(PropUtils.get("homepage.sidebar.section.header.class"));
        WebElement header = root.findElement(selector);

        return header;
    }

    public String getHeaderLabel() {
        WebElement header = getHeader();

        return header.getText();
    }

    public List<WebElement> getAllLinkElements() {
        By selector = By.className(PropUtils.get("homepage.link.class"));
        List<WebElement> elements = root.findElements(selector);

        return elements;
    }

    public Map<String, String> getAllLinks() {
        List<WebElement> elements = getAllLinkElements();
        Map<String, String> links = new HashMap<String, String>();

        for (WebElement link : elements) {
            links.put(link.getText(), link.getAttribute("href"));
        }

        return links;
    }
}
