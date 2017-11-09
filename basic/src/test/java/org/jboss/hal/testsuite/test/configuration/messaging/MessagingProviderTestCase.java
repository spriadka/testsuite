package org.jboss.hal.testsuite.test.configuration.messaging;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class MessagingProviderTestCase {
    private static final String
            PROVIDER_TBR_NAME = "test-provider_tbr" + RandomStringUtils.randomAlphanumeric(5),
            PROVIDER_TBA_NAME = "test-provider_tba" + RandomStringUtils.randomAlphanumeric(5),
            SECURITY_DOMAIN = "other",
            USER = "tester",
            PASSWORD = "12345";

    private static final Address
            MESSAGING_SUBSYSTEM_ADDRESS = Address.subsystem("messaging-activemq"),
            PROVIDER_TBR_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS.and("server", PROVIDER_TBR_NAME),
            PROVIDER_TBA_ADDRESS = MESSAGING_SUBSYSTEM_ADDRESS.and("server", PROVIDER_TBA_NAME);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);


    @BeforeClass
    public static void beforeClass() throws IOException {
        operations.add(PROVIDER_TBR_ADDRESS).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(PROVIDER_TBR_ADDRESS);
            operations.removeIfExists(PROVIDER_TBA_ADDRESS);
            new Administration(client).reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Drone
    private WebDriver browser;

    @Page
    private MessagingPage page;

    @Test
    public void addMessagingProvider() throws Exception {
        page.addMessagingProvider()
                .name(PROVIDER_TBA_NAME)
                .clusterPassword(PASSWORD)
                .clusterUser(USER)
                .securityEnabled(true)
                .clickSave();

        new ResourceVerifier(PROVIDER_TBA_ADDRESS, client).verifyExists()
                .verifyAttribute("security-enabled", true)
                .verifyAttribute("security-domain", SECURITY_DOMAIN)
                .verifyAttribute("cluster-user", USER)
                .verifyAttribute("cluster-password", PASSWORD);
    }

    @Test
    public void removeMessagingProvider() throws Exception {
        page.removeMessagingProvider(PROVIDER_TBR_NAME).confirmAndDismissReloadRequiredMessage();

        new ResourceVerifier(PROVIDER_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
