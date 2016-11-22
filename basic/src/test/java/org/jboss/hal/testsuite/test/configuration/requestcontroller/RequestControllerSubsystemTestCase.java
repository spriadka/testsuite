package org.jboss.hal.testsuite.test.configuration.requestcontroller;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.RequestControllerPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class RequestControllerSubsystemTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private RequestControllerPage page;

    private static final Address REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS = Address.subsystem("request-controller");

    private static final String
            ACTIVE_REQUESTS = "active-requests",
            MAX_REQUESTS = "max-requests",
            TRACK_INDIVIDUAL_ENDPOINTS = "track-individual-endpoints";

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    @Before
    public void before() {
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

    @Test
    public void verifyActiveRequests() throws Exception {
        ModelNodeResult value = operations.readAttribute(REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS, ACTIVE_REQUESTS,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);

        value.assertDefinedValue();

        Assert.assertEquals(page.getConfigFragment().edit().text(ACTIVE_REQUESTS), value.stringValue());
    }

    @Test
    public void editMaxRequests() throws Exception {
        int value = 42;
        ModelNodeResult originalValue = operations.readAttribute(REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS, MAX_REQUESTS,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MAX_REQUESTS, String.valueOf(value))
                    .verifyFormSaved()
                    .verifyAttribute(MAX_REQUESTS, value);
        } finally {
            operations.writeAttribute(REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS, MAX_REQUESTS, originalValue.value())
                    .assertSuccess();
        }
    }

    @Test
    public void toggleTrackIndividualEndpoints() throws Exception {
        final ModelNodeResult originalValue = operations.readAttribute(REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS,
                TRACK_INDIVIDUAL_ENDPOINTS, ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        final ModelNodeResult defaultValue = operations.readAttribute(REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS,
                TRACK_INDIVIDUAL_ENDPOINTS, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        defaultValue.assertSuccess();
        final boolean defaultValueBool = defaultValue.booleanValue(); //originalValue is undefined
        try {
            new ConfigChecker.Builder(client, REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, TRACK_INDIVIDUAL_ENDPOINTS, !defaultValueBool)
                    .verifyFormSaved()
                    .verifyAttribute(TRACK_INDIVIDUAL_ENDPOINTS, !defaultValueBool);

            new ConfigChecker.Builder(client, REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, TRACK_INDIVIDUAL_ENDPOINTS, defaultValueBool)
                    .verifyFormSaved()
                    .verifyAttribute(TRACK_INDIVIDUAL_ENDPOINTS, defaultValueBool);
        } finally {
            operations.writeAttribute(REQUEST_CONTROLLER_SUBSYSTEM_ADDRESS, TRACK_INDIVIDUAL_ENDPOINTS,
                    originalValue.value()).assertSuccess();
        }
    }


}
