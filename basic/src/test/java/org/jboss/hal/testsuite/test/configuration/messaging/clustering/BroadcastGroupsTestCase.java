package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 2.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class BroadcastGroupsTestCase extends AbstractMessagingTestCase {
    private static final String SERVER_NAME = "test-server_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String CONNECTOR_NAME = "connector_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String BG_NAME = "bg-group-test_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String BG_TBR_NAME = "bg-group-test-TBR_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String BG_TBA_NAME = "bg-group-test-TBA_" + RandomStringUtils.randomAlphanumeric(6);

    private static final Address NEW_SERVER = MESSAGING_SUBSYSTEM.and("server", SERVER_NAME);
    private static final Address CONNECTOR_ADDRESS = NEW_SERVER.and("connector", CONNECTOR_NAME);
    private static final Address BG_ADDRESS = NEW_SERVER.and("broadcast-group", BG_NAME);
    private static final Address BG_TBA_ADDRESS = NEW_SERVER.and("broadcast-group", BG_TBA_NAME);
    private static final Address BG_TBR_ADDRESS = NEW_SERVER.and("broadcast-group", BG_TBR_NAME);

    @Drone
    private WebDriver browser;
    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(NEW_SERVER);
        administration.reloadIfRequired();
        new ResourceVerifier(NEW_SERVER, client).verifyExists();
        operations.add(CONNECTOR_ADDRESS, Values.of("factory-class", "foo"));
        administration.reloadIfRequired();
        new ResourceVerifier(CONNECTOR_ADDRESS, client).verifyExists();
        operations.add(BG_ADDRESS);
        administration.reloadIfRequired();
        new ResourceVerifier(BG_ADDRESS, client).verifyExists();
        operations.add(BG_TBR_ADDRESS);
        administration.reloadIfRequired();
        new ResourceVerifier(BG_TBR_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.selectProvider(SERVER_NAME);
        page.selectView("Clustering");
        page.selectInTable(BG_NAME, 0);
    }
    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, OperationException, TimeoutException {
        operations.removeIfExists(BG_ADDRESS);
        operations.removeIfExists(BG_TBA_ADDRESS);
        operations.removeIfExists(BG_TBR_ADDRESS);
        administration.reloadIfRequired();
        operations.removeIfExists(NEW_SERVER);
        administration.reloadIfRequired();
    }

    @Test
    public void addBroadcastGroup() throws Exception {
        page.addBroadcastGroup(BG_TBA_NAME, createSocketBinding());
        new ResourceVerifier(BG_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateBroadcastGroupPeriod() throws Exception {
        editTextAndVerify(BG_ADDRESS, "broadcast-period", 1000L);
    }

    @Test
    public void updateBroadcastGroupPeriodNegativeValue() throws Exception {
        verifyIfErrorAppears("broadcast-period", "-1");
    }


    @Test
    public void updateBroadcastGroupConnectors() throws Exception {
        page.getConfigFragment().editTextAndSave("connectors", CONNECTOR_NAME);
        ModelNode expected = new ModelNode().add(CONNECTOR_NAME);
        new ResourceVerifier(BG_ADDRESS, client).verifyAttribute("connectors", expected);
    }

    @Test
    public void removeBroadcastGroup() throws Exception {
        page.selectInTable(BG_TBR_NAME, 0);
        page.remove();

        new ResourceVerifier(BG_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
