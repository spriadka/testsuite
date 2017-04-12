package org.jboss.hal.testsuite.test.configuration.elytron.securityrealm.custommodule;

import org.wildfly.security.auth.server.CloseableIterator;
import org.wildfly.security.auth.server.IdentityLocator;
import org.wildfly.security.auth.server.ModifiableRealmIdentity;
import org.wildfly.security.auth.server.ModifiableSecurityRealm;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SupportLevel;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.Evidence;

public class CustomSecurityRealm implements ModifiableSecurityRealm {

    @Override
    public ModifiableRealmIdentity getRealmIdentityForUpdate(IdentityLocator identityLocator) throws RealmUnavailableException {
        return null;
    }

    @Override
    public CloseableIterator<ModifiableRealmIdentity> getRealmIdentityIterator() throws RealmUnavailableException {
        return null;
    }

    @Override
    public RealmIdentity getRealmIdentity(IdentityLocator identityLocator) throws RealmUnavailableException {
        return null;
    }

    @Override
    public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> aClass, String s) throws RealmUnavailableException {
        return null;
    }

    @Override
    public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> aClass, String s) throws RealmUnavailableException {
        return null;
    }
}
