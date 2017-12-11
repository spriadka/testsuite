package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.infinispan.InfinispanPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public abstract class InfinispanTestCaseAbstract {

    protected static final OnlineManagementClient client;
    protected static final Administration administration;
    protected static final Operations operations;

    static {
        client = ConfigUtils.isDomain() ? ManagementClientProvider.withProfile("full-ha") : ManagementClientProvider.createOnlineManagementClient();
        administration = new Administration(client);
        operations = new Operations(client);
    }

    @Page
    protected InfinispanPage page;

    @Drone
    protected WebDriver browser;

    @AfterClass
    public static void cleanUp() {
        IOUtils.closeQuietly(client);
    }
}
