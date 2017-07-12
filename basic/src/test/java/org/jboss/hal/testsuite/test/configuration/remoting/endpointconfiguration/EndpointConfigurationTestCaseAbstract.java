package org.jboss.hal.testsuite.test.configuration.remoting.endpointconfiguration;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.RemotingSubsystemPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public abstract class EndpointConfigurationTestCaseAbstract {

    @Page
    protected RemotingSubsystemPage page;

    @Drone
    protected WebDriver browser;

    protected static final Address
            REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting"),
            ENDPOINT_CONFIGURATION_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("configuration", "endpoint");

    protected static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static Administration administration = new Administration(client);
    protected static Operations operations = new Operations(client);

    @Before
    public void initPage() {
        page.navigate();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    protected void enterTextAndVerify(String inputField, String value) throws Exception {
        ConfigFragment configFragment = page.getConfigFragment();
        configFragment.editTextAndSave(inputField, value);
        new ResourceVerifier(ENDPOINT_CONFIGURATION_ADDRESS, client).verifyAttribute(inputField, value);
    }

    protected void enterTextAndVerify(String inputField, int value) throws Exception {
        ConfigFragment configFragment = page.getConfigFragment();
        configFragment.editTextAndSave(inputField, String.valueOf(value));
        administration.reloadIfRequired();
        new ResourceVerifier(ENDPOINT_CONFIGURATION_ADDRESS, client).verifyAttribute(inputField, value);
    }

    protected void enterTextAndVerify(String inputField, long value) throws Exception {
        ConfigFragment configFragment = page.getConfigFragment();
        configFragment.editTextAndSave(inputField, String.valueOf(value));
        administration.reloadIfRequired();
        new ResourceVerifier(ENDPOINT_CONFIGURATION_ADDRESS, client).verifyAttribute(inputField, value);
    }


}
