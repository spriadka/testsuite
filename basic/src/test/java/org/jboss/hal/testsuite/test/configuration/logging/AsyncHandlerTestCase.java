package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AsyncHandlerTestCase extends LoggingAbstractTestCase {

    private static final String
            ASYNC_HANDLER = "async-handler",
            SUBHANDLERS = "subhandlers",
            QUEUE_LENGTH = "queue-length",
            LEVEL = "level",
            ENABLED = "enabled";

    private static final Address
            ASYNCHANDLER_ADDRESS = LOGGING_SUBSYSTEM.and(ASYNC_HANDLER, RandomStringUtils.randomAlphanumeric(6)),
            ASYNCHANDLER_TBR_ADDRESS = LOGGING_SUBSYSTEM.and(ASYNC_HANDLER, RandomStringUtils.randomAlphanumeric(6)),
            ASYNCHANDLER_TBA_ADDRESS = LOGGING_SUBSYSTEM.and(ASYNC_HANDLER, RandomStringUtils.randomAlphanumeric(6));

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(ASYNCHANDLER_ADDRESS, Values.of(QUEUE_LENGTH, 120)).assertSuccess();
        operations.add(ASYNCHANDLER_TBR_ADDRESS, Values.of(QUEUE_LENGTH, 120)).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(ASYNCHANDLER_ADDRESS);
        operations.removeIfExists(ASYNCHANDLER_TBA_ADDRESS);
        operations.removeIfExists(ASYNCHANDLER_TBR_ADDRESS);
    }

    @Drone
    private WebDriver browser;

    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToHandlerTab();
        page.switchToAsync();
        page.selectHandler(ASYNCHANDLER_ADDRESS.getLastPairValue());
    }

    @Test
    public void addAsyncHandler() throws Exception {
        page.addAsyncHandler(ASYNCHANDLER_TBA_ADDRESS.getLastPairValue(), "230");
        new ResourceVerifier(ASYNCHANDLER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void updateAsyncHandlerLevel() throws Exception {
        selectOptionAndVerify(ASYNCHANDLER_ADDRESS, LEVEL, "WARN");
    }

    @Test
    public void updateAsyncHandlerQueueLenWithWrongValue() {
        verifyIfErrorAppears(QUEUE_LENGTH, "0");
    }

    @Test
    public void updateAsyncHandlerQueueLen() throws Exception {
        editTextAndVerify(ASYNCHANDLER_ADDRESS, QUEUE_LENGTH, 90);
    }

    @Test
    public void disableAsyncHandler() throws Exception {
        editCheckboxAndVerify(ASYNCHANDLER_ADDRESS, ENABLED, false);
    }


    @Test
    public void addAsyncHandlerSubhandlers() throws Exception {
        addHandlers(ASYNCHANDLER_ADDRESS, SUBHANDLERS);
    }

    @Test
    public void addAsyncHandlerWrongSubhandlers() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.edit().text(SUBHANDLERS, "BLABLA"); //non existing handler

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed. But handlers are not really saved.", finished);

        operations.readAttribute(ASYNCHANDLER_ADDRESS, SUBHANDLERS).assertNotDefinedValue();
    }

    @Test
    public void removeAsyncHandler() throws Exception {
        page.removeInTable(ASYNCHANDLER_TBR_ADDRESS.getLastPairValue());

        new ResourceVerifier(ASYNCHANDLER_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
