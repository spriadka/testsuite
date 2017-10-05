package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddKerberosSecurityWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum KerberosSecurityInput implements WizardInput<AddKerberosSecurityWizard> {
    NAME {
        @Override
        public void fill(AddKerberosSecurityWizard window) {
            window.name(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddKerberosSecurityWizard window) {
            window.name("");
        }

        @Override
        public String getName() {
            return name();
        }
    },

    PATH {
        @Override
        public void fill(AddKerberosSecurityWizard window) {
            window.path(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddKerberosSecurityWizard window) {
            window.path("");
        }

        @Override
        public String getName() {
            return name();
        }
    },

    PRINCIPAL {
        @Override
        public void fill(AddKerberosSecurityWizard window) {
            window.principal(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddKerberosSecurityWizard window) {
            window.principal("");
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
