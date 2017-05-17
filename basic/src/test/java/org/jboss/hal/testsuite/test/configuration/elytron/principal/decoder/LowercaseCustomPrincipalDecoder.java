package org.jboss.hal.testsuite.test.configuration.elytron.principal.decoder;

import java.security.Principal;

import org.wildfly.security.auth.server.PrincipalDecoder;

public class LowercaseCustomPrincipalDecoder implements PrincipalDecoder {

    @Override
    public String getName(Principal principal) {
        return principal.getName().toLowerCase();
    }

}
