package org.jboss.hal.testsuite.test.configuration.undertow;

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
import org.jboss.hal.testsuite.fragment.config.undertow.UndertowFragment;
import org.jboss.hal.testsuite.page.config.UndertowPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
public class UndertowTestCaseAbstract {

    protected static Dispatcher dispatcher;
    protected ResourceVerifier verifier;
    protected static StatementContext context;
    protected static AddressTemplate undertowAddressTemplate = AddressTemplate.of("{default.profile}/subsystem=undertow");
    protected static AddressTemplate httpServerTemplate = undertowAddressTemplate.append("/server=*");

    @Drone
    protected WebDriver browser;

    @Page
    protected UndertowPage page;

    @Before
    public void mainBefore() {
        dispatcher = new Dispatcher();
        verifier = new ResourceVerifier(dispatcher);
        context = new DefaultContext();
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.getConfigFragment().editTextAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName) throws IOException, InterruptedException {
        editTextAndVerify(address, identifier, attributeName, RandomStringUtils.randomAlphabetic(6));
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

    public void editTextAreaAndVerify(ResourceAddress address, String identifier, String attributeName, String[] values) throws IOException, InterruptedException {
        page.getConfigFragment().editTextAndSave(identifier, String.join("\n", values));
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, values);
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
        UndertowFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShowedInForm());
        config.cancel();
    }

    protected static String createHTTPServer(Dispatcher dispatcher) {
        String name = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = httpServerTemplate.resolve(context, name);
        dispatcher.execute(new Operation.Builder("add", address).build());
        return name;
    }

    protected static void removeHTTPServer(Dispatcher dispatcher, String httpServer) {
        ResourceAddress address = httpServerTemplate.resolve(context, httpServer);
        dispatcher.execute(new Operation.Builder("remove", address).build());
    }

}
