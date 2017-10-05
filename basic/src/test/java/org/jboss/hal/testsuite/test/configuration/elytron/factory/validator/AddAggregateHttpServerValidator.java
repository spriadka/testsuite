package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddAggregateHttpServerWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.AggregateHttpServerInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddAggregateHttpServerValidator extends WizardValidator<AddAggregateHttpServerWizard> {

    public AddAggregateHttpServerValidator(ConfigFragment fragment) {
        this(Arrays.asList(AggregateHttpServerInput.values()), fragment);
    }

    public AddAggregateHttpServerValidator(Collection<WizardInput<AddAggregateHttpServerWizard>> wizardInputs, ConfigFragment fragment) {
        super(wizardInputs, fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddAggregateHttpServerWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Collections.singleton(AggregateHttpServerInput.NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(AggregateHttpServerInput.HTTP_SERVER_MECHANISM_FACTORIES),
                wizard);
    }
}
