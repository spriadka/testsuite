package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecurityTestCase extends AbstractMessagingTestCase {
    private static final String SERVER_NAME  = "test-provider_" + RandomStringUtils.randomAlphanumeric(5);

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

    @Before
    public void before() {
        page.selectProvider(SERVER_NAME);
        page.InvokeProviderSettings();
        page.switchToSecurityTab();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void updateClusterUser() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "cluster-user", "TESTER");
    }

    @Test
    public void updateClusterPassword() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "cluster-password", "TESTER.PASSWORD");
    }

    @Test
    public void updateSecurityEnabled() throws Exception {
        editCheckboxAndVerify(SERVER_ADDRESS, "security-enabled", false);
    }

    @Test
    public void updateSecurityInvalidationInterval() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "security-invalidation-interval", 10L);
    }

    @Test
    public void updateSecurityDomain() throws Exception {
        editTextAndVerify(SERVER_ADDRESS, "security-domain", "jboss-web-policy");
    }
}
