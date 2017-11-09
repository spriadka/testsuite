package org.jboss.hal.testsuite.test.configuration.elytron.role.decoder;

import java.util.Arrays;
import java.util.Iterator;
import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.RoleDecoder;
import org.wildfly.security.authz.Roles;

/**
 * Custom implementation of {@link RoleDecoder} for test purposes
 */
public class SuperMarioCustomRoleDecoder implements RoleDecoder {

    private static final String SUPERMARIO = "Super Mario";

    @Override
    public Roles decodeRoles(AuthorizationIdentity authorizationIdentity) {
        return new Roles() {

            @Override
            public Iterator<String> iterator() {
                String[] superMarioArray = {SUPERMARIO};
                return Arrays.stream(superMarioArray).iterator();
            }

            @Override
            public boolean contains(String roleName) {
                return SUPERMARIO.equals(roleName);
            }
        };
    }

}
