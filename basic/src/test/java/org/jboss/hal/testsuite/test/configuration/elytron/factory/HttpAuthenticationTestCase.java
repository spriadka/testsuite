package org.jboss.hal.testsuite.test.configuration.elytron.factory;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddHttpAuthenticationWizard;
import org.jboss.hal.testsuite.fragment.config.elytron.factory.AddMechanismConfigurationWizard;
import org.jboss.hal.testsuite.test.configuration.elytron.factory.validator.AddHttpAuthenticationValidator;
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
@Category(Elytron.class)
@RunAsClient
public class HttpAuthenticationTestCase extends ElytronFactoryTestCaseAbstract {

    private static final String HTTP_AUTHENTICATION_LABEL = "HTTP Authentication";
    private static final String HTTP_SERVER_MECHANISM_FACTORY = "http-server-mechanism-factory";
    private static final String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory";

    private static final String HTTP_AUTHENTICATION_FACTORY = "http-authentication-factory";
    private static final String ATTRIBUTES_TAB = "Attributes";
    private static final String MECHANISM_CONFIGURATIONS_TAB = "Mechanism Configurations";
    private static final String SECURITY_DOMAIN = "security-domain";
    private static final Address httpMechanismFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
            RandomStringUtils.randomAlphanumeric(7));

    private static final String MECHANISM_CONFIGURATIONS = "mechanism-configurations";


    @BeforeClass
    public static void beforeClass() throws IOException, TimeoutException, InterruptedException {
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.addProviderLoader(PROVIDER_LOADER_NAME_2);
        ops.add(httpMechanismFactoryAddress, Values.of(MODULE, MODULE_NAME_1));
        adminOps.reloadIfRequired();
    }

    @Test
    public void addHttpAuthenticationTest() throws Exception {
        final String httpAuthenticationName = "http_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String securityDomainName = "ApplicationDomain";
        final Address httpAuthenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationName);
        try {
            page.navigateToApplication()
                    .selectFactory(HTTP_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .addResource(AddHttpAuthenticationWizard.class)
                    .name(httpAuthenticationName)
                    .securityDomain(securityDomainName)
                    .httpServerMechanismFactory(httpMechanismFactoryAddress.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created HTTP authentication should be present in the table",
                    page.getResourceManager().isResourcePresent(httpAuthenticationName));
            new ResourceVerifier(httpAuthenticationAddress, client)
                    .verifyExists()
                    .verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpMechanismFactoryAddress.getLastPairValue())
                    .verifyAttribute(SECURITY_DOMAIN, securityDomainName);
        } finally {
            ops.removeIfExists(httpAuthenticationAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addHttpAuthenticationInvalidFieldsCombinationTest() {
        AddHttpAuthenticationWizard wizard = page.navigateToApplication()
                .selectFactory(HTTP_AUTHENTICATION_LABEL)
                .getResourceManager()
                .addResource(AddHttpAuthenticationWizard.class);
        new AddHttpAuthenticationValidator(page.getWindowFragment()).testInvalidCombinationsAndAssert(wizard);
    }

    @Test
    public void removeHttpAuthenticationTest() throws Exception {
        final String httpAuthenticationName = RandomStringUtils.randomAlphanumeric(7);
        final String securityDomain = "ApplicationDomain";
        final Address httpAuthenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationName);
        final ResourceVerifier authenticationVerifier = new ResourceVerifier(httpAuthenticationAddress, client);
        try {
            ops.add(httpAuthenticationAddress, Values.of(SECURITY_DOMAIN, securityDomain)
                    .and(HTTP_SERVER_MECHANISM_FACTORY, httpMechanismFactoryAddress.getLastPairValue()))
                    .assertSuccess();
            authenticationVerifier.verifyExists();
            page.navigateToApplication()
                    .selectFactory(HTTP_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .removeResource(httpAuthenticationName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed authentication should not be present in the table",
                    page.getResourceManager().isResourcePresent(httpAuthenticationName));
            authenticationVerifier.verifyDoesNotExist();
        } finally {
            ops.removeIfExists(httpAuthenticationAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editAttributesTest() throws Exception {
        final String realmName = "realm_" + RandomStringUtils.randomAlphanumeric(7);
        final String httpAuthenticationName = "http_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String httpFactory = "factory_" + RandomStringUtils.randomAlphanumeric(7);
        final String oldSecurityDomain = "OldSecurityDomain_" + RandomStringUtils.randomAlphanumeric(7);
        final String securityDomain = "NewSecurityDomain_" + RandomStringUtils.randomAlphanumeric(7);
        final Address realmAddress = elyOps.createSecurityRealm(realmName);
        final Address oldSecurityDomainAddress = elyOps.createSecurityDomain(oldSecurityDomain, realmName);
        final Address securityDomainAddress = elyOps.createSecurityDomain(securityDomain, realmName);
        final Address httpAuthenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationName);
        final Address httpFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, httpFactory);

        try {
            ops.add(httpFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpAuthenticationAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpFactory)
                    .and(SECURITY_DOMAIN, oldSecurityDomain)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(HTTP_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .selectByName(httpAuthenticationName);
            page.switchToConfigAreaTab(ATTRIBUTES_TAB);
            new ConfigChecker.Builder(client, httpAuthenticationAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, SECURITY_DOMAIN, securityDomain)
                    .edit(ConfigChecker.InputType.TEXT, HTTP_SERVER_MECHANISM_FACTORY, httpFactory)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(SECURITY_DOMAIN, securityDomain)
                    .verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, httpFactory);
        } finally {
            ops.removeIfExists(httpAuthenticationAddress);
            ops.removeIfExists(httpFactoryAddress);
            ops.removeIfExists(oldSecurityDomainAddress);
            ops.removeIfExists(securityDomainAddress);
            ops.removeIfExists(realmAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addMechanismConfigurationTest() throws Exception {
        final String httpAuthenticationName = "http_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String httpFactory = "factory_" + RandomStringUtils.randomAlphanumeric(7);
        final String securityDomain = "ApplicationDomain";
        final String mechanismName = RandomStringUtils.randomAlphanumeric(7);
        final String hostName = RandomStringUtils.randomAlphanumeric(7);
        final String protocol = RandomStringUtils.randomAlphanumeric(7);
        MechanismConfiguration configuration = new MechanismConfiguration()
                .hostName(hostName)
                .mechanismName(mechanismName)
                .protocol(protocol);
        final Address httpAuthenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationName);
        final Address httpFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, httpFactory);

        try {
            ops.add(httpFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpAuthenticationAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpFactory)
                    .and(SECURITY_DOMAIN, securityDomain)).assertSuccess();
            page.navigateToApplication()
                    .selectFactory(HTTP_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .selectByName(httpAuthenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_TAB);
            page.getConfigAreaResourceManager()
                    .addResource(AddMechanismConfigurationWizard.class)
                    .mechanismName(mechanismName)
                    .hostName(hostName)
                    .protocol(protocol)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Created mechanism configuration should be present in the table",
                    page.getConfigAreaResourceManager().isResourcePresent(getMechanismConfigurationValueWithTrailingComma(configuration)));
            new ResourceVerifier(httpAuthenticationAddress, client)
                    .verifyAttribute(MECHANISM_CONFIGURATIONS, new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty("host-name", hostName)
                            .addProperty("mechanism-name", mechanismName)
                            .addProperty("protocol", protocol)
                            .build()).build());

        } finally {
            ops.removeIfExists(httpAuthenticationAddress);
            ops.removeIfExists(httpFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    private String getMechanismConfigurationValueWithTrailingComma(MechanismConfiguration configuration) {
        return configuration.toString() + ",";
    }

    @Test
    public void removeMechanismConfigurationTest() throws Exception {
        final String httpAuthenticationName = "http_authentication_" + RandomStringUtils.randomAlphanumeric(7);
        final String httpFactory = "factory_" + RandomStringUtils.randomAlphanumeric(7);
        final String securityDomainName = "ApplicationDomain";
        final String mechanismName = RandomStringUtils.randomAlphanumeric(7);
        final String hostName = RandomStringUtils.randomAlphanumeric(7);
        final String protocol = RandomStringUtils.randomAlphanumeric(7);
        final Address httpAuthenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationName);
        final Address httpFactoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, httpFactory);
        final MechanismConfiguration configuration = new MechanismConfiguration()
                .protocol(protocol)
                .mechanismName(mechanismName)
                .hostName(hostName);
        try {
            ops.add(httpFactoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(httpAuthenticationAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, httpFactory)
                    .and(SECURITY_DOMAIN, securityDomainName)).assertSuccess();
            ops.writeAttribute(httpAuthenticationAddress, MECHANISM_CONFIGURATIONS
                    , new ModelNodeGenerator.ModelNodeListBuilder().addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty("host-name", hostName)
                            .addProperty("mechanism-name", mechanismName)
                            .addProperty("protocol", protocol)
                            .build()).build()).assertSuccess();
            adminOps.reloadIfRequired();
            page.navigateToApplication()
                    .selectFactory(HTTP_AUTHENTICATION_LABEL)
                    .getResourceManager()
                    .selectByName(httpAuthenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_TAB);
            page.getConfigAreaResourceManager()
                    .removeResource(getMechanismConfigurationValueWithTrailingComma(configuration))
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Recently removed mechanism confiuguration should not be present in the table anymore",
                    page.getConfigAreaResourceManager().isResourcePresent(getMechanismConfigurationValueWithTrailingComma(configuration)));
            new ResourceVerifier(httpAuthenticationAddress, client)
                    .verifyAttribute(MECHANISM_CONFIGURATIONS, new ModelNode().addEmptyList());

        } finally {
            ops.removeIfExists(httpAuthenticationAddress);
            ops.removeIfExists(httpFactoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException, OperationException {
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_1);
        elyOps.removeProviderLoader(PROVIDER_LOADER_NAME_2);
        ops.removeIfExists(httpMechanismFactoryAddress);
        adminOps.reloadIfRequired();
    }

}
