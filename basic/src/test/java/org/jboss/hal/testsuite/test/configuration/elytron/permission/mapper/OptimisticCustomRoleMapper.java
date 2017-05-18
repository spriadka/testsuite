package org.jboss.hal.testsuite.test.configuration.elytron.permission.mapper;

import org.wildfly.security.authz.PermissionMappable;
import org.wildfly.security.authz.PermissionMapper;
import org.wildfly.security.authz.Roles;
import org.wildfly.security.permission.PermissionVerifier;

/**
 * Custom implementation of {@link PermissionMapper} for test purposes
 */
public class OptimisticCustomRoleMapper implements PermissionMapper {

    @Override
    public PermissionVerifier mapPermissions(PermissionMappable arg0, Roles arg1) {
        return (permission) -> true;
    }

}
