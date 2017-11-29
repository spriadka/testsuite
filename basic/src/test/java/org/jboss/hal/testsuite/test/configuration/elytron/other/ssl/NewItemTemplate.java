package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.util.ModelNodeSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing New Item Template for LDAP Key Store
 */
public final class NewItemTemplate implements ModelNodeSerializable {

    private static final String NEW_ITEM_PATH = "new-item-path";
    private static final String NEW_ITEM_RDN = "new-item-rdn";
    private static final String NEW_ITEM_ATTRIBUTES = "new-item-attributes";

    private String path;
    private String rdn;
    private List<NewItemAttribute> attributes;

    private NewItemTemplate(Builder builder) {
        this.path = builder.path;
        this.rdn = builder.rdn;
        this.attributes = builder.attributes;
    }

    public static class Builder {
        private String path;
        private String rdn;
        private List<NewItemAttribute> attributes = new ArrayList<>();

        public Builder path(String value) {
            this.path = value;
            return this;
        }

        public Builder rdn(String value) {
            this.rdn = value;
            return this;
        }

        public Builder addAttribute(String key, List<String> value) {
            this.attributes.add(new NewItemAttribute(key, value));
            return this;
        }

        public Builder addAttribute(NewItemAttribute attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public Builder addAttributes(List<NewItemAttribute> attributes) {
            this.attributes.addAll(attributes);
            return this;
        }

        public NewItemTemplate build() {
            return new NewItemTemplate(this);
        }
    }

    public String getPath() {
        return path;
    }

    public String getRdn() {
        return rdn;
    }

    public List<NewItemAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public ModelNode toModelNode() {
        ModelNodeGenerator.ModelNodePropertiesBuilder builder =  new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NEW_ITEM_PATH, path)
                .addProperty(NEW_ITEM_RDN, rdn);
        ModelNodeGenerator.ModelNodeListBuilder attributesBuilder = new ModelNodeGenerator.ModelNodeListBuilder();
        attributes.forEach(attribute -> attributesBuilder.addNode(attribute.toModelNode()));
        builder.addProperty(NEW_ITEM_ATTRIBUTES, attributesBuilder.build());
        return builder.build();
    }
}
