package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.fragment.config.container.EJB3BeanPoolsFragment;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Assert;
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
public class EJB3BeanPoolTestCase {

    private EJB3BeanPoolsFragment fragment;
    private static final String BEAN_POOL_NAME = "bp_" + RandomStringUtils.randomAlphanumeric(5);
    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(CliConstants.EJB3_BEAN_POOL_ADDRESS + "=" + BEAN_POOL_NAME, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public EJB3Page page;

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(EJB3Page.class);
        fragment = page.beanPools();
    }

    @Test
    @InSequence(0)
    public void createBeanPool() {
        boolean result = fragment.addBeanPool()
                .name(BEAN_POOL_NAME)
                .maxPoolSize("30")
                .timeout("8")
                .timeoutUnit("MINUTES")
                .finish();
        Assert.assertTrue("Window should be closed", result);
        verifier.verifyResource(true);
    }

    @Test
    @InSequence(1)
    public void editMaxPoolSize(){
        checker.editTextAndAssert(page, "max-pool-size", "56").rowName(BEAN_POOL_NAME).invoke();
        checker.editTextAndAssert(page, "max-pool-size", "56F").rowName(BEAN_POOL_NAME).expectError().invoke();
    }

    @Test
    @InSequence(2)
    public void editTimeout(){
        checker.editTextAndAssert(page, "timeout", "56").rowName(BEAN_POOL_NAME).invoke();
        checker.editTextAndAssert(page, "timeout", "56F").rowName(BEAN_POOL_NAME).expectError().invoke();
    }

    @Test
    @InSequence(3)
    public void removeBeanPool() {
        fragment.removeBeanPool(BEAN_POOL_NAME);
        verifier.verifyResource(false);
    }

}
