package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.federation.FederationConfigArea;
import org.jboss.hal.testsuite.fragment.config.federation.IdentityProviderConfigArea;
import org.jboss.hal.testsuite.fragment.config.federation.KeyStoreConfigArea;
import org.jboss.hal.testsuite.fragment.config.federation.ServiceProviderConfigArea;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.PropUtils;

/**
 * @author jcechace
 */
@Location("#picletlink-federation")
public class FederationPage extends ConfigPage {
    @Override
    public FederationConfigArea getConfig() {
        return getConfig(FederationConfigArea.class);
    }

    public IdentityProviderConfigArea getIdpConfig() {
        return getConfig(IdentityProviderConfigArea.class);
    }

    public ServiceProviderConfigArea getSpConfig() {
        return getConfig(ServiceProviderConfigArea.class);
    }

    public KeyStoreConfigArea getKsConfig() {
        return getConfig(KeyStoreConfigArea.class);
    }

    public void switchToIdentityProvider() {
        String label = PropUtils.get("config.federation.idp.view.label");
        switchView(label);
    }

    public void switchToServiceProvider() {
        String label = PropUtils.get("config.federation.sp.view.label");
        switchView(label);
    }

    public void switchToKeyStore() {
        String label = PropUtils.get("config.federation.ks.view.label");
        switchView(label);
    }
}
