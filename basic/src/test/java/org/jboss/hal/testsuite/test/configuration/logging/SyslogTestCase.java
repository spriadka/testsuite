package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 27.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class SyslogTestCase extends LoggingAbstractTestCase {

    private static final String SYSLOG_HANDLER = "syslogHandler" + RandomStringUtils.randomAlphanumeric(5);
    //TBR ~ To Be Removed
    private static final String SYSLOG_HANDLER_TBR = "syslogHandlerTBR" + RandomStringUtils.randomAlphanumeric(5);
    //TBA ~ To Be Added
    private static final String SYSLOG_HANDLER_TBA = "syslogHandlerTBA" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address SYSLOG_HANDLER_ADDRESS = LOGGING_SUBSYSTEM
            .and("syslog-handler", SYSLOG_HANDLER);
    private static final Address SYSLOG_HANDLER_TBR_ADDRESS = LOGGING_SUBSYSTEM
            .and("syslog-handler", SYSLOG_HANDLER_TBR);
    private static final Address SYSLOG_HANDLER_TBA_ADDRESS = LOGGING_SUBSYSTEM
            .and("syslog-handler", SYSLOG_HANDLER_TBA);

    @BeforeClass
    public static void beforeClass() throws Exception {
        operations.add(SYSLOG_HANDLER_ADDRESS);
        new ResourceVerifier(SYSLOG_HANDLER_ADDRESS, client).verifyExists();
        operations.add(SYSLOG_HANDLER_TBR_ADDRESS);
        new ResourceVerifier(SYSLOG_HANDLER_TBR_ADDRESS, client).verifyExists();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(SYSLOG_HANDLER_ADDRESS);
        operations.removeIfExists(SYSLOG_HANDLER_TBA_ADDRESS);
        operations.removeIfExists(SYSLOG_HANDLER_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToHandlerTab();
        page.switchToSyslog();
        page.selectHandler(SYSLOG_HANDLER);
    }

    @Test
    public void addSyslogHandler() throws Exception {
        page.addSyslogHandler(SYSLOG_HANDLER_TBA);
        new ResourceVerifier(SYSLOG_HANDLER_TBA_ADDRESS, client).verifyExists();
    }

    @Test //https://issues.jboss.org/browse/HAL-813
    public void updateSyslogHandlerPortWrongValue() throws Exception {
        verifyIfErrorAppears("port", "-1");
    }

    @Test
    public void updateSyslogHandlerPort() throws Exception {
        editTextAndVerify(SYSLOG_HANDLER_ADDRESS, "port", 0);
    }

    @Test
    public void updateSyslogHandlerFacility() throws Exception {
        selectOptionAndVerify(SYSLOG_HANDLER_ADDRESS, "facility", "local-use-1");
    }


    @Test
    public void updateSyslogHandlerFormat() throws Exception {
        selectOptionAndVerify(SYSLOG_HANDLER_ADDRESS, "syslog-format", "RFC3164");
    }

    @Test
    public void removeSyslogHandler() throws Exception {
        page.removeInTable(SYSLOG_HANDLER_TBR);

        new ResourceVerifier(SYSLOG_HANDLER_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
