package org.jboss.hal.testsuite.test.configuration.io;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.IOSubsystemPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class WorkerTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private IOSubsystemPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String
            WORKER = "worker",
            IO_THREADS = "io-threads",
            TASK_KEEPALIVE = "task-keepalive",
            TASK_MAX_THREADS = "task-max-threads",
            STACK_SIZE = "stack-size",
            EDIT_FAIL_MESSAGE = "Probably fails because of https://issues.jboss.org/browse/HAL-1305. ";

    private static final Address
            IO_SUBSYSTEM_ADDRESS = Address.subsystem("io"),
            WORKER_TBA_ADDRESS = IO_SUBSYSTEM_ADDRESS.and(WORKER, "worker-tba" + RandomStringUtils.randomAlphanumeric(7)),
            WORKER_TBR_ADDRESS = IO_SUBSYSTEM_ADDRESS.and(WORKER, "worker-tbr" + RandomStringUtils.randomAlphanumeric(7)),
            WORKER_ADDRESS = IO_SUBSYSTEM_ADDRESS.and(WORKER, "worker" + RandomStringUtils.randomAlphanumeric(7));

    @BeforeClass
    public static void beforeClass() throws IOException, TimeoutException, InterruptedException {
        operations.add(WORKER_ADDRESS).assertSuccess();
        operations.add(WORKER_TBR_ADDRESS).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigateToWorkers();
        page.getResourceManager().selectByName(WORKER_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(WORKER_TBR_ADDRESS);
            operations.removeIfExists(WORKER_TBA_ADDRESS);
            operations.removeIfExists(WORKER_ADDRESS);
        } finally {
            administration.reloadIfRequired();
        }
    }

    @Test
    public void addWorker() throws Exception {
        page.addWorker().name(WORKER_TBA_ADDRESS.getLastPairValue()).saveAndDismissReloadRequiredWindow();

        Assert.assertTrue(page.getResourceManager().isResourcePresent(WORKER_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(WORKER_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeWorker() throws Exception {
        page.getResourceManager().removeResource(WORKER_TBR_ADDRESS.getLastPairValue()).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(page.getResourceManager().isResourcePresent(WORKER_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(WORKER_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editIOThreads() throws Exception {
        final int value = 1024;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, IO_THREADS);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, IO_THREADS, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(IO_THREADS, value);
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, IO_THREADS, originalNodeResult.value());
        }
    }

    @Test
    public void editIOThreadsInvalid() throws Exception {
        final int value = -1;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, IO_THREADS);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, IO_THREADS, String.valueOf(value))
                    .verifyFormNotSaved();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, IO_THREADS, originalNodeResult.value());
        }
    }

    @Test
    public void editStackSize() throws Exception {
        final long value = 1024;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, STACK_SIZE);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, STACK_SIZE, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(STACK_SIZE, value);
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, STACK_SIZE, originalNodeResult.value());
        }
    }

    @Test
    public void editStackSizeInvalid() throws Exception {
        final int value = -20;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, STACK_SIZE);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, STACK_SIZE, String.valueOf(value))
                    .verifyFormNotSaved();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, STACK_SIZE, originalNodeResult.value());
        }
    }

    @Test
    public void editTaskKeepAlive() throws Exception {
        final int value = 54242;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, TASK_KEEPALIVE);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TASK_KEEPALIVE, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(TASK_KEEPALIVE, value);
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, TASK_KEEPALIVE, originalNodeResult.value());
        }
    }

    @Test
    public void editTaskKeepAliveInvalid() throws Exception {
        final int value = -42;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, TASK_KEEPALIVE);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TASK_KEEPALIVE, String.valueOf(value))
                    .verifyFormNotSaved();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, TASK_KEEPALIVE, originalNodeResult.value());
        }
    }

    @Test
    public void editTaskMaxThreads() throws Exception {
        final int value = 1024;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, TASK_MAX_THREADS);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TASK_MAX_THREADS, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(TASK_MAX_THREADS, value);
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, TASK_MAX_THREADS, originalNodeResult.value());
        }
    }

    @Test
    public void editTaskMaxThreadsInvalid() throws Exception {
        final int value = -42;
        ModelNodeResult originalNodeResult = operations.readAttribute(WORKER_ADDRESS, TASK_MAX_THREADS);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, WORKER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, TASK_MAX_THREADS, String.valueOf(value))
                    .verifyFormNotSaved();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(EDIT_FAIL_MESSAGE + e.getMessage(), e.getCause());
        }  finally {
            operations.writeAttribute(WORKER_ADDRESS, TASK_MAX_THREADS, originalNodeResult.value());
        }
    }
}
