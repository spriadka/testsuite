package org.jboss.hal.testsuite.test.configuration.elytron.factory;

public enum MechanismConfigurationAttribute implements IndexedAttribute<String> {
    MECHANISM_NAME("mechanism-name", 0),
    HOST_NAME("host-name", 1),
    PROTOCOL("protocol", 2),
    PRE_REALM_PRINCIPAL_TRANSFORMER("pre-realm-principal-transformer", 3),
    POST_REALM_PRINCIPAL_TRANSFORMER("post-realm-principal-transformer", 4),
    FINAL_PRINCIPAL_TRANSFORMER("final-principal-transformer", 5),
    REALM_MAPPER("realm-mapper", 6),
    CREDENTIAL_SECURITY_FACTORY("credential-security-factory", 7);

    int index;
    String key;
    String value;

    MechanismConfigurationAttribute(String key, int index) {
        this.key = key;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
