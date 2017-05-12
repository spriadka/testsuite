package org.jboss.hal.testsuite.test.configuration.elytron.role.decoder;

import java.util.Arrays;
import java.util.Iterator;

import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.RoleDecoder;
import org.wildfly.security.authz.Roles;

public class BugsBunnyCustomRoleDecoder implements RoleDecoder {

    private static final String BUGSBUNNY = "Bugs Bunny";

    @Override
    public Roles decodeRoles(AuthorizationIdentity authorizationIdentity) {
        return new Roles() {

            @Override
            public Iterator<String> iterator() {
                String[] bugsBunnyArray = {BUGSBUNNY};
                return Arrays.stream(bugsBunnyArray).iterator();
            }

            @Override
            public boolean contains(String roleName) {
                return BUGSBUNNY.equals(roleName);
            }
        };
    }

}
