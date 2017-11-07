package org.jboss.hal.testsuite.test.configuration.elytron.other.ssl;

import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.util.ModelNodeSerializable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class representing New Item Attribute for {@link NewItemTemplate}
 */
public final class NewItemAttribute implements ModelNodeSerializable {

    private static final String NAME = "name";
    private static final String VALUE = "value";

    private String key;
    private List<String> value;

    public NewItemAttribute(String key, List<String> value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public ModelNode toModelNode() {
        return new ModelNodeGenerator.ModelNodePropertiesBuilder()
                .addProperty(NAME, key)
                .addProperty(VALUE, new ModelNodeGenerator.ModelNodeListBuilder()
                .addAll((String[]) value.toArray()).build()).build();
    }

    @Override
    public String toString() {
        return String.format("%s=%s", key, value.stream().collect(Collectors.joining(",")));
    }
}
