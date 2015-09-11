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
    private final String JNDI_NAME = "jndi-name";
    private final String CONTEXT_SERVICE = "context-service";
    private final String PRIORITY = "priority";

    //attribute names
    private final String JNDI_NAME_ATTR = "jndi-name";
    private final String CONTEXT_SERVICE_ATTR = "context-service";
    private final String PRIORITY_ATTR = "priority";

    //values
    private final String NUMERIC_VALID = "7";
    private final String NUMERIC_INVALID = "7f";
    private final String JNDI_INVALID = "test";
    private final String JNDI_DEFAULT = "java:/";
    private final String JNDI_VALID = JNDI_DEFAULT + RandomStringUtils.randomAlphanumeric(6);

    private final String EE_CHILD = "managed-thread-factory";

    private ResourceAddress address;
    private String threadFactory;

    @After
    public void after() {
        removeEEChild(EE_CHILD, threadFactory);
    }

    @Before
    public void before() {
        threadFactory = createThreadFactory();
        address = new ResourceAddress(eeAddress).add(EE_CHILD, threadFactory);
        reloadIfRequiredAndWaitForRunning();
        page.navigate();
        page.switchSubTab("Thread Factories");
        page.getResourceManager().getResourceTable().selectRowByText(0, threadFactory);
    }

    @Test
    public void editContextService() throws IOException, InterruptedException {
        editTextAndVerify(address, CONTEXT_SERVICE, CONTEXT_SERVICE_ATTR, "default");
    }

    @Test
    public void editJNDIName() throws IOException, InterruptedException {
        editTextAndVerify(address, JNDI_NAME, JNDI_NAME_ATTR, JNDI_VALID);
    }

    @Ignore("Currently, there is an inconsistency in JNDI name format (in datasources subsystem java:/ prefix is required)")
    @Test
    public void editJNDINameInvalid() throws IOException, InterruptedException {
        verifyIfErrorAppears(JNDI_NAME, JNDI_INVALID);
    }

    @Test
    public void editPriority() throws IOException, InterruptedException {
        editTextAndVerify(address, PRIORITY, PRIORITY_ATTR, NUMERIC_VALID);
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
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager().removeResource(threadFactory).confirm();

        Assert.assertFalse("Executor should not be present in table", config.resourceIsPresent(threadFactory));
        Assert.assertFalse("Executor should not be present on server", removeEEChild(EE_CHILD, threadFactory));
    }

    private String createThreadFactory() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        dispatcher.execute(new Operation.Builder("add", address)
                .param(JNDI_NAME, JNDI_DEFAULT + name)
                .build());
        return name;
    }
}
