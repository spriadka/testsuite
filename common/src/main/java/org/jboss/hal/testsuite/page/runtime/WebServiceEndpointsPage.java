package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.MetricsPage;

/**
 * Created by mkrajcov on 4/10/15.
 */
@Location("#webservice-runtime")
public class WebServiceEndpointsPage extends MetricsPage {
    public MetricsAreaFragment getWebServiceRequestMetricsArea() {
        return getMetricsArea("Web Service Requests");
    }
}
