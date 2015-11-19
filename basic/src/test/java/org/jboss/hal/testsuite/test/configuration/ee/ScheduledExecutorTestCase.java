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
    private final String CONTEXT_SERVICE_ATTR = "context-service";
    private final String CORE_THREADS_ATTR = "core-threads";
    private final String HUNG_TASK_THRESHOLD_ATTR = "hung-task-threshold";
    private final String JNDI_NAME_ATTR = "jndi-name";
    private final String KEEPALIVE_TIME_ATTR = "keepalive-time";
    private final String LONG_RUNNING_TASKS_ATTR = "long-running-tasks";
    private final String REJECT_POLICY_ATTR = "reject-policy";
    private final String THREAD_FACTORY_ATTR = "thread-factory";

    //values
    private final String NUMERIC_VALID = "7";
    private final String NUMERIC_INVALID = "7f";
    private final String JNDI_INVALID = "test";
    private final String JNDI_DEFAULT = "java:/";
    private final String JNDI_VALID = JNDI_DEFAULT + "ScheduledExecutor_" +  RandomStringUtils.randomAlphanumeric(6);
    private final String REJECT_POLICY_VALID = "RETRY_ABORT";

    private final String EE_CHILD = "managed-scheduled-executor-service";

    private ResourceAddress address;
    private String executorService;

    @Before
    public void before() {
        executorService = createScheduledExecutorService();
        address = new ResourceAddress(eeAddress).add(EE_CHILD, executorService);
        reloadIfRequiredAndWaitForRunning();
        page.navigate();
        page.switchSubTab("Scheduled Executor");
        page.getResourceManager().getResourceTable().selectRowByText(0, executorService);
    }

    @After
    public void after() {
        removeEEChild(EE_CHILD, executorService);
    }

    @Test
    public void editContextService() throws IOException, InterruptedException {
        editTextAndVerify(address, CONTEXT_SERVICE, CONTEXT_SERVICE_ATTR, "default");
    }

    @Test
    public void editCoreThreads() throws IOException, InterruptedException {
        editTextAndVerify(address, CORE_THREADS, CORE_THREADS_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editCoreThreadsInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(CORE_THREADS, NUMERIC_INVALID);
    }

    @Test
    public void editHungTaskThreshold() throws IOException, InterruptedException {
        editTextAndVerify(address, HUNG_TASK_THRESHOLD, HUNG_TASK_THRESHOLD_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editHungTaskThresholdInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(HUNG_TASK_THRESHOLD, NUMERIC_INVALID);
    }

    @Test
    public void editJNDIName() throws IOException, InterruptedException {
        editTextAndVerify(address, JNDI_NAME, JNDI_NAME_ATTR, JNDI_VALID);
    }

    @Ignore("Currently, there is an inconsistency in JNDI name format (in datasources subsystem java:/ prefix is required)")
    @Test
    public void editJNDINameInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(JNDI_NAME, JNDI_INVALID);
    }

    @Test
    public void editKeepAliveTime() throws IOException, InterruptedException {
        editTextAndVerify(address, KEEPALIVE_TIME, KEEPALIVE_TIME_ATTR, NUMERIC_VALID);
    }

    @Test
    public void editKeepAliveTimeInvalid() {
        verifyIfErrorAppears(KEEPALIVE_TIME, NUMERIC_INVALID);
    }

    @Test
    public void setLongRunningTasksToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, LONG_RUNNING_TASKS, LONG_RUNNING_TASKS_ATTR, true);
    }

    @Test
    public void setLongRunningTasksToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, LONG_RUNNING_TASKS, LONG_RUNNING_TASKS_ATTR, false);
    }

    @Test
    public void selectRejectPolicy() throws IOException, InterruptedException {
        selectOptionAndVerify(address, REJECT_POLICY, REJECT_POLICY_ATTR, REJECT_POLICY_VALID);
    }

    @Test
    public void editThreadFactory() throws IOException, InterruptedException {
        editTextAndVerify(address, THREAD_FACTORY, THREAD_FACTORY_ATTR);
    }

    @Test
    public void addExecutorInGUI() {
        String name = "ScheduledExecutorGUI" + RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text(JNDI_NAME, JNDI_VALID);
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        assertTrue("Scheduled executor should be present in table", config.resourceIsPresent(name));
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        verifier.verifyResource(address, true, 5000);
    }

    @Test
    public void removeExecutorInGUI() {
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager().removeResource(executorService).confirm();

        Assert.assertFalse("Scheduled executor should not be present in table", config.resourceIsPresent(executorService));
        Assert.assertFalse("Scheduled executor should not be present on server", removeEEChild(EE_CHILD, executorService));
    }

    private String createScheduledExecutorService() {
        String name = "ScheduledExecutor" + RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        dispatcher.execute(new Operation.Builder("add", address)
                .param(JNDI_NAME, JNDI_DEFAULT + name)
                .param(CORE_THREADS_ATTR, 5)
                .build());
        return name;
    }
}
