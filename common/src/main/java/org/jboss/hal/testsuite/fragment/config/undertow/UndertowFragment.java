package org.jboss.hal.testsuite.fragment.config.undertow;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
public class UndertowFragment extends ConfigFragment {

    private static final By CONTENT_ROOT = ByJQuery.selector("." + PropUtils.get("page.content.rhs.class") + ":visible");

    public Boolean isErrorShowedInForm() {
        By selector = ByJQuery.selector("div.form-item-error-desc:visible");
        return isElementVisible(selector);
    }

    public Boolean isElementVisible(By selector) {
        try {
            Graphene.waitAjax().until().element(selector).is().visible();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean editTextAndSave(String identifier, String value) {
        edit().text(identifier, value);
        return save();
    }

    public Boolean editCheckboxAndSave(String identifier, Boolean value) {
        edit().checkbox(identifier, value);
        return save();
    }

    public Boolean selectOptionAndSave(String identifier, String value) {
        edit().select(identifier, value);
        return save();
    }

}
