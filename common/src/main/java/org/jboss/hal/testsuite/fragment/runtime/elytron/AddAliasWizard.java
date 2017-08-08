package org.jboss.hal.testsuite.fragment.runtime.elytron;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindowWithOptionalFields;

public class AddAliasWizard extends AbstractAliasWizard {

    public AddAliasWizard alias(String value) {
        getEditor().text(ALIAS, value);
        return this;
    }

    public AbstractAliasWizard secretValue(String value) {
        openOptionalFieldsTabIfNotAlreadyOpened();
        getEditor().text(SECRET_VALUE, value);
        return this;
    }

    public AbstractAliasWizard entryType(String value) {
        openOptionalFieldsTabIfNotAlreadyOpened();
        getEditor().select(ENTRY_TYPE, value);
        return this;
    }
}
