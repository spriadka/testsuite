package org.jboss.hal.testsuite.test.configuration.JCA;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 21.10.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class JCASubsystemTestCase {

    private static final Address JCA_SUBSYSTEM_ADDRESS = Address.subsystem("jca");
    private static final Address CONNECTION_MANAGER_ADDRESS = JCA_SUBSYSTEM_ADDRESS.and("cached-connection-manager", "cached-connection-manager");
    private static final Address ARCHIVE_VALIDATION_ADDRESS = JCA_SUBSYSTEM_ADDRESS.and("archive-validation", "archive-validation");
    private static final Address BEAN_VALIDATION_ADDRESS = JCA_SUBSYSTEM_ADDRESS.and("bean-validation", "bean-validation");
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    @AfterClass
    public static void tearDown() throws IOException, TimeoutException, InterruptedException {
        try {
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Drone
    private WebDriver browser;
    @Page
    private JCAPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void updateDebug() throws Exception {
        toggleCheckboxAndWriteBackDefaultValue(CONNECTION_MANAGER_ADDRESS, "debug");
    }

    @Test
    public void updateError() throws Exception {
        toggleCheckboxAndWriteBackDefaultValue(CONNECTION_MANAGER_ADDRESS, "error");
    }

    @Test
    public void updateIgnoreUnknownConnections() throws Exception {
        toggleCheckboxAndWriteBackDefaultValue(CONNECTION_MANAGER_ADDRESS, "ignore-unknown-connections");
    }

    @Test
    public void updateArchiveValidationEnabled() throws Exception {
        page.switchToArchiveValidation();
        toggleCheckboxAndWriteBackDefaultValue(ARCHIVE_VALIDATION_ADDRESS, "enabled");
    }

    @Test
    public void updateArchiveValidationFailOnError() throws Exception {
        page.switchToArchiveValidation();
        toggleCheckboxAndWriteBackDefaultValue(ARCHIVE_VALIDATION_ADDRESS, "fail-on-error");
    }

    @Test
    public void updateArchiveValidationFailOnWarn() throws Exception {
        page.switchToArchiveValidation();
        toggleCheckboxAndWriteBackDefaultValue(ARCHIVE_VALIDATION_ADDRESS, "fail-on-warn");
    }

    @Test
    public void updateBeanValidationEnabled() throws Exception {
        page.switchToBeanValidation();
        toggleCheckboxAndWriteBackDefaultValue(BEAN_VALIDATION_ADDRESS, "enabled");
    }

    private void toggleCheckboxAndWriteBackDefaultValue(Address address, String attributeName) throws Exception {
        ModelNode value = operations.readAttribute(address, attributeName);
        try {
            new ConfigChecker.Builder(client, address)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, true)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, true);

            new ConfigChecker.Builder(client, address)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, false)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, false);
        } finally {
            operations.writeAttribute(address, attributeName, value);
        }
    }
}
