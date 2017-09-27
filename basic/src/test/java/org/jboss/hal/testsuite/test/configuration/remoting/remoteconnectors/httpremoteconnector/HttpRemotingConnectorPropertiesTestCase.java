package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors.httpremoteconnector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.remoting.RemotingPropertyWizard;
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
public class HttpRemotingConnectorPropertiesTestCase extends HttpRemotingConnectorTestCaseAbstract {

    private static final String PROPERTY = "property";
    private static final String VALUE = "value";
    private static final String PROPERTIES_TAB_LABEL = "Properties";

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        createHttpRemotingConnector(HTTP_CONNECTOR_ADDRESS);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, OperationException {
        try {
            operations.removeIfExists(HTTP_CONNECTOR_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Test
    public void addProperty() throws Exception {
        final String propertyName = "my_prop_" + RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = RandomStringUtils.randomAlphanumeric(7);
        final Address propertyAddress = HTTP_CONNECTOR_ADDRESS.and(PROPERTY, propertyName);
        try {
            navigateToRemotingHttpConnectorsTab();
            remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
            remotingSubsystemPage.getConfig().switchTo(PROPERTIES_TAB_LABEL);
            remotingSubsystemPage.getConfig().getResourceManager()
                    .addResource(RemotingPropertyWizard.class)
                    .name(propertyName)
                    .value(propertyValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly added property should be present in the properties table"
                    , remotingSubsystemPage.getConfig().getResourceManager().isResourcePresent(propertyName));
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
        final String propertyName = "property_to_be_removed_" + RandomStringUtils.randomAlphanumeric(7);
        final String propertyValue = RandomStringUtils.randomAlphanumeric(7);
        final Address propertyAddress = HTTP_CONNECTOR_ADDRESS.and(PROPERTY, propertyName);
        try {
            operations.add(propertyAddress, Values.of(VALUE, propertyValue));
            administration.reloadIfRequired();
            navigateToRemotingHttpConnectorsTab();
            remotingSubsystemPage.getResourceManager().selectByName(HTTP_CONNECTOR_ADDRESS.getLastPairValue());
            remotingSubsystemPage.getConfig().switchTo(PROPERTIES_TAB_LABEL);
            remotingSubsystemPage
                    .getConfig()
                    .getResourceManager()
                    .removeResource(propertyName)
                    .confirmAndDismissReloadRequiredMessage();
            Assert.assertFalse("Property should not be present in the properties table after removal",
                    remotingSubsystemPage.getConfig().getResourceManager().isResourcePresent(propertyName));
            new ResourceVerifier(propertyAddress, client)
                    .verifyDoesNotExist();
        } finally {
            operations.removeIfExists(propertyAddress);
            administration.reloadIfRequired();
        }
    }

}
