package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 3.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class DiscoveryGroupsTestCase extends AbstractMessagingTestCase {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryGroupsTestCase.class);

    private static final String DG_NAME = "dg-group-test" + RandomStringUtils.randomAlphanumeric(6);
    private static final String DG_TBR_NAME = "dg-group-test" + RandomStringUtils.randomAlphanumeric(6);
    private static final String DG_TBA_NAME = "dg-group-test" + RandomStringUtils.randomAlphanumeric(6);

    private static final Address DG_ADDRESS = DEFAULT_MESSAGING_SERVER.and("discovery-group", DG_NAME);
    private static final Address DG_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("discovery-group", DG_TBR_NAME);
    private static final Address DG_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and("discovery-group", DG_TBA_NAME);

    private static final List<String> socketBindings = new LinkedList<>();

    @BeforeClass
    public static void setUp() throws Exception {
        socketBindings.add(createSocketBinding());
        socketBindings.add(createSocketBinding());
        socketBindings.add(createSocketBinding());
        socketBindings.add(createSocketBinding());
        operations.add(DG_ADDRESS, Values.of("socket-binding", socketBindings.get(0)));
        new ResourceVerifier(DG_ADDRESS, client).verifyExists();
        operations.add(DG_TBR_ADDRESS, Values.of("socket-binding", socketBindings.get(1)));
        new ResourceVerifier(DG_TBR_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, CommandFailedException, TimeoutException, InterruptedException {
        operations.removeIfExists(DG_ADDRESS);
        operations.removeIfExists(DG_TBA_ADDRESS);
        operations.removeIfExists(DG_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        page.navigateToMessaging();
        page.selectView("Clustering");
        page.switchToDiscovery();
        page.selectInTable(DG_NAME, 0);
    }

    @Test
    public void addDiscoveryGroup() {
        page.addDiscoveryGroup(DG_NAME, socketBindings.get(2));

        new ResourceVerifier(DG_ADDRESS, client);
    }

    @Test
    public void updateDiscoveryGroupSocketBinding() throws Exception {
        editTextAndVerify(DG_ADDRESS, "socketBinding", "socket-binding", socketBindings.get(3));
    }

    @Test
    public void updateDiscoveryGroupRefreshTimeout() throws Exception {
        editTextAndVerify(DG_ADDRESS, "refreshTimeout", "refresh-timeout", 2000L);
    }

    @Test
    public void updateBroadcastGroupRefreshTimeoutNegativeValue() {
        verifyIfErrorAppears("refreshTimeout", "-1");
    }

    @Test
    public void updateDiscoveryGroupInitialTimeout() throws Exception {
        editTextAndVerify(DG_ADDRESS, "initialWaitTimeout", "initial-wait-timeout", 200L);
    }

    @Test
    public void updateBroadcastGroupInitialTimeoutNegativeValue() {
        verifyIfErrorAppears("initialWaitTimeout", "-1");
    }

    @Test
    public void removeDiscoveryGroup() throws Exception {
        page.selectInTable(DG_TBR_NAME, 0);
        page.remove();

        new ResourceVerifier(DG_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
