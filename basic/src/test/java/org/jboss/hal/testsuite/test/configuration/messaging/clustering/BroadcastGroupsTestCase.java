package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class BroadcastGroupsTestCase extends AbstractMessagingTestCase {

    private static final String
            HAL1327_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1327",
            HAL1328_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1328",
            HAL1329_FAIL_MESSAGE = "Probably fails becasue of https://issues.jboss.org/browse/HAL-1329";

    private static final String
            SERVER = "server",
            BROADCAST_PERIOD = "broadcast-period",
            CONNECTORS = "connectors",
            CONNECTOR = "connector",
            BROADCAST_GROUP = "broadcast-group",
            FACTORY_CLASS = "factory-class",
            SOCKET_BINDING = "socket-binding";

    private static final Address NEW_SERVER = MESSAGING_SUBSYSTEM.and(SERVER, RandomStringUtils.randomAlphanumeric(6));

    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(NEW_SERVER).assertSuccess();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, InterruptedException, OperationException, TimeoutException {
        operations.removeIfExists(NEW_SERVER);
    }

    @Test
    public void addBroadcastGroup() throws Exception {
        final Address connector = createConnector(NEW_SERVER),
                broadcastGroupAddress = NEW_SERVER.and(BROADCAST_GROUP, RandomStringUtils.randomAlphabetic(7));
        try {
            page.viewClusteringSettings(NEW_SERVER.getLastPairValue());
            page.selectInTable(broadcastGroupAddress.getLastPairValue(), 0);

            page.addBroadcastGroup()
                    .name(broadcastGroupAddress.getLastPairValue())
                    .socketBinding(createSocketBinding())
                    .connectors(connector.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed(HAL1327_FAIL_MESSAGE);
            new ResourceVerifier(broadcastGroupAddress, client).verifyExists();
        } finally {
            operations.removeIfExists(broadcastGroupAddress);
            operations.removeIfExists(connector);
        }
    }

    @Test
    public void updateBroadcastGroupPeriod() throws Exception {
        final long value = 1000;

        Address connectorAddress = null,
                broadcastGroupAddress = null;
        try {
            connectorAddress = createConnector(NEW_SERVER);
            broadcastGroupAddress = createBroadcastGroup(NEW_SERVER, createSocketBinding(), connectorAddress);

            page.viewClusteringSettings(NEW_SERVER.getLastPairValue());
            page.selectInTable(broadcastGroupAddress.getLastPairValue(), 0);

            new ConfigChecker.Builder(client, broadcastGroupAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, BROADCAST_PERIOD, String.valueOf(value))
                    .verifyFormSaved(HAL1328_FAIL_MESSAGE)
                    .verifyAttribute(BROADCAST_PERIOD, value);
        } finally {
            if (broadcastGroupAddress != null) {
                operations.removeIfExists(broadcastGroupAddress);
            }
            if (connectorAddress != null) {
                operations.removeIfExists(connectorAddress);
            }
        }
    }

    @Test
    public void updateBroadcastGroupPeriodNegativeValue() throws Exception {
        Address connectorAddress = null,
                broadcastGroupAddress = null;
        try {
            connectorAddress = createConnector(NEW_SERVER);
            broadcastGroupAddress = createBroadcastGroup(NEW_SERVER, createSocketBinding(), connectorAddress);

            page.viewClusteringSettings(NEW_SERVER.getLastPairValue());
            page.selectInTable(broadcastGroupAddress.getLastPairValue(), 0);

            new ConfigChecker.Builder(client, broadcastGroupAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, BROADCAST_PERIOD, "-1")
                    .verifyFormNotSaved();
        } finally {
            if (broadcastGroupAddress != null) {
                operations.removeIfExists(broadcastGroupAddress);
            }
            if (connectorAddress != null) {
                operations.removeIfExists(connectorAddress);
            }
        }
    }


    @Test
    public void updateBroadcastGroupConnectors() throws Exception {
        Address connectorAddress = null,
                newConnectorAddress = null,
                broadcastGroupAddress = null;
        try {
            connectorAddress = createConnector(NEW_SERVER);
            newConnectorAddress = createConnector(NEW_SERVER);
            broadcastGroupAddress = createBroadcastGroup(NEW_SERVER, createSocketBinding(), connectorAddress);

            page.viewClusteringSettings(NEW_SERVER.getLastPairValue());
            page.selectInTable(broadcastGroupAddress.getLastPairValue(), 0);

            new ConfigChecker.Builder(client, broadcastGroupAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, CONNECTORS, newConnectorAddress.getLastPairValue())
                    .verifyFormSaved(HAL1329_FAIL_MESSAGE)
                    .verifyAttribute(CONNECTORS,
                            new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(newConnectorAddress.getLastPairValue()))
                                    .build());
        } finally {
            if (broadcastGroupAddress != null) {
                operations.removeIfExists(broadcastGroupAddress);
            }
            if (connectorAddress != null) {
                operations.removeIfExists(connectorAddress);
            }
            if (newConnectorAddress != null) {
                operations.removeIfExists(newConnectorAddress);
            }
        }
    }

    @Test
    public void removeBroadcastGroup() throws Exception {
        Address connectorAddress = null,
                broadcastGroupAddress = null;
        try {
            connectorAddress = createConnector(NEW_SERVER);
            broadcastGroupAddress = createBroadcastGroup(NEW_SERVER, createSocketBinding(), connectorAddress);

            page.viewClusteringSettings(NEW_SERVER.getLastPairValue());
            page.selectInTable(broadcastGroupAddress.getLastPairValue(), 0);

            page.remove(broadcastGroupAddress.getLastPairValue());

        } finally {
            if (broadcastGroupAddress != null) {
                operations.removeIfExists(broadcastGroupAddress);
            }
            if (connectorAddress != null) {
                operations.removeIfExists(connectorAddress);
            }
        }

        new ResourceVerifier(broadcastGroupAddress, client).verifyDoesNotExist();
    }

    private Address createConnector(Address serverAddress) throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        final Address address = serverAddress.and(CONNECTOR, RandomStringUtils.randomAlphabetic(7));
        operations.add(address,
                Values.of(FACTORY_CLASS, "org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory"))
                .assertSuccess();
        administration.reloadIfRequired();
        return address;
    }

    private Address createBroadcastGroup(Address serverAddress, String socketBinding, Address connectorAddress) throws IOException {
        final Address address = serverAddress.and(BROADCAST_GROUP, RandomStringUtils.randomAlphabetic(7));
        operations.add(address, Values.of(CONNECTORS,
                new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(connectorAddress.getLastPairValue())).build())
            .and(SOCKET_BINDING, socketBinding)).assertSuccess();
        return address;
    }
}
