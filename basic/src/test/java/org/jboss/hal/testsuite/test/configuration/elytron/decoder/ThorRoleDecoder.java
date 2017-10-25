package org.jboss.hal.testsuite.test.configuration.elytron.decoder;

import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.RoleDecoder;
import org.wildfly.security.authz.Roles;

import java.util.Arrays;
import java.util.Iterator;

public class ThorRoleDecoder implements RoleDecoder {

    private static final String[] ASGARD = {"Thor", "Loki"};

    @Override
    public Roles decodeRoles(AuthorizationIdentity authorizationIdentity) {
        return new Roles() {
            @Override
            public boolean contains(String roleName) {
                return Arrays.asList(ASGARD).contains(roleName);
            }

            @Override
            public Iterator<String> iterator() {
                return Arrays.asList(ASGARD).iterator();
            }
        };
    }
}
