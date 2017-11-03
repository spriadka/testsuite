package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddAggregateSaslServerWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.AggregateSaslServerInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddAggregateSaslServerValidator extends WizardValidator<AddAggregateSaslServerWizard> {

    public AddAggregateSaslServerValidator(ConfigFragment fragment) {
        this(Arrays.asList(AggregateSaslServerInput.values()), fragment);
    }

    public AddAggregateSaslServerValidator(Collection<WizardInput<AddAggregateSaslServerWizard>> mandatoryInputs, ConfigFragment fragment) {
        super(mandatoryInputs, fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddAggregateSaslServerWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Collections.singleton(AggregateSaslServerInput.NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(AggregateSaslServerInput.SASL_SERVER_MECHANISM_FACTORIES)
                , wizard);
    }
}
