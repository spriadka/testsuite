package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableFragment;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

public class UndertowHostPage extends UndertowHTTPPage {

    public ConfigFragment switchToReferenceToFilterSubTab() {
        return getConfig().switchTo("Reference to Filter");
    }

    public ResourceTableFragment filterReferenceTable() {
        By selector = ByJQuery.selector("." + PropUtils.get("configarea.content.class") + ":visible");
        return getResourceManager().getResourceTableInElement(selector);
    }

    public void addReferenceToFilter() {

    }

    public void removeReferenceToFilter() {

    }
}
