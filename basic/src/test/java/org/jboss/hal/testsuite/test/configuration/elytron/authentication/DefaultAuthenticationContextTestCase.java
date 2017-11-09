package org.jboss.hal.testsuite.test.configuration.elytron.authentication;

import static org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import static org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.page.config.elytron.ElytronAuthenticationPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.elytron.ElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class DefaultAuthenticationContextTestCase extends AbstractElytronTestCase {

    @Page
    private ElytronAuthenticationPage page;

    private final ElytronAuthenticationOperations elytronAuthenticationOperations = new ElytronAuthenticationOperations(client);

    private static final String
        AUTHENTICATION_CONTEXT = "authentication-context",
        DEFAULT_AUTHENTICATION_CONTEXT = "default-authentication-context",
        FINAL_PROVIDERS = "final-providers",
        INITIAL_PROVIDERS = "initial-providers",
        DISALLOWED_PROVIDERS = "disallowed-providers",
        SECURITY_PROPERTIES = "security-properties";

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

    /**
     * @tpTestDetails Try to edit disallowed-providers attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editDisallowedProviders() throws Exception {
        final String
            providerLoader1 = RandomStringUtils.randomAlphanumeric(7),
            providerLoader2 = RandomStringUtils.randomAlphanumeric(7),
            providersString = providerLoader1 + "\n" + providerLoader2;
        final ModelNode providersNode = new ModelNodeListBuilder().addAll(providerLoader1, providerLoader2).build();

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    ElytronOperations.getElytronSubsystemAddress(),
                    DISALLOWED_PROVIDERS,
                    () -> {
                        elyOps.addProviderLoader(providerLoader1);
                        elyOps.addProviderLoader(providerLoader2);
                        page.navigate();
                        new ConfigChecker.Builder(client, ElytronOperations.getElytronSubsystemAddress())
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, DISALLOWED_PROVIDERS, providersString)
                                .verifyFormSaved()
                                .verifyAttribute(DISALLOWED_PROVIDERS, providersNode);
                    }
            );
        } finally {
            elyOps.removeProviderLoader(providerLoader1);
            elyOps.removeProviderLoader(providerLoader2);
        }
    }

    /**
     * @tpTestDetails Try to edit security-properties attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editSecurityProperties() throws Exception {
        final String
            key1 = RandomStringUtils.randomAlphanumeric(7),
            key2 = RandomStringUtils.randomAlphanumeric(7),
            value1 = RandomStringUtils.randomAlphanumeric(7),
            value2 = RandomStringUtils.randomAlphanumeric(7),
            securityPropertiesString = key1 + "=" + value1 + "\n" + key2 + "=" + value2;
        final ModelNode expectedSecurityPropertiesNode = new ModelNodePropertiesBuilder()
                .addProperty(key1, value1)
                .addProperty(key2, value2)
                .build();

        elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                ElytronOperations.getElytronSubsystemAddress(), SECURITY_PROPERTIES, () -> {
                    page.navigate();
                    new ConfigChecker.Builder(client, ElytronOperations.getElytronSubsystemAddress())
                            .configFragment(page.getConfigFragment())
                            .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_PROPERTIES, securityPropertiesString)
                            .verifyFormSaved().verifyAttribute(SECURITY_PROPERTIES, expectedSecurityPropertiesNode);
                });
    }

    private Address createAuthenticationContext() throws IOException {
        final Address address = elyOps.getElytronAddress(AUTHENTICATION_CONTEXT, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address).assertSuccess();
        return address;
    }

}
