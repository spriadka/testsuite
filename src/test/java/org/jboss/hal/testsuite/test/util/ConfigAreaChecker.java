package org.jboss.hal.testsuite.test.util;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Assert;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class ConfigAreaChecker {

    private ResourceVerifier verifier;

    public ConfigAreaChecker(ResourceVerifier verifier) {
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
        private int timeout = 0;

        List<String> inputsToClear = new ArrayList<>();
        private boolean expectedChange = true;

        private String rowName;
        private String tab;
        private String dmrAttribute;
        private ResourceVerifier insideVerifier;
        private String disclosureLabel;

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

        public Builder withVerifier(ResourceVerifier verifier) {
            this.insideVerifier = verifier;
            return this;
        }

        public Builder withTimeout(int timeout) {
            this.timeout = timeout;
            return this;
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

        public Builder expectError() {
            this.expectedChange = false;
            return this;
        }

        public Builder clear(String input) {
            inputsToClear.add(input);
            return this;
        }

        public Builder disclose(String label) {
            this.disclosureLabel = label;
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
            if (disclosureLabel != null) {
                By disclosure = ByJQuery.selector("a.header:has(td:contains('" + disclosureLabel + "'):visible)");
                fragment.getRoot().findElement(disclosure).click();
            }
            for (String i : inputsToClear) {
                edit.text(i, "");
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
            boolean finished = fragment.save();
            if (expectedChange) {
                Assert.assertTrue("Config was supposed to be saved successfully, read view should be active.", finished);
            } else {
                Assert.assertFalse("Config wasn't supposed to be saved, read-write view should be active.", finished);
            }
            if (expectedChange) {
                if (this.insideVerifier == null) {
                    this.insideVerifier = verifier;
                }
                switch (type) {
                    case TEXTAREA:
                        insideVerifier.verifyAttribute(dmrAttribute, lineValues);
                        break;
                    case SELECT:
                    case TEXT:
                        insideVerifier.verifyAttribute(dmrAttribute, stringValue, timeout);
                        break;
                    case CHECKBOX:
                        insideVerifier.verifyAttribute(dmrAttribute, String.valueOf(booleanValue));
                        break;
                }
            } else {
                fragment.cancel();
            }
        }
    }
}
