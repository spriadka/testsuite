package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.LoggingPage;
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

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 26.8.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class AsyncHandlerTestCase extends LoggingAbstractTestCase {
    private static final String ASYNCHANDLER = "asyncHandler" + RandomStringUtils.randomAlphanumeric(6);
    private static final String ASYNCHANDLER_TBR = "asyncHandler-TBR_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String ASYNCHANDLER_TBA = "asyncHandler-TBA_" + RandomStringUtils.randomAlphanumeric(6);

    private static final Address ASYNCHANDLER_ADDRESS = LOGGING_SUBSYSTEM.and("async-handler", ASYNCHANDLER);
    private static final Address ASYNCHANDLER_ADDRESS_TBR = LOGGING_SUBSYSTEM.and("async-handler", ASYNCHANDLER_TBR);
    private static final Address ASYNCHANDLER_ADDRESS_TBA = LOGGING_SUBSYSTEM.and("async-handler", ASYNCHANDLER_TBA);

    @BeforeClass
    public static void setUp() throws Exception {
        operations.add(ASYNCHANDLER_ADDRESS, Values.of("queue-length", 120));
        new ResourceVerifier(ASYNCHANDLER_ADDRESS, client).verifyExists();
        operations.add(ASYNCHANDLER_ADDRESS_TBR, Values.of("queue-length", 120));
        new ResourceVerifier(ASYNCHANDLER_ADDRESS_TBR, client).verifyExists();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(ASYNCHANDLER_ADDRESS);
        operations.removeIfExists(ASYNCHANDLER_ADDRESS_TBA);
        operations.removeIfExists(ASYNCHANDLER_ADDRESS_TBR);
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
        page.selectHandler(ASYNCHANDLER);
    }

    @Test
    public void addAsyncHandler() throws Exception {
        page.addAsyncHandler(ASYNCHANDLER_TBA, "230");
        new ResourceVerifier(ASYNCHANDLER_ADDRESS_TBA, client).verifyExists();
    }

    @Test
    public void updateAsyncHandlerLevel() throws Exception {
        selectOptionAndVerify(ASYNCHANDLER_ADDRESS, "level", "WARN");
    }

    @Test
    public void updateAsyncHandlerQueueLenWithWrongValue() {
        verifyIfErrorAppears("queue-length", "0");
    }

    @Test
    public void updateAsyncHandlerQueueLen() throws Exception {
        editTextAndVerify(ASYNCHANDLER_ADDRESS, "queue-length", 90);
    }

    @Test
    public void disableAsyncHandler() throws Exception {
        editCheckboxAndVerify(ASYNCHANDLER_ADDRESS, "enabled", false);
    }


    @Test
    public void addAsyncHandlerSubhandlers() throws Exception {
        editTextAreaAndVerify(ASYNCHANDLER_ADDRESS, "subhandlers", new String[]{"CONSOLE", "FILE"});
    }

    @Test
    public void addAsyncHandlerWrongSubhandlers() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();

        editPanelFragment.edit().text("subhandlers", "BLABLA"); //non existing handler

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed. But handlers are not really saved.", finished);

        operations.readAttribute(ASYNCHANDLER_ADDRESS, "subhandlers").assertNotDefinedValue();
    }

    @Test
    public void removeAsyncHandler() throws Exception {
        page.removeInTable(ASYNCHANDLER_TBR);

        new ResourceVerifier(ASYNCHANDLER_ADDRESS_TBR, client).verifyDoesNotExist();
    }
}
