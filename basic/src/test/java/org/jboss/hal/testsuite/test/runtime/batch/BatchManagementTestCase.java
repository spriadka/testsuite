package org.jboss.hal.testsuite.test.runtime.batch;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.fragment.runtime.batch.BatchTableRowFragment;
import org.jboss.hal.testsuite.page.runtime.BatchRuntimePage;
import org.jboss.hal.testsuite.test.runtime.deployments.DeploymentsOperations;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Shared.class)
public class BatchManagementTestCase {

    private static final String
        STARTED = "STARTED",
        STOPPING = "STOPPING",
        STOPPED = "STOPPED";
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final DeploymentsOperations deploymentOps = new DeploymentsOperations(client);
    private final BatchJobOperations batchOps = new BatchJobOperations(client);
    private String
        deploymentName,
        jobOneFileName,
        jobTwoFileName,
        jobOneName,
        jobTwoName;
    @Drone
    private WebDriver browser;

    @Page
    private BatchRuntimePage page;

    @After
    public void cleanUpDeployment() throws IOException, OperationException, CommandFailedException {
        deploymentOps.undeployIfExists(deploymentName);
    }

    @AfterClass
    public static void closeClient() {
        IOUtils.closeQuietly(client);
    }

    @Test
    public void jobsShownTest() throws Exception {
        deployBatchApplicationWithChunks(0);
        page.navigate2jobs();

        assertEquals(2, page.getAllRows().size());
        assertEquals(1, page.getRowsForJobXmlName(jobOneFileName).size());
        assertEquals(1, page.getRowsForJobXmlName(jobTwoFileName).size());
        assertTrue(page.getAllRows().stream().map(row -> row.getDeploymentValue())
                .allMatch(warNameInRow -> warNameInRow.equals(deploymentName)));
    }

    @Test
    public void executionsShownTest() throws Exception {
        deployBatchApplicationWithChunks(0);
        batchOps.startJob(deploymentName, jobOneFileName);
        batchOps.startJob(deploymentName, jobOneFileName);
        batchOps.startJob(deploymentName, jobTwoFileName);

        page.navigate2jobs();
        Set<BatchTableRowFragment> jobOneExecutionRowSet = page.getRowsForJobXmlName(jobOneFileName);
        Set<BatchTableRowFragment> jobTwoExecutionRowSet = page.getRowsForJobXmlName(jobTwoFileName);

        assertEquals(2, jobOneExecutionRowSet.size());
        Set<Execution> quickExecutionSet = batchOps.getJobExecutions(deploymentName, jobOneName);
        assertExecutionsMatchRows(quickExecutionSet, jobOneExecutionRowSet);

        assertEquals(1, jobTwoExecutionRowSet.size());
        Set<Execution> slowExecutionSet = batchOps.getJobExecutions(deploymentName, jobTwoName);
        assertExecutionsMatchRows(slowExecutionSet, jobTwoExecutionRowSet);
    }

    @Test
    public void filterTest() throws Exception {
        deployBatchApplicationWithChunks(0);
        batchOps.startJob(deploymentName, jobOneFileName);
        batchOps.startJob(deploymentName, jobOneFileName);
        batchOps.startJob(deploymentName, jobTwoFileName);
        page.navigate2jobs();
        assertEquals(3, page.getAllRows().size());

        page.setFilterText(jobOneName);
        assertEquals(2, page.getAllRows().size());
        assertTrue(everyJobFileNameInTableEquals(jobOneFileName));

        page.setFilterText(jobTwoName);
        assertEquals(1, page.getAllRows().size());
        assertTrue(everyJobFileNameInTableEquals(jobTwoFileName));

        page.setFilterText(BatchManagementTestCase.class.getSimpleName());
        assertEquals(3, page.getAllRows().size());
    }

