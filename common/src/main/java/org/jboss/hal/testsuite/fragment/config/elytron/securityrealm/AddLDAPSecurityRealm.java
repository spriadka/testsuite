package org.jboss.hal.testsuite.fragment.config.elytron.securityrealm;

public class AddLDAPSecurityRealm extends AbstractAddSecurityRealmWizard<AddLDAPSecurityRealm> {

    public AddLDAPSecurityRealm dirContext(String dirContext) {
        getEditor().text("dir-context", dirContext);
        return this;
    }

}
