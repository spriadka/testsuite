package org.jboss.hal.testsuite.test.configuration.elytron.principal.decoder;

import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.RoleDecoder;
import org.wildfly.security.authz.Roles;

import java.util.Arrays;
import java.util.Iterator;

public class RandomizedRoleDecoder implements RoleDecoder {

    private static final String RANDOM = "Random";

    @Override
    public Roles decodeRoles(AuthorizationIdentity authorizationIdentity) {
        return new Roles() {
            @Override
            public boolean contains(String roleName) {
                return RANDOM.contains(roleName);
            }

            @Override
            public Iterator<String> iterator() {
                String[] randomizedStrings = {RANDOM};
                return Arrays.asList(randomizedStrings).iterator();
            }
        };
    }
}