    @Test
    public void refreshJobListTest() throws Exception {
        deployBatchApplicationWithChunks(0);
        page.navigate2jobs();
        assertEquals(2, page.getAllRows().size());

        batchOps.startJob(deploymentName, jobOneFileName);
        batchOps.startJob(deploymentName, jobOneFileName);
        batchOps.startJob(deploymentName, jobTwoFileName);
        page.clickButton("Refresh");

        List<BatchTableRowFragment> allRows = page.getAllRows();
        assertEquals(3, allRows.size());
        assertTrue(allRows.stream().map(row -> row.getExecutionIdValue()) // stream of all executionIds in table
                .allMatch(executionIdTextValue -> !executionIdTextValue.trim().isEmpty())); // none of executionIds is empty
    }

    @Test
    public void jobStatusTest() throws Exception {
        deployBatchApplicationWithChunks(1000);
        long executionId = batchOps.startJob(deploymentName, jobOneFileName);
        Execution execution = batchOps.getExecution(deploymentName, jobOneName, executionId);
        page.navigate2jobs();

        page.selectRowForJobXmlName(jobOneFileName);
        assertExecutionMatchJobDetail(execution);

        batchOps.stopJob(deploymentName, executionId);
        execution = batchOps.getExecution(deploymentName, jobOneName, executionId);
        page.clickButton("Refresh");
        page.selectRowForJobXmlName(jobOneFileName);
        assertExecutionMatchJobDetail(execution);
    }

    @Test
    public void startJobTest() throws Exception {
        deployBatchApplicationWithChunks(1000);
        page.navigate2jobs();
        BatchTableRowFragment jobRow = page.selectRowForJobXmlName(jobTwoFileName);
        assertEquals("", jobRow.getBatchStatusValue());
        assertEquals(0, batchOps.getJobExecutions(deploymentName, jobTwoName).size());

        page.clickButton("Start");
        assertEquals(STARTED, jobRow.getBatchStatusValue());
        Set<Execution> jobExecutionSet = batchOps.getJobExecutions(deploymentName, jobTwoName);
        assertEquals(1, jobExecutionSet.size());
    }

    @Test
    public void stopJobTest() throws Exception {
        final long stoppingInterval = 5000;
        deployBatchApplicationWithBatchlet(stoppingInterval);
        long executionId = batchOps.startJob(deploymentName, jobOneFileName);
        page.navigate2jobs();
        BatchTableRowFragment jobRow = page.selectRowForJobXmlName(jobOneFileName);
        assertEquals(STARTED, jobRow.getBatchStatusValue());
        page.clickButton("Stop");
        assertEquals(STOPPING, jobRow.getBatchStatusValue());
        String actualStatus = batchOps.getExecution(deploymentName, jobOneName, executionId).batchStatus;
        assertTrue("We expected STOPPING or STOPPED but got '" + actualStatus + "'.",
                actualStatus.equals(STOPPING) || actualStatus.equals(STOPPED));
        assertJobHasStoppedOrFail(jobOneFileName, stoppingInterval);
    }

    private void assertJobHasStoppedOrFail(String jobName, long timeoutInMillis) throws InterruptedException {
        BatchTableRowFragment jobRow = page.selectRowForJobXmlName(jobName);
        long currentTime = System.currentTimeMillis();
        long endTime = currentTime + timeoutInMillis;
        while (currentTime < endTime + 1000) {
            page.clickButton("Refresh");
            if (jobRow.getBatchStatusValue().equals("STOPPED")) {
                return;
            }
            TimeUnit.MILLISECONDS.sleep(500);
            currentTime = System.currentTimeMillis();
        }
        fail("Job should have been STOPPED by now");
    }

