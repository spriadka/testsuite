package org.jboss.hal.testsuite.test.configuration.weld;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.WeldPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class WeldSubsystemTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private WeldPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final Address WELD_SUBSYSTEM_ADDRESS = Address.subsystem("weld");

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
    public void toggleDevelopmentMode() throws Exception {
        toggleCheckboxAndWriteBackDefaultValue(WELD_SUBSYSTEM_ADDRESS, "development-mode");
    }

    @Test
    public void toggleNonPortableMode() throws Exception {
        toggleCheckboxAndWriteBackDefaultValue(WELD_SUBSYSTEM_ADDRESS, "non-portable-mode");
    }

    @Test
    public void toggleRequireBeanDescriptor() throws Exception {
        toggleCheckboxAndWriteBackDefaultValue(WELD_SUBSYSTEM_ADDRESS, "require-bean-descriptor");
    }

    private void toggleCheckboxAndWriteBackDefaultValue(Address address, String attributeName) throws Exception {
        final ModelNodeResult value = operations.readAttribute(address, attributeName);
        value.assertSuccess();
        final boolean booleanValue = value.booleanValue();
        try {
            new ConfigChecker.Builder(client, address)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, !booleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, !booleanValue);

            new ConfigChecker.Builder(client, address)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, booleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, booleanValue);
        } finally {
            operations.writeAttribute(address, attributeName, value.value());
        }
    }



}
