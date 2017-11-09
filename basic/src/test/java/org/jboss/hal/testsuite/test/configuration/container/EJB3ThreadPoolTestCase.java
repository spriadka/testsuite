package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.container.EJB3ThreadPoolWizard;
import org.jboss.hal.testsuite.fragment.config.container.EJB3ThreadPoolsFragment;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class EJB3ThreadPoolTestCase {

    private static final String THREAD_POOL = "EJB3ThreadPool_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String THREAD_POOL_TBA = "EJB3ThreadPool-tba_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String THREAD_POOL_TBA2 = "EJB3ThreadPool-tba2_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String THREAD_POOL_TBR = "EJB3ThreadPool-tbr_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address THREAD_POOL_ADDRESS = Address.subsystem("ejb3").and("thread-pool", THREAD_POOL);
    private static final Address THREAD_POOL_TBA_ADDRESS = Address.subsystem("ejb3")
            .and("thread-pool", THREAD_POOL_TBA);
    private static final Address THREAD_POOL_TBA2_ADDRESS = Address.subsystem("ejb3")
            .and("thread-pool", THREAD_POOL_TBA2);
    private static final Address THREAD_POOL_TBR_ADDRESS = Address.subsystem("ejb3")
            .and("thread-pool", THREAD_POOL_TBR);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);

    @Drone WebDriver browser;
    @Page EJB3Page page;

    private EJB3ThreadPoolsFragment fragment;

    @BeforeClass
    public static void beforeClass() throws Exception {
        operations.add(THREAD_POOL_ADDRESS, Values.of("max-threads", 20));
        operations.add(THREAD_POOL_TBR_ADDRESS, Values.of("max-threads", 20));
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        fragment = page.threadPools();
        fragment.getResourceManager().selectByName(THREAD_POOL);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException {
        try {
            operations.removeIfExists(THREAD_POOL_ADDRESS);
            operations.removeIfExists(THREAD_POOL_TBA_ADDRESS);
            operations.removeIfExists(THREAD_POOL_TBA2_ADDRESS);
            operations.removeIfExists(THREAD_POOL_TBR_ADDRESS);
        } finally {
            client.close();
        }
    }

    @Test
    public void createThreadPool() throws Exception {
        boolean result = fragment.addThreadPool()
                .name(THREAD_POOL_TBA)
                .maxThreads("50")
                .finish();

        assertTrue("Window should be closed", result);
        assertTrue(fragment.resourceIsPresent(THREAD_POOL_TBA));
        new ResourceVerifier(THREAD_POOL_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void editMaxThreads() throws Exception {
        new ConfigChecker.Builder(client, THREAD_POOL_ADDRESS)
                .configFragment(fragment)
                .editAndSave(ConfigChecker.InputType.TEXT, "max-threads", 1234)
                .verifyFormSaved()
                .verifyAttribute("max-threads", 1234);
        new ConfigChecker.Builder(client, THREAD_POOL_ADDRESS)
                .configFragment(fragment)
                .editAndSave(ConfigChecker.InputType.TEXT, "max-threads", "1589f")
                .verifyFormNotSaved();
    }

    @Test
    public void removeBeanPool() throws Exception {
        fragment.removeThreadPool(THREAD_POOL_TBR);
        new ResourceVerifier(THREAD_POOL_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void createThreadPoolWithoutMaxThreads() throws Exception {
        EJB3ThreadPoolWizard wizard = fragment.addThreadPool();

        boolean result = wizard.name(THREAD_POOL_TBA2)
                .maxThreads("")
                .finish();
        assertFalse("Window should not be closed.", result);
        new ResourceVerifier(THREAD_POOL_TBA2_ADDRESS, client).verifyDoesNotExist();
    }
}
