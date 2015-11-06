package org.jboss.hal.testsuite.test.configuration.JCA;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.config.resourceadapters.ConfigPropertiesFragment;
import org.jboss.hal.testsuite.page.config.JCAPage;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 24.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ResourceAdaptersPolicyTestCase {

    private static final String SIZE = "Size=7";
    private static final String WATERMARK = "Watermark=9";
    private static final String NAME_NO_TRANSACTION = "ran_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ARCHIVE = "ran_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String NO_TRANSACTION = "NoTransaction";
    private static final String NAME = "CD-test";
    private static final String JNDINAME = "java:/CD-test";

    private  FinderNavigation navigation;

    private ModelNode path = new ModelNode("/subsystem=resource-adapters/resource-adapter=" + NAME_NO_TRANSACTION +
                                                "/connection-definitions=" + NAME);
    private ResourceAddress address = new ResourceAddress(path);
    private static Dispatcher dispatcher;
    private static ResourceVerifier verifier;
    private static CliClient cliClient = CliClientFactory.getClient();

    @BeforeClass
    public static void setUp() {
        dispatcher = new Dispatcher();
        verifier  = new ResourceVerifier(dispatcher);
        String command = "/subsystem=resource-adapters/resource-adapter=" + NAME_NO_TRANSACTION +
                ":add" +
                "(archive=" + ARCHIVE +
                ",transaction-support=" + NO_TRANSACTION + ")";
        cliClient.executeCommand(command);
        command = "/subsystem=resource-adapters/resource-adapter=" + NAME_NO_TRANSACTION
                + "/connection-definitions=" + NAME
                + ":add(jndi-name=" + JNDINAME
                + ",class-name=class,enabled=false)";
        cliClient.executeCommand(command);
    }

    @AfterClass
    public static void tearDown() {
        String command = "/subsystem=resource-adapters/resource-adapter=" + NAME_NO_TRANSACTION + ":remove";
        cliClient.executeCommand(command);
        dispatcher.close();
    }

    @Drone
    private WebDriver browser;
    @Page
    private JCAPage jcaPage;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class)
                .addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                .addAddress(FinderNames.SUBSYSTEM, "Resource Adapters")
                .addAddress("Resource Adapter", NAME_NO_TRANSACTION);

        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();

        jcaPage.switchToConnectionDefinitions();

        ConfigPropertiesFragment fragment = jcaPage.getConfig().poolConfig();
        fragment.edit();
    }


    @Test
    @InSequence(0)
    public void setDecrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacity-decrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");
    }

    @Test //property is not saved for next test .. https://issues.jboss.org/browse/HAL-809
    @InSequence(1)
    public void setDecrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacity-decrementer-properties", WATERMARK);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-properties", "[(\"Watermark\" => \"9\")]");

    }

    @Test
    @InSequence(2)
    public void unsetDecrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacity-decrementer-properties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-properties", "undefined");
    }

    @Test
    @InSequence(3)
    public void unsetDecrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select("capacity-decrementer-class", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-decrementer-class", "undefined");
    }

    @Test
    @InSequence(4)
    public void setIncrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor(). select("capacity-incrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-class", "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");
    }

    @Test //property is not saved for next test .. https://issues.jboss.org/browse/HAL-809
    @InSequence(5)
    public void setIncrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacity-incrementer-properties", SIZE);

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-properties", "[(\"Size\" => \"7\")]");
    }

    @Test
    @InSequence(6)
    public void unsetIncrementerProperty() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().text("capacity-incrementer-properties", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-properties", "undefined");
    }

    @Test
    @InSequence(7)
    public void unsetIncrementerClass() {
        ConfigFragment editPanelFragment = jcaPage.getConfigFragment();
        editPanelFragment.getEditor().select("capacity-incrementer-class", "");

        boolean finished = editPanelFragment.save();
        assertTrue("Config should be saved and closed.", finished);

        verifier.verifyAttribute(address, "capacity-incrementer-class", "undefined");
    }
}
