package org.jboss.hal.testsuite.test.util;

import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.util.ResourceVerifier;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class ConfigFragmentUtils {

    private ResourceVerifier verifier;

    public ConfigFragmentUtils(ResourceVerifier verifier) {
        this.verifier = verifier;
    }

    public void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expectedChange) {
        changeTextAndAssert(fragment, identifier, value, expectedChange, identifier);
    }

    public void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expectedChange) {
        changeCheckboxAndAssert(fragment, identifier, value, expectedChange, identifier);
    }

    public void changeTextAndAssert(ConfigFragment fragment, String rowName, String identifier, String value, boolean expectedChange) {
        changeTextAndAssert(fragment, rowName, identifier, value, expectedChange, identifier);
    }

    public void changeCheckboxAndAssert(ConfigFragment fragment, String rowName, String identifier, boolean value, boolean expectedChange) {
        changeCheckboxAndAssert(fragment, rowName, identifier, value, expectedChange, identifier);
    }

    public void changeTextAndAssert(ConfigFragment fragment, String rowName, String identifier, String value, boolean expectedChange, String dmrAttribute) {
        fragment.getResourceManager().selectByName(rowName);
        changeTextAndAssert(fragment, identifier, value, expectedChange, dmrAttribute);
    }

    public void changeCheckboxAndAssert(ConfigFragment fragment, String rowName, String identifier, boolean value, boolean expectedChange, String dmrAttribute) {
        fragment.getResourceManager().selectByName(rowName);
        changeCheckboxAndAssert(fragment, identifier, value, expectedChange, dmrAttribute);
    }

    /**
     * Edits value of text element identified by param identifier and verifies changes in gui and cli
     *
     * @param fragment
     * @param identifier
     * @param value
     * @param expectedChange
     * @param dmrAttribute
     */
    public void changeTextAndAssert(ConfigFragment fragment, String identifier, String value, boolean expectedChange, String dmrAttribute) {
        Editor edit = fragment.edit();
        edit.text(identifier, value);
        fragment.saveAndAssert(expectedChange);
        if (expectedChange == true) {
            verifier.verifyAttribute(dmrAttribute, value);
        }else{
            fragment.cancel();
        }
    }

    /**
     * Edits value of checkbox element identified by param identifier and verifies changes in gui and cli
     *
     * @param fragment
     * @param identifier
     * @param value
     * @param expectedChange
     * @param dmrAttribute
     */
    public void changeCheckboxAndAssert(ConfigFragment fragment, String identifier, boolean value, boolean expectedChange, String dmrAttribute) {
        Editor edit = fragment.edit();
        edit.checkbox(identifier, value);
        fragment.saveAndAssert(expectedChange);
        if (expectedChange == true) {
            verifier.verifyAttribute(dmrAttribute, String.valueOf(value));
        }else{
            fragment.cancel();
        }
    }
}
