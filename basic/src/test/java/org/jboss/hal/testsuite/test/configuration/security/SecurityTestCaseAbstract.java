package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.DomainManager;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.SecurityPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 16.10.15.
 */
public abstract class SecurityTestCaseAbstract {

    @Page
    public SecurityPage page;

    @Drone
    public WebDriver browser;

    protected final String CODE = "code";
    protected final String FLAG = "flag";
    protected final String MODULE = "module";
    protected final String MODULE_OPTIONS = "module-options";

    protected final String CODE_ATTR = "code";
    protected final String FLAG_ATTR = "flag";
    protected final String MODULE_ATTR = "module";
    protected final String MODULE_OPTIONS_ATTR = "module-options";

    protected final String FLAG_VALUE = "optional";
    protected final String MODULE_OPTIONS_VALUE = "example=value,test=example";

    protected static Dispatcher dispatcher = new Dispatcher();
    protected static StatementContext context = new DefaultContext();
    protected ResourceVerifier verifier = new ResourceVerifier(dispatcher);

    protected static final AddressTemplate SECURITY_DOMAIN_TEMPLATE = AddressTemplate.of("{default.profile}/subsystem=security/security-domain=*/");

    @Before
    public void before() {
        page.navigate();
    }

    @AfterClass
    public static void tearDown() {
        dispatcher.close();
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.editTextAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName) throws IOException, InterruptedException {
        editTextAndVerify(address, identifier, attributeName, RandomStringUtils.randomAlphabetic(6));
    }

    protected void editCheckboxAndVerify(ResourceAddress address, String identifier, String attributeName, Boolean value) throws IOException, InterruptedException {
        page.editCheckboxAndSave(identifier, value);
        reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value.toString());
    }

    public void selectOptionAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.selectOptionAndSave(identifier, value);
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
        ConfigFragment config = page.getConfigFragment();
        page.editTextAndSave(identifier, value);
        Assert.assertTrue(page.isErrorShownInForm());
        config.cancel();
    }

}
