package org.jboss.hal.testsuite.test.configuration.elytron.other;

import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;

public class AllPermissionPolicyProvider extends Policy {
    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
        return domain.implies(permission);
    }
}
