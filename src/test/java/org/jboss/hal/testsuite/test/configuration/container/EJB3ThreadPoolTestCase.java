package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.config.container.EJB3ThreadPoolWizard;
import org.jboss.hal.testsuite.fragment.config.container.EJB3ThreadPoolsFragment;
import org.jboss.hal.testsuite.page.config.DomainConfigurationPage;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.page.config.StandaloneConfigurationPage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class EJB3ThreadPoolTestCase {

    private static CliClient cli = CliClientFactory.getClient();
    private String ejb3Subsystem;
    private EJB3ThreadPoolsFragment fragment;
    private FinderNavigation finderNavigation;

    @Drone
    public WebDriver browser;

    @Page
    public EJB3Page page;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            ejb3Subsystem = "/profile=full/subsystem=ejb3/";
            finderNavigation = new FinderNavigation(browser, DomainConfigurationPage.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full")
                    .addAddress(FinderNames.SUBSYSTEM, "EJB 3");
        } else {
            ejb3Subsystem = "/subsystem=ejb3/";
            finderNavigation = new FinderNavigation(browser, StandaloneConfigurationPage.class)
                    .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "EJB 3");
        }
    }

    @Test
    public void createThreadPool() {
        String name = RandomStringUtils.randomAlphanumeric(8);
        ResourceVerifier verifier = new ResourceVerifier(ejb3Subsystem + "thread-pool=" + name, cli);

        finderNavigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        fragment = page.threadPools();

        try {
            boolean result = fragment.addThreadPool()
                    .name(name)
                    .maxThreads("50")
                    .finish();

            assertTrue("Window should be closed", result);
            assertTrue(fragment.resourceIsPresent(name));
            verifier.verifyResource(true);

        } finally {
            removeIfPresent(name);
        }
    }


    @Test
    public void editMaxThreads() {
        String name = RandomStringUtils.randomAlphanumeric(8);
        cli.executeCommand(CliUtils.buildCommand(ejb3Subsystem + "thread-pool=" + name, ":add",
                new String[]{"max-threads=99"}));

        finderNavigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        fragment = page.threadPools();

        ResourceVerifier verifier = new ResourceVerifier(ejb3Subsystem + "thread-pool=" + name, cli);
        ConfigAreaChecker checker = new ConfigAreaChecker(verifier);
        try {
            checker.editTextAndAssert(page, "max-threads", "1589").rowName(name).withTimeout(1000).invoke();
            checker.editTextAndAssert(page, "max-threads", "1589f").expectError().rowName(name).withTimeout(1000)
                    .invoke();

        } finally {
            removeIfPresent(name);
        }
    }

    @Test
    public void removeBeanPool() {
        String name = RandomStringUtils.randomAlphanumeric(8);
        cli.executeCommand(CliUtils.buildCommand(ejb3Subsystem + "thread-pool=" + name, ":add",
                new String[]{"max-threads=99"}));
        ResourceVerifier verifier = new ResourceVerifier(ejb3Subsystem + "thread-pool=" + name, cli);

        finderNavigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        fragment = page.threadPools();

        try {
            fragment.removeThreadPool(name);
            verifier.verifyResource(false);

        } finally {
            removeIfPresent(name);
        }
    }

    @Test
    public void createThreadPoolWithoutMaxThreads() {
        String name = RandomStringUtils.randomAlphanumeric(20);
        ResourceVerifier verifier = new ResourceVerifier(ejb3Subsystem + "thread-pool=" + name, cli);

        finderNavigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        fragment = page.threadPools();
        EJB3ThreadPoolWizard wizard = fragment.addThreadPool();

        boolean result = wizard.name(name)
                .maxThreads("")
                .finish();
        assertFalse("Window should not be closed.", result);
        verifier.verifyResource(ejb3Subsystem + "thread-pool=" + name, false);
    }

    private void removeIfPresent(final String name) {
        if (cli.executeForSuccess(
                CliUtils.buildCommand(ejb3Subsystem + "thread-pool=" + name, ":read-resource"))) {
            cli.executeCommand(CliUtils.buildCommand(ejb3Subsystem + "thread-pool=" + name, ":remove"));
        }
    }
}
