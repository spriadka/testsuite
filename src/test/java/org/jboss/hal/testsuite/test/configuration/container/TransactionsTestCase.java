package org.jboss.hal.testsuite.test.configuration.container;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class TransactionsTestCase {

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(CliConstants.TRANSACTIONS_SUBSYSTEM_ADDRESS, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public TransactionsPage page;

    @Before
    public void before() {
        Console.withBrowser(browser).refreshAndNavigate(TransactionsPage.class);
    }

    @After
    public void after(){
        client.reload(false);
    }

    @Test
    public void toggleStatistics() {
        checker.editCheckboxAndAssert(page, "enableStatistics", true).invoke();
        checker.editCheckboxAndAssert(page, "enableStatistics", false).invoke();
    }

    @Test
    public void toggleJTS() {
        checker.editCheckboxAndAssert(page, "jts", true).invoke();
        checker.editCheckboxAndAssert(page, "jts", false).invoke();
    }

    @Test
    public void toggleHornetQStore() {
        checker.editCheckboxAndAssert(page, "hornetqStore", true).dmrAttribute("use-hornetq-store").invoke();
        checker.editCheckboxAndAssert(page, "hornetqStore", false).dmrAttribute("use-hornetq-store").invoke();
    }

    @Test
    public void toggleTSMStatus() {
        checker.editCheckboxAndAssert(page, "enableTsmStatus", true).invoke();
        checker.editCheckboxAndAssert(page, "enableTsmStatus", false).invoke();
    }

    @Test
    public void editNodeIdentifier() {
        checker.editTextAndAssert(page, "nodeIdentifier", "500").invoke();
    }

    @Test
    public void editDefaultTimeout() {
        checker.editTextAndAssert(page, "defaultTimeout", "500").invoke();
        checker.editTextAndAssert(page, "defaultTimeout", "-500").expectError().invoke();
        checker.editTextAndAssert(page, "defaultTimeout", "asdf").expectError().invoke();
    }

    @Ignore("\"failure-description\" => \"WFLYCTL0105: process-id-uuid is invalid in combination with process-id-socket-binding\"")
    @Test
    public void editProcessIdSocket() {
    }

    @Ignore("\"failure-description\" => \"WFLYCTL0105: process-id-uuid is invalid in combination with process-id-socket-binding\"")
    @Test
    public void editMaxPorts() {
    }

    @Test
    public void editSocketBinding() {
        checker.editTextAndAssert(page, "socketBinding", RandomStringUtils.randomAlphanumeric(5)).tab("Recovery").invoke();
    }

    @Test
    public void editStatusSocketBinding() {
        checker.editTextAndAssert(page, "statusSocketBinding", RandomStringUtils.randomAlphanumeric(5)).tab("Recovery").invoke();
    }

    @Test
    public void toggleRecoveryListener() {
        checker.editCheckboxAndAssert(page, "recoveryListener", true).tab("Recovery").invoke();
        checker.editCheckboxAndAssert(page, "recoveryListener", false).tab("Recovery").invoke();
    }

    @Test
    public void editPath() {
        checker.editTextAndAssert(page, "path", RandomStringUtils.randomAlphanumeric(5)).tab("Path").invoke();
    }

    @Test
    public void editRelativeTo() {
        checker.editTextAndAssert(page, "relativeTo", RandomStringUtils.randomAlphanumeric(5)).tab("Path").invoke();
    }

    @Test
    public void editObjectStorePath() {
        checker.editTextAndAssert(page, "objectStorePath", RandomStringUtils.randomAlphanumeric(5)).tab("Path").invoke();
    }

    @Test
    public void editObjectStoreRelativeTo() {
        checker.editTextAndAssert(page, "objectStoreRelativeTo", RandomStringUtils.randomAlphanumeric(5)).tab("Path").invoke();
    }

}
