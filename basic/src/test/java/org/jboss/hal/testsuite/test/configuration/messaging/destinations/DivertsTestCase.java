package org.jboss.hal.testsuite.test.configuration.messaging.destinations;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
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
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class DivertsTestCase extends AbstractMessagingTestCase {

    private static final String DIVERT = "test-divert_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String DIVERT_TBA = "test-divert-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String DIVERT_TBR = "test-divert-TBR_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address DIVERT_ADDRESS = DEFAULT_MESSAGING_SERVER.and("divert", DIVERT);
    private static final Address DIVERT_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("divert", DIVERT_TBA);
    private static final Address DIVERT_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("divert", DIVERT_TBR);

    private static final String DIVERT_ADDRESS_ARG = "divert";
    private static final String FORWARD_ADDRESS_ARG = "forward";


    @BeforeClass
    public static void setUp() throws Exception {
        addDivert(DIVERT_ADDRESS, FORWARD_ADDRESS_ARG, DIVERT_ADDRESS_ARG, DIVERT);
        addDivert(DIVERT_TBR_ADDRESS, FORWARD_ADDRESS_ARG, DIVERT_ADDRESS_ARG, DIVERT_TBR);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(DIVERT_ADDRESS);
        operations.removeIfExists(DIVERT_TBA_ADDRESS);
        operations.removeIfExists(DIVERT_TBR_ADDRESS);
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectQueuesAndTopics();
        page.switchToDiverts();
        page.selectInTable(DIVERT);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void addDiverts() throws Exception {
        page.addDiverts(DIVERT_TBA, DIVERT_ADDRESS_ARG, FORWARD_ADDRESS_ARG);
        new ResourceVerifier(DIVERT_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateDivertsDivertAddress() throws Exception {
        editTextAndVerify(DIVERT_ADDRESS, "divertAddress", "divert-address", "divAdd");
    }

    @Test
    public void updateDivertsForwardAddress() throws Exception {
        editTextAndVerify(DIVERT_ADDRESS, "forwardingAddress", "forwarding-address", "fowAdd");
    }

    @Test
    public void updateDivertExclusive() throws Exception {
        editCheckboxAndVerify(DIVERT_ADDRESS, "exclusive", true);
    }

    @Test
    public void updateDivertsFilter() throws Exception {
        editTextAndVerify(DIVERT_ADDRESS, "filter", "myFilter");
    }

    @Test
    public void updateDivertsTransformerClass() throws Exception {
        BackupAndRestoreAttributes backup = new BackupAndRestoreAttributes.Builder(DIVERT_ADDRESS)
                .build();
        try {
            client.apply(backup.backup());
            editTextAndVerify(DIVERT_ADDRESS, "transformerClass", "transformer-class-name", "clazz");
        } finally {
            client.apply(backup.restore());
        }
    }

    @Test
    public void removeDiverts() throws Exception {
        page.remove(DIVERT_TBR);
        new ResourceVerifier(DIVERT_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    private static void addDivert(Address address, String forwardingAddress, String divertAddress, String routingName) throws Exception {
        operations.add(address, Values.empty()
                .and("divert-address", divertAddress)
                .and("forwarding-address", forwardingAddress)
                .and("routing-name", routingName));
        new ResourceVerifier(address, client).verifyExists();
    }
}
