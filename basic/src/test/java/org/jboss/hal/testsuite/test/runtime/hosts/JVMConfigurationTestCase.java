package org.jboss.hal.testsuite.test.runtime.hosts;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.runtime.HostJVMConfigurationPage;
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
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Testcase covering settings attributes on JVM configuration page of host
 */
@Category(Domain.class)
@RunWith(Arquillian.class)
public class JVMConfigurationTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    private static final String
            JVM_NAME = "myJVM_" + RandomStringUtils.randomAlphanumeric(5),
            AGENT_LIB_IDENTIFIER = "agent-lib",
            AGENT_PATH_IDENTIFIER = "agent-path",
            ENV_CLASSPATH_IGNORED_IDENTIFIER = "env-classpath-ignored",
            ENVIRONMENT_VARIABLES_IDENTIFIER = "environment-variables",
            HEAP_SIZE_IDENTIFIER = "heap-size",
            JAVA_AGENT_IDENTIFIER = "java-agent",
            JAVA_HOME_IDENTIFIER = "java-home",
            JVM_OPTIONS_IDENTIFIER = "jvm-options",
            LAUNCH_COMMAND_IDENTIFIER = "launch-command",
            MAX_HEAP_SIZE_IDENTIFIER = "max-heap-size",
            STACK_SIZE_IDENTIFIER = "stack-size",
            TYPE_IDENTIFIER = "type";
    private static final Address JVM_ADDRESS = Address.host(client.options().defaultHost).and("jvm", JVM_NAME);

    @Drone
    private WebDriver browser;

    @Page
    private HostJVMConfigurationPage page;


    @BeforeClass
    public static void beforeClass() throws IOException {
        //add JVM configuration
        operations.add(JVM_ADDRESS).assertSuccess();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(JVM_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Before
    public void before() {
        page.navigate();
        page.selectJVMConfiguration(JVM_NAME);
    }

    @Test
    public void editAgentLib() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, AGENT_LIB_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AGENT_LIB_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(AGENT_LIB_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, AGENT_LIB_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editAgentPath() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, AGENT_PATH_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, AGENT_PATH_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(AGENT_PATH_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, AGENT_PATH_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void toggleEnvClasspathIgnored() throws Exception {
        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, ENV_CLASSPATH_IGNORED_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ENV_CLASSPATH_IGNORED_IDENTIFIER, true)
                    .verifyFormSaved()
                    .verifyAttribute(ENV_CLASSPATH_IGNORED_IDENTIFIER, true);
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, ENV_CLASSPATH_IGNORED_IDENTIFIER, false)
                    .verifyFormSaved()
                    .verifyAttribute(ENV_CLASSPATH_IGNORED_IDENTIFIER, false);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, ENV_CLASSPATH_IGNORED_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editEnvironmentVariables() throws Exception {
        final String KEY = "jvmEnvPropKey_" + RandomStringUtils.randomAlphanumeric(5);
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, ENVIRONMENT_VARIABLES_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, ENVIRONMENT_VARIABLES_IDENTIFIER, KEY + "=" + VALUE)
                    .verifyFormSaved();

            Assert.assertTrue("Property with key='" + KEY + "' and value='" + VALUE + "' is not defined in '" + ENVIRONMENT_VARIABLES_IDENTIFIER + "' attribute.",
                    hasModelNodeListGivenProperty(operations.readAttribute(JVM_ADDRESS, ENVIRONMENT_VARIABLES_IDENTIFIER), KEY, VALUE));

        } finally {
            operations.writeAttribute(JVM_ADDRESS, ENVIRONMENT_VARIABLES_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editHeapSize() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, HEAP_SIZE_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, HEAP_SIZE_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(HEAP_SIZE_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, HEAP_SIZE_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editJavaAgent() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, JAVA_AGENT_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, JAVA_AGENT_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(JAVA_AGENT_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, JAVA_AGENT_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editJavaHome() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, JAVA_HOME_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, JAVA_HOME_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(JAVA_HOME_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, JAVA_HOME_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editJVMOptions() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, JVM_OPTIONS_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, JVM_OPTIONS_IDENTIFIER, VALUE)
                    .verifyFormSaved();

            Assert.assertTrue("Value is not present among values in list of '" + JVM_OPTIONS_IDENTIFIER + "' attribute!",
                    isValueInsideModelNodeList(operations.readAttribute(JVM_ADDRESS, JVM_OPTIONS_IDENTIFIER).value().asList(), VALUE));
        } finally {
            operations.writeAttribute(JVM_ADDRESS, JVM_OPTIONS_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editLaunchCommand() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, LAUNCH_COMMAND_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, LAUNCH_COMMAND_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(LAUNCH_COMMAND_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, LAUNCH_COMMAND_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editMaxHeapSize() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, MAX_HEAP_SIZE_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, MAX_HEAP_SIZE_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(MAX_HEAP_SIZE_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, MAX_HEAP_SIZE_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void editStackSize() throws Exception {
        final String VALUE = RandomStringUtils.randomAlphanumeric(5);

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, STACK_SIZE_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, STACK_SIZE_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(STACK_SIZE_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, STACK_SIZE_IDENTIFIER, originalValue.value());
        }
    }

    @Test
    public void selectType() throws Exception {
        final String VALUE = "SUN";

        ModelNodeResult originalValue = operations.readAttribute(JVM_ADDRESS, TYPE_IDENTIFIER, ReadAttributeOption.INCLUDE_DEFAULTS);
        originalValue.assertSuccess();
        try {
            new ConfigChecker.Builder(client, JVM_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.SELECT, TYPE_IDENTIFIER, VALUE)
                    .verifyFormSaved()
                    .verifyAttribute(TYPE_IDENTIFIER, VALUE);
        } finally {
            operations.writeAttribute(JVM_ADDRESS, TYPE_IDENTIFIER, originalValue.value());
        }
    }

    private boolean isValueInsideModelNodeList(List<ModelNode> list, String value) {
        for (ModelNode node : list) {
            if (value.equals(node.asString())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasModelNodeListGivenProperty(ModelNodeResult result, String key, String value) throws IOException {
        result.assertSuccess();
        if (!result.hasDefinedValue()) {
            return false;
        }
        for (Property property : result.value().asPropertyList()) {
            if (property.getName().equals(key) && property.getValue().asString().equals(value)) {
                return true;
            }
        }
        return false;
    }

}
