package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.messaging.ProviderSettingsWindow;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class SecurityTestCase extends AbstractMessagingTestCase {
    private static final String
            SERVER_NAME  = "test-provider_" + RandomStringUtils.randomAlphanumeric(5),
            CLUSTER_USER = "cluster-user",
            CLUSTER_PASSWORD = "cluster-password",
            SECURITY_ENABLED = "security-enabled",
            SECURITY_INVALIDATION_INTERVAL = "security-invalidation-interval",
            SECURITY_DOMAIN = "security-domain",
            NOT_SAVED_FAIL_MESSAGE = "Probably caused by https://issues.jboss.org/browse/HAL-1310";

    private static final Address SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and("server", SERVER_NAME);

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(SERVER_ADDRESS);
        new ResourceVerifier(SERVER_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(SERVER_ADDRESS);
    }

    @Page
    private MessagingPage page;

    private ProviderSettingsWindow wizardWindow;

    @Before
    public void before() {
        page.invokeProviderSettings(SERVER_NAME);
        wizardWindow = page.providerSettingsWindow();
        wizardWindow.switchToSecurityTab().maximize();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void updateClusterUser() throws Exception {
        final String value = "TESTER";
        new ConfigChecker.Builder(client, SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .wizardWindow(wizardWindow)
                .editAndSave(ConfigChecker.InputType.TEXT, CLUSTER_USER, value)
                .verifyFormSaved(NOT_SAVED_FAIL_MESSAGE)
                .verifyAttribute(CLUSTER_USER, value);
    }

    @Test
    public void updateClusterPassword() throws Exception {
        final String value = "TESTER.PASSWORD";
        new ConfigChecker.Builder(client, SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .wizardWindow(wizardWindow)
                .editAndSave(ConfigChecker.InputType.TEXT, CLUSTER_PASSWORD, value)
                .verifyFormSaved(NOT_SAVED_FAIL_MESSAGE)
                .verifyAttribute(CLUSTER_PASSWORD, value);
    }

    @Test
    public void toggleSecurityEnabled() throws Exception {
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(SERVER_ADDRESS, SECURITY_ENABLED);
        originalModelNodeResult.assertSuccess();
        final boolean originalValue = originalModelNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .wizardWindow(wizardWindow)
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, SECURITY_ENABLED, !originalValue)
                    .verifyFormSaved(NOT_SAVED_FAIL_MESSAGE)
                    .verifyAttribute(SECURITY_ENABLED, !originalValue);

            page.invokeProviderSettings(SERVER_NAME);
            wizardWindow = page.providerSettingsWindow();
            wizardWindow.switchToSecurityTab().maximize();

            new ConfigChecker.Builder(client, SERVER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .wizardWindow(wizardWindow)
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, SECURITY_ENABLED, originalValue)
                    .verifyFormSaved(NOT_SAVED_FAIL_MESSAGE)
                    .verifyAttribute(SECURITY_ENABLED, originalValue);
        } finally {
            operations.writeAttribute(SERVER_ADDRESS, SECURITY_ENABLED, originalModelNodeResult.value()).assertSuccess();
        }
    }

    @Test
    public void updateSecurityInvalidationInterval() throws Exception {
        final long value = 10L;
        new ConfigChecker.Builder(client, SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .wizardWindow(wizardWindow)
                .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_INVALIDATION_INTERVAL, String.valueOf(value))
                .verifyFormSaved(NOT_SAVED_FAIL_MESSAGE)
                .verifyAttribute(SECURITY_INVALIDATION_INTERVAL, value);
    }

    @Test
    public void updateSecurityDomain() throws Exception {
        final String value = "jboss-web-policy";
        new ConfigChecker.Builder(client, SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .wizardWindow(wizardWindow)
                .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_DOMAIN, value)
                .verifyFormSaved(NOT_SAVED_FAIL_MESSAGE)
                .verifyAttribute(SECURITY_DOMAIN, value);
    }
}
