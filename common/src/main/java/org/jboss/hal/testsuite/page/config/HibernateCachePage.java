package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class HibernateCachePage extends ConfigPage implements Navigatable {

    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class);
        }
        navigation.addAddress(FinderNames.SUBSYSTEM, "Infinispan")
                .addAddress("Cache Container", "hibernate");
        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible(20);
    }

    private static final By CONTENT = By.id(PropUtils.get("page.content.id"));

    public CacheFragment content() {
        return Graphene.createPageFragment(CacheFragment.class, getContentRoot().findElement(CONTENT));
    }

    public CacheFragment local() {
        switchTab("Local Caches");
        return content();
    }

    public CacheFragment replicated() {
        switchTab("Replicated Caches");
        return content();
    }

    public CacheFragment distributed() {
        switchTab("Distributed Caches");
        return content();
    }

    public CacheFragment invalidation() {
        switchTab("Invalidation Caches");
        return content();
    }

    public boolean editTextAndSave(String identifier, String value) {
        ConfigFragment configFragment = getConfigFragment();
        Editor editor = configFragment.edit();
        editor.text(identifier, value);
        return configFragment.save();
    }

    public boolean selectOptionAndSave(String identifier, String value) {
        ConfigFragment configFragment = getConfigFragment();
        configFragment.edit().select(identifier, value);
        return configFragment.save();
    }

    public Boolean editCheckboxAndSave(String identifier, Boolean value) {
        ConfigFragment configFragment = getConfigFragment();
        configFragment.edit().checkbox(identifier, value);
        return configFragment.save();
    }

    public Boolean isErrorShownInForm() {
        By selector = ByJQuery.selector("div.form-item-error-desc:visible");
        return isElementVisible(selector);
    }

    private Boolean isElementVisible(By selector) {
        try {
            Graphene.waitAjax().until().element(selector).is().visible();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void selectCache(String name) {
        getResourceManager().getResourceTable().selectRowByText(0, name).click();
    }


}
