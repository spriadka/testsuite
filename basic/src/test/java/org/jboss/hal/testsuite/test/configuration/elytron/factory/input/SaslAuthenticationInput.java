package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddSaslAuthenticationWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum  SaslAuthenticationInput implements WizardInput<AddSaslAuthenticationWizard> {
    NAME {
        @Override
        public void fill(AddSaslAuthenticationWizard window) {
            window.name(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddSaslAuthenticationWizard window) {
            window.name("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, SASL_SERVER_FACTORY {
        @Override
        public void fill(AddSaslAuthenticationWizard window) {
            window.saslServerFactory(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddSaslAuthenticationWizard window) {
            window.saslServerFactory("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, SECURITY_DOMAIN {
        @Override
        public void fill(AddSaslAuthenticationWizard window) {
            window.securityDomain(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddSaslAuthenticationWizard window) {
            window.securityDomain("");
        }

        @Override
        public String getName() {
            return name();
        }
    };
}
