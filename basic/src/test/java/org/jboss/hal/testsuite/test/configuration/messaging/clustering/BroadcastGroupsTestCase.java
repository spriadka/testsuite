package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
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

/**
 * Created by pcyprian on 2.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class BroadcastGroupsTestCase extends AbstractMessagingTestCase {
    private static final String
            SERVER_NAME = "test-server_" + RandomStringUtils.randomAlphanumeric(6),
            CONNECTOR_NAME = "connector_" + RandomStringUtils.randomAlphanumeric(6),
            BG_NAME = "bg-group-test_" + RandomStringUtils.randomAlphanumeric(6),
            BG_TBR_NAME = "bg-group-test-TBR_" + RandomStringUtils.randomAlphanumeric(6),
            BG_TBA_NAME = "bg-group-test-TBA_" + RandomStringUtils.randomAlphanumeric(6),
            BROADCAST_PERIOD = "broadcast-period",
            CONNECTORS = "connectors",
            HAL1327_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1327",
            HAL1328_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1328",
            HAL1329_FAIL_MESSAGE = "Probably fails becasue of https://issues.jboss.org/browse/HAL-1329";

    private static final Address NEW_SERVER = MESSAGING_SUBSYSTEM.and("server", SERVER_NAME);
    private static final Address CONNECTOR_ADDRESS = NEW_SERVER.and("connector", CONNECTOR_NAME);
    private static final Address BG_ADDRESS = NEW_SERVER.and("broadcast-group", BG_NAME);
    private static final Address BG_TBA_ADDRESS = NEW_SERVER.and("broadcast-group", BG_TBA_NAME);
    private static final Address BG_TBR_ADDRESS = NEW_SERVER.and("broadcast-group", BG_TBR_NAME);

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(NEW_SERVER);
        new ResourceVerifier(NEW_SERVER, client).verifyExists();
        operations.add(CONNECTOR_ADDRESS, Values.of("factory-class", "org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory"));
        new ResourceVerifier(CONNECTOR_ADDRESS, client).verifyExists();
        operations.add(BG_ADDRESS);
        new ResourceVerifier(BG_ADDRESS, client).verifyExists();
        operations.add(BG_TBR_ADDRESS);
        new ResourceVerifier(BG_TBR_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewClusteringSettings(SERVER_NAME);
        page.selectInTable(BG_NAME, 0);
    }
    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, OperationException, TimeoutException {
        operations.removeIfExists(BG_ADDRESS);
        operations.removeIfExists(BG_TBA_ADDRESS);
        operations.removeIfExists(BG_TBR_ADDRESS);
        administration.reloadIfRequired();
        operations.removeIfExists(NEW_SERVER);
    }

    @Test
    public void addBroadcastGroup() throws Exception {
        boolean isSaved = page.addBroadcastGroup()
                .name(BG_TBA_NAME)
                .socketBinding(createSocketBinding())
                .saveAndDismissReloadRequiredWindow();
        Assert.assertTrue("Form should be saved! " + HAL1327_FAIL_MESSAGE, isSaved);
        new ResourceVerifier(BG_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateBroadcastGroupPeriod() throws Exception {
        final long value = 1000;

        new ConfigChecker.Builder(client, BG_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, BROADCAST_PERIOD, String.valueOf(value))
                .verifyFormSaved(HAL1328_FAIL_MESSAGE)
                .verifyAttribute(BROADCAST_PERIOD, value);
    }

    @Test
    public void updateBroadcastGroupPeriodNegativeValue() throws Exception {
        verifyIfErrorAppears("broadcast-period", "-1");
    }


    @Test
    public void updateBroadcastGroupConnectors() throws Exception {
        final ModelNode expected = new ModelNode().add(CONNECTOR_NAME);

        new ConfigChecker.Builder(client, BG_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, CONNECTORS, CONNECTOR_NAME)
                .verifyFormSaved(HAL1329_FAIL_MESSAGE)
                .verifyAttribute(CONNECTORS, expected);
    }

    @Test
    public void removeBroadcastGroup() throws Exception {
        page.remove(BG_TBR_NAME);

        new ResourceVerifier(BG_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
