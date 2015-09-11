package org.jboss.hal.testsuite.test.configuration.ee;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
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
import java.util.concurrent.TimeUnit;

/**
 * @author Jan Kasik
 *         Created on 8.9.15.
 */
public class EETestCaseAbstract {

    @Drone
    protected WebDriver browser;

    @Page
    protected ConfigurationPage page;

    protected Dispatcher dispatcher;
    protected ResourceVerifier verifier;

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

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        ConfigFragment config = page.getConfigFragment();
        config.edit().text(identifier, value);
        config.save();
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName) throws IOException, InterruptedException {
        editTextAndVerify(address, identifier, attributeName, RandomStringUtils.randomAlphabetic(6));
    }

    protected void editCheckboxAndVerify(ResourceAddress address, String identifier, String attributeName, Boolean value) throws IOException, InterruptedException {
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
        final int timeout = 60000;
        if (ConfigUtils.isDomain()) {
            new DomainManager(CliClientFactory.getClient()).reloadIfRequiredAndWaitUntilRunning(timeout);
        } else {
            CliClientFactory.getClient().reload();
        }
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
