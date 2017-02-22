package org.jboss.hal.testsuite.test.configuration.messaging.destinations;

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
public class AddressSettingsTestCase extends AbstractMessagingTestCase {

    private static final String PATTERN = "test-pattern_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PATTERN_TBA = "test-pattern-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PATTERN_TBR = "test-pattern-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address PATTERN_ADDRESS = DEFAULT_MESSAGING_SERVER.and("address-setting", PATTERN);
    private static final Address PATTERN_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("address-setting", PATTERN_TBA);
    private static final Address PATTERN_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("address-setting", PATTERN_TBR);

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(PATTERN_ADDRESS);
        new ResourceVerifier(PATTERN_ADDRESS, client).verifyExists();
        operations.add(PATTERN_TBR_ADDRESS);
        new ResourceVerifier(PATTERN_TBR_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewQueuesAndTopics("default");
        page.switchToAddressSettings();
        page.selectInTable(PATTERN);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(PATTERN_ADDRESS);
        operations.removeIfExists(PATTERN_TBA_ADDRESS);
        operations.removeIfExists(PATTERN_TBR_ADDRESS);
    }

    @Test
    public void addAddressSetting() throws Exception {
        page.addAddressSettings(PATTERN_TBA);
        new ResourceVerifier(PATTERN_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateDeadLetterAddressSetting() throws Exception {
        editTextAndVerify(PATTERN_ADDRESS, "deadLetterQueue", "dead-letter-address", "jms.queue.ExpiryQueue");
    }

    @Test
    public void updateExpiryAddressSetting() throws Exception {
        editTextAndVerify(PATTERN_ADDRESS, "expiryQueue", "expiry-address", "jms.queue.ExpiryQueue");
    }

    @Test
    public void updateRedeliveryDelayAddressSetting() throws Exception {
        editTextAndVerify(PATTERN_ADDRESS, "redeliveryDelay", "redelivery-delay", 10L);
    }

    @Test
    public void updateRedeliveryDelayAddressSettingWrongValue() {
        verifyIfErrorAppears("redeliveryDelay", "-1");
    }

    @Test
    public void updateMaxDeliveryAttemptsAddressSetting() throws Exception {
        editTextAndVerify(PATTERN_ADDRESS, "maxDelivery", "max-delivery-attempts", 0);
    }

    @Test
    public void removeAddressSetting() throws Exception {
        page.remove(PATTERN_TBR);
        new ResourceVerifier(PATTERN_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
