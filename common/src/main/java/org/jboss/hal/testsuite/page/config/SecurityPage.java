package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.security.SecurityDomainAddWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.openqa.selenium.By;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 16.10.15.
 */
public class SecurityPage extends ConfigurationPage implements Navigatable {

    private FinderNavigation navigation;
    private static final String SECURITY_DOMAIN = "Security Domain";

    @Override
    public void navigate() {
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEM);
        }
        navigation.addAddress(FinderNames.SUBSYSTEM, "Security")
                .addAddress(SECURITY_DOMAIN);
        navigation.selectColumn();
    }

    public void viewJBossEJBPolicy() {
        viewSecurityDomain("jboss-ejb-policy");
    }

    public void viewJBossWebPolicy() {
        viewSecurityDomain("jboss-web-policy");
    }

    public void viewOther() {
        viewSecurityDomain("other");
    }

    public SecurityDomainAddWizard addSecurityDomain() {
        navigation.resetNavigation().addAddress(SECURITY_DOMAIN).selectColumn().invoke(FinderNames.ADD);
        return getResourceManager().addResource(SecurityDomainAddWizard.class);
    }

    public void viewSecurityDomain(String name) {
        navigation.resetNavigation().addAddress(SECURITY_DOMAIN, name).selectRow().invoke(FinderNames.VIEW);
    }

    public void switchToAuthentication() {
        switchSubTab("Authentication");
    }

    public void switchToAuthorization() {
        switchSubTab("Authorization");
    }

    public void switchToAudit() {
        switchSubTab("Audit");
    }

    public void switchToACL() {
        switchSubTab("ACL");
    }

    public void addAuthenticationModule(String name, String code) {
        WizardWindow wizard = getResourceManager().addResource();
        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text("code", code);
        editor.select("flag", "optional");
        wizard.finish();
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


}
