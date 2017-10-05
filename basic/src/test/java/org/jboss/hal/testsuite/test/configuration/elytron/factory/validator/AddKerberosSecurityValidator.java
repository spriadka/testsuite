package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddKerberosSecurityWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.KerberosSecurityInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddKerberosSecurityValidator extends WizardValidator<AddKerberosSecurityWizard> {
    public AddKerberosSecurityValidator(Collection<WizardInput<AddKerberosSecurityWizard>> mandatoryInputs, ConfigFragment fragment) {
        super(mandatoryInputs, fragment);
    }

    public AddKerberosSecurityValidator(ConfigFragment fragment) {
        this(Arrays.asList(KerberosSecurityInput.values()), fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddKerberosSecurityWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Collections.singleton(KerberosSecurityInput.NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(KerberosSecurityInput.PATH), wizard);
        fillFieldsAndAssert(Collections.singleton(KerberosSecurityInput.PRINCIPAL), wizard);
        fillFieldsAndAssert(Arrays.asList(KerberosSecurityInput.NAME, KerberosSecurityInput.PATH), wizard);
        fillFieldsAndAssert(Arrays.asList(KerberosSecurityInput.NAME, KerberosSecurityInput.PRINCIPAL), wizard);
        fillFieldsAndAssert(Arrays.asList(KerberosSecurityInput.PATH, KerberosSecurityInput.PRINCIPAL), wizard);
    }
}
