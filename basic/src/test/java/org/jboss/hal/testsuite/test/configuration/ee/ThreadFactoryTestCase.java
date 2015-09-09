package org.jboss.hal.testsuite.test.configuration.ee;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Jan Kasik
 *         Created on 9.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ThreadFactoryTestCase extends EETestCaseAbstract {

    //identifiers
    private static final String JNDI_NAME = "jndi-name";
    private static final String CONTEXT_SERVICE = "context-service";
    private static final String PRIORITY = "priority";

    //attribute-names
    private static final String JNDI_NAME_ADDR = "jndi-name";
    private static final String CONTEXT_SERVICE_ADDR = "context-service";
    private static final String PRIORITY_ADDR = "priority";

    //values
    private static final String NUMERIC_VALID = "7";
    private static final String NUMERIC_INVALID = "7f";
    private static final String JNDI_INVALID = "test";
    private static final String JNDI_DEFAULT = "java:/";
    private static final String JNDI_VALID = JNDI_DEFAULT + RandomStringUtils.randomAlphanumeric(6);

    private static final String EE_CHILD = "managed-thread-factory";

    private static ResourceAddress address;
    private static String threadFactory;

    @After
    public void after() {
        removeThreadFactory(threadFactory);
    }

    @Before
    public void before() {
        threadFactory = createThreadFactory();
        address = new ResourceAddress(eeAddress).add(EE_CHILD, threadFactory);
        reloadAndWaitForRunning();
        navigateToEEServices();
        page.switchSubTab("Thread Factories");
        page.getResourceManager().getResourceTable().selectRowByText(0, threadFactory);
    }

    @Test
    public void editContextService() throws IOException, InterruptedException {
        editTextAndVerify(address, CONTEXT_SERVICE, CONTEXT_SERVICE_ADDR, "default");
    }

    @Test
    public void editJNDIName() throws IOException, InterruptedException {
        editTextAndVerify(address, JNDI_NAME, JNDI_NAME_ADDR, JNDI_VALID);
    }

    @Ignore("Currently, there is an inconsistency in JNDI name format (in datasources subsystem java:/ prefix is required)")
    @Test
    public void editJNDINameInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(JNDI_NAME, JNDI_INVALID);
    }

    @Test
    public void editPriority() throws IOException, InterruptedException {
        editTextAndVerify(address, PRIORITY, PRIORITY_ADDR, NUMERIC_VALID);
    }

    @Test
    public void editPriorityInvalid() {
        verifyIfErrorAppears(PRIORITY, NUMERIC_INVALID);
    }

    @Test
    public void addThreadFactoryInGUI() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text(JNDI_NAME, JNDI_VALID);
        boolean result = wizard.finish();

        assertTrue("Window should be closed", result);
        assertTrue("Executor should be present in table", config.resourceIsPresent(name));
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        verifier.verifyResource(address, true, 5000);
    }

    @Test
    public void removeThreadFactoryInGUI() {
        String name = createThreadFactory();
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager().removeResource(name).confirm();

        Assert.assertFalse("Executor should not be present in table", config.resourceIsPresent(name));
        Assert.assertFalse("Executor should not be present on server", removeThreadFactory(name));
    }

    private String createThreadFactory() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        dispatcher.execute(new Operation.Builder("add", address)
                .param(JNDI_NAME, JNDI_DEFAULT + name)
                .build());
        return name;
    }

    private boolean removeThreadFactory(String name) {
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        return dispatcher.execute(new Operation.Builder("remove", address).build()).isSuccessful();
    }
}
