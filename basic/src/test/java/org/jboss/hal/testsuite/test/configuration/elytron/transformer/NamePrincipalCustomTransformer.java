package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import java.security.Principal;

import org.wildfly.extension.elytron.capabilities.PrincipalTransformer;
import org.wildfly.security.auth.principal.NamePrincipal;

/**
 * Custom {@link PrincipalTransformer} implementation for testing purposes
 */
public class NamePrincipalCustomTransformer implements PrincipalTransformer {

    @Override
    public Principal apply(Principal t) {
        return new NamePrincipal(t.getName());
    }

}
