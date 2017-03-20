package org.jboss.hal.testsuite.fragment.config.messaging;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;

public class ProviderSettingsWindow extends WindowFragment {

    public void switchTo(String label) {
        By selector = ByJQuery.selector("div.gwt-Label:contains('" + label + "')");
        Graphene.waitModel(browser)
                .withTimeout(500, TimeUnit.MILLISECONDS)
                .until().element(selector).is().visible();
        browser.findElement(selector).click();
    }

    public ProviderSettingsWindow switchToClusterCredentialReferenceTab() {
        switchTo("Cluster Credential Reference");
        return this;
    }

    public ProviderSettingsWindow switchToJournalTab() {
        switchTo("Journal");
        return this;
    }

    public ProviderSettingsWindow switchToSecurityTab() {
        switchTo("Security");
        return this;
    }

    public ProviderSettingsWindow maximize() {
        maximizeWindow();
        return this;
    }

    public ConfigFragment getConfigFragment() {
        final WebElement contentRoot = root.findElement(ByJQuery.selector(".gwt-TabPanelBottom > div:visible"));
        return Graphene.createPageFragment(ConfigFragment.class, contentRoot);
    }
}
