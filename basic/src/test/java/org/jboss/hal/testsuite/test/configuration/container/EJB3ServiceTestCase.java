package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class EJB3ServiceTestCase {

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier timerVerifier = new ResourceVerifier(CliConstants.EJB3_TIMER_SERVICE_ADDRESS, client);
    private ResourceVerifier asyncVerifier = new ResourceVerifier(CliConstants.EJB3_ASYNC_SERVICE_ADDRESS, client);
    private ResourceVerifier remoteVerifier = new ResourceVerifier(CliConstants.EJB3_REMOTE_SERVICE_ADDRESS, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(timerVerifier);
    private String ejbName;
    private FinderNavigation finderNavigation;

    @Drone
    public WebDriver browser;

    @Page
    public EJB3Page page;

    @Before
    public void before() {
        ejbName = createThreadPool();
        //Console.withBrowser(browser).refreshAndNavigate(EJB3Page.class);
        if (ConfigUtils.isDomain()) {
            finderNavigation = new FinderNavigation(browser, DomainConfigurationPage.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full")
                    .addAddress(FinderNames.SUBSYSTEM, "EJB 3");
        } else {
            finderNavigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "EJB 3");
        }
        finderNavigation.selectRow().invoke("View");
        Application.waitUntilVisible();
        page.service();
    }

    @After
    public void after() {
        removeThreadPool(ejbName);
    }

    @Test
    public void assignTimerServiceToThreadPool() {
        page.switchSubTab("Timer");
        checker.editTextAndAssert(page, "thread-pool-name", ejbName).invoke();
        checker.editTextAndAssert(page, "thread-pool-name", "default").invoke();
    }

    @Test
    public void assignAsyncServiceToThreadPool() {
        page.switchSubTab("Async");
        checker.editTextAndAssert(page, "thread-pool-name", ejbName).withVerifier(asyncVerifier).invoke();
        checker.editTextAndAssert(page, "thread-pool-name", "default").withVerifier(asyncVerifier).invoke();
    }

    @Test
    public void assignRemoteServiceToThreadPool() {
        page.switchSubTab("Remoting Service");
        checker.editTextAndAssert(page, "thread-pool-name", ejbName).withVerifier(remoteVerifier).invoke();
        checker.editTextAndAssert(page, "thread-pool-name", "default").withVerifier(remoteVerifier).invoke();
    }

    private String createThreadPool() {
        String name = "threadPool" + RandomStringUtils.randomAlphanumeric(5);
        client.executeCommand(CliUtils.buildCommand(CliConstants.EJB3_THREAD_POOL_ADDRESS + "=" + name, ":add", new String[]{"max-threads=30", "keepalive-time={time=30, unit=MINUTES}"}));
        return name;
    }

    private void removeThreadPool(String name) {
        client.executeCommand(CliUtils.buildCommand(CliConstants.EJB3_THREAD_POOL_ADDRESS + "=" + name, ":remove"));
    }
}
