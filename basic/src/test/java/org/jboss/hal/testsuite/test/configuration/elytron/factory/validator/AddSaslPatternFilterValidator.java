package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddSaslPatternFilterWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.SaslServerFilterInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddSaslPatternFilterValidator extends WizardValidator<AddSaslPatternFilterWizard> {

    public AddSaslPatternFilterValidator(ConfigFragment fragment) {
        this(Arrays.asList(SaslServerFilterInput.values()), fragment);
    }

    public AddSaslPatternFilterValidator(Collection<WizardInput<AddSaslPatternFilterWizard>> mandatoryInputs, ConfigFragment fragment) {
        super(mandatoryInputs, fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddSaslPatternFilterWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Arrays.asList(SaslServerFilterInput.PATTERN_FILTER,
                SaslServerFilterInput.PREDEFINED_FILTER), wizard);
    }
}
