package org.jboss.hal.testsuite.test.configuration.elytron;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.junit.AfterClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public abstract class AbstractElytronTestCase {

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final ElytronOperations elyOps = new ElytronOperations(client);
    protected static final Operations ops = new Operations(client);
    protected static final Administration adminOps = new Administration(client);
    protected static final String
        MODULE_NAME_1 = "org.jboss.as.cli",
        MODULE_NAME_2 = "org.jboss.as.controller",
        NAME = "name",
        ATTRIBUTES_LABEL = "Attributes",
        PROVIDER_NAME = "provider-name",
        MODULE = "module",
        CLASS_NAME = "class-name",
        CONFIGURATION = "configuration";

    @Drone
    protected WebDriver browser;

    @AfterClass
    public static void parentAfterClass() throws IOException, OperationException, InterruptedException, TimeoutException {
        IOUtils.closeQuietly(client);
    }

}
