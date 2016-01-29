package org.jboss.hal.testsuite.test.configuration.messaging;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

/**
 * Created by pcyprian on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class MessagingProviderTestCase {
    private static final String NAME = "test-provider";
    private static final String SECURITYDOMAIN = "other";
    private static final String USER = "tester";
    private static final String PASSWORD = "12345";
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();

    private Address address = Address.subsystem("messaging-activemq").and("server", NAME);

    @AfterClass
    public static void tearDown() {
        IOUtils.closeQuietly(client);
    }

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @Test
    @InSequence(0)
    public void addMessagingProvider() throws Exception {
        page.navigateToMessagingProvider();
        page.makeNavigation();
        page.createProvider(NAME, true, SECURITYDOMAIN, USER, PASSWORD);

        new ResourceVerifier(address, client).verifyExists()
            .verifyAttribute("security-enabled", true)
            .verifyAttribute("security-domain", SECURITYDOMAIN)
            .verifyAttribute("cluster-user", USER)
            .verifyAttribute("cluster-password", PASSWORD);
    }

    @Test
    @InSequence(1)
    public void removeMessagingProvider() throws Exception {
        page.selectProvider(NAME);
        page.removeProvider();

        new ResourceVerifier(address, client).verifyDoesNotExist();
    }
}
