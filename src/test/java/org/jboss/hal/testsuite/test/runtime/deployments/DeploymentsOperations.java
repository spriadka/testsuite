package org.jboss.hal.testsuite.test.runtime.deployments;

import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliConstants;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class DeploymentsOperations {

    private CliClient client;

    public DeploymentsOperations(CliClient client) {
        this.client = client;
    }

    public boolean exists(String deploymentName) {
        String command = CliConstants.DEPLOYMENT_ADDRESS + "=" + deploymentName + ":read-resource";
        return client.executeForSuccess(command);
    }

    public void undeploy(String deploymentName) {
        String command = "undeploy " + deploymentName;
        client.executeCommand(command);
    }

    public boolean isEnabled(String deploymentName) {
        String command = CliConstants.DEPLOYMENT_ADDRESS + "=" + deploymentName + ":read-resource";
        return client.executeForResponse(command).get("result").get("enabled").asBoolean();
    }

    public boolean isEnabledInServerGroup(String serverGroup, String deploymentName) {
        String command = CliConstants.SERVER_GROUP_ADDRESS + "=" + serverGroup + ":read-children-resources(child-type=deployment)";
        return client.executeForResponse(command).get("result").get(deploymentName).get("enabled").asBoolean();
    }

    public boolean isAssignedToServerGroup(String serverGroup, String deploymentName) {
        String command = CliConstants.SERVER_GROUP_ADDRESS + "=" + serverGroup + ":read-children-resources(child-type=deployment)";
        return client.executeForResponse(command).get("result").has(deploymentName);
    }
}
