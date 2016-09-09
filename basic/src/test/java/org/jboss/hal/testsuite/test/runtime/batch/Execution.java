package org.jboss.hal.testsuite.test.runtime.batch;

public class Execution {

    final long executionId, instanceId;
    final String
        batchStatus,
        createTime,
        endTime,
        exitStatus,
        lastUpdatedTime,
        startTime;

    public Execution(long executionId, long instanceId, String batchStatus, String createTime, String endTime,
            String exitStatus, String lastUpdatedTime, String startTime) {
        this.executionId = executionId;
        this.instanceId = instanceId;
        this.batchStatus = batchStatus;
        this.createTime = createTime;
        this.endTime = endTime;
        this.exitStatus = exitStatus;
        this.lastUpdatedTime = lastUpdatedTime;
        this.startTime = startTime;
    }
}
