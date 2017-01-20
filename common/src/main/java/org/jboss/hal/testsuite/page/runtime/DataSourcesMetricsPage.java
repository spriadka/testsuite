package org.jboss.hal.testsuite.page.runtime;

import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.MetricsPage;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class DataSourcesMetricsPage extends MetricsPage {

    public void navigate() {
        navigate2runtimeSubsystem("Datasources");
    }

    public MetricsAreaFragment getConnectionPoolMetricsArea() {
        return getMetricsArea("Connection Pool");
    }

    public MetricsAreaFragment getPreparedStatementCacheMetricsArea() {
        return getMetricsArea("Prepared Statement Cache");
    }
}
