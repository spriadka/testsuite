package org.jboss.hal.testsuite.test.configuration.ee;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.fragment.config.ee.EEConfigFragment;
import org.jboss.hal.testsuite.page.config.EEServicesPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

/**
 * @author Jan Kasik
 *         Created on 8.9.15.
 */
public class EETestCaseAbstract {

    @Drone
    protected WebDriver browser;

    @Page
    protected EEServicesPage page;

    protected Dispatcher dispatcher;
    protected ResourceVerifier verifier;
    protected ResourceAddress eeAddress;
    private StatementContext context;

    @Before
    public void mainBefore() {
        context  = new DefaultContext();
        dispatcher = new Dispatcher();
        verifier = new ResourceVerifier(dispatcher);
        eeAddress =  AddressTemplate.of("{default.profile}/subsystem=ee").resolve(context);
    }

    @After
    public void mainAfter() {
        reloadIfRequiredAndWaitForRunning();
        dispatcher.close();
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.getConfigFragment().editTextAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName) throws IOException, InterruptedException {
        editTextAndVerify(address, identifier, attributeName, "ee_" + attributeName + RandomStringUtils.randomAlphabetic(4));
    }

    protected void editCheckboxAndVerify(ResourceAddress address, String identifier, String attributeName, Boolean value) throws IOException, InterruptedException {
        page.getConfigFragment().editCheckboxAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value.toString());
    }

    public void selectOptionAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.getConfigFragment().selectOptionAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void reloadIfRequiredAndWaitForRunning() {
        final int timeout = 60000;
        if (ConfigUtils.isDomain()) {
            new DomainManager(CliClientFactory.getClient()).reloadIfRequiredAndWaitUntilRunning(timeout);
        } else {
            CliClientFactory.getClient().reload();
        }
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        EEConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShownInForm());
        config.cancel();
    }

    protected boolean removeEEChild(String childType, String name) {
        ResourceAddress address = new ResourceAddress(eeAddress).add(childType, name);
        return dispatcher.execute(new Operation.Builder("remove", address).build()).isSuccessful();
    }
}
