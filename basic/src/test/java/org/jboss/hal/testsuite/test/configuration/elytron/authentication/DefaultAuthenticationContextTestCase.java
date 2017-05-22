package org.jboss.hal.testsuite.test.configuration.elytron.authentication;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.page.config.elytron.ElytronAuthenticationPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;

@RunWith(Arquillian.class)
public class DefaultAuthenticationContextTestCase extends AbstractElytronTestCase {

    @Page
    private ElytronAuthenticationPage page;

    private final ElytronAuthenticationOperations elytronAuthenticationOperations = new ElytronAuthenticationOperations(client);

    private static final String
        AUTHENTICATION_CONTEXT = "authentication-context",
        DEFAULT_AUTHENTICATION_CONTEXT = "default-authentication-context",
        FINAL_PROVIDERS = "final-providers",
        INITIAL_PROVIDERS = "initial-providers";

    /**
     * @tpTestDetails Try to edit default-authentication-context attribute value in Web Console's Elytron subsystem
     * configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editDefaultAuthenticationContext() throws Exception {
        final Address authenticationContextAddress = createAuthenticationContext();

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    ElytronOperations.getElytronSubsystemAddress(),
                    DEFAULT_AUTHENTICATION_CONTEXT,
                    () -> {
                        page.navigate();
                        new ConfigChecker.Builder(client, ElytronOperations.getElytronSubsystemAddress())
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, DEFAULT_AUTHENTICATION_CONTEXT, authenticationContextAddress.getLastPairValue())
                                .verifyFormSaved()
                                .verifyAttribute(DEFAULT_AUTHENTICATION_CONTEXT, authenticationContextAddress.getLastPairValue());
                    }
            );
        } finally {
            ops.removeIfExists(authenticationContextAddress);
        }
    }

    /**
     * @tpTestDetails Try to edit final-providers attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editFinalProviders() throws Exception {
        final String providerLoader = RandomStringUtils.randomAlphanumeric(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    ElytronOperations.getElytronSubsystemAddress(),
                    FINAL_PROVIDERS,
                    () -> {
                        elyOps.addProviderLoader(providerLoader);
                        page.navigate();
                        new ConfigChecker.Builder(client, ElytronOperations.getElytronSubsystemAddress())
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, FINAL_PROVIDERS, providerLoader)
                                .verifyFormSaved()
                                .verifyAttribute(FINAL_PROVIDERS, providerLoader);
                    }
            );
        } finally {
            elyOps.removeProviderLoader(providerLoader);
        }
    }

    /**
     * @tpTestDetails Try to edit initial-providers attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editInitialProviders() throws Exception {
        final String providerLoader = RandomStringUtils.randomAlphanumeric(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    ElytronOperations.getElytronSubsystemAddress(),
                    INITIAL_PROVIDERS,
                    () -> {
                        elyOps.addProviderLoader(providerLoader);
                        page.navigate();
                        new ConfigChecker.Builder(client, ElytronOperations.getElytronSubsystemAddress())
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, INITIAL_PROVIDERS, providerLoader)
                                .verifyFormSaved()
                                .verifyAttribute(INITIAL_PROVIDERS, providerLoader);
                    }
            );
        } finally {
            elyOps.removeProviderLoader(providerLoader);
        }
    }

    private Address createAuthenticationContext() throws IOException {
        final Address address = elyOps.getElytronAddress(AUTHENTICATION_CONTEXT, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address).assertSuccess();
        return address;
    }

}
