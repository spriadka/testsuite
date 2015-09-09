package org.jboss.hal.testsuite.test.configuration.ee;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.cli.Library;
import org.jboss.hal.testsuite.cli.TimeoutException;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.ConfigurationPage;
import org.jboss.hal.testsuite.page.config.DomainConfigEntryPoint;
import org.jboss.hal.testsuite.page.config.StandaloneConfigEntryPoint;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Jan Kasik
 *         Created on 8.9.15.
 */
public class EETestCaseAbstract {

    @Drone
    protected static WebDriver browser;

    @Page
    protected ConfigurationPage page;

    protected static Dispatcher dispatcher;
    protected static ResourceVerifier verifier;

    private StatementContext context = new DefaultContext();
    private AddressTemplate serverTemplate = AddressTemplate.of("/host=master/server=*");
    protected AddressTemplate eeAddressTemplate = AddressTemplate.of("{default.profile}/subsystem=ee");
    protected ResourceAddress eeAddress;

    @Before
    public void mainBefore() {
        dispatcher = new Dispatcher();
        verifier = new ResourceVerifier(dispatcher);
        eeAddress = eeAddressTemplate.resolve(context);
    }

    @After
    public void mainAfter() {
        reloadIfRequiredAndWaitForRunning();
        dispatcher.close();
    }

    protected void navigateToEEServices() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainConfigEntryPoint.class);
            navigation.addAddress(FinderNames.CONFIGURATION, FinderNames.PROFILES)
                    .addAddress(FinderNames.PROFILE, "full")
                    .addAddress(FinderNames.SUBSYSTEM, "EE");
        } else {
            navigation = new FinderNavigation(browser, StandaloneConfigEntryPoint.class);
            navigation.addAddress(FinderNames.CONFIGURATION, FinderNames.SUBSYSTEMS)
                    .addAddress(FinderNames.SUBSYSTEM, "EE");
        }
        navigation.selectRow().invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
        //TODO: remove this after HAL-836 is resolved
        By selector = ByJQuery.selector(".link-bar-first");
        browser.findElement(selector).click();
        Console.withBrowser(browser).waitUntilLoaded();
        page.view("EE");
        Console.withBrowser(browser).waitUntilLoaded();
        //END OF REMOVE
        page.switchTab("Services");
    }

    public void editTextAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        ConfigFragment config = page.getConfigFragment();
        config.edit().text(identifier, value);
        config.save();
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    public void editTextAndVerify(ResourceAddress address, String identifier, String attributeName) throws IOException, InterruptedException {
        editTextAndVerify(address, identifier, attributeName, RandomStringUtils.randomAlphabetic(6));
    }

    public void editCheckboxAndVerify(ResourceAddress address, String identifier, String attributeName, Boolean value) throws IOException, InterruptedException {
        ConfigFragment config = page.getConfigFragment();
        config.edit().checkbox(identifier, value);
        config.save();
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value.toString());
    }

    public void selectOptionAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        ConfigFragment config = page.getConfigFragment();
        config.edit().select(identifier, value);
        config.save();
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void reloadIfRequiredAndWaitForRunning() {
        if (isReloadRequired()) {
            reloadAndWaitForRunning();
        }
    }

    protected void reloadAndWaitForRunning() {
        List<String> all = listAllServerNames();
        List<String> servers = listAllRunningServers(all);
        reloadServers();
        waitUntilRunning(servers);
    }

    private void waitUntilRunning(List<String> servers) throws TimeoutException {
        int timeout = 60000;
        final int step = 500;
        while (!isServersRunning(servers)) {
            if (timeout <= 0) {
                throw new TimeoutException("Waiting for running server timed out!");
            }
            timeout -= step;
            Library.letsSleep(step);
        }
    }

    /**
     * Returns true if all given servers are running
     *
     * @param servers
     * @return true if all servers are running
     */
    private Boolean isServersRunning(List<String> servers) {
        if (ConfigUtils.isDomain()) {
            for (String server : servers) {
                Library.letsSleep(100); //little break between requests
                if (!isServerRunning(server)) {
                    return false;
                }
            }
            return true;
        } else {
            return dispatcher.execute(new Operation.Builder(READ_ATTRIBUTE_OPERATION, ResourceAddress.ROOT)
                    .param(NAME, "server-state")
                    .build()).payload().asString().contains("running");
        }
    }

    /**
     * Only use in domain mode
     * @param server name of server
     * @return True if given server is running
     */
    private Boolean isServerRunning(String server) {
        final String result = dispatcher.execute(new Operation
                .Builder(READ_ATTRIBUTE_OPERATION, serverTemplate.resolve(context, server))
                .param("name", "server-state")
                .build())
                .payload().asString();
        return result.contains("running");
    }

    /**
     * Only use in domain mode
     * @param servers list of servers from which will be filtered non-running servers
     * @return List of all running servers
     */
    private List<String> listAllRunningServers(List<String> servers) {
        List<String> running = new LinkedList<>();
        for (String server : servers) {
            Library.letsSleep(100);
            if (isServerRunning(server)) {
                running.add(server);
            }
        }
        return running;
    }

    /**
     * Only use in domain mode
     * @return List of all servers present on host=master
     */
    private List<String> listAllServerNames() {

        if (ConfigUtils.isDomain()) {
            final ResourceAddress address = new ResourceAddress().add("host", "master");
            String response = dispatcher.execute(new Operation.Builder("read-children-names", address)
                    .param("child-type", "server")
                    .build()).payload().asString();
            return Arrays.asList(response.replaceAll("[\\[\\]\"]", "").split(","));
        } else {
            return Collections.emptyList();
        }
    }

    private Boolean isReloadRequired() {
        if (ConfigUtils.isDomain()) {
            final List<String> servers = listAllServerNames();
            for (String server : servers) {
                Library.letsSleep(100); //little break between requests
                if (isReloadRequiredForServerOnDomain(server)) {
                    return true;
                }
            }
            return false;
        } else {
            return dispatcher.execute(new Operation.Builder(READ_ATTRIBUTE_OPERATION, ResourceAddress.ROOT)
                    .param(NAME, "server-state")
                    .build()).payload().asString().contains("restart-required");
        }
    }

    private Boolean isReloadRequiredForServerOnDomain(String server) {
        String result = dispatcher.execute(new Operation
                .Builder(READ_ATTRIBUTE_OPERATION, serverTemplate.resolve(context, server))
                .param(NAME, "server-state")
                .build()).payload().asString();
        return result.contains("restart-required");
    }

    private void reloadServers() {
        final String operationName;
        if (ConfigUtils.isDomain()) {
            operationName = RELOAD_SERVERS;
        } else {
            operationName = "reload";
        }
        dispatcher.execute(new Operation.Builder(operationName, ResourceAddress.ROOT).build());
    }

    protected Boolean isErrorShowedInForm() {
        By selector = ByJQuery.selector("div.form-item-error-desc:visible");
        return isElementVisible(selector);
    }

    protected Boolean isElementVisible(By selector) {
        try {
            Graphene.waitModel().withTimeout(5, TimeUnit.SECONDS).until().element(selector).is().visible();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        config.edit().text(identifier, value);
        config.save();
        Assert.assertTrue(isErrorShowedInForm());
        config.cancel();
    }
}
