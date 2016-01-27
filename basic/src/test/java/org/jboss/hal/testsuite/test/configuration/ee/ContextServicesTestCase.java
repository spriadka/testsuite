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
 *         Created on 8.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ContextServicesTestCase extends EETestCaseAbstract {

    //identifiers
    private final String JNDI_NAME = "jndi-name";
    private final String USE_TRANSACTIONAL = "use-transaction-setup-provider";

    //attribute-names
    private final String JNDI_NAME_ATTR = "jndi-name";
    private final String USE_TRANSACTIONAL_ATTR = "use-transaction-setup-provider";

    //values
    private final String JNDI_INVALID = "test";
    private final String JNDI_DEFAULT = "java:/";
    private final String JNDI_VALID = JNDI_DEFAULT + "eeTestAbstract_" + RandomStringUtils.randomAlphanumeric(6);

    private final String EE_CHILD = "context-service";

    private ResourceAddress address;
    private String contextService;

    @Before
    public void before() {
        contextService = createContextService();
        reloadIfRequiredAndWaitForRunning();
        address = new ResourceAddress(eeAddress).add(EE_CHILD, contextService);
        page.navigate();
        page.switchSubTab("Context Service");
        page.getResourceManager().getResourceTable().selectRowByText(0, contextService);
    }

    @After
    public void after() {
        removeEEChild(EE_CHILD, contextService);
    }

    @Test
    public void editJNDIName() throws IOException, InterruptedException {
        editTextAndVerify(address, JNDI_NAME, JNDI_NAME_ATTR, JNDI_VALID);
    }

    @Ignore("Currently, there is an inconsistency in JNDI name format (in datasources subsystem java:/ prefix is required)")
    @Test
    public void editJNDINameInvalid() {
        verifyIfErrorAppears(JNDI_NAME, JNDI_INVALID);
    }

    @Test
    public void setUseTransactionalToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, USE_TRANSACTIONAL, USE_TRANSACTIONAL_ATTR, true);
    }

    @Test
    public void setUseTransactionalToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, USE_TRANSACTIONAL, USE_TRANSACTIONAL_ATTR, false);
    }

    @Test
    public void addContextServiceInGUI() {
        String name = "EEContextService_" + RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text(JNDI_NAME, JNDI_VALID);
        wizard.saveAndDismissReloadRequiredWindow();

        assertTrue("Context service should be present in table", config.resourceIsPresent(name));
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        verifier.verifyResource(address, true, 5000);
        removeEEChild(EE_CHILD, name);
    }

    @Test
    public void removeContextServiceInGUI() {
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager().removeResource(contextService).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse("Context service should not be present in table", config.resourceIsPresent(contextService));
        Assert.assertFalse("Context service should not be present on server", removeEEChild(EE_CHILD, contextService));
    }

    private String createContextService() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = new ResourceAddress(eeAddress).add(EE_CHILD, name);
        dispatcher.execute(new Operation.Builder("add", address)
                .param("jndi-name", JNDI_DEFAULT + name)
                .build());
        return name;
    }

}
