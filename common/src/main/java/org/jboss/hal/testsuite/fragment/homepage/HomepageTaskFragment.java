package org.jboss.hal.testsuite.fragment.homepage;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author jcechace
 */
public class HomepageTaskFragment extends BaseFragment {

    public String getHeaderLabel() {
        WebElement header = getHeader();

        return header.getText();
    }

    public WebElement getHeader() {
        By selector = By.className(PropUtils.get("homepage.task.header.class"));
        WebElement header = root.findElement(selector);

        return header;
    }

    public boolean isOpened() {
        String openedClass = PropUtils.get("homepage.task.opened.class");

        return  root.getAttribute("class").contains(openedClass);
    }

    public void open() {
        String openedClass = PropUtils.get("homepage.task.opened.class");
        // TODO: workaround, remove this once resolved
        getHeader().click();
        if (!isOpened()) {
            getHeader().click();
        }
        Graphene.waitGui().until().element(root).attribute("class").contains(openedClass);
    }


    public void close() {
        String closedClass = PropUtils.get("homepage.task.closed.class");
        // TODO: workaround, remove this once resolved
        getHeader().click();
        if (isOpened()) {
            getHeader().click();
        }
        Graphene.waitGui().until().element(root).attribute("class").contains(closedClass);
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
        WebElement link = root.findElement(selector);
        return link;
    }


}
