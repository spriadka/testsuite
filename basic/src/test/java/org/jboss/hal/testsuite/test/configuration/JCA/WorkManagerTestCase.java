package org.jboss.hal.testsuite.test.configuration.JCA;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Created by pcyprian on 22.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class WorkManagerTestCase {
    private FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=jca/workmanager=default/short-running-threads=default");
    private ResourceAddress address = new ResourceAddress(path);
    Dispatcher dispatcher = new Dispatcher();
    ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    CliClient cliClient = CliClientFactory.getClient();

    @Drone
    private WebDriver browser;
    @Page
    private JCAPage page;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "JCA");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
        page.switchToWorkManagerTab();
        page.clickView();
    }

    @Test
    public void updateKeepAliveTimeOut() {
        page.edit().text("keepaliveTime", "1");
        page.clickButton("Save");
        verifier.verifyAttribute(address, "keepalive-time.time", "1");

        page.edit().text("keepaliveTime", "10");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "keepalive-time.time", "10");
    }

    @Test
    public void updateKeepAliveTimeOutUnit() {
        page.edit().select("keepaliveTimeUnit", "MILLISECONDS");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "keepalive-time.unit", "MILLISECONDS");

        page.edit().select("keepaliveTimeUnit", "SECONDS");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "keepalive-time.unit", "SECONDS");
    }

    @Test
    public void updateAllowCoreTimeout() {
        page.edit().checkbox("allowCoreTimeout", true);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "allow-core-timeout", true);

        page.edit().checkbox("allowCoreTimeout", false);
        page.clickButton("Save");

        verifier.verifyAttribute(address, "allow-core-timeout", false);
    }

    @Test
    public void updateThreadFactory() {
        page.edit().text("threadFactory", "tf");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "thread-factory", "tf");

        page.edit().text("threadFactory", "");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "thread-factory", "undefined");
    }

    @Test
    public void updateMaxThreads() {
        page.switchToSizing();

        page.edit().text("maxThreads", "1");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "max-threads", "1");

        page.edit().text("maxThreads", "50");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "max-threads", "50");
    }

    @Test
    public void updateCoreThreads() {
        page.switchToSizing();

        page.edit().text("coreThreads", "100");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "core-threads", "100");

        page.edit().text("coreThreads", "50");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "core-threads", "50");
    }

    @Test
    public void updateQueueLength() {
        page.switchToSizing();

        page.edit().text("queueLength", "1000");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "queue-length", "1000");

        page.edit().text("queueLength", "50");
        page.clickButton("Save");

        verifier.verifyAttribute(address, "queue-length", "50");
    }

}
