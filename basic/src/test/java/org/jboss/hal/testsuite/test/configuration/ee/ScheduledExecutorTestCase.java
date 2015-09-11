package org.jboss.hal.testsuite.test.configuration.ee;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Jan Kasik
 *         Created on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ScheduledExecutorTestCase extends EETestCaseAbstract {

    //identifiers
    private final String CONTEXT_SERVICE = "context-service";
    private final String CORE_THREADS = "core-threads";
    private final String HUNG_TASK_THRESHOLD = "hung-task-threshold";
    private final String JNDI_NAME = "jndi-name";
    private final String KEEPALIVE_TIME = "keepalive-time";
    private final String LONG_RUNNING_TASKS = "long-running-tasks";
    private final String REJECT_POLICY = "reject-policy";
    private final String THREAD_FACTORY = "thread-factory";

    //names
    private final String CONTEXT_SERVICE_ADDR = "context-service";
    private final String CORE_THREADS_ADDR = "core-threads";
    private final String HUNG_TASK_THRESHOLD_ADDR = "hung-task-threshold";
    private final String JNDI_NAME_ADDR = "jndi-name";
    private final String KEEPALIVE_TIME_ADDR = "keepalive-time";
    private final String LONG_RUNNING_TASKS_ADDR = "long-running-tasks";
    private final String REJECT_POLICY_ADDR = "reject-policy";
    private final String THREAD_FACTORY_ADDR = "thread-factory";

    //values
    private final String NUMERIC_VALID = "7";
    private final String NUMERIC_INVALID = "7f";
    private final String JNDI_INVALID = "test";
    private final String JNDI_DEFAULT = "java:/";
    private final String JNDI_VALID = JNDI_DEFAULT + RandomStringUtils.randomAlphanumeric(6);
    private final String REJECT_POLICY_VALID = "RETRY_ABORT";

    private final String EE_CHILD = "managed-scheduled-executor-service";

    private ResourceAddress address;
    private String executorService;

    @Before
    public void before() {
        executorService = createScheduledExecutorService();
        address = new ResourceAddress(eeAddress).add(EE_CHILD, executorService);
        reloadIfRequiredAndWaitForRunning();
        navigateToEEServices();
        page.switchSubTab("Scheduled Executor");
        page.getResourceManager().getResourceTable().selectRowByText(0, executorService);
    }

    @After
    public void after() {
        removeScheduledExecutorService(executorService);
    }

    @Test
    public void editContextService() throws IOException, InterruptedException {
        editTextAndVerify(address, CONTEXT_SERVICE, CONTEXT_SERVICE_ADDR, "default");
    }

    @Test
    public void editCoreThreads() throws IOException, InterruptedException {
        editTextAndVerify(address, CORE_THREADS, CORE_THREADS_ADDR, NUMERIC_VALID);
    }

    @Test
    public void editCoreThreadsInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(CORE_THREADS, NUMERIC_INVALID);
    }

    @Test
    public void editHungTaskThreshold() throws IOException, InterruptedException {
        editTextAndVerify(address, HUNG_TASK_THRESHOLD, HUNG_TASK_THRESHOLD_ADDR, NUMERIC_VALID);
    }

    @Test
    public void editHungTaskThresholdInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(HUNG_TASK_THRESHOLD, NUMERIC_INVALID);
    }

    @Test
    public void editJNDIName() throws IOException, InterruptedException {
        editTextAndVerify(address, JNDI_NAME, JNDI_NAME_ADDR, JNDI_VALID);
    }

    @Ignore("Currently, there is an inconsistency in JNDI name format (in datasources subsystem java:/ prefix is required)")
    @Test
    public void editJNDINameInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(JNDI_NAME, JNDI_INVALID);
    }

    @Test
    public void editKeepAliveTime() throws IOException, InterruptedException {
        editTextAndVerify(address, KEEPALIVE_TIME, KEEPALIVE_TIME_ADDR, NUMERIC_VALID);
    }

    @Test
    public void editKeepAliveTimeInvalid() {
        verifyIfErrorAppears(KEEPALIVE_TIME, NUMERIC_INVALID);
    }

    @Test
    public void setLongRunningTasksToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, LONG_RUNNING_TASKS, LONG_RUNNING_TASKS_ADDR, true);
    }

    @Test
    public void setLongRunningTasksToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, LONG_RUNNING_TASKS, LONG_RUNNING_TASKS_ADDR, false);
    }

    @Test
    public void selectRejectPolicy() throws IOException, InterruptedException {
        selectOptionAndVerify(address, REJECT_POLICY, REJECT_POLICY_ADDR, REJECT_POLICY_VALID);
    }

    @Test
    public void editThreadFactory() throws IOException, InterruptedException {
        editTextAndVerify(address, THREAD_FACTORY, THREAD_FACTORY_ADDR);
    }

    @Test
    public void addExecutorInGUI() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text(CORE_THREADS, NUMERIC_VALID);
        editor.text(JNDI_NAME, JNDI_VALID);
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        assertTrue("Executor should be present in table", config.resourceIsPresent(name));
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        verifier.verifyResource(address, true, 5000);
    }

    @Test
    public void removeExecutorInGUI() {
        String name = createScheduledExecutorService();
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager().removeResource(name).confirm();

        Assert.assertFalse("Executor should not be present in table", config.resourceIsPresent(name));
        Assert.assertFalse("Executor should not be present on server", removeScheduledExecutorService(name));
    }

    private String createScheduledExecutorService() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        dispatcher.execute(new Operation.Builder("add", address)
                .param(JNDI_NAME, JNDI_DEFAULT + name)
                .param(CORE_THREADS_ADDR, 5)
                .build());
        return name;
    }

    private Boolean removeScheduledExecutorService(String name) {
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        return dispatcher.execute(new Operation.Builder("remove", address).build()).isSuccessful();
    }
}
