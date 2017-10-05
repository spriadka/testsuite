package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MechanismConfiguration {
    private SortedSet<IndexedAttribute<String>> attributes = new TreeSet<>(Comparator.comparingInt(IndexedAttribute::getIndex));

    public MechanismConfiguration mechanismName(String value) {
        MechanismConfigurationAttribute mechanismNameAttribute = MechanismConfigurationAttribute.MECHANISM_NAME;
        mechanismNameAttribute.setValue(value);
        this.attributes.add(mechanismNameAttribute);
        return this;
    }


    public MechanismConfiguration hostName(String value) {
        MechanismConfigurationAttribute hostNameAttribute = MechanismConfigurationAttribute.HOST_NAME;
        hostNameAttribute.setValue(value);
        this.attributes.add(hostNameAttribute);
        return this;
    }

    public MechanismConfiguration protocol(String value) {
        MechanismConfigurationAttribute protocolAttribute = MechanismConfigurationAttribute.PROTOCOL;
        protocolAttribute.setValue(value);
        this.attributes.add(protocolAttribute);
        return this;
    }

    public MechanismConfiguration preRealmPrincipalTransformer(String value) {
        MechanismConfigurationAttribute preRealmPrincipalTransformerAttribute
                = MechanismConfigurationAttribute.PRE_REALM_PRINCIPAL_TRANSFORMER;
        preRealmPrincipalTransformerAttribute.setValue(value);
        this.attributes.add(preRealmPrincipalTransformerAttribute);
        return this;
    }

    public MechanismConfiguration postRealmPrincipalTransformer(String value) {
        MechanismConfigurationAttribute postRealmPrincipalTransformerAttribute
                = MechanismConfigurationAttribute.POST_REALM_PRINCIPAL_TRANSFORMER;
        postRealmPrincipalTransformerAttribute.setValue(value);
        this.attributes.add(postRealmPrincipalTransformerAttribute);
        return this;
    }

    public MechanismConfiguration finalPrincipalTransformer(String value) {
        MechanismConfigurationAttribute finalPrincipalTransformerAttribute = MechanismConfigurationAttribute.FINAL_PRINCIPAL_TRANSFORMER;
        finalPrincipalTransformerAttribute.setValue(value);
        this.attributes.add(finalPrincipalTransformerAttribute);
        return this;
    }

    public MechanismConfiguration realmMapper(String value) {
        MechanismConfigurationAttribute realmMapperAttribute = MechanismConfigurationAttribute.REALM_MAPPER;
        realmMapperAttribute.setValue(value);
        this.attributes.add(realmMapperAttribute);
        return this;
    }

    public MechanismConfiguration credentialSecurityFactory(String value) {
        MechanismConfigurationAttribute credentialSecurityAttribute = MechanismConfigurationAttribute.CREDENTIAL_SECURITY_FACTORY;
        credentialSecurityAttribute.setValue(value);
        this.attributes.add(credentialSecurityAttribute);
        return this;
    }

    @Override
    public String toString() {
        return attributes.stream().map(attribute -> String.format("%s: %s", attribute.getKey(), attribute.getValue()))
                .collect(Collectors.joining(", "));
    }
}
