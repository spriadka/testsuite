package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.logging.Logging;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RootLoggerTestCase extends LoggingAbstractTestCase {

    private static final Address ROOT_LOGGER_ADDRESS = LOGGING_SUBSYSTEM.and("root-logger", "ROOT");
    private static BackupAndRestoreAttributes backup;

    @Drone
    private WebDriver browser;
    @Page
    private LoggingPage page;

    @BeforeClass
    public static void beforeClass() throws CommandFailedException {
        backup = new BackupAndRestoreAttributes.Builder(ROOT_LOGGER_ADDRESS).build();
        client.apply(backup.backup());
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException {
        client.apply(backup.restore());
    }

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void editLevel() throws Exception {
        selectOptionAndVerify(ROOT_LOGGER_ADDRESS, "level", "WARN");
    }

    @Test
    public void editFilterSpec() throws Exception {
        editTextAndVerify(ROOT_LOGGER_ADDRESS, "filter-spec", "not(match(\"JBAS.*\"))");
    }

    @Test
    public void editHandlers() throws Exception {
        String handler = "testHandler_" + RandomStringUtils.randomAlphanumeric(5);
        ModelNodeResult originalHandlersResult = operations.readAttribute(ROOT_LOGGER_ADDRESS, "handlers",
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        client.apply(Logging.handler()
                .console()
                .add(handler)
                .build());
        try {
            editTextAreaAndVerify(ROOT_LOGGER_ADDRESS, "handlers", new String[]{handler});
        } finally {
            operations.writeAttribute(ROOT_LOGGER_ADDRESS, "handlers", originalHandlersResult.value());
            ResourceVerifier rootLoggerVerifier = new ResourceVerifier(ROOT_LOGGER_ADDRESS, client, 4000);
            if (originalHandlersResult.hasDefinedValue()) {
                rootLoggerVerifier.verifyAttribute("handlers", originalHandlersResult.value());
            } else {
                rootLoggerVerifier.verifyAttributeIsUndefined("handlers");
            }
            client.apply(Logging.handler().console().remove(handler));
        }
    }

    @Test
    public void undefineHandlers() throws Exception {
        ConfigFragment editPanelFragment = page.getConfigFragment();
        Editor editor = editPanelFragment.edit();

        editor.text("handlers", "");
        boolean finished = editPanelFragment.save();

        assertTrue("Config should be saved and closed.", finished);
        new ResourceVerifier(ROOT_LOGGER_ADDRESS, client).verifyAttributeIsUndefined("handlers");
    }
}
