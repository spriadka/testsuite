package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddMechanismConfigurationWizard;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddSaslAuthenticationWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddSaslAuthenticationValidator;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class SaslAuthenticationTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String SASL_AUTHENTICATION_LABEL = "SASL Authentication";
    private static final String PROVIDER_SASL_SERVER_FACTORY = "provider-sasl-server-factory";
    private static final String PROVIDER_SASL_SERVER_FACTORY_NAME = "provider_sasl_factory_" + RandomStringUtils.randomAlphanumeric(7);
    private static final Address PROVIDER_SASL_SERVER_FACTORY_ADDRESS = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY,
            PROVIDER_SASL_SERVER_FACTORY_NAME);
    private static final String SASL_AUTHENTICATION = "sasl-authentication-factory";
    private static final String SASL_SERVER_FACTORY = "sasl-server-factory";
    private static final String SECURITY_DOMAIN = "security-domain";

    private static final String ATTRIBUTES_LABEL = "Attributes";
    private static final String MECHANISM_CONFIGURATIONS_LABEL = "Mechanism Configurations";
    private static final String MECHANISM_CONFIGURATIONS = "mechanism-configurations";
    private static final String MECHANISM_NAME = "mechanism-name";
    private static final String HOST_NAME = "host-name";
    private static final String PROTOCOL = "protocol";

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_2);
        ops.add(PROVIDER_SASL_SERVER_FACTORY_ADDRESS);
        adminOps.reloadIfRequired();
    }

    @Test
    public void addSaslAuthenticationTest() throws Exception {
        final String saslAuthenticationName = "sasl_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String securityDomain = "ApplicationDomain";
        final Address saslAuthenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION, saslAuthenticationName);
        try {
            page.navigateToApplication()
                    .selectFactory(SASL_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .addResource(AddSaslAuthenticationWizard.class)
                    .name(saslAuthenticationName)
                    .securityDomain(securityDomain)
                    .saslServerFactory(PROVIDER_SASL_SERVER_FACTORY_NAME)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created SASL authentication should be present in the table",
                    page.getResourceManager().isResourcePresent(saslAuthenticationName));
            new ResourceVerifier(saslAuthenticationAddress, client)
                    .verifyExists()
                    .verifyAttribute(SASL_SERVER_FACTORY, PROVIDER_SASL_SERVER_FACTORY_NAME)
                    .verifyAttribute(SECURITY_DOMAIN, securityDomain);

        } finally {
            ops.removeIfExists(saslAuthenticationAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addSaslAuthenticationMissingFieldsTest() {
        AddSaslAuthenticationWizard wizard = page.navigateToApplication()
                .selectFactory(SASL_AUTHENTICATION_LABEL)
                .getResourceManager()
                .addResource(AddSaslAuthenticationWizard.class);
        new AddSaslAuthenticationValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);
    }

    @Test
    public void removeSaslAuthenticationTest() throws Exception {
        final String saslAuthenticationName = "sasl_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final Address saslAuthenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION, saslAuthenticationName);
        try {
            createSASLAuthenticationInModel(saslAuthenticationAddress);
            page.navigateToApplication()
                    .selectFactory(SASL_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .removeResource(saslAuthenticationName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed SASL authentication should not be present in the table anymore",
                    page.getResourceManager().isResourcePresent(saslAuthenticationName));
            new ResourceVerifier(saslAuthenticationAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(saslAuthenticationAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editAttributestTest() throws Exception {
        final String saslAuthenticationName = "sasl_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String saslAuthenticationFactoryName = "sasl_authentication_factory_" + RandomStringUtils.randomAlphanumeric(7);
        final String securityDomain = "ManagementDomain";
        final Address saslAuthenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION, saslAuthenticationName);
        final Address saslAuthenticationFactoryAddress = elyOps.getElytronAddress(PROVIDER_SASL_SERVER_FACTORY, saslAuthenticationFactoryName);
        try {
            createSASLAuthenticationInModel(saslAuthenticationAddress);
            createSASLAuthenticationFactoryInModel(saslAuthenticationFactoryAddress);
            page.navigateToApplication()
                    .selectFactory(SASL_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .selectByName(saslAuthenticationName);
            page.switchToConfigAreaTab(ATTRIBUTES_LABEL);
            new ConfigChecker.Builder(client, saslAuthenticationAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, SASL_SERVER_FACTORY, saslAuthenticationFactoryName)
                    .edit(ConfigChecker.InputType.TEXT, SECURITY_DOMAIN, securityDomain)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(SASL_SERVER_FACTORY, saslAuthenticationFactoryName)
                    .verifyAttribute(SECURITY_DOMAIN, securityDomain);
        } finally {
            ops.removeIfExists(saslAuthenticationAddress);
            ops.removeIfExists(saslAuthenticationFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addMechanismConfigurationTest() throws Exception {
        final String saslAuthenticationName = "sasl_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String mechanismName = "mechanism_" + RandomStringUtils.randomAlphanumeric(7);
        final String hostName = "host_" + RandomStringUtils.randomAlphanumeric(7);
        final String protocol = "prorocol_" + RandomStringUtils.randomAlphanumeric(7);
        final MechanismConfiguration mechanismConfiguration = new MechanismConfiguration().mechanismName(mechanismName)
                .hostName(hostName)
                .protocol(protocol);
        final Address saslAuthenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION, saslAuthenticationName);
        try {
            createSASLAuthenticationInModel(saslAuthenticationAddress);
            page.navigateToApplication()
                    .selectFactory(SASL_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .selectByName(saslAuthenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_LABEL);
            page.getConfigAreaResourceManager()
                    .addResource(AddMechanismConfigurationWizard.class)
                    .hostName(hostName)
                    .mechanismName(mechanismName)
                    .protocol(protocol)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created mechanism configuration should be present in the table",
                    page.getConfigAreaResourceManager()
                            .isResourcePresent(getMechanismConfigurationValueWithTrailingComma(mechanismConfiguration)));
            new ResourceVerifier(saslAuthenticationAddress, client)
                    .verifyAttribute(MECHANISM_CONFIGURATIONS, new ModelNodeGenerator.ModelNodeListBuilder()
                    .addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(MECHANISM_NAME, mechanismName)
                            .addProperty(HOST_NAME, hostName)
                            .addProperty(PROTOCOL, protocol).build()).build());

        } finally {
            ops.removeIfExists(saslAuthenticationAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeMechanismConfiguration() throws Exception {
        final String saslAuthenticationName = "sasl_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String mechanismName = "mechanism_" + RandomStringUtils.randomAlphanumeric(7);
        final String hostName = "host_" + RandomStringUtils.randomAlphanumeric(7);
        final String protocol = "prorocol_" + RandomStringUtils.randomAlphanumeric(7);
        final MechanismConfiguration configuration = new MechanismConfiguration().hostName(hostName)
                .protocol(protocol)
                .mechanismName(mechanismName);
        final Address saslAuthenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION, saslAuthenticationName);
        try {
            createSASLAuthenticationInModel(saslAuthenticationAddress);
            ops.writeAttribute(saslAuthenticationAddress, MECHANISM_CONFIGURATIONS, new ModelNodeGenerator.ModelNodeListBuilder()
                    .addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(PROTOCOL, protocol)
                            .addProperty(HOST_NAME, hostName)
                            .addProperty(MECHANISM_NAME, mechanismName)
                            .build()).build()).assertSuccess();
            adminOps.reloadIfRequired();
            page.navigateToApplication()
                    .selectFactory(SASL_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .selectByName(saslAuthenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_LABEL);
            page.getConfigAreaResourceManager()
                    .removeResource(getMechanismConfigurationValueWithTrailingComma(configuration))
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Created mechanism configuration should not be present in the table anymore", page.getConfigAreaResourceManager()
                    .isResourcePresent(getMechanismConfigurationValueWithTrailingComma(configuration)));
            new ResourceVerifier(saslAuthenticationAddress, client)
                    .verifyAttribute(MECHANISM_CONFIGURATIONS, new ModelNode().addEmptyList());
        } finally {
            ops.removeIfExists(saslAuthenticationAddress);
            adminOps.reloadIfRequired();
        }
    }

    private String getMechanismConfigurationValueWithTrailingComma(MechanismConfiguration configuration) {
        return configuration.toString() + ",";
    }

    private void createSASLAuthenticationInModel(Address saslAuthenticatinAddress) throws IOException, TimeoutException, InterruptedException {
        String securityDomain = "ApplicationDomain";
        ops.add(saslAuthenticatinAddress, Values.of(SECURITY_DOMAIN, securityDomain)
                .and(SASL_SERVER_FACTORY, PROVIDER_SASL_SERVER_FACTORY_NAME)).assertSuccess();
        adminOps.reloadIfRequired();
    }

    private void createSASLAuthenticationFactoryInModel(Address saslAuthenticationFactoryAddress) throws InterruptedException, TimeoutException, IOException {
        ops.add(saslAuthenticationFactoryAddress).assertSuccess();
        adminOps.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_2);
        ops.removeIfExists(PROVIDER_SASL_SERVER_FACTORY_ADDRESS);
        adminOps.reloadIfRequired();
    }
}
