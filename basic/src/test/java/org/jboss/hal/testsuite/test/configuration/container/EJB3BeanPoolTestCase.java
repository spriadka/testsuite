package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.container.EJB3BeanPoolsFragment;
import org.jboss.hal.testsuite.page.config.EJB3Page;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class EJB3BeanPoolTestCase {

    private static final String BEAN_POOL = "bean-pool_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BEAN_POOL_TBA = "bean-pool-tba_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BEAN_POOL_TBR = "bean-pool-tbr_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address BEAN_POOL_ADDRESS = Address.subsystem("ejb3").and("strict-max-bean-instance-pool", BEAN_POOL);
    private static final Address BEAN_POOL_TBR_ADDRESS = Address.subsystem("ejb3").and("strict-max-bean-instance-pool", BEAN_POOL_TBR);
    private static final Address BEAN_POOL_TBA_ADDRESS = Address.subsystem("ejb3").and("strict-max-bean-instance-pool", BEAN_POOL_TBA);

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static Operations operations = new Operations(client);
    private static Administration administration = new Administration(client);
    private EJB3BeanPoolsFragment fragment;

    @Drone
    public WebDriver browser;

    @Page
    public EJB3Page page;

    @BeforeClass
    public static void beforeClass() throws IOException {
        operations.add(BEAN_POOL_ADDRESS);
        operations.add(BEAN_POOL_TBR_ADDRESS);
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, InterruptedException, TimeoutException {
        try {
            operations.removeIfExists(BEAN_POOL_ADDRESS);
            operations.removeIfExists(BEAN_POOL_TBA_ADDRESS);
            operations.removeIfExists(BEAN_POOL_TBR_ADDRESS);
            administration.reloadIfRequired(); // reload after removal of pools is needed
        } finally {
            client.close();
        }
    }

    @Before
    public void before() {
        page.navigate();
        fragment = page.beanPools();
        fragment.getResourceManager().selectByName(BEAN_POOL);
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void createBeanPool() throws Exception {
        boolean result = fragment.addBeanPool()
                .name(BEAN_POOL_TBA)
                .maxPoolSize("30")
                .timeout("8")
                .timeoutUnit("MINUTES")
                .finish();
        Assert.assertTrue("Window should be closed", result);
        new ResourceVerifier(BEAN_POOL_TBA_ADDRESS, client).verifyExists("Probably fails because of https://issues.jboss.org/browse/HAL-1233");
    }

    @Test
    public void editMaxPoolSize() throws Exception {
        new ConfigChecker.Builder(client, BEAN_POOL_ADDRESS)
                .configFragment(fragment)
                .editAndSave(ConfigChecker.InputType.TEXT, "max-pool-size", 42)
                .verifyFormSaved()
                .verifyAttribute("max-pool-size", 42);
        new ConfigChecker.Builder(client, BEAN_POOL_ADDRESS)
                .configFragment(fragment)
                .editAndSave(ConfigChecker.InputType.TEXT, "max-pool-size", "56F")
                .verifyFormNotSaved();
    }

    @Test
    public void editTimeout() throws Exception {
        new ConfigChecker.Builder(client, BEAN_POOL_ADDRESS)
                .configFragment(fragment)
                .editAndSave(ConfigChecker.InputType.TEXT, "timeout", 42L)
                .verifyFormSaved()
                .verifyAttribute("timeout", 42L);
        new ConfigChecker.Builder(client, BEAN_POOL_ADDRESS)
                .configFragment(fragment)
                .editAndSave(ConfigChecker.InputType.TEXT, "timeout", "56F")
                .verifyFormNotSaved();
    }

    @Test
    public void removeBeanPool() throws Exception {
        fragment.removeBeanPool(BEAN_POOL_TBR);
        new ResourceVerifier(BEAN_POOL_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
