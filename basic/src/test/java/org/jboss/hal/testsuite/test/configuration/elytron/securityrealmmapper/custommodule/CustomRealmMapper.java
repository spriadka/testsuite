package org.jboss.hal.testsuite.test.configuration.elytron.securityrealmmapper.custommodule;

import org.wildfly.security.auth.server.RealmMapper;
import org.wildfly.security.evidence.Evidence;

import java.security.Principal;

/**
 * Dummy impl of {@link RealmMapper} for testing purposes
 */
public class CustomRealmMapper implements RealmMapper {

    @Override
    public String getRealmMapping(String s, Principal principal, Evidence evidence) {
        return null;
    }

}
