package org.jboss.hal.testsuite.test.configuration.jgroups;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class JGroupUDPTestCase extends JGroupAbstractTestCase {

    @BeforeClass
    public static void setUpUDP() throws IOException {
        BASE_ADDRESS =  JGROUPS_ADDRESS.and("stack", "udp");
        PROTOCOL_ADDRESS = BASE_ADDRESS.and("protocol", DEFAULT_PROTOCOL);
        TRANSPORT_ADDRESS = BASE_ADDRESS.and("transport", "UDP");

        jGroupsOperations.addProperty(TRANSPORT_ADDRESS, TRANSPORT_PROPERTY_TBR, RandomStringUtils.randomAlphanumeric(5));
        jGroupsOperations.addProperty(PROTOCOL_ADDRESS, PROTOCOL_PROPERTY_TBR, RandomStringUtils.randomAlphanumeric(5));

    }

    @Before
    public void beforeUDP() {
        page.selectStackByName("udp");
        Console.withBrowser(browser).waitUntilLoaded();
        page.switchToTransport();
    }
}
