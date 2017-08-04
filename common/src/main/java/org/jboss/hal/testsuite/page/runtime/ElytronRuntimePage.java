package org.jboss.hal.testsuite.page.runtime;

import org.jboss.hal.testsuite.page.MetricsPage;

public class ElytronRuntimePage extends MetricsPage {

    private static final String ElYTRON_RUNTIME_LABEL = "Security - Elytron";
    private static final String REFRESH_BUTTON_LABEL = "Refresh";

    @Override
    public void navigate() {
        navigate2runtimeSubsystem(ElYTRON_RUNTIME_LABEL);
    }

    public void refreshCredentialStores() {
        clickButton(REFRESH_BUTTON_LABEL);
    }
}
