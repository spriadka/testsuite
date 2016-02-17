package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class EJB3ServiceTestCase {

    private static final String THREAD_POOL_NAME_ID = "thread-pool-name";
    private static final String THREAD_POOL_NAME = "ejb3-service_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address THREAD_POOL_ADDRESS = Address.subsystem("ejb3").and("thread-pool", THREAD_POOL_NAME);
    private static final Address TIMER_SERVICE_ADDRESS = Address.subsystem("ejb3").and("service", "timer-service");
    private static final Address ASYNC_SERVICE_ADDRESS = Address.subsystem("ejb3").and("service", "async");
    private static final Address REMOTE_SERVICE_ADDRESS = Address.subsystem("ejb3").and("service", "remote");

    private final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final Operations operations = new Operations(client);
    private final Administration administration = new Administration(client);
    @Drone
    public WebDriver browser;

    @Page
    public EJB3Page page;

    @Before
    public void before() throws IOException {
        operations.add(THREAD_POOL_ADDRESS);
        page.navigate();
        page.service();
    }

    @After
    public void after() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(THREAD_POOL_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void assignTimerServiceToThreadPool() throws Exception {
        page.switchSubTab("Timer");
        assignServiceToThreadPool(TIMER_SERVICE_ADDRESS);
    }

    @Test
    public void assignAsyncServiceToThreadPool() throws Exception {
        page.switchSubTab("Async");
        assignServiceToThreadPool(ASYNC_SERVICE_ADDRESS);
    }

    @Test
    public void assignRemoteServiceToThreadPool() throws Exception {
        page.switchSubTab("Remoting Service");
        assignServiceToThreadPool(REMOTE_SERVICE_ADDRESS);
    }

    public void assignServiceToThreadPool(Address serviceAddress) throws Exception {
        new ConfigChecker.Builder(client, serviceAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, THREAD_POOL_NAME_ID, THREAD_POOL_NAME)
                .verifyFormSaved()
                .verifyAttribute(THREAD_POOL_NAME_ID, THREAD_POOL_NAME);
        if (administration.isReloadRequired()) {
            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
            administration.reload();
        }
        new ConfigChecker.Builder(client, serviceAddress)
                .configFragment(page.getConfigFragment())
                .editAndSave(ConfigChecker.InputType.TEXT, THREAD_POOL_NAME_ID, "default")
                .verifyFormSaved()
                .verifyAttribute(THREAD_POOL_NAME_ID, "default");

    }
}
