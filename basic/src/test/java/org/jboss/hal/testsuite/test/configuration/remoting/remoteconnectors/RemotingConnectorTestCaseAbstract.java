package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.page.config.RemotingSubsystemPage;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public abstract class RemotingConnectorTestCaseAbstract {

    @Drone
    protected WebDriver browser;

    @Page
    protected RemotingSubsystemPage page;

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Administration administration = new Administration(client);
    protected static final Operations operations = new Operations(client);

    protected static final Address REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting");
    protected static final Address CONNECTOR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("connector", "connector" + RandomStringUtils.randomAlphanumeric(7));

    protected static final String SOCKET_BINDING = "socket-binding";
    protected static final String CONNECTOR_SOCKET_BINDING = "connector_socket_binding_" + RandomStringUtils.randomAlphanumeric(7);

    protected void navigateToRemotingConnectorTab() {
        page.navigate();
        page.switchToRemoteConnectorsTab();
    }

    protected static void createSocketBinding(String name) throws CommandFailedException {
        client.apply(new AddSocketBinding.Builder(name)
                .port(AvailablePortFinder.getNextAvailableNonPrivilegedPort())
                .build());
    }

}
