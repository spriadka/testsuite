package org.jboss.hal.testsuite.test.configuration.elytron.factory.input;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddSaslPatternFilterWizard;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

public enum SaslServerFilterInput implements WizardInput<AddSaslPatternFilterWizard> {
    PREDEFINED_FILTER {
        @Override
        public void fill(AddSaslPatternFilterWizard window) {
            window.predefinedFilter("HASH_MD5");
        }

        @Override
        public void clear(AddSaslPatternFilterWizard window) {
            window.predefinedFilter("");
        }

        @Override
        public String getName() {
            return name();
        }
    }, PATTERN_FILTER {
        @Override
        public void fill(AddSaslPatternFilterWizard window) {
            window.patternFilter(RandomStringUtils.randomAlphanumeric(7));
        }

        @Override
        public void clear(AddSaslPatternFilterWizard window) {
            window.patternFilter("");
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
