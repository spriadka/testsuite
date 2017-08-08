package org.jboss.hal.testsuite.page.runtime;

import org.jboss.hal.testsuite.fragment.runtime.elytron.AddAliasWizard;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.page.MetricsPage;

public class ElytronRuntimePage extends MetricsPage {

    private static final String ElYTRON_RUNTIME_LABEL = "Security - Elytron";
    private static final String REFRESH_BUTTON_LABEL = "Refresh";
    private static final String ALIASES_TAB_LABEL = "Aliases";

    @Override
    public void navigate() {
        navigate2runtimeSubsystem(ElYTRON_RUNTIME_LABEL);
    }

    public void refreshCredentialStores() {
        clickButton(REFRESH_BUTTON_LABEL);
    }

    public AddAliasWizard invokeAddAlias() {
        return getConfig().switchTo(ALIASES_TAB_LABEL).getResourceManager().addResource(AddAliasWizard.class);
    }

    public ResourceManager getAliases() {
        return getConfig().switchTo(ALIASES_TAB_LABEL).getResourceManager();
    }

    public ResourceManager getCredentialStores() {
        return getResourceManager();
    }

    public void selectCredentialStore(String credentialStoreName) {
        getCredentialStores().selectByName(credentialStoreName);
    }

    public void selectAlias(String aliasName) {
        getAliases().selectByName(aliasName);
    }
}
