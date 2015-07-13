package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.MetricsPage;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#standalone-runtime/host-vm")
public class JVMPage extends MetricsPage {

    public MetricsAreaFragment getHeapUsageMetricsArea(){
        return getMetricsArea("Heap Usage");
    }

    public MetricsAreaFragment getNonHeapUsageMetricsArea(){
        return getMetricsArea("Non Heap Usage");
    }

    public MetricsAreaFragment getThreadUsageMetricsArea(){
        return getMetricsArea("Thread Usage");
    }
}
