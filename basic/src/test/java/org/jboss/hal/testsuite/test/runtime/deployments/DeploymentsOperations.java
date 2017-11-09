package org.jboss.hal.testsuite.test.runtime.deployments;

import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.UndeployCommand;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.wildfly.extras.creaper.commands.deployments.Deploy;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class DeploymentsOperations {

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

    public void deploy(Archive<?> archive) throws CommandFailedException {
        Deploy deployCommand = new Deploy
                .Builder(archive.as(ZipExporter.class).exportAsInputStream(), archive.getName(), true)
                .build();
        client.apply(deployCommand);
    }

    public void undeploy(String deploymentName) throws IOException, CommandFailedException {
        UndeployCommand.Builder builder = new UndeployCommand.Builder(deploymentName);
        if (client.options().isDomain) {
            builder.fromAllGroups();
        }
        client.apply(builder.build());
    }

    public void undeployIfExists(String deploymentName) throws IOException, OperationException, CommandFailedException {
        if (operations.exists(Address.of("deployment", deploymentName))) {
            undeploy(deploymentName);
        }
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

    public void verifyDeploymentContentDefault(List<String> itemsInDeploment) {
        assertTrue(itemInListExistsLowerCase(itemsInDeploment, "meta-inf/manifest.mf | 127 b"));
        assertTrue(itemInListExistsLowerCase(itemsInDeploment, "index.jsp | 361 b"));
        assertTrue(itemInListExistsLowerCase(itemsInDeploment, "page.html | 68 b"));
    }

    public boolean deploymentNeverEnabled(List<String> previewListItems) {
        return itemInListExistsLowerCase(previewListItems, "the deployment was never enabled");
    }

    public boolean deploymentNeverDisabled(List<String> previewListItems) {
        return itemInListExistsLowerCase(previewListItems, "the deployment was never disabled");
    }

    public boolean deplomentEnabledTimestampExists(List<String> previewListItems) {
        return itemInListExistsLowerCase(previewListItems, "last enabled at");
    }

    public boolean deplomentDisabledTimestampExists(List<String> previewListItems) {
        return itemInListExistsLowerCase(previewListItems, "last disabled at");
    }

    public boolean itemInListExistsLowerCase(List<String> lines, String item) {
        for (String line : lines) {
            String lowLine = line.toLowerCase();
            if (lowLine.contains(item)) {
                return true;
            }
        }
        return false;
    }

}
