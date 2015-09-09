package org.jboss.hal.testsuite.test.configuration.ee;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
public class ExecutorTestCase extends EETestCaseAbstract {

    //identifiers
    private static final String CONTEXT_SERVICE = "context-service";
    private static final String CORE_THREADS = "core-threads";
    private static final String HUNG_TASK_THRESHOLD = "hung-task-threshold";
    private static final String JNDI_NAME = "jndi-name";
    private static final String KEEPALIVE_TIME = "keepalive-time";
    private static final String LONG_RUNNING_TASKS = "long-running-tasks";
    private static final String MAX_THREADS = "max-threads";
    private static final String QUEUE_LENGTH = "queue-length";
    private static final String REJECT_POLICY = "reject-policy";
    private static final String THREAD_FACTORY = "thread-factory";

    //names
    private static final String CONTEXT_SERVICE_ADDR = "context-service";
    private static final String CORE_THREADS_ADDR = "core-threads";
    private static final String HUNG_TASK_THRESHOLD_ADDR = "hung-task-threshold";
    private static final String JNDI_NAME_ADDR = "jndi-name";
    private static final String KEEPALIVE_TIME_ADDR = "keepalive-time";
    private static final String LONG_RUNNING_TASKS_ADDR = "long-running-tasks";
    private static final String MAX_THREADS_ADDR = "max-threads";
    private static final String QUEUE_LENGTH_ADDR = "queue-length";
    private static final String REJECT_POLICY_ADDR = "reject-policy";
    private static final String THREAD_FACTORY_ADDR = "thread-factory";

    //values
    private static final String NUMERIC_VALID = "7";
    private static final String NUMERIC_INVALID = "7f";
    private static final String JNDI_DEFAULT = "java:/";
    private static final String JNDI_VALID = JNDI_DEFAULT + RandomStringUtils.randomAlphanumeric(6);
    private static final String REJECT_POLICY_VALID = "RETRY_ABORT";

    private ResourceAddress address;
    private static String executorService;

    @After
    public void after() {
        removeExecutorService(executorService);
    }

    @Before
    public void before() {
        executorService = createExecutorService();
        address = new ResourceAddress(eeAddress).add("managed-executor-service", executorService);
        reloadAndWaitForRunning();
        navigateToEEServices();
        page.switchSubTab("Executor");
        page.getResourceManager().getResourceTable().selectRowByText(0, executorService);
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
    public void editMaxThreads() throws IOException, InterruptedException {
        editTextAndVerify(address, MAX_THREADS, MAX_THREADS_ADDR, NUMERIC_VALID);
    }

    @Test
    public void editMaxThreadsInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(MAX_THREADS, NUMERIC_INVALID);
    }

    @Test
    public void editQueueLength() throws IOException, InterruptedException {
        editTextAndVerify(address, QUEUE_LENGTH, QUEUE_LENGTH_ADDR, NUMERIC_VALID);
    }

    @Test
    public void editQueueLengthInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(QUEUE_LENGTH, NUMERIC_INVALID);
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
        ResourceAddress address = new ResourceAddress(eeAddress).add("managed-executor-service", name);
        verifier.verifyResource(address, true, 5000);
    }

    @Test
    public void removeExecutorInGUI() {
        String name = createExecutorService();
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager().removeResource(name).confirm();

        Library.letsSleep(500);
        Assert.assertFalse("Executor should not be present in table", config.resourceIsPresent(name));
        Assert.assertFalse("Executor should not be present on server", removeExecutorService(name));
    }

    private String createExecutorService() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = new ResourceAddress(eeAddress).add("managed-executor-service", name);
        dispatcher.execute(new Operation.Builder("add", address)
                .param(JNDI_NAME, JNDI_DEFAULT + name)
                .param(CORE_THREADS_ADDR, 5)
                .build());
        return name;
    }

    private Boolean removeExecutorService(String name) {
        ResourceAddress address = new ResourceAddress(eeAddress).add("managed-executor-service", name);
        return dispatcher.execute(new Operation.Builder("remove", address).build()).isSuccessful();
    }
}
