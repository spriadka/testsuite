package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.MetricsPage;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#ds-metrics")
public class DataSourcesMetricsPage extends MetricsPage {

    public MetricsAreaFragment getConnectionPoolMetricsArea(){
        return getMetricsArea("Connection Pool");
    }

    public MetricsAreaFragment getPreparedStatementCacheMetricsArea(){
        return getMetricsArea("Prepared Statement Cache");
    }
}
