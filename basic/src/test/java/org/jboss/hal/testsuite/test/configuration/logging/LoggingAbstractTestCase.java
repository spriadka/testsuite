package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.wildfly.extras.creaper.commands.logging.AddPeriodicRotatingFileLogHandler;
import org.wildfly.extras.creaper.commands.logging.Logging;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

public abstract class LoggingAbstractTestCase {

    @Page
    private LoggingPage page;

    protected static Address LOGGING_SUBSYSTEM;

    protected static OnlineManagementClient client;
    protected static Operations operations;
    protected static Administration administration;

    protected static final String
            FORMATTER = "formatter",
            NAMED_FORMATTER = "named-formatter",
            PERIODIC_SIZE_ROTATING_FILE_HANDLER = "periodic-size-rotating-file-handler";

    @BeforeClass
    public static void beforeClass_() {
        client = ManagementClientProvider.createOnlineManagementClient();
        operations = new Operations(client);
        administration = new Administration(client);
        LOGGING_SUBSYSTEM = Address.subsystem("logging");
    }

    @AfterClass
    public static void afterClass_() throws InterruptedException, TimeoutException, IOException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    protected void editTextAndVerify(Address address, String name, String value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);

        new ResourceVerifier(address, client, 500).verifyAttribute(name, value);
    }

    protected void editTextAndVerify(Address address, String name, Integer value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, value.toString());
        assertTrue("Config should be saved and closed.", finished);

        new ResourceVerifier(address, client, 500).verifyAttribute(name, value);
    }

    protected void editCheckboxAndVerify(Address address, String name, boolean value) throws Exception {
        boolean finished = page.getConfigFragment().editCheckboxAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);

        new ResourceVerifier(address, client, 500).verifyAttribute(name, value);
    }

    protected  void selectOptionAndVerify(Address address, String name, String value) throws Exception {
        boolean finished = page.getConfigFragment().selectOptionAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);

        new ResourceVerifier(address, client, 500).verifyAttribute(name, value);
    }

    protected void editTextAreaAndVerify(Address address, String name, String[] values) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, String.join("\n", values));

        assertTrue("Config should be saved and closed.", finished);

        ModelNode modelNode = new ModelNode();

        for (String value : values) {
            modelNode.add(value);
        }

        new ResourceVerifier(address, client, 500).verifyAttribute(name, modelNode);
    }

    protected static void createFileHandler(Address address, String path) throws IOException {
        operations.add(address, Values.empty()
                .andObject("file", Values.empty()
                        .and("path", path)
                        .and("relative-to", "jboss.server.log.dir")));
    }

    protected static void createPeriodicFileHandler(String name, String path) throws CommandFailedException {
        AddPeriodicRotatingFileLogHandler addPeriodicRotatingFileLogHandler = Logging
                .handler().periodicRotatingFile()
                .add(name, path, "%H%m")
                .build();
        client.apply(addPeriodicRotatingFileLogHandler);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShownInForm());
        config.cancel();
    }

    protected final String getTmpDirPath(String subdir) {
        return new File(System.getProperty("java.io.tmpdir"), subdir).getAbsolutePath();
    }

    protected void addHandlers(Address address, String attributeName) throws Exception {
        String handler_one = "log-cat-test-handler_" + RandomStringUtils.randomAlphanumeric(5);
        String handler_two = "log-cat-test-handler_" + RandomStringUtils.randomAlphanumeric(5);
        client.apply(Logging.handler().console().add(handler_one).build(),
                Logging.handler().console().add(handler_two).build());
        administration.reloadIfRequired();
        try {
            editTextAreaAndVerify(address, attributeName, new String[]{handler_one, handler_two});
        } finally {
            operations.undefineAttribute(address, attributeName);
            client.apply(Logging.handler().console().remove(handler_one),
                    Logging.handler().console().remove(handler_two));
            administration.reloadIfRequired();
        }
    }
}
