package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceCleanup;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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

    static final AddressTemplate ADDRESS_TEMPLATE = AddressTemplate
            .of("{default.profile}/subsystem=ejb3/thread-pool=*");

    private static Dispatcher dispatcher;

    @Drone WebDriver browser;
    @Page EJB3Page page;

    private DefaultContext statementContext;
    private ResourceVerifier verifier;
    private ResourceCleanup cleanup;
    private FinderNavigation finderNavigation;
    private EJB3ThreadPoolsFragment fragment;

    @BeforeClass
    public static void beforeClass() {
        dispatcher = new Dispatcher();
    }

    @Before
    public void before() {
        statementContext = new DefaultContext();
        verifier = new ResourceVerifier(dispatcher);
        cleanup = new ResourceCleanup(dispatcher);

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
    }

    @AfterClass
    public static void afterClass() {
        dispatcher.close();
    }

    @Test
    public void createThreadPool() {
        String name = RandomStringUtils.randomAlphanumeric(8);
        ResourceAddress address = ADDRESS_TEMPLATE.resolve(statementContext, name);
        dispatcher.execute(new Operation.Builder(ModelDescriptionConstants.ADD, address).param("max-threads", 99).build());

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
            verifier.verifyResource(address);

        } finally {
            cleanup.removeIfPresent(address);
        }
    }

    @Test
    public void editMaxThreads() {
        String name = RandomStringUtils.randomAlphanumeric(8);
        ResourceAddress address = ADDRESS_TEMPLATE.resolve(statementContext, name);
        dispatcher.execute(new Operation.Builder("add", address).param("max-threads", 99).build());

        finderNavigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        fragment = page.threadPools();

        ConfigAreaChecker checker = new ConfigAreaChecker(verifier, address);
        try {
            checker.editTextAndAssert(page, "max-threads", "1589").rowName(name).withTimeout(1000).invoke();
            checker.editTextAndAssert(page, "max-threads", "1589f").expectError().rowName(name).withTimeout(1000)
                    .invoke();

        } finally {
            cleanup.removeIfPresent(address);
        }
    }

    @Test
    public void removeBeanPool() {
        String name = RandomStringUtils.randomAlphanumeric(8);
        ResourceAddress address = ADDRESS_TEMPLATE.resolve(statementContext, name);
        dispatcher.execute(new Operation.Builder("add", address).param("max-threads", 99).build());

        finderNavigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        fragment = page.threadPools();

        try {
            fragment.removeThreadPool(name);
            verifier.verifyResource(address, false);

        } finally {
            cleanup.removeIfPresent(address);
        }
    }

    @Test
    public void createThreadPoolWithoutMaxThreads() {
        String name = RandomStringUtils.randomAlphanumeric(20);
        ResourceAddress address = ADDRESS_TEMPLATE.resolve(statementContext, name);

        finderNavigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        fragment = page.threadPools();
        EJB3ThreadPoolWizard wizard = fragment.addThreadPool();

        boolean result = wizard.name(name)
                .maxThreads("")
                .finish();
        assertFalse("Window should not be closed.", result);
        verifier.verifyResource(address, false);
    }
}
