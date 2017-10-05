package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddAggregateHttpServerWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum AggregateHttpServerInput implements WizardInput<AddAggregateHttpServerWizard> {
    NAME {
        @Override
        public void fill(AddAggregateHttpServerWizard window) {
            window.name(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddAggregateHttpServerWizard window) {
            window.name("");
        }

        @Override
        public String getName() {
            return name();
        }
    },
    HTTP_SERVER_MECHANISM_FACTORIES {
        @Override
        public void fill(AddAggregateHttpServerWizard window) {
            window.httpServerMechanismFactories(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddAggregateHttpServerWizard window) {
            window.httpServerMechanismFactories("");
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
