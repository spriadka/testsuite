package org.jboss.hal.testsuite.test.configuration.jsf;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.JsfPage;
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
public class JsfSubsystemTestCase {

    private static final String DEFAULT_JSF_IMPL_SLOT = "default-jsf-impl-slot";

    private static final Address JSF_SUBSYSTEM_ADDRESS = Address.subsystem("jsf");

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);

    @Drone
    private WebDriver browser;

    @Page
    private JsfPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

    @Test
    public void editDefaultJsfImplSlot() throws Exception {
        String value = "jsf_" + RandomStringUtils.randomAlphanumeric(5);
        ModelNodeResult result = operations.readAttribute(JSF_SUBSYSTEM_ADDRESS, DEFAULT_JSF_IMPL_SLOT);
        result.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JSF_SUBSYSTEM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, DEFAULT_JSF_IMPL_SLOT, value)
                    .verifyFormSaved()
                    .verifyAttribute(DEFAULT_JSF_IMPL_SLOT, value);
        } finally {
            operations.writeAttribute(JSF_SUBSYSTEM_ADDRESS, DEFAULT_JSF_IMPL_SLOT, result.value()).assertSuccess();
        }
    }
}