    @Test
    public void restartJobTest() throws Exception {
        deployBatchApplicationWithChunks(1000);
        long executionId = batchOps.startJob(deploymentName, jobOneFileName);
        batchOps.stopJob(deploymentName, executionId);
        page.navigate2jobs();
        BatchTableRowFragment jobRow = page.selectRowForJobXmlName(jobOneFileName);
        assertEquals(STOPPED, jobRow.getBatchStatusValue());

        page.clickButton("Restart");
        assertEquals(STARTED, page.getRowByExecutionId(String.valueOf(executionId + 1)).getBatchStatusValue());
        String actualStatus = batchOps.getExecution(deploymentName, jobOneName, executionId + 1).batchStatus;
        assertEquals(STARTED, actualStatus);
    }

    private void assertExecutionsMatchRows(Set<Execution> executionSet, Set<BatchTableRowFragment> rowSet) {
        assertEquals(executionSet.size(), rowSet.size());
        executionSet.forEach(execution -> {
            BatchTableRowFragment actualRow = rowSet
                    .stream()
                    .filter(row -> Long.valueOf(row.getExecutionIdValue()).equals(execution.executionId))
                    .findFirst().get();
            assertEquals(execution.instanceId, (long) Long.valueOf(actualRow.getInstanceIdValue()));
            assertEquals(execution.batchStatus, actualRow.getBatchStatusValue());
            assertEquals(execution.startTime, actualRow.getStartTimeValue());
        });
    }

    private void assertExecutionMatchJobDetail(Execution execution) {
        MetricsAreaFragment jobStatusArea = page.getMetricsArea("Job Status");
        assertAttributeEquals(String.valueOf(execution.instanceId), jobStatusArea.getMetric("Instance Id"));
        assertAttributeEquals(execution.batchStatus, jobStatusArea.getMetric("Batch Status"));
        assertAttributeEquals(execution.exitStatus, jobStatusArea.getMetric("Exit Status"));
        assertAttributeEquals(execution.createTime, jobStatusArea.getMetric("Create Time"));
        assertAttributeEquals(execution.startTime, jobStatusArea.getMetric("Start Time"));
        assertAttributeEquals(execution.endTime, jobStatusArea.getMetric("End Time"));
        assertTimesCloseEnough(execution.lastUpdatedTime, jobStatusArea.getMetric("Last Updated Time"));
    }

    private void assertAttributeEquals(String expectedAttrValueFromModel, String actualAttrValueFromBrowser) {
        if (expectedAttrValueFromModel.equals("undefined")) {
            assertEquals("", actualAttrValueFromBrowser);
        } else {
            assertEquals(expectedAttrValueFromModel, actualAttrValueFromBrowser);
        }
    }

    private void assertTimesCloseEnough(String expectedTimeFromModel, String actualTimeFromBrowser) {
        final long
            maxDiffMilis = 100,
            actualDiffMilis = Duration.between(getInstant(expectedTimeFromModel), getInstant(actualTimeFromBrowser))
                .toMillis();
        Assert.assertTrue("Difference between expected time from model '" + expectedTimeFromModel + "' and actual time "
                + "from browser '" + actualTimeFromBrowser + "' is '" + actualDiffMilis + "' ms what is more than "
                + "maximum allowed difference of '" + maxDiffMilis + "' ms.", actualDiffMilis <= maxDiffMilis);
    }

    private Instant getInstant(String timeString) {
        DateTimeFormatter formater = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .optionalStart()
            .appendOffset("+HHMM", "0000")
            .toFormatter();
        return formater.parse(timeString, Instant::from);
    }

    private boolean everyJobFileNameInTableEquals(String expectedJobFileName) {
        return page.getAllRows().stream().map(row -> row.getJobXmlNameValue())
                .allMatch(jobFileName -> jobFileName.equals(expectedJobFileName));
    }

