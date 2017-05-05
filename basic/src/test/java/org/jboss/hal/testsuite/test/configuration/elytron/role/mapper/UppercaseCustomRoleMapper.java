package org.jboss.hal.testsuite.test.configuration.elytron.role.mapper;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.wildfly.security.authz.RoleMapper;
import org.wildfly.security.authz.Roles;

public class UppercaseCustomRoleMapper implements RoleMapper {

    @Override
    public Roles mapRoles(Roles originalRoles) {
        return new Roles() {

            private Stream<String> roleStream = StreamSupport.stream(originalRoles.spliterator(), false)
                    .map((String originalRoleName) -> originalRoleName.toUpperCase());

            @Override
            public Iterator<String> iterator() {
                return roleStream.iterator();
            }

            @Override
            public boolean contains(String roleName) {
                return roleStream.anyMatch(roleName::equals);
            }
        };
    }

}
