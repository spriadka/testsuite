package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class JGroupTCPTestCase extends JGroupAbstractTestCase {

    @BeforeClass
    public static void setUpTCP() throws IOException {
        BASE_ADDRESS =  JGROUPS_ADDRESS.and("stack", "tcp");
        PROTOCOL_ADDRESS = BASE_ADDRESS.and("protocol", DEFAULT_PROTOCOL);
        TRANSPORT_ADDRESS = BASE_ADDRESS.and("transport", "TCP");
        jGroupsOperations.addProperty(TRANSPORT_ADDRESS, TRANSPORT_PROPERTY_TBR, TRANSPORT_PROPERTY_TBR_VALUE);
        jGroupsOperations.addProperty(PROTOCOL_ADDRESS, PROTOCOL_PROPERTY_TBR, PROTOCOL_PROPERTY_TBR_VALUE);
    }

    @Before
    public void beforeTCP() {
        page.selectStackByName("tcp");
        Console.withBrowser(browser).waitUntilLoaded();
        page.switchToTransport();
        Console.withBrowser(browser).waitUntilLoaded();
    }
}
