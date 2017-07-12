package org.jboss.hal.testsuite.test.configuration.remoting.endpointconfiguration;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;


@RunWith(Arquillian.class)
@RunAsClient
public class EndpointConfigurationSecurityTestCase extends EndpointConfigurationTestCaseAbstract {

    private final String AUTH_REALM = "auth-realm",
            AUTHENTICATION_RETRIES = "authentication-retries",
            AUTHORIZE_ID = "authorize-id",
            SASL_PROTOCOL = "sasl-protocol";

    @Before
    public void switchToTab() {
        page.switchToEndpointSecurity();
    }

    @Test
    public void editAuthRealm() throws Exception {
        final String authRealm = "my-realm";
        final ModelNodeResult originalAuthRealmNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTH_REALM);
        try {
            enterTextAndVerify(AUTH_REALM, authRealm);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTH_REALM, originalAuthRealmNodeResult.value());
        }
    }

    @Test
    public void editAuthenticationRetries() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTHENTICATION_RETRIES);
        try {
            enterTextAndVerify(AUTHENTICATION_RETRIES, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTHENTICATION_RETRIES, originalModelNodeResult.value());
        }
    }

    @Test
    public void editAuthorizeId() throws Exception {
        final String authorizeId = "authorize_id_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalAuthorizeIdModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTHORIZE_ID);
        try {
            enterTextAndVerify(AUTHORIZE_ID, authorizeId);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, AUTHORIZE_ID, originalAuthorizeIdModelNodeResult.value());
        }
    }

    @Test
    public void editSASLProtocol() throws Exception {
        final String value = "random_sasl_protocol_" + RandomStringUtils.randomAlphanumeric(7);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SASL_PROTOCOL);
        try {
            enterTextAndVerify(SASL_PROTOCOL, value);
        } finally {
            operations.writeAttribute(ENDPOINT_CONFIGURATION_ADDRESS, SASL_PROTOCOL, originalModelNodeResult.value());
        }
    }

}
