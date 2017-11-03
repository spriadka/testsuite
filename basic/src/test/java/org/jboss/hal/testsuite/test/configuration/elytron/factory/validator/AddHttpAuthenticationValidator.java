package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddHttpAuthenticationWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.HttpAuthenticationInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddHttpAuthenticationValidator extends WizardValidator<AddHttpAuthenticationWizard> {
    public AddHttpAuthenticationValidator(Collection<WizardInput<AddHttpAuthenticationWizard>> mandatoryInputs, ConfigFragment fragment) {
        super(mandatoryInputs, fragment);
    }

    public AddHttpAuthenticationValidator(ConfigFragment fragment) {
        this(Arrays.asList(HttpAuthenticationInput.values()), fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddHttpAuthenticationWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Collections.singleton(HttpAuthenticationInput.NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(HttpAuthenticationInput.HTTP_SERVER_MECHANISM_FACTORY),
                wizard);
        fillFieldsAndAssert(Collections.singleton(HttpAuthenticationInput.SECURITY_DOMAIN), wizard);
        fillFieldsAndAssert(Arrays.asList(HttpAuthenticationInput.NAME,
                HttpAuthenticationInput.HTTP_SERVER_MECHANISM_FACTORY), wizard);
        fillFieldsAndAssert(Arrays.asList(HttpAuthenticationInput.NAME,
                HttpAuthenticationInput.SECURITY_DOMAIN), wizard);
        fillFieldsAndAssert(Arrays.asList(HttpAuthenticationInput.SECURITY_DOMAIN,
                HttpAuthenticationInput.HTTP_SERVER_MECHANISM_FACTORY), wizard);
    }
}
