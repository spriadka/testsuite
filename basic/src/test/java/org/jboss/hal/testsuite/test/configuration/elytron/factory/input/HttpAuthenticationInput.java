package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddHttpAuthenticationWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum  HttpAuthenticationInput implements WizardInput<AddHttpAuthenticationWizard> {
    NAME {
        @Override
        public void fill(AddHttpAuthenticationWizard window) {
            window.name(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddHttpAuthenticationWizard window) {
            window.name("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, HTTP_SERVER_MECHANISM_FACTORY {
        @Override
        public void fill(AddHttpAuthenticationWizard window) {
            window.httpServerMechanismFactory(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddHttpAuthenticationWizard window) {
            window.httpServerMechanismFactory("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, SECURITY_DOMAIN {
        @Override
        public void fill(AddHttpAuthenticationWizard window) {
            window.securityDomain(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddHttpAuthenticationWizard window) {
            window.securityDomain("");
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
