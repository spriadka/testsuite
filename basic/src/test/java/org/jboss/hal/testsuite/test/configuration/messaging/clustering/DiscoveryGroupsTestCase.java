package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class DiscoveryGroupsTestCase extends AbstractMessagingTestCase {

    private static final String DG_NAME = "dg-group-test" + RandomStringUtils.randomAlphanumeric(6);
    private static final String DG_TBR_NAME = "dg-group-test" + RandomStringUtils.randomAlphanumeric(6);
    private static final String DG_TBA_NAME = "dg-group-test" + RandomStringUtils.randomAlphanumeric(6);

    private static final String
            JGROUPS_CHANNEL = "jgroups-channel",
            DISCOVERY_GROUP = "discovery-group",
            SOCKET_BINDING = "socket-binding";

    private static final Address DG_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP, DG_NAME);
    private static final Address DG_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP, DG_TBR_NAME);
    private static final Address DG_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP, DG_TBA_NAME);
    private static final Address DG_TBA2_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP, "dg-group_TBA2_" + RandomStringUtils.randomAlphanumeric(5));
    private static final Address DG_INV_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP, "dg-group_INV_" + RandomStringUtils.randomAlphanumeric(5));

    private static final List<String> socketBindings = new LinkedList<>();

    @BeforeClass
    public static void setUp() throws Exception {
        socketBindings.add(createSocketBinding());
        socketBindings.add(createSocketBinding());
        operations.add(DG_ADDRESS, Values.of(JGROUPS_CHANNEL, "ee")).assertSuccess();
        operations.add(DG_TBR_ADDRESS, Values.of(JGROUPS_CHANNEL, "ee")).assertSuccess();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, CommandFailedException, TimeoutException, InterruptedException {
        operations.removeIfExists(DG_ADDRESS);
        operations.removeIfExists(DG_TBA_ADDRESS);
        operations.removeIfExists(DG_TBA2_ADDRESS);
        operations.removeIfExists(DG_TBR_ADDRESS);
        operations.removeIfExists(DG_INV_ADDRESS);
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.viewClusteringSettings("default");
        page.switchToDiscovery();
        page.selectInTable(DG_NAME, 0);
    }

    @Ignore("Ignored until https://issues.jboss.org/browse/WFLY-6607 is resolved")
    @Test
    public void addDiscoveryGroupWithSocketBindingDefined() throws Exception {
        page.addDiscoveryGroup()
                .name(DG_TBA_ADDRESS.getLastPairValue())
                .socketBinding(socketBindings.get(0))
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowClosed();

        Assert.assertTrue("Discovery group should be present!",
                page.getResourceManager().isResourcePresent(DG_TBA_ADDRESS.getLastPairValue()));

        new ResourceVerifier(DG_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void addDiscoveryGroupWithJGroupsChannelDefined() throws Exception {
        page.addDiscoveryGroup()
                .name(DG_TBA2_ADDRESS.getLastPairValue())
                .jgroupsChannel("ee")
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowClosed();

        Assert.assertTrue("Discovery group should be present!",
                page.getResourceManager().isResourcePresent(DG_TBA2_ADDRESS.getLastPairValue()));

        new ResourceVerifier(DG_TBA2_ADDRESS, client).verifyExists();
    }

    @Test
    public void addDiscoveryGroupWithOnlyNameDefined() throws Exception {
        page.addDiscoveryGroup()
                .name(DG_INV_ADDRESS.getLastPairValue())
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowOpen();

        Assert.assertFalse("Discovery group should NOT be present!",
                page.getResourceManager().isResourcePresent(DG_INV_ADDRESS.getLastPairValue()));

        new ResourceVerifier(DG_INV_ADDRESS, client).verifyDoesNotExist();
    }

    @Ignore("Ignored until https://issues.jboss.org/browse/WFLY-6607 is resolved")
    @Test
    public void updateDiscoveryGroupSocketBinding() throws Exception {
        editTextAndVerify(DG_ADDRESS, SOCKET_BINDING, socketBindings.get(1));
    }

    @Test
    public void updateDiscoveryGroupRefreshTimeout() throws Exception {
        editTextAndVerify(DG_ADDRESS, "refresh-timeout", 2000L);
    }

    @Test
    public void updateBroadcastGroupRefreshTimeoutNegativeValue() {
        verifyIfErrorAppears("refresh-timeout", "-1");
    }

    @Test
    public void updateDiscoveryGroupInitialTimeout() throws Exception {
        editTextAndVerify(DG_ADDRESS, "initial-wait-timeout", 200L);
    }

    @Test
    public void updateBroadcastGroupInitialTimeoutNegativeValue() {
        verifyIfErrorAppears("initial-wait-timeout", "-1");
    }

    @Test
    public void removeDiscoveryGroup() throws Exception {
        page.selectInTable(DG_TBR_NAME, 0);
        page.remove();

        new ResourceVerifier(DG_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
