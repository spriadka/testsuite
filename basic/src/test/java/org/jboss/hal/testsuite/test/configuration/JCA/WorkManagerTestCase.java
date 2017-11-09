package org.jboss.hal.testsuite.test.configuration.JCA;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Created by pcyprian on 22.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class WorkManagerTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration adminOps = new Administration(client);

    private FinderNavigation navigation;

    private final Address threadPoolExecutorAddress = Address.subsystem("jca").and("workmanager", "default")
            .and("short-running-threads", "default");
    private final ResourceVerifier verifier = new ResourceVerifier(threadPoolExecutorAddress, client);

    @AfterClass
    public static void tearDown() throws IOException, InterruptedException, TimeoutException {
        try {
            adminOps.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Drone
    private WebDriver browser;
    @Page
    private JCAPage page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .step(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "JCA");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
        page.switchToWorkManagerTab();
        page.clickView();
    }

    @Test
    public void updateKeepAliveTimeOut() throws Exception {
        page.edit().text("keepaliveTime", "1");
        page.clickButton("Save");
        verifier.verifyAttribute("keepalive-time.time", 1L);

        page.edit().text("keepaliveTime", "10");
        page.clickButton("Save");

        verifier.verifyAttribute("keepalive-time.time", 10L);
    }

    @Test
    public void updateKeepAliveTimeOutUnit() throws Exception {
        page.edit().select("keepaliveTimeUnit", "MILLISECONDS");
        page.clickButton("Save");

        verifier.verifyAttribute("keepalive-time.unit", "MILLISECONDS");

        page.edit().select("keepaliveTimeUnit", "SECONDS");
        page.clickButton("Save");

        verifier.verifyAttribute("keepalive-time.unit", "SECONDS");
    }

    @Test
    public void updateAllowCoreTimeout() throws Exception {
        page.edit().checkbox("allowCoreTimeout", true);
        page.clickButton("Save");

        verifier.verifyAttribute("allow-core-timeout", true);

        page.edit().checkbox("allowCoreTimeout", false);
        page.clickButton("Save");

        verifier.verifyAttribute("allow-core-timeout", false);
    }

    @Test
    public void updateThreadFactory() throws Exception {
        page.edit().text("threadFactory", "tf");
        page.clickButton("Save");

        verifier.verifyAttribute("thread-factory", "tf");

        page.edit().text("threadFactory", "");
        page.clickButton("Save");

        verifier.verifyAttributeIsUndefined("thread-factory");
    }

    @Test
    public void updateMaxThreads() throws Exception {
        page.switchToSizing();

        page.edit().text("maxThreads", "1");
        page.clickButton("Save");

        verifier.verifyAttribute("max-threads", 1);

        page.edit().text("maxThreads", "50");
        page.clickButton("Save");

        verifier.verifyAttribute("max-threads", 50);
    }

    @Test
    public void updateCoreThreads() throws Exception {
        page.switchToSizing();

        page.edit().text("coreThreads", "100");
        page.clickButton("Save");

        verifier.verifyAttribute("core-threads", 100);

        page.edit().text("coreThreads", "50");
        page.clickButton("Save");

        verifier.verifyAttribute("core-threads", 50);
    }

    @Test
    public void updateQueueLength() throws Exception {
        page.switchToSizing();

        page.edit().text("queueLength", "1000");
        page.clickButton("Save");

        verifier.verifyAttribute("queue-length", 1000);

        page.edit().text("queueLength", "50");
        page.clickButton("Save");

        verifier.verifyAttribute("queue-length", 50);
    }

}
