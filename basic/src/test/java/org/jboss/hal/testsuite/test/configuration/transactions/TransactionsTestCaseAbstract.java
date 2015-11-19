package org.jboss.hal.testsuite.test.configuration.transactions;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 12.10.15.
 */
public abstract class TransactionsTestCaseAbstract {

    protected static AddressTemplate transactionsTemplate = AddressTemplate.of("{default.profile}/subsystem=transactions/");
    protected static StatementContext context = new DefaultContext();
    protected static ResourceAddress address = transactionsTemplate.resolve(context);
    protected static Dispatcher dispatcher = new Dispatcher();
    protected ResourceVerifier verifier = new ResourceVerifier(dispatcher);
    protected static TransactionsOperations operations = new TransactionsOperations(dispatcher);

    protected final String JOURNAL_STORE_ENABLE_ASYNC_IO = "journal-store-enable-async-io";
    protected static final String USE_JDBC_STORE = "use-jdbc-store";
    protected static final String USE_JOURNAL_STORE = "use-journal-store";

    protected final String JOURNAL_STORE_ENABLE_ASYNC_IO_ATTR = "journal-store-enable-async-io";
    protected static final String USE_JDBC_STORE_ATTR = "use-jdbc-store";
    protected static final String USE_JOURNAL_STORE_ATTR = "use-journal-store";

    @Drone
    public WebDriver browser;

    @Page
    public TransactionsPage page;

    //helper methods
    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.getConfigFragment().editTextAndSave(identifier, value);
        TransactionsOperations.reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName) throws IOException, InterruptedException {
        editTextAndVerify(address, identifier, attributeName, "transactions" + RandomStringUtils.randomAlphabetic(6));
    }

    protected void editCheckboxAndVerify(ResourceAddress address, String identifier, String attributeName, Boolean value) throws IOException, InterruptedException {
        page.getConfigFragment().editCheckboxAndSave(identifier, value);
        TransactionsOperations.reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value.toString());
    }

    protected void selectOptionAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.getConfigFragment().selectOptionAndSave(identifier, value);
        TransactionsOperations.reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShownInForm());
        config.cancel();
    }
}
