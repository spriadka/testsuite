package org.jboss.hal.testsuite.test.runtime.deployments;

import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class DeploymentsOperations {

    private static final String COMMAND_UNDEPLOY = "undeploy %s";

    private OnlineManagementClient client;
    private Operations operations;

    public DeploymentsOperations(OnlineManagementClient client) {
        this.client = client;
        this.operations = new Operations(client);
    }

    public void verifyDeploymentExists(String deploymentName) throws Exception {
        String message = String.format("Deployment '%s' should exist!", deploymentName);
        new ResourceVerifier(Address.of("deployment", deploymentName), client, 10000).verifyExists(message);
    }

    public void verifyDeploymentDoesNotExist(String deploymentName) throws Exception {
        String message = String.format("Deployment '%s' should not exist!", deploymentName);
        new ResourceVerifier(Address.of("deployment", deploymentName), client, 10000).verifyDoesNotExist(message);
    }

    public void undeploy(String deploymentName) throws IOException {
        operations.invoke(String.format(COMMAND_UNDEPLOY, deploymentName), Address.root());
    }

    private void verifyDeploymentEnabled(String deploymentName, boolean isEnabled) throws Exception {
        String status = isEnabled ? "enabled" : "disabled";
        Address address = Address.of("deployment", deploymentName);
        new ResourceVerifier(address, client, 20000).verifyAttribute("enabled", isEnabled,
                "Deployment should be " + status);
    }

    public void verifyIsDeploymentEnabled(String deploymentName) throws Exception {
        verifyDeploymentEnabled(deploymentName, true);
    }

    public void verifyIsDeploymentDisabled(String deploymentName) throws Exception {
        verifyDeploymentEnabled(deploymentName, false);
    }

    public void verifyIsDeploymentAssignedToServerGroup(String serverGroup, String deploymentName) throws Exception {
        Address address = Address.of("server-group", serverGroup).and("deployment", deploymentName);
        new ResourceVerifier(address, client, 20000).verifyExists("Deployment should be assigned to server group.");
    }

    private void verifyDeployementEnabledInServerGroup(String serverGroup, String deploymentName, boolean isEnabled)
            throws Exception {
        String status = isEnabled ? "enabled" : "disabled";
        Address address = Address.of("server-group", serverGroup).and("deployment", deploymentName);
        new ResourceVerifier(address, client, 20000).verifyAttribute("enabled", isEnabled,
                "Deployment should be " + status);
    }

    public void verifyIsDeploymentEnabledInServerGroup(String serverGroup, String deploymentName) throws Exception {
        verifyDeployementEnabledInServerGroup(serverGroup, deploymentName, true);
    }

    public void verifyIsDeploymentDisabledInServerGroup(String serverGroup, String deploymentName) throws Exception {
        verifyDeployementEnabledInServerGroup(serverGroup, deploymentName, false);
    }
}
