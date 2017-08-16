package org.jboss.hal.testsuite.test.configuration.messaging.providerSettings;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.fragment.config.messaging.ProviderSettingsWindow;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
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

@Category(Elytron.class)
@RunWith(Arquillian.class)
public class CredentialReferenceTestCase extends AbstractMessagingTestCase {

    private static final String
            SERVER = "server",
            CLUSTER_CREDENTIAL_REFERENCE = "cluster-credential-reference";

    private static final Address SERVER_ADDRESS = MESSAGING_SUBSYSTEM.and(SERVER, RandomStringUtils.randomAlphanumeric(5));

    private ProviderSettingsWindow providerSettingsWindow;

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(SERVER_ADDRESS);
    }

    @Before
    public void before() {
        page.invokeProviderSettings(SERVER_ADDRESS.getLastPairValue());
        providerSettingsWindow = page.providerSettingsWindow().switchToClusterCredentialReferenceTab();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(SERVER_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void editCredentialReferenceClearText() throws Exception {
        integrationNCheckerBasedOnServerMode()
                .credetialReferenceAttributeName(CLUSTER_CREDENTIAL_REFERENCE)
                .build()
                .setClearTextCredentialReferenceAndVerify();
    }

    @Test
    public void editCredentialReferenceStoreReference() throws Exception {
        integrationNCheckerBasedOnServerMode()
                .credetialReferenceAttributeName(CLUSTER_CREDENTIAL_REFERENCE)
                .build()
                .setCredentialStoreCredentialReferenceAndVerify();
    }

    @Test
    public void editCredentialReferenceIllegalCombination() throws Exception {
        integrationNCheckerBasedOnServerMode()
                .credetialReferenceAttributeName(CLUSTER_CREDENTIAL_REFERENCE)
                .build()
                .testIllegalCombinationCredentialReferenceAttributes();
    }

    private ElytronIntegrationChecker.Builder integrationNCheckerBasedOnServerMode() {
        ElytronIntegrationChecker.Builder builder = new ElytronIntegrationChecker.Builder(client)
                .address(SERVER_ADDRESS)
                .configFragment(page.getConfigFragment());
        if (ConfigUtils.isDomain()) {
            builder.wizardWindow(providerSettingsWindow);
        }
        return builder;
    }
}