    /**
     * Deploy batch application as web archive with {@link #deploymentName} name containing two batch jobs named
     * {@link #jobOneName} and {@link #jobTwoName} with configuration files named {@link #jobOneFileName}
     * and {@link #jobTwoFileName} configured to run batch job executing chunks
     * @param itemProcessingSleep - if set greater than zero it will slow down processing of each item in running batch
     * job for given number of milliseconds
     */
    private void deployBatchApplicationWithChunks(long itemProcessingSleep) throws CommandFailedException {
        String randomString = "-" + RandomStringUtils.randomAlphabetic(6);
        deploymentName = String.format("%s-chunks-%s.war", BatchManagementTestCase.class.getSimpleName(), randomString);
        jobOneName = "job-one-" + randomString;
        jobTwoName = "job-two-" + randomString;
        jobOneFileName = jobOneName + ".xml";
        jobTwoFileName = jobTwoName + ".xml";
        WebArchive archive = ShrinkWrap.create(WebArchive.class, deploymentName)
                .addClasses(Checkpoint.class, Reader.class, Processor.class, Writer.class)
                .addAsWebInfResource(getBatchJobWithChunksConfigAsset(jobOneName, itemProcessingSleep), "classes/META-INF/batch-jobs/" + jobOneFileName)
                .addAsWebInfResource(getBatchJobWithChunksConfigAsset(jobTwoName, itemProcessingSleep), "classes/META-INF/batch-jobs/" + jobTwoFileName)
                .addAsWebInfResource("batch/names.txt", "classes/org/jboss/hal/testsuite/test/runtime/batch/names.txt")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        deploymentOps.deploy(archive);
    }

    /**
     * Deploy batch application as web archive with {@link #deploymentName} name containing one batch job named
     * {@link #jobOneName}  with configuration files named {@link #jobOneFileName} configured to run batch job executing batchlet
     * @param stoppingInterval - interval after which the batch job is going to be stopped
     */
    private void deployBatchApplicationWithBatchlet(long stoppingInterval) throws CommandFailedException {
        String randomString = "-" + RandomStringUtils.randomAlphabetic(6);
        deploymentName = String.format("%s-batchlet-%s.war", BatchManagementTestCase.class.getSimpleName(), randomString);
        jobOneName = "job-one" + randomString;
        jobOneFileName = jobOneName + ".xml";
        WebArchive archive = ShrinkWrap.create(WebArchive.class, deploymentName)
                .addClasses(Batchlet.class)
                .addAsWebInfResource(getBatchJobWithBatchletConfigAsset(jobOneName, stoppingInterval), "classes/META-INF/batch-jobs/" + jobOneFileName)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        deploymentOps.deploy(archive);
    }

    private StringAsset getBatchJobWithChunksConfigAsset(String jobId, long itemProcessingSleep) {
        return new StringAsset(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<job id=\"" + jobId + "\" xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/jobXML_1_0.xsd\" version=\"1.0\">\n" +
                "    <step id=\"import-file-chunk\">\n" +
                "        <chunk item-count=\"3\">\n" +
                "            <reader ref=\"testReader\" />\n" +
                "            <processor ref=\"testProcessor\">\n" +
                "                <properties>\n" +
                "                    <property name=\"itemProcessingSleep\" value=\"" + itemProcessingSleep + "\"/>\n" +
                "                </properties>\n" +
                "            </processor>\n" +
                "            <writer ref=\"testWriter\" />\n" +
                "        </chunk>\n" +
                "        <end on=\"END\"/>\n" +
                "    </step>\n" +
                "</job>\n");
    }

    private StringAsset getBatchJobWithBatchletConfigAsset(String jobId, long stoppingInterval) {
        return new StringAsset(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<job id=\"" + jobId +  "\" xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        " xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/jobXML_1_0.xsd\" version=\"1.0\">\n" +
                        "    <step id=\"run-batchlet-step\">\n" +
                        "        <batchlet ref=\"testBatchlet\">\n" +
                        "            <properties>\n" +
                        "                <property name=\"stoppingInterval\" value=\"" + stoppingInterval + "\"/>\n" +
                        "            </properties>\n" +
                        "        </batchlet>\n" +
                        "    </step>\n" +
                        "</job>");
    }

}
