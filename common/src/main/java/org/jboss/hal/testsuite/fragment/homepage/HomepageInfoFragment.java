package org.jboss.hal.testsuite.fragment.homepage;

import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author jcechace
 */
public class HomepageInfoFragment extends BaseFragment {

    public String getHeaderLabel() {
        WebElement header = getHeader();

        return header.getText();
    }

    public WebElement getHeader() {
        By selector = By.className(PropUtils.get("homepage.info.header.class"));
        WebElement header = root.findElement(selector);

        return header;
    }

    public WebElement getLinkByLabel(String label) {
        By selector = ByJQuery.selector("a:contains('" + label + "'):visible");

        return getLinkBy(selector);
    }

    public WebElement getLinkByHref(String href) {
        By selector = ByJQuery.selector("a[href$='" + href + "']:visible");

        return getLinkBy(selector);
    }


    public WebElement getLink(String label, String href) {
        By selector = ByJQuery.selector("a[href$='" + href + "']" +
                ":contains('" + label + "'):visible");

        return getLinkBy(selector);
    }

    private WebElement getLinkBy(By selector) {
        WebElement link = getHeader().findElement(selector);
        return link;
    }

}
