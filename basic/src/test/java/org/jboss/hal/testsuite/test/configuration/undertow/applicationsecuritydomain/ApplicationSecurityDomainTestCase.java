package org.jboss.hal.testsuite.test.configuration.undertow.applicationsecuritydomain;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.ApplicationSecurityDomainPage;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
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
public class ApplicationSecurityDomainTestCase {

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
            OVERRIDE_DEPLOYMENT_CONFIG = "override-deployment-config",
            ENABLE_JACC = "enable-jacc",
            HTTP_AUTHENTICATION_FACTORY = "http-authentication-factory";

    private static Address
            securityRealmAddress,
            securityDomainAddress,
            serviceLoaderFactoryAddress,
            keyStoreAddress,
            httpAuthenticationAddress;

    private static final Address
            UNDERTOWN_SUBSYSTEM_ADDRESS = Address.subsystem("undertow"),
            APP_SEC_DOMAIN_ADDRESS = UNDERTOWN_SUBSYSTEM_ADDRESS.and(APPLICATION_SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7)),
            APP_SEC_DOMAIN_TBR_ADDRESS = UNDERTOWN_SUBSYSTEM_ADDRESS.and(APPLICATION_SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7));

    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException {
        securityRealmAddress = securityDomainOperations.createSecurityRealm();
        securityDomainAddress = securityDomainOperations.createSecurityDomain(securityRealmAddress.getLastPairValue());
        serviceLoaderFactoryAddress = securityDomainOperations.createServiceLoaderFactory();
        httpAuthenticationAddress = securityDomainOperations.createHTTPAuthentication(serviceLoaderFactoryAddress.getLastPairValue(),
                securityDomainAddress.getLastPairValue());

        keyStoreAddress = securityDomainOperations.createKeyStore();

        operations.add(APP_SEC_DOMAIN_ADDRESS, Values.of(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationAddress.getLastPairValue())).assertSuccess();
        operations.add(APP_SEC_DOMAIN_TBR_ADDRESS, Values.of(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationAddress.getLastPairValue())).assertSuccess();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(APP_SEC_DOMAIN_ADDRESS);
            operations.removeIfExists(APP_SEC_DOMAIN_TBR_ADDRESS);
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

    @Before
    public void before() {
        page.navigate();
        page.getResourceManager().selectByName(APP_SEC_DOMAIN_ADDRESS.getLastPairValue());
    }

    /**
     * @tpTestDetails Try to create application security domain in Web Console's Undertow configuration.
     * Validate that created resource is visible in resource table.
     * Validate that create resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void addApplicationSecurityDomain() throws Exception {
        final Address domainAddress = Address.subsystem("undertow").and(APPLICATION_SECURITY_DOMAIN, RandomStringUtils.randomAlphanumeric(7));
        try {
            page.addApplicationSecurityDomain()
                    .name(domainAddress.getLastPairValue())
                    .httpAuthenticationFactory(httpAuthenticationAddress.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Domain should be present in table!",
                    page.getResourceManager().isResourcePresent(domainAddress.getLastPairValue()));

            new ResourceVerifier(domainAddress, client).verifyExists()
                    .verifyAttribute(HTTP_AUTHENTICATION_FACTORY, httpAuthenticationAddress.getLastPairValue());
        } finally {
            operations.removeIfExists(domainAddress);
            administration.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to remove already created application security domain in Web Console's Undertow subsystem
     * configuration.
     * Validate that resource is not present in resource table after removing.
     * Validate that resource is not present in model.
     */
    @Test
    public void removeApplicationSecurityDomain() throws Exception {
        page.getResourceManager().removeResource(APP_SEC_DOMAIN_TBR_ADDRESS.getLastPairValue())
                .confirmAndDismissReloadRequiredMessage()
                .assertClosed();

        Assert.assertFalse("Domain should not be present in table!",
                page.getResourceManager().isResourcePresent(APP_SEC_DOMAIN_TBR_ADDRESS.getLastPairValue()));

        new ResourceVerifier(APP_SEC_DOMAIN_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    /**
     * @tpTestDetails Try to set value of checkbox labeled 'Enable jacc' to negation of original value and then back to
     * original value with validating, that value got propagated to model.
     */
    @Test
    public void toggleEnableJacc() throws Exception {
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(APP_SEC_DOMAIN_ADDRESS, ENABLE_JACC);
        originalModelNodeResult.assertSuccess();
        final boolean originalBooleanValue = originalModelNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ENABLE_JACC, !originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(ENABLE_JACC, !originalBooleanValue);

            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ENABLE_JACC, originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(ENABLE_JACC, originalBooleanValue);
        } finally {
            operations.writeAttribute(APP_SEC_DOMAIN_ADDRESS, ENABLE_JACC, originalModelNodeResult.value());
        }
    }

    /**
     * @tpTestDetails Try to set value of checkbox labeled 'Override deployment config' to negation of original value
     * and then back to original value with validating, that value got propagated to model.
     */
    @Test
    public void toggleOverrideDeploymentConfig() throws Exception {
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(APP_SEC_DOMAIN_ADDRESS, OVERRIDE_DEPLOYMENT_CONFIG);
        originalModelNodeResult.assertSuccess();
        final boolean originalBooleanValue = originalModelNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, OVERRIDE_DEPLOYMENT_CONFIG, !originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(OVERRIDE_DEPLOYMENT_CONFIG, !originalBooleanValue);

            new ConfigChecker.Builder(client, APP_SEC_DOMAIN_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, OVERRIDE_DEPLOYMENT_CONFIG, originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(OVERRIDE_DEPLOYMENT_CONFIG, originalBooleanValue);
        } finally {
            operations.writeAttribute(APP_SEC_DOMAIN_ADDRESS, OVERRIDE_DEPLOYMENT_CONFIG, originalModelNodeResult.value());
        }
    }
}
