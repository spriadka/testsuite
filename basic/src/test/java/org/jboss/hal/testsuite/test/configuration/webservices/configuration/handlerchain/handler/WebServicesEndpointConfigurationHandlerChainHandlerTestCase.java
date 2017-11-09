package org.jboss.hal.testsuite.test.configuration.webservices.configuration.handlerchain.handler;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.webservices.WebServicesHandlerChainHandlerWizard;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.test.configuration.webservices.WebServicesTestCaseAbstract;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class WebServicesEndpointConfigurationHandlerChainHandlerTestCase extends WebServicesTestCaseAbstract {
    private static final String endpointConfigurationName = "TestEndpointConfig_" + RandomStringUtils.randomAlphanumeric(7);
    private static final Address endpointConfigurationAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);
    private static final String preHandlerChainName = "pre_handler_chain_" + RandomStringUtils.randomAlphanumeric(7);
    private static final Address preHandlerChainAddress = endpointConfigurationAddress.and(PRE_HANDLER_CHAIN, preHandlerChainName);
    private static final String postHandlerChainName = "post_handler_chain_" + RandomStringUtils.randomAlphanumeric(7);
    private static final Address postHandlerChainAddress = endpointConfigurationAddress.and(POST_HANDLER_CHAIN, postHandlerChainName);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        operations.add(endpointConfigurationAddress);
        administration.reloadIfRequired();
        operations.add(preHandlerChainAddress);
        operations.add(postHandlerChainAddress);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(endpointConfigurationAddress);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void testAddPreHandlerChainHandler() throws Exception {
        final String preHandlerChainHandlerName = "pre_handler" + RandomStringUtils.randomAlphanumeric(7);
        final String preHandlerChainHandlerClass = "TestPreHandler";
        final Address preHandlerChainHandlerAddress = preHandlerChainAddress.and(HANDLER, preHandlerChainHandlerName);
        try {
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainHandlerInUI(preHandlerChainName, preHandlerChainHandlerName, preHandlerChainHandlerClass);
            administration.reloadIfRequired();
            new ResourceVerifier(preHandlerChainHandlerAddress, client)
                    .verifyExists()
                    .verifyAttribute("class", preHandlerChainHandlerClass);
        } finally {
            operations.removeIfExists(preHandlerChainHandlerAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPreHandlerChainHandlerWithoutNameOrClassShowsError() throws IOException, OperationException, TimeoutException, InterruptedException {
        final String preHandlerChainHandlerName = "MissingPreHandlerChainHandlerClassHandler";
        final String preHandlerChainHandlerClass = "MissingPreHandlerChainHandlerNameClass";
        try {
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            page.getResourceManager().selectByName(preHandlerChainName);
            WebServicesHandlerChainHandlerWizard wizard = page.getConfig()
                    .switchTo(HANDLER_CLASSES_TAB_LABEL)
                    .getResourceManager()
                    .addResource(WebServicesHandlerChainHandlerWizard.class);
            wizard.saveWithState().assertWindowOpen();
            Assert.assertTrue("Validation error should be visible", page.getWindowFragment().isErrorShownInForm());
            wizard.name(preHandlerChainHandlerName);
            wizard.saveWithState().assertWindowOpen();
            Assert.assertTrue("Missing class validation error should be visibe", page.getWindowFragment().isErrorShownInForm());
            wizard.getEditor().getText("name").clear();
            wizard.className(preHandlerChainHandlerClass);
            wizard.saveWithState().assertWindowOpen();
            Assert.assertTrue("Missing name validation error should be visible", page.getWindowFragment().isErrorShownInForm());
        } finally {
            operations.removeIfExists(preHandlerChainAddress.and(HANDLER, preHandlerChainHandlerName));
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPreHandlerChainHandlerChainDuplicateShowsError() throws Exception {
        final String preHandlerChainHandlerName = "handler_with_duplicate_" + RandomStringUtils.randomAlphanumeric(7);
        final String preHandlerChainHandlerClass = "PreHandlerTestClass";
        final Address preHandlerChainHandlerAddress = preHandlerChainAddress.and(HANDLER, preHandlerChainHandlerName);
        try {
            operations.add(preHandlerChainHandlerAddress, Values.of("class", preHandlerChainHandlerClass)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainHandlerInUI(preHandlerChainName, preHandlerChainHandlerName, preHandlerChainHandlerClass);
            Assert.assertNotNull("An alert regarding adding duplicate Pre Handler Chain Handler should be visible", page.getAlertArea());
        } finally {
            operations.removeIfExists(preHandlerChainHandlerAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testRemovePreHandlerChainHandler() throws Exception {
        final String preHandlerChainHandlerName = "handler_to_be_removed" + RandomStringUtils.randomAlphanumeric(7);
        final String preHandlerChainHandlerClass = "PreHandlerTestClass";
        final Address preHandlerChainHandlerAddress = preHandlerChainAddress.and(HANDLER, preHandlerChainHandlerName);
        try {
            operations.add(preHandlerChainHandlerAddress, Values.of("class", preHandlerChainHandlerClass)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            ResourceManager preHandlerChainHandlersManager = page.getConfig().switchTo(HANDLER_CLASSES_TAB_LABEL).getResourceManager();
            preHandlerChainHandlersManager
                    .removeResource(preHandlerChainHandlerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(preHandlerChainHandlerAddress, client).verifyDoesNotExist();
            Assert.assertFalse(preHandlerChainHandlersManager.isResourcePresent(preHandlerChainHandlerName));
        } finally {
            operations.removeIfExists(preHandlerChainHandlerAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPostHandlerChainHandler() throws Exception {
        final String postHandlerChainHandlerName = "post_handler" + RandomStringUtils.randomAlphanumeric(7);
        final String postHandlerChainHandlerClass = "TestPostHandler";
        final Address postHandlerChainHandlerAddress = postHandlerChainAddress.and(HANDLER, postHandlerChainHandlerName);
        try {
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainHandlerInUI(postHandlerChainName, postHandlerChainHandlerName, postHandlerChainHandlerClass);
            administration.reloadIfRequired();
            new ResourceVerifier(postHandlerChainHandlerAddress, client)
                    .verifyExists()
                    .verifyAttribute("class", postHandlerChainHandlerClass);
        } finally {
            operations.removeIfExists(postHandlerChainHandlerAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPostHandlerChainHandlerWithoutNameOrClassShowsError() throws InterruptedException, TimeoutException, IOException, OperationException {
        final String postHandlerChainHandlerName = "MissingPostHandlerChainHandlerClassHandler";
        final String postHandlerChainHandlerClass = "MissingPostHandlerChainHandlerNameClass";
        try {
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            page.getResourceManager().selectByName(postHandlerChainName);
            WebServicesHandlerChainHandlerWizard wizard = page.getConfig()
                    .switchTo(HANDLER_CLASSES_TAB_LABEL)
                    .getResourceManager()
                    .addResource(WebServicesHandlerChainHandlerWizard.class);
            wizard.saveWithState().assertWindowOpen();
            Assert.assertTrue("Validation error should be visible", page.getWindowFragment().isErrorShownInForm());
            wizard.name(postHandlerChainHandlerName);
            wizard.saveWithState().assertWindowOpen();
            Assert.assertTrue("Missing class validation error should be visibe", page.getWindowFragment().isErrorShownInForm());
            wizard.getEditor().getText("name").clear();
            wizard.className(postHandlerChainHandlerClass);
            wizard.saveWithState().assertWindowOpen();
            Assert.assertTrue("Missing name validation error should be visible", page.getWindowFragment().isErrorShownInForm());
        } finally {
            operations.removeIfExists(postHandlerChainAddress.and(HANDLER, postHandlerChainHandlerName));
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPostHandlerChainHandlerChainDuplicateShowsError() throws Exception {
        final String postHandlerChainHandlerName = "handler_with_duplicate_" + RandomStringUtils.randomAlphanumeric(7);
        final String postHandlerChainHandlerClass = "PostHandlerTestClass";
        final Address preHandlerChainHandlerAddress = postHandlerChainAddress.and(HANDLER, postHandlerChainHandlerName);
        try {
            operations.add(preHandlerChainHandlerAddress, Values.of("class", postHandlerChainHandlerClass)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainHandlerInUI(postHandlerChainName, postHandlerChainHandlerName, postHandlerChainHandlerClass);
            Assert.assertNotNull("An error regarding duplicate Post Handler Chain handler should be visible", page.getAlertArea());
        } finally {
            operations.removeIfExists(preHandlerChainHandlerAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testRemovePostHandlerChainHandler() throws Exception {
        final String postHandlerChainHandlerName = "handler_to_be_removed" + RandomStringUtils.randomAlphanumeric(7);
        final String postHandlerChainHandlerClass = "PostHandlerTestClass";
        final Address postHandlerChainHandlerAddress = postHandlerChainAddress.and(HANDLER, postHandlerChainHandlerName);
        try {
            operations.add(postHandlerChainHandlerAddress, Values.of("class", postHandlerChainHandlerClass)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            ResourceManager postHandlerChainHandlersManager = page.getConfig().switchTo(HANDLER_CLASSES_TAB_LABEL).getResourceManager();
            postHandlerChainHandlersManager
                    .removeResource(postHandlerChainHandlerName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            administration.reloadIfRequired();
            new ResourceVerifier(postHandlerChainHandlerAddress, client).verifyDoesNotExist();
            Assert.assertFalse(postHandlerChainHandlersManager.isResourcePresent(postHandlerChainHandlerName));
        } finally {
            operations.removeIfExists(postHandlerChainHandlerAddress);
            administration.reloadIfRequired();
        }
    }
}
