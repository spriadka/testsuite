package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.undertow.UndertowFragment;
import org.jboss.hal.testsuite.page.config.UndertowPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class UndertowTestCaseAbstract {

    protected static OnlineManagementClient client;
    protected static UndertowOperations undertowOps;
    protected static Operations operations;
    protected static Administration administration;

    protected static final Address UNDERTOW_ADDRESS = Address.subsystem("undertow");

    //shared valid values
    protected static final int NUMERIC_VALID = 25;
    protected static final long NUMERIC_VALID_LONG = 25L;
    protected static final String NUMERIC_INVALID = "25fazf";


    @Drone
    protected WebDriver browser;

    @Page
    protected UndertowPage page;

    @BeforeClass
    public static void mainSetUp() throws IOException, CommandFailedException, TimeoutException, InterruptedException {
        client = ManagementClientProvider.createOnlineManagementClient();
        operations = new Operations(client);
        administration = new Administration(client);
        undertowOps = new UndertowOperations(client);
    }

    @AfterClass
    public static void mainTearDown() throws InterruptedException, IOException, TimeoutException, OperationException, CommandFailedException {
        try {
            undertowOps.cleanupReferences();
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    protected void editTextAndVerify(Address address, String attributeName, String value) throws Exception {
        page.getConfigFragment().editTextAndSave(attributeName, value);
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String attributeName, int value) throws Exception {
        page.getConfigFragment().editTextAndSave(attributeName, String.valueOf(value));
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String attributeName, long value) throws Exception {
        page.getConfigFragment().editTextAndSave(attributeName, String.valueOf(value));
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String attributeName) throws Exception {
        editTextAndVerify(address, attributeName, "undertow_" + attributeName + RandomStringUtils.randomAlphabetic(4));
    }

    protected void editCheckboxAndVerify(Address address, String attributeName, Boolean value) throws Exception {
        page.getConfigFragment().editCheckboxAndSave(attributeName, value);
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    protected void editCheckboxAndVerify(Address address, String identifier, String attributeName, Boolean value) throws Exception {
        page.getConfigFragment().editCheckboxAndSave(identifier, value);
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    public void selectOptionAndVerify(Address address, String attributeName, String value) throws Exception {
        page.getConfigFragment().selectOptionAndSave(attributeName, value);
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    public void editTextAreaAndVerify(Address address, String attributeName, String[] values) throws Exception {
        page.getConfigFragment().editTextAndSave(attributeName, String.join("\n", values));
        ModelNode expected = new ModelNode();
        for (String value : values) {
            expected.add(value);
        }
        new ResourceVerifier(address, client).verifyAttribute(attributeName, expected);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        UndertowFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShowedInForm());
        config.cancel();
    }

}
