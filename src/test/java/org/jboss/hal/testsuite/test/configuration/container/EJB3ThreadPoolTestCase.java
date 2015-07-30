package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.config.container.EJB3ThreadPoolWizard;
import org.jboss.hal.testsuite.fragment.config.container.EJB3ThreadPoolsFragment;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.DOMAIN_SERVER_ONE_PREFIX;
import static org.jboss.hal.testsuite.cli.CliConstants.EJB3_THREAD_POOL_ADDRESS;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class EJB3ThreadPoolTestCase {

    private EJB3ThreadPoolsFragment fragment;
    private static final String THREAD_POOL_NAME = "tp_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String THREAD_POOL_NAME_INVALID = "tpi_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String THREAD_DMR = (ConfigUtils.isDomain() ? DOMAIN_SERVER_ONE_PREFIX : "") + EJB3_THREAD_POOL_ADDRESS + "=" + THREAD_POOL_NAME;
    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(THREAD_DMR, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public EJB3Page page;

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(EJB3Page.class);
        fragment = page.threadPools();
    }

    @Test
    @InSequence(0)
    public void createThreadPool() {
        boolean result = fragment.addThreadPool()
                .name(THREAD_POOL_NAME)
                .maxThreads("50")
                .finish();
        Assert.assertTrue("Window should be closed", result);
        verifier.verifyResource(true);
    }


    @Test
    @InSequence(2)
    public void editMaxThreads(){
        checker.editTextAndAssert(page, "max-threads", "1589").rowName(THREAD_POOL_NAME).invoke();
        checker.editTextAndAssert(page, "max-threads", "1589f").expectError().rowName(THREAD_POOL_NAME).invoke();
    }

    @Test
    @InSequence(3)
    public void removeBeanPool() {
        fragment.removeThreadPool(THREAD_POOL_NAME);
        verifier.verifyResource(false);
    }

    @Test
    public void createThreadPoolWithoutMaxThreads(){
        EJB3ThreadPoolWizard wizard = fragment.addThreadPool();

        boolean result = wizard.name(THREAD_POOL_NAME_INVALID)
                .maxThreads("")
                .finish();

        Assert.assertFalse("Window should not be closed.", result);
        verifier.verifyResource(EJB3_THREAD_POOL_ADDRESS + "=" + THREAD_POOL_NAME_INVALID, false);
    }

}
