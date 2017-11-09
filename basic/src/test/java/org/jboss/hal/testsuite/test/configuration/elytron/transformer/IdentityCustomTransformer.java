package org.jboss.hal.testsuite.test.configuration.elytron.transformer;

import java.security.Principal;

import org.wildfly.extension.elytron.capabilities.PrincipalTransformer;

/**
 * Custom {@link PrincipalTransformer} implementation for testing purposes
 */
public class IdentityCustomTransformer implements PrincipalTransformer {

    @Override
    public Principal apply(Principal t) {
        return t;
    }

}
