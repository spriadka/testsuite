package org.jboss.hal.testsuite.test.util;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.ResourceVerifier;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class ConfigAreaUtils {

    private ResourceVerifier verifier;

    public ConfigAreaUtils(ResourceVerifier verifier) {
        this.verifier = verifier;
    }

    public Builder editTextAndAssert(ConfigPage page, String identifier, String value) {
        return new Builder(page, EditorType.TEXT, identifier, value);
    }

    public Builder editTextAndAssert(ConfigPage page, String identifier, String[] values) {
        return new Builder(page, EditorType.TEXTAREA, identifier, values);
    }

    public Builder editSelectAndAssert(ConfigPage page, String identifier, String value) {
        return new Builder(page, EditorType.SELECT, identifier, value);
    }

    public Builder editCheckboxAndAssert(ConfigPage page, String identifier, boolean value) {
        return new Builder(page, EditorType.CHECKBOX, identifier, value);
    }

    private enum EditorType {
        SELECT,
        TEXT,
        CHECKBOX,
        TEXTAREA
    }

    public class Builder {
        private ConfigPage page;
        private String identifier;
        private String stringValue;
        private boolean booleanValue;
        private String[] lineValues;
        private EditorType type;

        private boolean defineFirst = false;
        private boolean expectedChange = true;

        private String defineIdentifier;
        private String rowName;
        private String tab;
        private String dmrAttribute;

        private Builder(ConfigPage page, EditorType type, String identifier, String value) {
            this.page = page;
            this.type = type;
            this.identifier = identifier;
            this.stringValue = value;
        }

        private Builder(ConfigPage page, EditorType type, String identifier, boolean value) {
            this.page = page;
            this.type = type;
            this.identifier = identifier;
            this.booleanValue = value;
        }

        private Builder(ConfigPage page, EditorType type, String identifier, String[] value) {
            this.page = page;
            this.type = type;
            this.identifier = identifier;
            this.lineValues = value;
        }

        public Builder rowName(String rowName) {
            this.rowName = rowName;
            return this;
        }

        public Builder tab(String tab) {
            this.tab = tab;
            return this;
        }

        public Builder dmrAttribute(String dmrAttribute) {
            this.dmrAttribute = dmrAttribute;
            return this;
        }

        public Builder defineFirst(String defineIdentifier) {
            this.defineFirst = true;
            this.defineIdentifier = defineIdentifier;
            return this;
        }

        public Builder expectError() {
            this.expectedChange = false;
            return this;
        }

        public void invoke() {
            if (dmrAttribute == null) dmrAttribute = identifier;
            if (rowName != null) page.getResourceManager().selectByName(rowName);
            ConfigAreaFragment area = page.getConfig();
            ConfigFragment fragment;
            if (tab != null) {
                area.clickTabByLabel(tab);
            }
            fragment = Graphene.createPageFragment(ConfigFragment.class, area.getRoot());
            Editor edit = fragment.edit();
            if (defineFirst) {
                edit.checkbox(defineIdentifier, true);
            }
            switch (type) {
                case TEXTAREA:
                    edit.text(identifier, String.join("\n", lineValues));
                    break;
                case TEXT:
                    edit.text(identifier, stringValue);
                    break;
                case CHECKBOX:
                    edit.checkbox(identifier, booleanValue);
                    break;
                case SELECT:
                    edit.select(identifier, stringValue);
                    break;
            }
            fragment.saveAndAssert(expectedChange);
            if (expectedChange) {
                switch (type) {
                    case TEXTAREA:
                        verifier.verifyAttribute(dmrAttribute, lineValues);
                        break;
                    case SELECT:
                    case TEXT:
                        verifier.verifyAttribute(dmrAttribute, stringValue);
                        break;
                    case CHECKBOX:
                        verifier.verifyAttribute(dmrAttribute, String.valueOf(booleanValue));
                        break;
                }
            } else {
                fragment.cancel();
            }
        }
    }
}
