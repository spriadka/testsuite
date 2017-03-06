package org.jboss.hal.testsuite.test.configuration.io;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.IOSubsystemPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
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
@Category(Shared.class)
public class BufferPoolTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private IOSubsystemPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String
            BUFFERS_PER_SLICE = "buffers-per-slice",
            BUFFER_SIZE = "buffer-size",
            DIRECT_BUFFERS = "direct-buffers";

    private static final Address
            IO_SUBSYSTEM_ADDRESS = Address.subsystem("io"),
            BUFFER_POOL_TBA_ADDRESS = IO_SUBSYSTEM_ADDRESS.and("buffer-pool", "buffer-pool-tba" + RandomStringUtils.randomAlphanumeric(7)),
            BUFFER_POOL_TBR_ADDRESS = IO_SUBSYSTEM_ADDRESS.and("buffer-pool", "buffer-pool-tbr" + RandomStringUtils.randomAlphanumeric(7)),
            BUFFER_POOL_ADDRESS = IO_SUBSYSTEM_ADDRESS.and("buffer-pool", "buffer-pool" + RandomStringUtils.randomAlphanumeric(7));

    @BeforeClass
    public static void beforeClass() throws IOException, TimeoutException, InterruptedException {
        operations.add(BUFFER_POOL_ADDRESS).assertSuccess();
        operations.add(BUFFER_POOL_TBR_ADDRESS).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigateToBufferPools();
        page.getResourceManager().selectByName(BUFFER_POOL_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(BUFFER_POOL_ADDRESS);
            operations.removeIfExists(BUFFER_POOL_TBA_ADDRESS);
            operations.removeIfExists(BUFFER_POOL_TBR_ADDRESS);
        } finally {
            administration.reloadIfRequired();
        }
    }

    @Test
    public void editBuffersPerSlice() throws Exception {
        final int value = 10;
        final ModelNodeResult originalNodeResult = operations.readAttribute(BUFFER_POOL_ADDRESS, BUFFERS_PER_SLICE);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, BUFFER_POOL_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, BUFFERS_PER_SLICE, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(BUFFERS_PER_SLICE, value);
        } finally {
            operations.writeAttribute(BUFFER_POOL_ADDRESS, BUFFERS_PER_SLICE, originalNodeResult.value());
        }
    }

    @Test
    public void editBuffersPerSliceInvalid() throws Exception {
        final int value = -1;
        final ModelNodeResult originalNodeResult = operations.readAttribute(BUFFER_POOL_ADDRESS, BUFFERS_PER_SLICE);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, BUFFER_POOL_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, BUFFERS_PER_SLICE, String.valueOf(value))
                    .verifyFormNotSaved();
        } finally {
            operations.writeAttribute(BUFFER_POOL_ADDRESS, BUFFERS_PER_SLICE, originalNodeResult.value());
        }
    }

    @Test
    public void editBufferSize() throws Exception {
        final int value = 1024;
        final ModelNodeResult originalNodeResult = operations.readAttribute(BUFFER_POOL_ADDRESS, BUFFER_SIZE);
        originalNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, BUFFER_POOL_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, BUFFER_SIZE, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(BUFFER_SIZE, value);
        } finally {
            operations.writeAttribute(BUFFER_POOL_ADDRESS, BUFFER_SIZE, originalNodeResult.value());
        }
    }

    @Test
    public void editBufferSizeInvalid() throws Exception {
        final int value = -1;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(BUFFER_POOL_ADDRESS, BUFFER_SIZE);
        originalModelNodeResult.assertSuccess();
        try {
            new ConfigChecker.Builder(client, BUFFER_POOL_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, BUFFER_SIZE, String.valueOf(value))
                    .verifyFormNotSaved();
        } finally {
            operations.writeAttribute(BUFFER_POOL_ADDRESS, BUFFER_SIZE, originalModelNodeResult.value());
        }
    }

    @Test
    public void toggleDirectBuffers() throws Exception {
        final ModelNodeResult originalNodeResult = operations.readAttribute(BUFFER_POOL_ADDRESS, DIRECT_BUFFERS);
        originalNodeResult.assertSuccess();
        final boolean originalValue = !originalNodeResult.hasDefinedValue() || originalNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, BUFFER_POOL_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, DIRECT_BUFFERS, !originalValue)
                    .verifyFormSaved()
                    .verifyAttribute(DIRECT_BUFFERS, !originalValue);

            new ConfigChecker.Builder(client, BUFFER_POOL_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, DIRECT_BUFFERS, originalValue)
                    .verifyFormSaved()
                    .verifyAttribute(DIRECT_BUFFERS, originalValue);
        } finally {
            operations.writeAttribute(BUFFER_POOL_ADDRESS, DIRECT_BUFFERS, originalNodeResult.value());
        }
    }

    @Test
    public void addBufferPool() throws Exception {
        page.addBufferPool().name(BUFFER_POOL_TBA_ADDRESS.getLastPairValue()).saveAndDismissReloadRequiredWindow();

        Assert.assertTrue(page.getResourceManager().isResourcePresent(BUFFER_POOL_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(BUFFER_POOL_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeBufferPool() throws Exception {
        page.getResourceManager().removeResource(BUFFER_POOL_TBR_ADDRESS.getLastPairValue()).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(page.getResourceManager().isResourcePresent(BUFFER_POOL_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(BUFFER_POOL_TBR_ADDRESS, client).verifyDoesNotExist();
    }

}
