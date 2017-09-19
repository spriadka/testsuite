package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors.httpremoteconnector;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.RemotingSubsystemPage;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;

public abstract class HttpRemotingConnectorTestCaseAbstract {

    @Page
    protected RemotingSubsystemPage remotingSubsystemPage;

    @Drone
    protected WebDriver browser;

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Administration administration = new Administration(client);
    protected static final Operations operations = new Operations(client);

    protected static final Address REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting");
    protected static final Address HTTP_CONNECTOR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS
            .and("http-connector", "http-connector_" + RandomStringUtils.randomAlphanumeric(7));

    protected static final String CONNECTOR_REF = "connector-ref";

    protected void navigateToRemotingHttpConnectorsTab() {
        remotingSubsystemPage.navigate();
        remotingSubsystemPage.switchToRemoteConnectorsTab();
        remotingSubsystemPage.switchSubTab("HTTP Connectors");
    }

    protected static void createHttpRemotingConnector(Address connectorAddress) throws IOException {
        operations.add(connectorAddress, Values.of(CONNECTOR_REF, RandomStringUtils.randomAlphanumeric(7))).assertSuccess();
    }

}
