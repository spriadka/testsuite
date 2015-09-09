package org.jboss.hal.testsuite.test.configuration.ee;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author Jan Kasik
 *         Created on 8.9.15.
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class ContextServicesTestCase extends EETestCaseAbstract {

    //identifiers
    private static final String JNDI_NAME = "jndi-name";
    private static final String USE_TRANSACTIONAL = "use-transaction-setup-provider";

    //attribute-names
    private static final String JNDI_NAME_ATTR = "jndi-name";
    private static final String USE_TRANSACTIONAL_ATTR = "use-transaction-setup-provider";

    //values
    private static final String JNDI_INVALID = "test";
    private static final String JNDI_DEFAULT = "java:/";
    private static final String JNDI_VALID = JNDI_DEFAULT + RandomStringUtils.randomAlphanumeric(6);

    private static ResourceAddress address;
    private static String contextService;

    @Before
    public void before() {
        contextService = createContextService();
        reloadAndWaitForRunning();
        address = new ResourceAddress(eeAddress).add("context-service", contextService);
        navigateToEEServices();
        page.switchSubTab("Context Service");
        page.getResourceManager().getResourceTable().selectRowByText(0, contextService);
    }

    @After
    public void after() {
        removeContextService(contextService);
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

    private String createContextService() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = new ResourceAddress(eeAddress).add("context-service", name);
        dispatcher.execute(new Operation.Builder("add", address)
                .param("jndi-name", JNDI_DEFAULT + name)
                .build());
        return name;
    }

    private void removeContextService(String name) {
        ResourceAddress address =  new ResourceAddress(eeAddress).add("context-service", name);
        dispatcher.execute(new Operation.Builder("remove", address).build());
    }

}
