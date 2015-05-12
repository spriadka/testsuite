package org.jboss.hal.testsuite.test.runtime.deployments;

import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliConstants;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class DeploymentsOperations {

    private static final String COMMAND_READ_RESOURCE = CliConstants.DEPLOYMENT_ADDRESS + "=%s:read-resource";
    private static final String COMMAND_UNDEPLOY = "undeploy %s";
    private static final String COMMAND_READ_CHILDREN_DEPLOYMENT = CliConstants.DEPLOYMENT_ADDRESS + "=%s:read-children-resources(child-type=deployment)";

    private CliClient client;

    public DeploymentsOperations(CliClient client) {
        this.client = client;
    }

    public boolean exists(String deploymentName) {
        String command = String.format(COMMAND_READ_RESOURCE, deploymentName);
        return client.executeForSuccess(command);
    }

    public void undeploy(String deploymentName) {
        String command = String.format(COMMAND_UNDEPLOY, deploymentName);
        client.executeCommand(command);
    }

    public boolean isEnabled(String deploymentName) {
        String command = String.format(COMMAND_READ_RESOURCE, deploymentName);
        return client.executeForResponse(command).get("result").get("enabled").asBoolean();
    }

    public boolean isEnabledInServerGroup(String serverGroup, String deploymentName) {
        String command = String.format(COMMAND_READ_CHILDREN_DEPLOYMENT, serverGroup);
        return client.executeForResponse(command).get("result").get(deploymentName).get("enabled").asBoolean();
    }

    public boolean isAssignedToServerGroup(String serverGroup, String deploymentName) {
        String command = String.format(COMMAND_READ_CHILDREN_DEPLOYMENT, serverGroup);
        return client.executeForResponse(command).get("result").has(deploymentName);
    }
}
