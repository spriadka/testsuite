package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddCustomCredentialSecurityWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum CustomCredentialSecurityInput implements WizardInput<AddCustomCredentialSecurityWizard> {
    CLASS_NAME {
        @Override
        public void fill(AddCustomCredentialSecurityWizard window) {
            window.className(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddCustomCredentialSecurityWizard window) {
            window.className("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, NAME {
        @Override
        public void fill(AddCustomCredentialSecurityWizard window) {
            window.name(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddCustomCredentialSecurityWizard window) {
            window.name("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, MODULE {
        @Override
        public void fill(AddCustomCredentialSecurityWizard window) {
            window.module(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddCustomCredentialSecurityWizard window) {
            window.module("");
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
