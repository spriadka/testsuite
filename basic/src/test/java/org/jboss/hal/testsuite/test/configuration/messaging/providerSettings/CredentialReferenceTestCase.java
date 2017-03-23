package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
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
public class CredentialReferenceTestCase extends AbstractMessagingTestCase {

    private static final String SERVER_NAME = "test-provider_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and("server", SERVER_NAME);

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(SERVER_ADDRESS);
    }

    @Before
    public void before() {
        page.invokeProviderSettings(SERVER_NAME);
        page.providerSettingsWindow().switchToClusterCredentialReferenceTab();
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(SERVER_ADDRESS);
    }

    @Test
    public void editCredentialReferenceClearText() throws Exception {
        new ElytronIntegrationChecker.Builder(client)
                .address(SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("cluster-credential-reference")
                .build()
                .setClearTextCredentialReferenceAndVerify();
    }

    @Test
    public void editCredentialReferenceStoreReference() throws Exception {
        new ElytronIntegrationChecker.Builder(client)
                .address(SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("cluster-credential-reference")
                .build()
                .setCredentialStoreCredentialReferenceAndVerify();
    }

    @Test
    public void editCredentialReferenceIllegalCombination() throws Exception {
        new ElytronIntegrationChecker.Builder(client)
                .address(SERVER_ADDRESS)
                .configFragment(page.getConfigFragment())
                .credetialReferenceAttributeName("cluster-credential-reference")
                .build()
                .testIllegalCombinationCredentialReferenceAttributes();
    }
}
