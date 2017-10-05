package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddCustomCredentialSecurityWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.CustomCredentialSecurityInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddCustomCredentialSecurityValidator extends WizardValidator<AddCustomCredentialSecurityWizard> {
    public AddCustomCredentialSecurityValidator(Collection<WizardInput<AddCustomCredentialSecurityWizard>> mandatoryInputs, ConfigFragment fragment) {
        super(mandatoryInputs, fragment);
    }

    public AddCustomCredentialSecurityValidator(ConfigFragment fragment) {
        this(Arrays.asList(CustomCredentialSecurityInput.values()), fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddCustomCredentialSecurityWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Collections.singleton(CustomCredentialSecurityInput.NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(CustomCredentialSecurityInput.CLASS_NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(CustomCredentialSecurityInput.MODULE), wizard);
        fillFieldsAndAssert(Arrays.asList(CustomCredentialSecurityInput.NAME,
                CustomCredentialSecurityInput.CLASS_NAME), wizard);
        fillFieldsAndAssert(Arrays.asList(CustomCredentialSecurityInput.NAME,
                CustomCredentialSecurityInput.MODULE), wizard);
        fillFieldsAndAssert(Arrays.asList(CustomCredentialSecurityInput.CLASS_NAME,
                CustomCredentialSecurityInput.MODULE), wizard);
    }
}
