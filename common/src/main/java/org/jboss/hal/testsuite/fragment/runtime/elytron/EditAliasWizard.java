package org.jboss.hal.testsuite.fragment.runtime.elytron;

public class EditAliasWizard extends AbstractAliasWizard {

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
