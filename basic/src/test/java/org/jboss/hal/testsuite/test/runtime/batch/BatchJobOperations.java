package org.jboss.hal.testsuite.test.runtime.batch;

import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.testsuite.page.runtime.BatchRuntimePage.MONITORED_SERVER;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadResourceOption;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * Encapsulates functionality of reading and handling batch job executions in management model.
 *
 * @author pjelinek
 */
public class BatchJobOperations {

    private static final String
        BATCH_JBERET = "batch-jberet",
        HOST_NAME = ConfigUtils.getDefaultHost();
    private final OnlineManagementClient client;
    private final Operations ops;

    public BatchJobOperations(final OnlineManagementClient client) {
        this.client = client;
        this.ops = new Operations(client);
    }

    /**
     * @return execution id
     */
    public long startJob(final String deploymentName, final String jobXmlName) throws IOException {
        ModelNodeResult result = ops.invoke("start-job", getBatchSubsystemInDeploymentAddress(deploymentName),
                Values.of("job-xml-name", jobXmlName));
        result.assertDefinedValue();
        return result.value().asLong();
    }

    public void stopJob(final String deploymentName, final long executionId) throws IOException {
        ops.invoke("stop-job", getBatchSubsystemInDeploymentAddress(deploymentName),
                Values.of("execution-id", executionId)).assertSuccess();
    }

    public Set<Execution> getJobExecutions(final String deploymentName, final String jobName) throws IOException {
        ModelNodeResult executionsResult = ops.invoke("read-children-resources",
                getBatchSubsystemInDeploymentAddress(deploymentName).and("job", jobName),
                Values.of("child-type", "execution").and("include-runtime", "true"));
        executionsResult.assertDefinedValue();
        List<Property> executionList = executionsResult.value().asPropertyList();
        return executionList.stream().map(executionProperty -> {
                return getExecution(Long.valueOf(executionProperty.getName()), executionProperty.getValue());
            }).collect(toSet());
    }

    public Execution getExecution(final String deploymentName, final String jobName, final long executionId)
            throws IOException {
        Address executionAddress = getBatchSubsystemInDeploymentAddress(deploymentName).and("job", jobName)
                .and("execution", String.valueOf(executionId));
        ModelNodeResult executionResult = ops.readResource(executionAddress, ReadResourceOption.INCLUDE_RUNTIME);
        executionResult.assertDefinedValue();
        return getExecution(executionId, executionResult.value());
    }

    private Address getBatchSubsystemInDeploymentAddress(String deploymentName) {
        if (ConfigUtils.isDomain()) {
            return Address.host(HOST_NAME).and("server", MONITORED_SERVER).and("deployment", deploymentName)
                    .and("subsystem", BATCH_JBERET);
        } else {
            return Address.deployment(deploymentName).and("subsystem", BATCH_JBERET);
        }
    }

    private Execution getExecution(long executionId, ModelNode node) {
        return new Execution(executionId,
                node.get("instance-id").asLong(),
                node.get("batch-status").asString(),
                node.get("create-time").asString(),
                node.get("end-time").asString(),
                node.get("exit-status").asString(),
                node.get("last-updated-time").asString(),
                node.get("start-time").asString());
    }
}
