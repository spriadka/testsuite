package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.config.remoting.RemotingPropertyWizard;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
public class RemotingConnectorPropertiesTestCase extends RemotingConnectorTestCaseAbstract {

    private static final String PROPERTIES_LABEL = "Properties";
    private static final String PROPERTY = "property";
    private static final String VALUE = "value";

    @BeforeClass
    public static void setUp() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        createSocketBinding(CONNECTOR_SOCKET_BINDING);
        operations.add(CONNECTOR_ADDRESS, Values.of(SOCKET_BINDING, CONNECTOR_SOCKET_BINDING));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws CommandFailedException, IOException, OperationException, TimeoutException, InterruptedException {
        try {
            operations.removeIfExists(CONNECTOR_ADDRESS);
            client.apply(new RemoveSocketBinding(CONNECTOR_SOCKET_BINDING));
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void addProperty() throws Exception {
        final String propertyName = "prop_" + RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = RandomStringUtils.randomAlphanumeric(5);
        final Address propertyAddress = CONNECTOR_ADDRESS.and(PROPERTY, propertyName);
        try {
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo(PROPERTIES_LABEL);
            page.getConfig().getResourceManager().addResource(RemotingPropertyWizard.class)
                    .name(propertyName)
                    .value(propertyValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly added property should be present in the properties table"
                    , page.getConfig().getResourceManager().isResourcePresent(propertyName));
            new ResourceVerifier(propertyAddress, client)
                    .verifyExists()
                    .verifyAttribute(VALUE, propertyValue);
        } finally {
            operations.removeIfExists(propertyAddress);
            administration.reloadIfRequired();
        }
    }

    @Test
    public void removeProperty() throws Exception {
        final String propertyName = "prop_" + RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = RandomStringUtils.randomAlphanumeric(5);
        final Address propertyAddress = CONNECTOR_ADDRESS.and(PROPERTY, propertyName);
        try {
            operations.add(propertyAddress, Values.of(VALUE, propertyValue));
            administration.reloadIfRequired();
            navigateToRemotingConnectorTab();
            page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
            page.getConfig().switchTo(PROPERTIES_LABEL);
            page.getConfig().getResourceManager().removeResource(propertyName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed property should not be present in the properties table"
                    , page.getConfig().getResourceManager().isResourcePresent(propertyName));
            new ResourceVerifier(propertyAddress, client)
                    .verifyDoesNotExist();
        } finally {
            operations.removeIfExists(propertyAddress);
            administration.reloadIfRequired();
        }
    }
}
