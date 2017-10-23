package org.jboss.hal.testsuite.test.configuration.elytron.other;
import org.jboss.security.jacc.JBossPolicyConfigurationFactory;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

public class CustomPolicyConfigurationProvider extends PolicyConfigurationFactory {
    @Override
    public PolicyConfiguration getPolicyConfiguration(String s, boolean b) throws PolicyContextException {
        return new JBossPolicyConfigurationFactory().getPolicyConfiguration(s, b);
    }

    @Override
    public boolean inService(String s) throws PolicyContextException {
        return true;
    }
}
