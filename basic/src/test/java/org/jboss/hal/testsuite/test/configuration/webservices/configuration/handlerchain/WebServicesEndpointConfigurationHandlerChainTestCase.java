package org.jboss.hal.testsuite.test.configuration.webservices.configuration.handlerchain;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.webservices.WebServicesHandlerChainWizard;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.test.configuration.webservices.WebServicesTestCaseAbstract;
import org.jboss.hal.testsuite.util.ConfigChecker;
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
public class WebServicesEndpointConfigurationHandlerChainTestCase extends WebServicesTestCaseAbstract {
    private static final String endpointConfigurationName = "my_endpoint_config_with_handler_chains_" + RandomStringUtils.randomAlphanumeric(7);
    private static final Address endpointConfigurationAddress = WEBSERVICES_ADDRESS.and(ENDPOINT_CONFIGURATION, endpointConfigurationName);

    @BeforeClass
    public static void setUp() throws InterruptedException, TimeoutException, IOException {
        operations.add(endpointConfigurationAddress);
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
    public void testAddPreHandlerChain() throws Exception {
        final String preHandlerChainName = "pre_handler_" + RandomStringUtils.randomAlphanumeric(7);
        final String preHandlerChainProtocolBindings = "" +
                "###" + RandomStringUtils.randomAlphanumeric(7) +
                "_protocol_" + RandomStringUtils.randomAlphanumeric(7)
                + "###";
        final Address preHandlerChainAddress = endpointConfigurationAddress.and(PRE_HANDLER_CHAIN, preHandlerChainName);
        try {
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainInUI(preHandlerChainName, preHandlerChainProtocolBindings);
            new ResourceVerifier(preHandlerChainAddress, client)
                    .verifyExists()
                    .verifyAttribute("protocol-bindings", preHandlerChainProtocolBindings);
        } finally {
            operations.removeIfExists(preHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPreHandlerChainDuplicateShowsError() throws Exception {
        final String preHandlerChainName = "pre_handler" + RandomStringUtils.randomAlphanumeric(7);
        final Address preHandlerChainAddress = endpointConfigurationAddress.and(PRE_HANDLER_CHAIN, preHandlerChainName);
        try {
            operations.add(preHandlerChainAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainInUI(preHandlerChainName, "protocol_bindings");
            Assert.assertNotNull("Error regarding duplicate pre handler chain should be visible", page.getAlertArea());
        } finally {
            operations.removeIfExists(preHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPreHandlerChainWithoutNameShowsError() throws Exception {
        page.navigate();
        page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
        page.getResourceManager()
                .addResource(WebServicesHandlerChainWizard.class)
                .saveWithState().assertWindowOpen();
        Assert.assertTrue("Validation error regarding missing pre handler name should be visible", page.getWindowFragment().isErrorShownInForm());
    }

    @Test
    public void testEditPreHandlerChainProtocolBindings() throws Exception {
        final String preHandlerChainName = "pre_handler" + RandomStringUtils.randomAlphanumeric(7);
        final Address preHandlerChainAddress = endpointConfigurationAddress.and(PRE_HANDLER_CHAIN, preHandlerChainName);
        final String preHandlerProtocolBindings = "test_protocol_bindings_" + RandomStringUtils.randomAlphanumeric(7);
        final String protocolBindingsValue = "new_protocol_bindings_value_" + RandomStringUtils.randomAlphanumeric(7);
        try {
            operations.add(preHandlerChainAddress, Values.of(PROTOCOL_BINDINGS, preHandlerProtocolBindings)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            page.getResourceManager().selectByName(preHandlerChainName);
            new ConfigChecker.Builder(client, preHandlerChainAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PROTOCOL_BINDINGS, protocolBindingsValue)
                    .verifyFormSaved()
                    .verifyAttribute(PROTOCOL_BINDINGS, protocolBindingsValue);
        } finally {
            operations.removeIfExists(preHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testRemovePreHandlerChain() throws Exception {
        final String preHandlerChainName = "pre_handler" + RandomStringUtils.randomAlphanumeric(7);
        final Address preHandlerChainAddress = endpointConfigurationAddress.and(PRE_HANDLER_CHAIN, preHandlerChainName);
        final String preHandlerChainProtocolBindings = "test_protocol_bindings_" + RandomStringUtils.randomAlphanumeric(7);
        try {
            operations.add(preHandlerChainAddress, Values.of(PROTOCOL_BINDINGS, preHandlerChainProtocolBindings)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, PRE_HANDLER_CHAIN_TAB_LABEL);
            ResourceManager preHandlerChainsManager = page.getResourceManager();
            preHandlerChainsManager.removeResource(preHandlerChainName).confirmAndDismissReloadRequiredMessage();
            administration.reloadIfRequired();
            new ResourceVerifier(preHandlerChainAddress, client).verifyDoesNotExist();
            Assert.assertFalse(preHandlerChainsManager.isResourcePresent(preHandlerChainName));

        } finally {
            operations.removeIfExists(preHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPostHandlerChain() throws Exception {
        final String postHandlerChainName = "post_handler_" + RandomStringUtils.randomAlphanumeric(7);
        final String postHandlerChainProtocolBindings = "" +
                "###" + RandomStringUtils.randomAlphanumeric(7) +
                "_protocol_" + RandomStringUtils.randomAlphanumeric(7)
                + "###";
        final Address postHandlerChainAddress = endpointConfigurationAddress.and(POST_HANDLER_CHAIN, postHandlerChainName);
        try {
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainInUI(postHandlerChainName, postHandlerChainProtocolBindings);
            new ResourceVerifier(postHandlerChainAddress, client)
                    .verifyExists()
                    .verifyAttribute(PROTOCOL_BINDINGS, postHandlerChainProtocolBindings);
        } finally {
            operations.removeIfExists(postHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPostHandlerChainDuplicateShowsError() throws Exception {
        final String postHandlerChainName = "post_handler" + RandomStringUtils.randomAlphanumeric(7);
        final String postHandlerChainProtocolBindings = "" +
                "###" + RandomStringUtils.randomAlphanumeric(7) +
                "_protocol_" + RandomStringUtils.randomAlphanumeric(7)
                + "###";
        final Address postHandlerChainAddress = endpointConfigurationAddress.and(POST_HANDLER_CHAIN, postHandlerChainName);
        try {
            operations.add(postHandlerChainAddress).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            page.createHandlerChainInUI(postHandlerChainName, postHandlerChainProtocolBindings);
            Assert.assertNotNull("Error regarding duplicate pre handler chain should be visible", page.getAlertArea());
        } finally {
            operations.removeIfExists(postHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testAddPostHandlerChainWithoutNameShowsError() throws Exception {
        page.navigate();
        page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
        page.getResourceManager()
                .addResource(WebServicesHandlerChainWizard.class)
                .saveWithState().assertWindowOpen();
        Assert.assertTrue("Validation error regarding missing pre handler name should be visible", page.getWindowFragment().isErrorShownInForm());
    }

    @Test
    public void testEditPostHandlerChainProtocolBindings() throws Exception {
        final String postHandlerChainName = "pre_handler" + RandomStringUtils.randomAlphanumeric(7);
        final Address postHandlerChainAddress = endpointConfigurationAddress.and(POST_HANDLER_CHAIN, postHandlerChainName);
        final String postHandlerChainProtocolBindings = "test_protocol_bindings_" + RandomStringUtils.randomAlphanumeric(7);
        final String protocolBindingsValue = "new_protocol_bindings_value_" + RandomStringUtils.randomAlphanumeric(7);
        try {
            operations.add(postHandlerChainAddress, Values.of(PROTOCOL_BINDINGS, postHandlerChainProtocolBindings)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            page.getResourceManager().selectByName(postHandlerChainName);
            new ConfigChecker.Builder(client, postHandlerChainAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, PROTOCOL_BINDINGS, protocolBindingsValue)
                    .verifyFormSaved()
                    .verifyAttribute(PROTOCOL_BINDINGS, protocolBindingsValue);
        } finally {
            operations.removeIfExists(postHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void testRemovePostHandlerChain() throws Exception {
        final String postHandlerChainName = "pre_handler" + RandomStringUtils.randomAlphanumeric(7);
        final Address postHandlerChainAddress = endpointConfigurationAddress.and(POST_HANDLER_CHAIN, postHandlerChainName);
        final String postHandlerChainProtocolBindings = "test_protocol_bindings_" + RandomStringUtils.randomAlphanumeric(7);
        try {
            operations.add(postHandlerChainAddress, Values.of(PROTOCOL_BINDINGS, postHandlerChainProtocolBindings)).assertSuccess();
            administration.reloadIfRequired();
            page.navigate();
            page.switchToEndpointConfigurationHandlerChainTab(endpointConfigurationName, POST_HANDLER_CHAIN_TAB_LABEL);
            ResourceManager postHandlerChainsManager = page.getResourceManager();
            postHandlerChainsManager.removeResource(postHandlerChainName).confirmAndDismissReloadRequiredMessage();
            administration.reloadIfRequired();
            new ResourceVerifier(postHandlerChainAddress, client).verifyDoesNotExist();
            Assert.assertFalse(postHandlerChainsManager.isResourcePresent(postHandlerChainName));

        } finally {
            operations.removeIfExists(postHandlerChainAddress);
            administration.reloadIfRequired();
        }
    }


}
