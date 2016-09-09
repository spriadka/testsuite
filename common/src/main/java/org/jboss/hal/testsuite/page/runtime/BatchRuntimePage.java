package org.jboss.hal.testsuite.page.runtime;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;

import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.shared.table.ResourceTableRowFragment;
import org.jboss.hal.testsuite.page.MetricsPage;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Encapsulates GUI interaction with Batch subsystem runtime page
 *
 * @author pjelinek
 */
public class BatchRuntimePage extends MetricsPage implements Navigatable {

    public static final String MONITORED_SERVER = "server-one";

    @Override
    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .step(FinderNames.HOST, ConfigUtils.getDefaultHost())
                    .step(FinderNames.SERVER, MONITORED_SERVER);
        } else {
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .step(FinderNames.SERVER, FinderNames.STANDALONE_SERVER);
        }
        navigation = navigation.step(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "Batch");

        navigation.selectRow().invoke("View");
        Application.waitUntilVisible();
    }

    public BatchRuntimePage navigate2jobs() {
        navigate();
        switchTab("Jobs");
        Console.withBrowser(browser).waitUntilLoaded();
        return this;
    }

    public List<ResourceTableRowFragment> getAllRows() {
        return getResourceManager().getResourceTable().getAllRows();
    }

    public Set<ResourceTableRowFragment> getRowsForJob(final String jobFileName) {
        return getAllRows().stream().filter(row -> {
            return row.getCell(1).getText().equals(jobFileName);
        }).collect(toSet());
    }

    public ResourceTableRowFragment selectRowForJob(String jobFileName) {
        return getResourceManager().getResourceTable().selectRowByText(1, jobFileName);
    }

    public ResourceTableRowFragment getRowByExecutionId(String executionId) {
        return getResourceManager().getResourceTable().getRowByText(2, executionId);
    }

    public String getDeploymentFromRow(ResourceTableRowFragment jobTableRow) {
        return jobTableRow.getCellValue(0);
    }

    public String getJobFileNameFromRow(ResourceTableRowFragment jobTableRow) {
        return jobTableRow.getCellValue(1);
    }

    public String getExecutionIdFromRow(ResourceTableRowFragment jobTableRow) {
        return jobTableRow.getCellValue(2);
    }

    public String getInstanceIdFromRow(ResourceTableRowFragment jobTableRow) {
        return jobTableRow.getCellValue(3);
    }

    public String getBatchStatusFromRow(ResourceTableRowFragment jobTableRow) {
        return jobTableRow.getCellValue(4);
    }

    public String getStartTimeFromRow(ResourceTableRowFragment jobTableRow) {
        return jobTableRow.getCellValue(5);
    }

    public void setFilterText(String text) {
        WebElement filterElement = Console.withBrowser(browser)
                .findElement(ByJQuery.selector("input[type='text']"), getContentPanel());
        filterElement.clear();
        filterElement.sendKeys(text);
    }

    private WebElement getContentPanel() {
        By contentPanelSelector = ByJQuery.selector("." + PropUtils.get("page.content.rhs.class") + ":visible");
        return getContentRoot().findElement(contentPanelSelector);
    }

}
