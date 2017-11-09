package org.jboss.hal.testsuite.test.configuration.elytron;

import static org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodePropertiesBuilder;
import static org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.elytron.FactoryPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ConfigChecker.InputType;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class ElytronAuthenticationTestCase extends AbstractElytronTestCase {

    private static final String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = "service-loader-http-server-mechanism-factory",
            SERVICE_LOADER_SASL_SERVER_FACTORY = "service-loader-sasl-server-factory",
            HTTP_AUTHENTICATION_LABEL = "HTTP Authentication",
            HTTP_AUTHENTICATION_FACTORY = "http-authentication-factory",
            HTTP_SERVER_MECHANISM_FACTORY = "http-server-mechanism-factory",
            SASL_AUTHENTICATION_LABEL = "SASL Authentication",
            SASL_AUTHENTICATION_FACTORY = "sasl-authentication-factory",
            SASL_SERVER_FACTORY = "sasl-server-factory", SECURITY_DOMAIN = "security-domain",
            SECURITY_DOMAIN_NAME = "test-domain", NAME = "name", MODULE = "module",
            MECHANISM_CONFIGURATIONS_LABEL = "Mechanism Configurations",
            MECHANISM_CONFIGURATIONS = "mechanism-configurations", MECHANISM_NAME = "mechanism-name",
            HOST_NAME = "host-name";

    private static Address securityRealmAddress, securityDomainAddress;

    @Page
    private FactoryPage page;

    @BeforeClass
    public static void beforeClass() throws Exception {
        String realmName = "test-realm";
        securityRealmAddress = createSecurityRealm(realmName);
        securityDomainAddress = createSecurityDomain(SECURITY_DOMAIN_NAME, realmName);
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, TimeoutException {
        ops.remove(securityDomainAddress);
        ops.remove(securityRealmAddress);
        adminOps.reloadIfRequired();
    }

    private static Address createSecurityRealm(String realmName) throws Exception {
        Address realmAddress = elyOps.getElytronAddress("properties-realm", realmName);
        ModelNode userPropertiesNode = new ModelNodePropertiesBuilder().addProperty("path", "mgmt-users.properties")
                .addProperty("relative-to", ConfigUtils.getConfigDirPathName()).build();
        ops.add(realmAddress, Values.of("users-properties", userPropertiesNode)).assertSuccess();
        return realmAddress;
    }

    private static Address createSecurityDomain(String domainName, String realmName) throws Exception {
        Address domainAddress = elyOps.getElytronAddress(SECURITY_DOMAIN, domainName);
        ModelNode realmsNode = new ModelNodeListBuilder()
                .addNode(new ModelNodePropertiesBuilder().addProperty("realm", realmName).build()).build();
        ops.add(domainAddress, Values.of("default-realm", realmName).and("realms", realmsNode)).assertSuccess();
        return domainAddress;
    }

    @Test
    public void addHttpAuthenticationTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, authenticationName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(HTTP_AUTHENTICATION_LABEL);

            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, authenticationName);
            editor.text(HTTP_SERVER_MECHANISM_FACTORY, factoryName);
            editor.text(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Authentication should be present in the table!",
                    page.resourceIsPresentInMainTable(authenticationName));
            new ResourceVerifier(authenticationAddress, client).verifyExists()
                    .verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, factoryName)
                    .verifyAttribute(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME);

        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeHttpAuthenticationTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, authenticationName);
        final ResourceVerifier authenticationVerifier = new ResourceVerifier(authenticationAddress, client);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(authenticationAddress,
                    Values.of(HTTP_SERVER_MECHANISM_FACTORY, factoryName).and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(HTTP_AUTHENTICATION_LABEL);

            page.getResourceManager().removeResource(authenticationName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted authentication should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(authenticationName));
            authenticationVerifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editHttpAuthenticationAttributesTest() throws Exception {
        final String factoryName1 = RandomStringUtils.randomAlphanumeric(5),
                factoryName2 = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5),
                realm2name = RandomStringUtils.randomAlphanumeric(5),
                domain2name = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName1),
                factoryAddress2 = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, factoryName2),
                securityRealmAddress2 = createSecurityRealm(realm2name),
                securityDomainAddress2 = createSecurityDomain(domain2name, realm2name),
                authenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, authenticationName);

        try {
            ops.add(factoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(factoryAddress2, Values.of(MODULE, MODULE_NAME_2)).assertSuccess();
            ops.add(authenticationAddress,
                    Values.of(HTTP_SERVER_MECHANISM_FACTORY, factoryName1).and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(HTTP_AUTHENTICATION_LABEL).getResourceManager()
                    .selectByName(authenticationName);

            new ConfigChecker.Builder(client, authenticationAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, HTTP_SERVER_MECHANISM_FACTORY, factoryName2).verifyFormSaved()
                    .verifyAttribute(HTTP_SERVER_MECHANISM_FACTORY, factoryName2);

            new ConfigChecker.Builder(client, authenticationAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, SECURITY_DOMAIN, domain2name).verifyFormSaved()
                    .verifyAttribute(SECURITY_DOMAIN, domain2name);
        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress1);
            ops.removeIfExists(factoryAddress2);
            ops.removeIfExists(securityDomainAddress2);
            ops.removeIfExists(securityRealmAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void httpAuthenticationAddConfigurationsTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5),
                mechanismNameValue = RandomStringUtils.randomAlphanumeric(5),
                hostNameValue = RandomStringUtils.randomAlphanumeric(5), expectedConfigurationString = MECHANISM_NAME
                        + ": " + mechanismNameValue + ", " + HOST_NAME + ": " + hostNameValue + ",";
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, authenticationName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(authenticationAddress,
                    Values.of(HTTP_SERVER_MECHANISM_FACTORY, factoryName).and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(HTTP_AUTHENTICATION_LABEL).getResourceManager()
                    .selectByName(authenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_LABEL);

            WizardWindow wizard = page.getConfigAreaResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(MECHANISM_NAME, mechanismNameValue);
            editor.text(HOST_NAME, hostNameValue);
            boolean closed = wizard.finishAndDismissReloadRequiredWindow();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created configuration should be present in the table!",
                    page.resourceIsPresentInConfigAreaTable(expectedConfigurationString));

            final ModelNode expectedConfigurationsNode = new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder().addProperty(MECHANISM_NAME, mechanismNameValue)
                            .addProperty(HOST_NAME, hostNameValue).build())
                    .build();

            new ResourceVerifier(authenticationAddress, client)
                    .verifyExists().verifyAttribute(MECHANISM_CONFIGURATIONS, expectedConfigurationsNode);
        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void httpAuthenticationRemoveConfigurationsTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5),
                mechanismNameValue = RandomStringUtils.randomAlphanumeric(5),
                hostNameValue = RandomStringUtils.randomAlphanumeric(5), configurationString = MECHANISM_NAME + ": "
                        + mechanismNameValue + ", " + HOST_NAME + ": " + hostNameValue + ",";
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(HTTP_AUTHENTICATION_FACTORY, authenticationName);
        final ModelNode initConfigurationsNode = new ModelNodeListBuilder().addNode(new ModelNodePropertiesBuilder()
                .addProperty(MECHANISM_NAME, mechanismNameValue).addProperty(HOST_NAME, hostNameValue).build()).build();

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(authenticationAddress, Values.of(HTTP_SERVER_MECHANISM_FACTORY, factoryName)
                    .and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME).and(MECHANISM_CONFIGURATIONS, initConfigurationsNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(HTTP_AUTHENTICATION_LABEL).getResourceManager()
                    .selectByName(authenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_LABEL);

            page.getConfigAreaResourceManager().removeResource(configurationString)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted configuration should not be present in the table any more!",
                    page.resourceIsPresentInConfigAreaTable(configurationString));
            ModelNode emptyNodeList = new ModelNodeListBuilder().empty().build();
            new ResourceVerifier(authenticationAddress, client)
                    .verifyExists().verifyAttribute(MECHANISM_CONFIGURATIONS, emptyNodeList);

        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void addSaslAuthenticationTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION_FACTORY, authenticationName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();

            page.navigateToApplication().selectFactory(SASL_AUTHENTICATION_LABEL);

            WizardWindow wizard = page.getResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(NAME, authenticationName);
            editor.text(SASL_SERVER_FACTORY, factoryName);
            editor.text(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME);
            boolean closed = wizard.finish();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Authentication should be present in the table!",
                    page.resourceIsPresentInMainTable(authenticationName));
            new ResourceVerifier(authenticationAddress, client).verifyExists()
                    .verifyAttribute(SASL_SERVER_FACTORY, factoryName)
                    .verifyAttribute(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME);

        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeSaslAuthenticationTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION_FACTORY, authenticationName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(authenticationAddress,
                    Values.of(SASL_SERVER_FACTORY, factoryName).and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(SASL_AUTHENTICATION_LABEL);

            page.getResourceManager().removeResource(authenticationName).confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            assertFalse("Deleted authentication should not be present in the table any more!",
                    page.resourceIsPresentInMainTable(authenticationName));
            new ResourceVerifier(authenticationAddress, client).verifyDoesNotExist();

        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void editSaslAuthenticationAttributesTest() throws Exception {
        final String factoryName1 = RandomStringUtils.randomAlphanumeric(5),
                factoryName2 = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5),
                realm2name = RandomStringUtils.randomAlphanumeric(5),
                domain2name = RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress1 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                factoryName1),
                factoryAddress2 = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY, factoryName2),
                securityRealmAddress2 = createSecurityRealm(realm2name),
                securityDomainAddress2 = createSecurityDomain(domain2name, realm2name),
                authenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION_FACTORY, authenticationName);

        try {
            ops.add(factoryAddress1, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(factoryAddress2, Values.of(MODULE, MODULE_NAME_2)).assertSuccess();
            ops.add(authenticationAddress,
                    Values.of(SASL_SERVER_FACTORY, factoryName1).and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(SASL_AUTHENTICATION_LABEL).getResourceManager()
                    .selectByName(authenticationName);

            new ConfigChecker.Builder(client, authenticationAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, SASL_SERVER_FACTORY, factoryName2).verifyFormSaved()
                    .verifyAttribute(SASL_SERVER_FACTORY, factoryName2);

            new ConfigChecker.Builder(client, authenticationAddress).configFragment(page.getConfigFragment())
                    .editAndSave(InputType.TEXT, SECURITY_DOMAIN, domain2name).verifyFormSaved()
                    .verifyAttribute(SECURITY_DOMAIN, domain2name);
        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress1);
            ops.removeIfExists(factoryAddress2);
            ops.removeIfExists(securityDomainAddress2);
            ops.removeIfExists(securityRealmAddress2);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void saslAuthenticationAddConfigurationsTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5),
                mechanismNameValue = RandomStringUtils.randomAlphanumeric(5),
                hostNameValue = RandomStringUtils.randomAlphanumeric(5), expectedConfigurationString = MECHANISM_NAME
                        + ": " + mechanismNameValue + ", " + HOST_NAME + ": " + hostNameValue + ",";
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION_FACTORY, authenticationName);

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(authenticationAddress,
                    Values.of(SASL_SERVER_FACTORY, factoryName).and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(SASL_AUTHENTICATION_LABEL).getResourceManager()
                    .selectByName(authenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_LABEL);

            WizardWindow wizard = page.getConfigAreaResourceManager().addResource();
            Editor editor = wizard.getEditor();
            editor.text(MECHANISM_NAME, mechanismNameValue);
            editor.text(HOST_NAME, hostNameValue);
            boolean closed = wizard.finishAndDismissReloadRequiredWindow();

            assertTrue("Dialog should be closed!", closed);
            assertTrue("Created configuration should be present in the table! "
                    + "Reported https://issues.jboss.org/browse/HAL-1267",
                    page.resourceIsPresentInConfigAreaTable(expectedConfigurationString));

            final ModelNode expectedConfigurationsNode = new ModelNodeListBuilder()
                    .addNode(new ModelNodePropertiesBuilder().addProperty(MECHANISM_NAME, mechanismNameValue)
                            .addProperty(HOST_NAME, hostNameValue).build())
                    .build();

            new ResourceVerifier(authenticationAddress, client)
                    .verifyExists().verifyAttribute(MECHANISM_CONFIGURATIONS, expectedConfigurationsNode);
        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void saslAuthenticationRemoveConfigurationsTest() throws Exception {
        final String factoryName = RandomStringUtils.randomAlphanumeric(5),
                authenticationName = RandomStringUtils.randomAlphanumeric(5),
                mechanismNameValue = RandomStringUtils.randomAlphanumeric(5),
                hostNameValue = RandomStringUtils.randomAlphanumeric(5), configurationString = MECHANISM_NAME + ": "
                        + mechanismNameValue + ", " + HOST_NAME + ": " + hostNameValue + ",";
        final Address factoryAddress = elyOps.getElytronAddress(SERVICE_LOADER_SASL_SERVER_FACTORY,
                factoryName),
                authenticationAddress = elyOps.getElytronAddress(SASL_AUTHENTICATION_FACTORY, authenticationName);
        final ModelNode initConfigurationsNode = new ModelNodeListBuilder().addNode(new ModelNodePropertiesBuilder()
                .addProperty(MECHANISM_NAME, mechanismNameValue).addProperty(HOST_NAME, hostNameValue).build()).build();

        try {
            ops.add(factoryAddress, Values.of(MODULE, MODULE_NAME_1)).assertSuccess();
            ops.add(authenticationAddress, Values.of(SASL_SERVER_FACTORY, factoryName)
                    .and(SECURITY_DOMAIN, SECURITY_DOMAIN_NAME).and(MECHANISM_CONFIGURATIONS, initConfigurationsNode))
                    .assertSuccess();

            page.navigateToApplication().selectFactory(SASL_AUTHENTICATION_LABEL).getResourceManager()
                    .selectByName(authenticationName);
            page.switchToConfigAreaTab(MECHANISM_CONFIGURATIONS_LABEL);

            page.getConfigAreaResourceManager().removeResource(configurationString)
                    .confirmAndDismissReloadRequiredMessage().assertClosed();
            assertFalse("Deleted configuration should not be present in the table any more! "
                    + "Reported https://issues.jboss.org/browse/HAL-1267",
                    page.resourceIsPresentInConfigAreaTable(configurationString));
            ModelNode emptyNodeList = new ModelNodeListBuilder().empty().build();
            new ResourceVerifier(authenticationAddress, client)
                    .verifyExists().verifyAttribute(MECHANISM_CONFIGURATIONS, emptyNodeList);

        } finally {
            ops.removeIfExists(authenticationAddress);
            ops.removeIfExists(factoryAddress);
            adminOps.reloadIfRequired();
        }
    }

}
