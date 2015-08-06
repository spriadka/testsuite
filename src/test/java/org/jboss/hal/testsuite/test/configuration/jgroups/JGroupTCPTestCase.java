package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Created by jkasik <jkasik@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class JGroupTCPTestCase extends JGroupAbstractTestCase {

    @BeforeClass
    public static void setUpTCP() {
        String profile = "";
        if (ConfigUtils.isDomain())
            profile = "/profile=full-ha";
        verifier.setDmrPath(profile + "/subsystem=jgroups/stack=tcp/transport=TCP");
        jGroupsOperations.setBaseDrmPath(profile + "/subsystem=jgroups/stack=tcp/");
        jGroupsOperations.setTransport("TCP");
        jGroupsOperations.addTransportProperty(PROPERTY_NAME_P, PROPERTY_VALUE_P);
        jGroupsOperations.addProtocolProperty(DEFAULT_PROTOCOL, PROPERTY_NAME_P, PROPERTY_VALUE_P);
    }

    @Before
    public void beforeTCP() {
        page.selectStackByName("tcp");
        Console.withBrowser(browser).waitUntilLoaded();
        page.switchToTransport();
        Console.withBrowser(browser).waitUntilLoaded();
    }
}
