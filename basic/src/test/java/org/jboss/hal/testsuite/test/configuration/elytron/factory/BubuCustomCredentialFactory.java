package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import java.security.GeneralSecurityException;

import org.wildfly.extension.elytron.capabilities.CredentialSecurityFactory;
import org.wildfly.security.credential.BearerTokenCredential;
import org.wildfly.security.credential.Credential;

/**
 * Custom implementation of {@link CredentialSecurityFactory} for test purposes
 */
public class BubuCustomCredentialFactory implements CredentialSecurityFactory {

    private static final String BUBU = "Bubu";

    @Override
    public Credential create() throws GeneralSecurityException {
        return new BearerTokenCredential(BUBU);
    }

}
