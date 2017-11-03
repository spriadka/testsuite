package org.jboss.hal.testsuite.test.configuration.elytron.factory.validator;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddSaslAuthenticationWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.input.SaslAuthenticationInput;
import org.jboss.hal.testsuite.util.WizardValidator;
import org.jboss.hal.testsuite.util.configurationchanges.WizardInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AddSaslAuthenticationValidator extends WizardValidator<AddSaslAuthenticationWizard> {

    public AddSaslAuthenticationValidator(Collection<WizardInput<AddSaslAuthenticationWizard>> mandatoryInputs, ConfigFragment fragment) {
        super(mandatoryInputs, fragment);
    }

    public AddSaslAuthenticationValidator(ConfigFragment fragment) {
        this(Arrays.asList(SaslAuthenticationInput.values()), fragment);
    }

    @Override
    public void testInvalidCombinationsAndAssert(AddSaslAuthenticationWizard wizard) {
        fillFieldsAndAssert(Collections.emptySet(), wizard);
        fillFieldsAndAssert(Collections.singleton(SaslAuthenticationInput.NAME), wizard);
        fillFieldsAndAssert(Collections.singleton(SaslAuthenticationInput.SASL_SERVER_FACTORY), wizard);
        fillFieldsAndAssert(Collections.singleton(SaslAuthenticationInput.SECURITY_DOMAIN), wizard);
        fillFieldsAndAssert(Arrays.asList(SaslAuthenticationInput.NAME
                , SaslAuthenticationInput.SASL_SERVER_FACTORY), wizard);
        fillFieldsAndAssert(Arrays.asList(SaslAuthenticationInput.NAME,
                SaslAuthenticationInput.SECURITY_DOMAIN), wizard);
        fillFieldsAndAssert(Arrays.asList(SaslAuthenticationInput.SECURITY_DOMAIN
                , SaslAuthenticationInput.SASL_SERVER_FACTORY), wizard);
    }
}
