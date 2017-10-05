package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddProviderFilteringSaslServerWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.ProviderFilteringSaslServerInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddProviderFilteringSaslServerValidator extends WizardValidator<AddProviderFilteringSaslServerWizard> {
    public AddProviderFilteringSaslServerValidator(Collection<WizardInput<AddProviderFilteringSaslServerWizard>> mandatoryInputs, ConfigFragment fragment) {
        super(mandatoryInputs, fragment);
    }

    public AddProviderFilteringSaslServerValidator(ConfigFragment fragment) {
        this(Arrays.asList(ProviderFilteringSaslServerInput.values()), fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddProviderFilteringSaslServerWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Collections.singleton(ProviderFilteringSaslServerInput.NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(ProviderFilteringSaslServerInput.SASL_SERVER_FACTORY), wizard);
        fillFieldsAndAssert(Collections.singleton(ProviderFilteringSaslServerInput.ENABLING), wizard);
        fillFieldsAndAssert(Arrays.asList(ProviderFilteringSaslServerInput.NAME,
                ProviderFilteringSaslServerInput.ENABLING), wizard);
        fillFieldsAndAssert(Arrays.asList(ProviderFilteringSaslServerInput.SASL_SERVER_FACTORY
                , ProviderFilteringSaslServerInput.ENABLING), wizard);
    }
}
