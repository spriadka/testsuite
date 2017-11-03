package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddAggregateSaslServerWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum  AggregateSaslServerInput implements WizardInput<AddAggregateSaslServerWizard> {
    NAME {
        @Override
        public void fill(AddAggregateSaslServerWizard window) {
            window.name(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddAggregateSaslServerWizard window) {
            window.name("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, SASL_SERVER_MECHANISM_FACTORIES {
        @Override
        public void fill(AddAggregateSaslServerWizard window) {
            window.saslServerMechanismFactories(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddAggregateSaslServerWizard window) {
            window.saslServerMechanismFactories("");
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
