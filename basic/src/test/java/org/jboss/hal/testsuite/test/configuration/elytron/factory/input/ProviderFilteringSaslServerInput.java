package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddProviderFilteringSaslServerWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum ProviderFilteringSaslServerInput implements WizardInput<AddProviderFilteringSaslServerWizard> {
    NAME {
        @Override
        public void fill(AddProviderFilteringSaslServerWizard window) {
            window.name(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddProviderFilteringSaslServerWizard window) {
            window.name("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, SASL_SERVER_FACTORY {
        @Override
        public void fill(AddProviderFilteringSaslServerWizard window) {
            window.saslServerFactory(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddProviderFilteringSaslServerWizard window) {
            window.saslServerFactory("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, ENABLING {

        @Override
        public void fill(AddProviderFilteringSaslServerWizard window) {
            window.enabling(true);
        }

        @Override
        public void clear(AddProviderFilteringSaslServerWizard window) {
            window.enabling(false);
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
