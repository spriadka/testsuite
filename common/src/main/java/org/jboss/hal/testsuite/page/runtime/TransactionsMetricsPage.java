package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.MetricsPage;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#tx-metrics")
public class TransactionsMetricsPage extends MetricsPage {

    public void navigate() {
        navigate2runtimeSubsystem("Transactions");
    }

    public MetricsAreaFragment getSuccessRationMetricsArea() {
        return getMetricsArea("Success Ratio");
    }

    public MetricsAreaFragment getFailureOriginMetricsArea() {
        return getMetricsArea("Failure Origin");
    }

    public MetricsAreaFragment getGeneralStatisticsMetricsArea() {
        return getMetricsArea("General Statistics");
    }

}
