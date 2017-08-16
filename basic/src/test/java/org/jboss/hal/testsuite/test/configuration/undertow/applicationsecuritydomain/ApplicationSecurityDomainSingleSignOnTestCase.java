package org.jboss.hal.testsuite.test.configuration.undertow.applicationsecuritydomain;


import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.ApplicationSecurityDomainPage;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Elytron.class)
public class ApplicationSecurityDomainSingleSignOnTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private ApplicationSecurityDomainPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);
    private static final UndertowElytronOperations securityDomainOperations = new UndertowElytronOperations(client);

    private static final String
            APPLICATION_SECURITY_DOMAIN = "application-security-domain",
            HTTP_AUTHENTICATION_FACTORY = "http-authentication-factory",
            CREDENTIAL_REFERENCE = "credential-reference",
            CLEAR_TEXT = "clear-text",
            SETTING = "setting",
            SINGLE_SIGN_ON = "single-sign-on",
            KEY_STORE = "key-store",
            KEY_ALIAS = "key-alias";

    private static Address
            securityRealmAddress,
            securityDomainAddress,
            serviceLoaderFactoryAddress,
            keyStoreAddress,
            httpAuthenticationAddress;

    private static final Address
            UNDERTOW_SUBSYSTEM_ADDRESS = Address.subsystem("undertow"),
            APP_SEC_DOMAIN_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS.and(APPLICATION_SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7)),
            APP_SEC_DOMAIN_WITH_SSO_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS.and(APPLICATION_SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7)),
            APP_SEC_DOMAIN_SSO_SETTING = APP_SEC_DOMAIN_WITH_SSO_ADDRESS.and(SETTING, SINGLE_SIGN_ON),
            APP_SEC_DOMAIN_WITH_SSO_TBR_ADDRESS = UNDERTOW_SUBSYSTEM_ADDRESS.and(APPLICATION_SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7));


    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        securityRealmAddress = securityDomainOperations.createSecurityRealm();
        securityDomainAddress = securityDomainOperations.createSecurityDomain(securityRealmAddress.getLastPairValue());
        serviceLoaderFactoryAddress = securityDomainOperations.createServiceLoaderFactory();
        httpAuthenticationAddress = securityDomainOperations.createHTTPAuthentication(serviceLoaderFactoryAddress.getLastPairValue(),
                securityDomainAddress.getLastPairValue());

        keyStoreAddress = securityDomainOperations.createKeyStore();

        operations.add(APP_SEC_DOMAIN_ADDRESS, Values.of(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationAddress.getLastPairValue())).assertSuccess();

        operations.add(APP_SEC_DOMAIN_WITH_SSO_ADDRESS, Values.of(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationAddress.getLastPairValue())).assertSuccess();
        enableSSOOnDomain(APP_SEC_DOMAIN_WITH_SSO_ADDRESS);

        operations.add(APP_SEC_DOMAIN_WITH_SSO_TBR_ADDRESS, Values.of(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationAddress.getLastPairValue())).assertSuccess();
        enableSSOOnDomain(APP_SEC_DOMAIN_WITH_SSO_TBR_ADDRESS);

        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.getResourceManager().selectByName(APP_SEC_DOMAIN_WITH_SSO_ADDRESS.getLastPairValue());
        page.switchToSSOConfigTab();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(APP_SEC_DOMAIN_ADDRESS);
            operations.removeIfExists(APP_SEC_DOMAIN_WITH_SSO_ADDRESS);
            operations.removeIfExists(APP_SEC_DOMAIN_WITH_SSO_TBR_ADDRESS);
            operations.removeIfExists(httpAuthenticationAddress);
            operations.removeIfExists(serviceLoaderFactoryAddress);
            operations.removeIfExists(securityDomainAddress);
            operations.removeIfExists(securityRealmAddress);
            operations.removeIfExists(keyStoreAddress);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    /**
     * @tpTestDetails Try to enable Single Sign On for selected security domain by clicking on button present in
     * configuration and then filling in form in wizard which pops up.
     * Validate that SSO was enabled in model.
     */
    @Test
    public void testEnableSSO() throws Exception {
        final Address keyStoreAddress = securityDomainOperations.createKeyStore();
        try {
            page.getResourceManager().selectByName(APP_SEC_DOMAIN_ADDRESS.getLastPairValue());
            page.switchToSSOConfigTab();
            page.enableSSO()
                    .keyAlias(RandomStringUtils.randomAlphanumeric(7))
                    .keyStore(keyStoreAddress.getLastPairValue())
                    .clearText(RandomStringUtils.randomAlphanumeric(7))
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            new ResourceVerifier(APP_SEC_DOMAIN_ADDRESS.and(SETTING, SINGLE_SIGN_ON), client).verifyExists();
        } finally {
            operations.removeIfExists(APP_SEC_DOMAIN_ADDRESS.and(SETTING, SINGLE_SIGN_ON));
            operations.removeIfExists(keyStoreAddress);
            administration.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to disable Single Sign On for selected security domain with already enabled SSO by clicking on
     * button present in Web Console configuration.
     * Validate that SSO was disabled in model.
     */
    @Test
    public void testDisableSSO() throws Exception {
        try {
            page.getResourceManager().selectByName(APP_SEC_DOMAIN_WITH_SSO_TBR_ADDRESS.getLastPairValue());
            page.switchToSSOConfigTab();
            page.disableSSO()
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            new ResourceVerifier(APP_SEC_DOMAIN_WITH_SSO_TBR_ADDRESS.and(SETTING, SINGLE_SIGN_ON), client)
                    .verifyDoesNotExist();
        } finally {
            operations.removeIfExists(APP_SEC_DOMAIN_WITH_SSO_TBR_ADDRESS.and(SETTING, SINGLE_SIGN_ON));
            administration.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to edit credential reference of aalready created security domain in Web Console's Undertow
     * subsystem configuration.
     * Validate edited attribute values in the model.
     * Test setting <ul>
     * <li>store + alias</li>
     * <li>clear text</li>
     * <li>illegal combination of both</li></ul>
     */
    @Test
    public void testCredentialReferenceSetting() throws Exception {
        page.getResourceManager().selectByName(APP_SEC_DOMAIN_WITH_SSO_ADDRESS.getLastPairValue());
        ConfigFragment configFragment = page.swithToSSOCredentialReferenceTab();
        final ElytronIntegrationChecker integrationChecker = new ElytronIntegrationChecker.Builder(client)
                .configFragment(configFragment)
                .address(APP_SEC_DOMAIN_WITH_SSO_ADDRESS.and(SETTING, SINGLE_SIGN_ON))
                .build();

        integrationChecker.setClearTextCredentialReferenceAndVerify();
        integrationChecker.setCredentialStoreCredentialReferenceAndVerify();
        integrationChecker.testIllegalCombinationCredentialReferenceAttributes();
    }

    /**
     * @tpTestDetails Try to edit field labeled 'Client SSL context', verify that form switched back to read-only mode
     * and validate that there is expected value present in model.
     */
    @Test
    public void editClientSSLContext() throws Exception {
        final String clientSSLContext = "client-ssl-context";
        final Address
                keyStoreAddress = securityDomainOperations.createKeyStore(),
                keyManagerAddress = securityDomainOperations.createKeyManager(keyStoreAddress),
                clientSSLContextAddress = securityDomainOperations.createClientSSLContext(keyManagerAddress);
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(APP_SEC_DOMAIN_SSO_SETTING, clientSSLContext);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, clientSSLContext, clientSSLContextAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(clientSSLContext, clientSSLContextAddress.getLastPairValue());
        } finally {
            operations.writeAttribute(APP_SEC_DOMAIN_SSO_SETTING, clientSSLContext, originalModelNodeResult.value());
            operations.removeIfExists(clientSSLContextAddress);
            operations.removeIfExists(keyManagerAddress);
            operations.removeIfExists(keyStoreAddress);
            administration.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to edit field labeled 'Cookie name', verify that form switched back to read-only mode and
     * validate that there is expected value present in model.
     */
    @Test
    public void editCookieName() throws Exception {
        final String cookieName = "cookie-name",
                value = RandomStringUtils.randomAlphanumeric(7);

        new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, cookieName, value)
                .verifyFormSaved()
                .verifyAttribute(cookieName, value);
    }

    /**
     * @tpTestDetails Try to edit field labeled 'Domain', verify that form switched back to read-only mode and validate
     * that there is expected value present in model.
     */
    @Test
    public void editDomain() throws Exception {
        final String domain = "domain",
                value = RandomStringUtils.randomAlphanumeric(7);

        new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, domain, value)
                .verifyFormSaved()
                .verifyAttribute(domain, value);
    }

    /**
     * @tpTestDetails Try to set value of checkbox labeled 'Http only' to negation of original value and then back to
     * original value with validating, that value got propagated to model.
     */
    @Test
    public void toggleHttpOnly() throws Exception {
        final String httpOnly = "http-only";
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(APP_SEC_DOMAIN_SSO_SETTING, httpOnly);
        originalModelNodeResult.assertSuccess();
        final boolean originalBooleanValue = originalModelNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, httpOnly, !originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(httpOnly, !originalBooleanValue);

            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, httpOnly, originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(httpOnly, originalBooleanValue);
        } finally {
            operations.writeAttribute(APP_SEC_DOMAIN_SSO_SETTING, httpOnly, originalModelNodeResult.value());
        }
    }

    /**
     * @tpTestDetails Try to edit field labeled 'Key alias', verify that form switched back to read-only mode and
     * validate that there is expected value present in model.
     */
    @Test
    public void editKeyAlias() throws Exception {
        final String value = RandomStringUtils.randomAlphanumeric(7);

        new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, KEY_ALIAS, value)
                .verifyFormSaved()
                .verifyAttribute(KEY_ALIAS, value);
    }

    /**
     * @tpTestDetails Try to edit field labeled 'Key store', verify that form switched back to read-only mode and
     * validate that there is expected value present in model.
     */
    @Test
    public void editKeyStore() throws Exception {
        final Address keyStoreAddress = securityDomainOperations.createKeyStore();

        final ModelNodeResult originalModelNodeResult = operations.readAttribute(APP_SEC_DOMAIN_SSO_SETTING, KEY_STORE);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, KEY_STORE, keyStoreAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(KEY_STORE, keyStoreAddress.getLastPairValue());
        } finally {
            operations.writeAttribute(APP_SEC_DOMAIN_SSO_SETTING, KEY_STORE, originalModelNodeResult.value());
            operations.removeIfExists(keyStoreAddress);
            administration.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to edit field labeled 'Path', verify that form switched back to read-only mode and validate
     * that there is expected value present in model.
     */
    @Test
    public void editPath() throws Exception {
        final String path = "path",
                value = RandomStringUtils.randomAlphanumeric(7);

        new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, path, value)
                .verifyFormSaved()
                .verifyAttribute(path, value);
    }

    /**
     * @tpTestDetails Try to set value of checkbox labeled 'Secure' to negation of original value and then back to
     * original value with validating, that value got propagated to model.
     */
    @Test
    public void toggleSecure() throws Exception {
        final String secure = "secure";
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(APP_SEC_DOMAIN_SSO_SETTING, secure);
        originalModelNodeResult.assertSuccess();
        final boolean originalBooleanValue = originalModelNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, secure, !originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(secure, !originalBooleanValue);

            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_SSO_SETTING)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, secure, originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(secure, originalBooleanValue);
        } finally {
            operations.writeAttribute(APP_SEC_DOMAIN_SSO_SETTING, secure, originalModelNodeResult.value());
        }
    }

    private static void enableSSOOnDomain(Address securityDomainAddress) throws IOException {
        operations.add(securityDomainAddress.and(SETTING, SINGLE_SIGN_ON),
                Values.of(KEY_STORE, keyStoreAddress.getLastPairValue())
                        .and(KEY_ALIAS, RandomStringUtils.randomAlphanumeric(7))
                        .and(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                                .addProperty(CLEAR_TEXT, RandomStringUtils.randomAlphanumeric(7)).build()))
        .assertSuccess();
    }

}
